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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

/**
 * Maps a table to a class; <a href="../readme.htm#NotForUse">
 * not for general use</a>.
 *
 * <p>ClassTableMap contains information about a table that is viewed as a
 * class and mapped to an element type. It contains methods to get the
 * class table, as well as to get and set the base table (if any).</p>
 *
 * <p>ClassTableMap inherits from ClassTableMapBase. See that class for methods to
 * get the element type name, and to manipulate child maps (column maps, related
 * class table maps, related property table maps, and ElementInsertionMaps).</p>
 *
 * <p>ClassTableMaps are stored in the Map object and in RelatedClassTableMap
 * objects.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 * @see ClassTableMapBase
 */

public class ClassTableMap extends ClassTableMapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   // The following variables are inherited from ClassTableMapBase

//   private XMLName   elementTypeName = null;
//   private Hashtable columnMaps = new Hashtable();
//   private Hashtable relatedClassTableMaps = new Hashtable();
//   private Hashtable propertyTableMaps = new Hashtable();
//   private Hashtable elementInsertionMaps = new Hashtable();

   // The following variables are new to ClassTableMap

   private Table     table = null;
   private Table     baseTable = null;
   private LinkInfo  baseLinkInfo = null;

   // ********************************************************************
   // Constructor
   // ********************************************************************

   private ClassTableMap(Table table)
   {
      this.table = table;
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new ClassTableMap.
    *
    * @param table The table being mapped.
    *
    * @return The ClassTableMap.
    */
   public static ClassTableMap create(Table table)
   {
      checkArgNull(table, ARG_TABLE);
      return new ClassTableMap(table);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   /**
    * Set the element type name.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    */
   public void setElementTypeName(String uri, String localName)
   {
      super.setElementTypeName(uri, localName);
   }

   /**
    * Set the element type name.
    *
    * @param elementTypeName The element type name.
    */
   public void setElementTypeName(XMLName elementTypeName)
   {
      super.setElementTypeName(elementTypeName);
   }

   // ********************************************************************
   // Class table
   // ********************************************************************

   /**
    * Get the Table that the ClassTableMap maps.
    *
    * @return The Table.
    */
   public final Table getTable()
   {
      return table;
   }

   // ********************************************************************
   // Base table
   // ********************************************************************

   /**
    * Get the base Table.
    *
    * @return The base Table. Null if no base Table exists.
    */
   public final Table getBaseTable()
   {
      return baseTable;
   }

   /**
    * Set the base Table.
    *
    * @param baseTable The base table. If there is no base table,
    *    set this to null, in which case whether to use the base table will
    *    be set to false.
    */
   public void setBaseTable(Table baseTable)
   {
      this.baseTable = baseTable;
      if (baseTable == null)
      {
         this.baseLinkInfo = null;
      }      
   }

   /**
    * Get the LinkInfo used to link the class table to the base class table.
    *
    * @return The LinkInfo. The "parent" table is the base class table. Null if
    *    the base class table is not used. 
    */
   public final LinkInfo getBaseLinkInfo()
   {
      return baseLinkInfo;
   }

   /**
    * Set the LinkInfo used to link the table to the base table.
    *
    * <p>This method may not be called if the base table is null.</p>
    *
    * <p>Setting the baseLinkInfo argument to null when the base table is non-null
    * is useful if you want the map objects to preserve inheritance information
    * but want to store the data for the class in a single table, rather than in a
    * base table and a class table. Inheritance information can then be used elsewhere,
    * such as when an XML Schema is generated from a Map.</p>
    *
    * @param baseLinkInfo The LinkInfo. The "parent" table is the base table.
    *    Null if the base table is not used.
    */
   public void setBaseLinkInfo(LinkInfo baseLinkInfo)
   {
      if (baseTable == null)
         throw new IllegalStateException("Cannot call ClassTableMap.setUseBaseTable() if the base table is null.");
      this.baseLinkInfo = baseLinkInfo;
   }
}
