// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.filters;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmlutils.*;

import java.util.*;

/**
 * A set of filters to be applied to the database to retrieve data.
 *
 * <p>The normal way to construct a FilterSet object is to write an XML
 * document using the filter language (filters.dtd), then compile it into
 * a FilterSet object. This is then passed to DBMSToDOM. For example:</p>
 *
 * <pre>
 *    // Compile a filter document.
 *    compiler = new FilterCompiler(parserUtils);
 *    filterSet = compiler.compile(map, new InputSource(new FileReader("SalesFilter.ftr")));
 *    <br />
 *    // Retrieve the document according to the filter.
 *    doc = dbmsToDOM.retrieveDocument(transferInfo, filterSet, null, null);
 * </pre>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class FilterSet
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private XMLDBMSMap map;
   private Vector     wrapperNames = new Vector();
   private Vector     filters = new Vector();
   private Hashtable  uris = new Hashtable();              // Indexed by prefix
   private Hashtable  prefixes = new Hashtable();          // Indexed by URI
   private Hashtable  rsNames = new Hashtable();           // Indexed by result set name

   //*********************************************************************
   // Constants
   //*********************************************************************

   private static String ARGUMENT = "Programming error. Argument ";
   private static String NOTNULL  = " must not be null.";
   private static String ARG_PREFIX = "prefix";
   private static String ARG_URI = "uri";
   private static final Object O = new Object();

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new FilterSet object.
    *
    * @param map The XMLDBMSMap to which the filter set applies.
    */
   public FilterSet(XMLDBMSMap map)
   {
      this.map = map;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //**************************************************************************
   // Namespaces
   //**************************************************************************

   /**
    * Get a namespace URI.
    *
    * @param prefix The namespace prefix.
    *
    * @return The namespace URI.
    * @exception IllegalArgumentException Thrown if the prefix is not found.
    */
   public final String getNamespaceURI(String prefix)
   {
      String uri;

      checkArgNull(prefix, ARG_PREFIX);
      uri = (String)uris.get(prefix);
      if (uri == null)
         throw new IllegalArgumentException("Prefix not found: " + prefix);
      return uri;
   }

   /**
    * Get a namespace prefix.
    *
    * @param uri The namespace URI.
    *
    * @return The namespace prefix.
    * @exception IllegalArgumentException Thrown if the URI is not found.
    */
   public final String getNamespacePrefix(String uri)
   {
      String prefix;

      checkArgNull(uri, ARG_URI);
      prefix = (String)prefixes.get(uri);
      if (prefix == null)
         throw new IllegalArgumentException("URI not found: " + uri);
      return prefix;
   }

   /**
    * Get a Hashtable containing all namespace URIs hashed by prefix.
    *
    * @return The Hashtable.
    */
   public final Hashtable getNamespaceURIs()
   {
      return (Hashtable)uris.clone();
   }

   /**
    * Get a Hashtable containing all namespace prefixes hashed by URI.
    *
    * @return The Hashtable.
    */
   public final Hashtable getNamespacePrefixes()
   {
      return (Hashtable)prefixes.clone();
   }

   /**
    * Add a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    * @param uri The namespace URI.
    *
    * @exception IllegalArgumentException Thrown if the prefix or URI is already used.
    */
   public void addNamespace(String prefix, String uri)
   {
      checkArgNull(prefix, ARG_PREFIX);
      checkArgNull(uri, ARG_URI);
      if (uris.get(prefix) != null)
         throw new IllegalArgumentException("Prefix already used: " + prefix);
      if (prefixes.get(uri) != null)
         throw new IllegalArgumentException("URI already used: " + uri);
      uris.put(prefix, uri);
      prefixes.put(uri, prefix);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception IllegalArgumentException Thrown if the prefix is not found.
    */
   public void removeNamespaceByPrefix(String prefix)
   {
      String uri;

      checkArgNull(prefix, ARG_PREFIX);

      uri = (String)uris.remove(prefix);
      if (uri == null)
         throw new IllegalArgumentException("Prefix not found: " + prefix);
      prefixes.remove(uri);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception IllegalArgumentException Thrown if the prefix is not found.
    */
   public void removeNamespaceByURI(String uri)
   {
      String prefix;

      checkArgNull(uri, ARG_URI);

      prefix = (String)prefixes.remove(uri);
      if (prefix == null)
         throw new IllegalArgumentException("URI not found: " + uri);
      uris.remove(prefix);
   }

   /**
    * Remove all namespace URIs.
    */
   public void removeNamespaces()
   {
      uris.clear();
      prefixes.clear();
   }

   //*********************************************************************
   // Wrapper elements
   //*********************************************************************

   /**
    * Get a specific wrapper element name.
    *
    * @param level The nesting level of the wrapper element. 1-based.
    *
    * @return XMLName of the wrapper element
    */
   public final XMLName getWrapperName(int level)
   {
      return (XMLName)wrapperNames.elementAt(level - 1);
   }

   /**
    * Get the wrapper element names.
    *
    * @return A Vector containing the XMLNames of the wrapper elements. May be empty.
    */
   public final Vector getWrapperNames()
   {
      return (Vector)wrapperNames.clone();
   }

   /**
    * Add a wrapper element name at the specified nesting level.
    *
    * <p>This method shifts wrapper element names at or above the specified nesting
    * level upward one position.</p>
    *
    * @param wrapperName XMLName of the wrapper element.
    * @param level Nesting level of the wrapper element. This number is 1-based.
    *    A value of 0 means to append the wrapper element name to the end of the list.
    * @exception IllegalArgumentException Thrown if the nesting level is greater
    *    than the highest nesting level plus 1.
    */
   public void addWrapperName(XMLName wrapperName, int level)
   {
      if (level == 0)
      {
         wrapperNames.addElement(wrapperName);
      }
      else if ((level > 0) && (level <= wrapperNames.size() + 1))
      {
         wrapperNames.insertElementAt(wrapperName, level - 1);
      }
      else
         throw new IllegalArgumentException("Invalid nesting level for wrapper element: " + level);
   }

   /**
    * Remove the wrapper element at the specified nesting level.
    *
    * <p>This method shifts wrapper element names at or above the specified nesting
    * level downward one position.</p>
    *
    * @param level Nesting level of the wrapper element name. This number is 1-based.
    * @exception IllegalArgumentException Thrown if the nesting level is greater
    *    than the highest nesting level.
    */
   public void removeWrapperName(int level)
   {
      if ((level > 0) && (level <= wrapperNames.size()))
      {
         wrapperNames.removeElementAt(level - 1);
      }
      else
         throw new IllegalArgumentException("Invalid nesting level for wrapper element name: " + level);
   }

   /**
    * Remove all wrapper element names.
    */
   public void removeAllWrapperNames()
   {
      wrapperNames.removeAllElements();
   }

   //*********************************************************************
   // Filters
   //*********************************************************************

   /**
    * Get the filters.
    *
    * @return A Vector containing RootFilter and ResultSetFilter objects
    */
   public final Vector getFilters()
   {
      return (Vector)filters.clone();
   }

   /**
    * Create a root filter.
    *
    * @return The root filter.
    */
   public RootFilter createRootFilter()
   {
      RootFilter filter;

      filter = new RootFilter(map);
      filters.addElement(filter);
      return filter;
   }

   /**
    * Create a result set filter.
    *
    * @param name The name used to identify the result set. If this is null, "Default"
    *    is used.
    * @return The filter.
    * @exception IllegalArgumentException Thrown if the name is already being used.
    */
   public ResultSetFilter createResultSetFilter(String name)
   {
      ResultSetFilter filter;

      if (name == null) name = "Default";
      if (rsNames.get(name) != null)
         throw new IllegalArgumentException("A result set filter for the result set named " + name + " has already been created.");
      rsNames.put(name, O);
      filter = new ResultSetFilter(map, name);
      filters.addElement(filter);
      return filter;
   }

   /**
    * Remove the ith filter.
    *
    * <p>This method shifts filters at or above the specified index
    * downward one position.</p>
    *
    * @param index Index of the filter to remove. 0-based.
    * @exception IllegalArgumentException Thrown if the index is invalid.
    */
   public void removeFilter(int index)
   {
      Object filter;

      if ((index >= 0) && (index < filters.size()))
      {
         filter = filters.elementAt(index);
         if (filter instanceof ResultSetFilter)
         {
            rsNames.remove(((ResultSetFilter)filter).getResultSetName());
         }
         filters.removeElementAt(index);
      }
      else
         throw new IllegalArgumentException("Invalid filter index: " + index);
   }

   /**
    * Remove all filters.
    */
   public void removeAllFilters()
   {
      filters.removeAllElements();
      rsNames.clear();
   }

   //*********************************************************************
   // Filter parameters
   //*********************************************************************

   /**
    * Set the parameters to use with the filters.
    *
    * <p>This method should be called before the filters are used to construct
    * WHERE clauses, since the filters are optimized for the parameters only
    * being set once.</p>
    *
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    */

   public void setFilterParameters(Hashtable params)
   {
      // Set the filter parameters.

      FilterBase         filter;
      Enumeration        tableFilters, relatedTableFilters;
      TableFilter        tableFilter;
      RelatedTableFilter relatedTableFilter;

      for (int i = 0; i < filters.size(); i++)
      {
         filter = (FilterBase)filters.elementAt(i);
         if (filter instanceof RootFilter)
         {
            ((RootFilter)filter).getRootFilterConditions().setParameters(params);
         }

         tableFilters = filter.getTableFilters();
         while (tableFilters.hasMoreElements())
         {
            tableFilter = (TableFilter)tableFilters.nextElement();
            relatedTableFilters = tableFilter.getRelatedTableFilters();
            while (relatedTableFilters.hasMoreElements())
            {
               relatedTableFilter = (RelatedTableFilter)relatedTableFilters.nextElement();
               relatedTableFilter.setParameters(params);
            }
         }
      }
   }

   //*********************************************************************
   // Private methods
   //*********************************************************************

   private void checkArgNull(Object o, String argName)
   {
      if (o == null) throw new IllegalArgumentException (ARGUMENT + argName + NOTNULL);
   }

}
