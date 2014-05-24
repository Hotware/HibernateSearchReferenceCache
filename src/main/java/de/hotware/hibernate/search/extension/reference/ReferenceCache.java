package de.hotware.hibernate.search.extension.reference;

import java.io.Serializable;

public interface ReferenceCache {
	
	public Object find(Serializable id);
	
	public void addToCache(Serializable id, Object value);
	
	public void removeFromCache(Serializable id);
	
	public void purge();

}
