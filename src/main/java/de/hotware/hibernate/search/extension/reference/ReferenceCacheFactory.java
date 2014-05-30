package de.hotware.hibernate.search.extension.reference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import de.hotware.hibernate.search.extension.reference.annotations.ReferenceCached;
import de.hotware.hibernate.search.extension.reference.annotations.ReferenceCached.CacheType;

public final class ReferenceCacheFactory {

	private static final Logger LOGGER = Logger
			.getLogger(ReferenceCacheFactory.class.getName());

	private static final Map<Class<?>, ReferenceCache> CACHES = new HashMap<>();
	private static final Lock lock = new ReentrantLock();

	private ReferenceCacheFactory() {
		throw new AssertionError("can't touch this!");
	}

	public static ReferenceCache getReferenceCache(Class<?> clazz) {
		lock.lock();
		try {
			if (!CACHES.containsKey(clazz)) {
				ReferenceCached annotation = clazz
						.getAnnotation(ReferenceCached.class);
				if (annotation != null) {
					if (annotation.cacheSize() > 0) {
						if (annotation.cacheType() == CacheType.LRU_HASHMAP) {
							CACHES.put(clazz,
									new LRUReferenceCache(clazz.getName(),
											annotation.cacheSize()));
						} else {
							LOGGER.warning("unsupported CacheType specified for class: " +  clazz);
						}
					} else {
						throw new IllegalArgumentException("cacheSize was <= 0");
					}
				}
			}
			return CACHES.get(clazz);
		} finally {
			lock.unlock();
		}
	}

}
