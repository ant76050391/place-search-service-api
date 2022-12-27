package org.example.cache;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.enums.ServiceExceptionMessages;
import org.example.exception.ServiceException;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
@SuppressWarnings("unchecked")
public class ReactorCacheAspect {
  private final ReactiveCacheManager reactiveCacheManager;

  @Pointcut("@annotation(org.example.cache.ReactorCacheable)")
  public void pointcut() {}

  @Around("pointcut()")
  public Object around(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
    Type rawType = parameterizedType.getRawType();

    if (!rawType.equals(Mono.class) && !rawType.equals(Flux.class)) {
      throw new IllegalArgumentException(
          "The return type is not Mono/Flux. Use Mono/Flux for return type. method: "
              + method.getName());
    }
    ReactorCacheable reactorCacheable = method.getAnnotation(ReactorCacheable.class);
    String cacheName = reactorCacheable.name();
    Object[] args = joinPoint.getArgs();

    ThrowingSupplier retriever = () -> joinPoint.proceed(args);

    // NOTE: 리턴타입이 Mono면
    if (rawType.equals(Mono.class)) {
      Type returnTypeInsideMono = parameterizedType.getActualTypeArguments()[0];
      Class<?> returnClass = ResolvableType.forType(returnTypeInsideMono).resolve();
      return reactiveCacheManager
          .findCachedMono(cacheName, generateKey(args), retriever, returnClass)
          .doOnError(
              e -> log.error("Failed to processing mono cache. method: " + method.getName(), e));
    }
    // NOTE: 리턴타입이 Flux면
    else {
      return reactiveCacheManager
          .findCachedFlux(cacheName, generateKey(args), retriever)
          .doOnError(
              e -> log.error("Failed to processing flux cache. method: " + method.getName(), e));
    }
  }

  // NOTE: argument들의 조합으로 cache key를 생성
  private String generateKey(Object... objects) {
    return Arrays.stream(objects)
        .map(obj -> obj == null ? "" : obj.toString())
        .collect(Collectors.joining(":"));
  }

  @FunctionalInterface
  public interface ThrowingSupplier<T> extends Supplier<T> {
    @Override
    default T get() {
      try {
        return getThrows();
      } catch (Throwable th) {
        log.error("cache retriever exception : " + th.getMessage(), th);
        throw new ServiceException(ServiceExceptionMessages.SERVER_CACHING_ERROR);
      }
    }

    T getThrows() throws Throwable;
  }
}
