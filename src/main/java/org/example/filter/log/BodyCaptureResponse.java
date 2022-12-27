package org.example.filter.log;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class BodyCaptureResponse extends ServerHttpResponseDecorator {
  private final StringBuilder body = new StringBuilder();

  public BodyCaptureResponse(ServerHttpResponse delegate) {
    super(delegate);
  }

  @Override
  public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
    return super.writeAndFlushWith(body);
  }

  @Override
  public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
    Flux<DataBuffer> buffer = Flux.from(body);
    return super.writeWith(buffer.doOnNext(this::capture));
  }

  private void capture(DataBuffer buffer) {
    this.body.append(StandardCharsets.UTF_8.decode(buffer.asByteBuffer().asReadOnlyBuffer()));
  }

  public String getFullBody() {
    return this.body.toString();
  }
}
