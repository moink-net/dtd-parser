// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.objectcache;

/**
 * A cache for pre-compiled XSLT stylesheets, Map objects, and Properties objects.
 *
 * <p>The cache is keyed by filename.</p>
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class ObjectCache
{
    /**
     * This hashtable holds associated file names and compiled objects.
     * I think it should be a Hashmap rather than a Hashtable.
    */
    static private java.util.Hashtable cache;
    /**
     * Construct a new ObjectCache.
     *
     * <p>Since the cache is static, this method does nothing useful.</p>
     */
    public ObjectCache()
    {
        super();
    }
    /**
     * Clear the cache.
     */
    public static void clear()
    {
        cache.clear();
    }
    /**
    * Remove a specific object from the cache.
    *
    * @param key The filename of the object.
    */
    public static void remove(Object key)
    {
        cache.remove(key);
    }
    /**
    * Get an object from the cache.
    *
    * @param key The filename of the object.
    *
    * @return The object
    */
    public static Object get(Object key)
    {
        if (cache.get(key) == null)
            {
            return null;
        }
        else
            {
            Object o = cache.get(key);
            return o;
        }
    }
    /**
    * Put an object in the cache.
    *
    * @param key The filename of the object.
    * @param value The object.
    */
    public static void put(Object key, Object value)
    {
        cache.put(key, value);
    }

    /**
    * Get the Hashtable used by the cache.
    *
    * @return The Hashtable
    */
    public static java.util.Hashtable getMap()
    {
        if (cache == null)
            {
            synchronized (ObjectCache.class)
            
            {
                if (cache == null)
                    {
                    cache = new java.util.Hashtable();
                }
            }
        }
        return cache;
    }
}