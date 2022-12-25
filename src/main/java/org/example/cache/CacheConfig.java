package org.example.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CacheConfig {
	private static final int DEFAULT_EXPIRE_AFTER_WRITE = 10;
	private static final int DEFAULT_CACHE_MAXIMUM_SIZE = 20000;


	@Bean
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<CaffeineCache> caches = Arrays.stream(Caches.values())
				.map(cache -> new CaffeineCache(cache.getName(),
						Caffeine.newBuilder().recordStats()
								.expireAfterWrite(cache.getExpireAfterWrite(), TimeUnit.SECONDS)
								.maximumSize(cache.getMaximumSize())
								.build()))
				.collect(Collectors.toList());

		cacheManager.setCaches(caches);
		return cacheManager;
	}

	@Getter
	@NoArgsConstructor
	public enum Caches {
		SEARCH_KEYWORDS("getSearchKeywords", 30);

		private String name;
		private int expireAfterWrite = DEFAULT_EXPIRE_AFTER_WRITE;
		private int maximumSize = DEFAULT_CACHE_MAXIMUM_SIZE;

		Caches(String name, int expireAfterWrite) {
			this.name = name;
			this.expireAfterWrite = expireAfterWrite;
		}

		Caches(String name, int expireAfterWrite, int maximumSize) {
			this.expireAfterWrite = expireAfterWrite;
			this.maximumSize = maximumSize;
		}
	}
}
