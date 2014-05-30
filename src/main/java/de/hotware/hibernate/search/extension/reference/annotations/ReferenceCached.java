package de.hotware.hibernate.search.extension.reference.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReferenceCached {

	CacheType cacheType();
	
	int cacheSize();
	
	public static enum CacheType {
		LRU_HASHMAP,
		EHCACHE
	}

}
