package de.tudarmstadt.ito.xmldbms.objectcache;

/**
 * An Object Cache, cache pre-compiled XSLstylesheets, MapObjects, Properties objects using filename.
 * @author: Adam Flinton
 */
public class ObjectCache {
/**
 * This hashmap holds associated file names and compiled objects.
*/
	static private java.util.HashMap cache;
/**
 * This does nothing.
 */
public ObjectCache() {
	super();
}




/**
 * This method clears all keys & values from the hashmap (i.e it empties it entirely)
 * Creation date: (18/04/01 12:32:35)
 */
public static void clear() {
	
	cache.clear();
	
	}/**
 * This Method clears a specific Key/Value pair from the cache
 * You need to feed in the key value.
 * Creation date: (18/04/01 12:33:56)
 * @param key java.lang.Object
 */   
public static void remove(Object key) {
	
	cache.remove(key);
	
	}/**
 * Insert the method's description here.
 * Creation date: (18/04/01 14:11:05)
 * @return java.lang.Object
 * @param key java.lang.Object
 */ 
public static Object get(Object key) {

	if (cache.get(key) == null)
	{return null;}
	
	else {Object o = cache.get(key);
	return o;}
}/**
 * This returns the map used to cache the objects.
*/
	public static java.util.HashMap getMap()
	{
		if(cache == null)
		{
			synchronized(ObjectCache.class)
			{
				if(cache == null)
				{
					cache = new java.util.HashMap();
				}
			}
		}
		return cache;
	}/**
 * Insert the method's description here.
 * Creation date: (18/04/01 14:08:07)
 * @param key java.lang.Object
 * @param object java.lang.Object
 */ 
public static void put(Object key, Object value) {

	cache.put(key,value);
	
	}}