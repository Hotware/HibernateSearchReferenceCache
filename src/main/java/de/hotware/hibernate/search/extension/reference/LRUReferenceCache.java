package de.hotware.hibernate.search.extension.reference;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class LRUReferenceCache implements ReferenceCache {
	
	private static final Logger LOGGER = Logger.getLogger(LRUReferenceCache.class.getName());

	private final LinkedHashMap<Serializable, Object> map;
	private final String className;

	public LRUReferenceCache(String className, final int cacheSize) {
		this.map = new LinkedHashMap<Serializable, Object>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(
					java.util.Map.Entry<Serializable, Object> eldest) {
				return this.size() > cacheSize;
			}

		};
		this.className = className;
	}

	@Override
	public Object find(Serializable id) {
		Object ret = this.map.get(id);
		if(ret == null) {
			LOGGER.info("Cache for " + className + ": miss for " + id);
		} else {
			LOGGER.info("Cache for " + className + ": hit for " + id);
		}
		return ret;
	}

	@Override
	public void addToCache(Serializable id, Object value) {
		LOGGER.info("Cache for " + className + ": adding value for " + id);
		this.map.put(id, value);
	}

	@Override
	public void removeFromCache(Serializable id) {
		LOGGER.info("Cache for " + className + ": removing value for " + id);
		this.map.remove(id);
	}

	@Override
	public void purge() {
		LOGGER.info("Cache for " + className + ": purging the cache");
		this.map.clear();
	}

}
