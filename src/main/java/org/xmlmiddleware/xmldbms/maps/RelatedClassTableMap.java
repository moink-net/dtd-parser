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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.xmlutils.*;

/**
 * Maps a primary or foreign key that references a class table;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>A RelatedClassTableMap contains a pointer to the ClassTableMap of the child
 * table, as well as the information needed to join the tables and order the
 * child table in the parent.</p>
 *
 * <p>RelatedClassMap inherits from RelatedMapBase. See RelatedMapBase for methods
 * to get and set the element type, link, and order information.</p>
 *
 * <p>A RelatedClassTableMap can contain an element type name different
 * from the element type name in the ClassTableMap it points to. This allows the
 * same class table to be mapped to different element types depending on the
 * parent table. This is the inverse of the mapping created when a &lt;UseClassMap>
 * element in the XML-DBMS mapping language is used inside a &lt;RelatedClass>
 * element.</p>
 *
 * <p>RelatedClassTableMaps are stored in ClassTableMaps.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see RelatedMapBase
 */

public class RelatedClassTableMap extends RelatedMapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   // The following variables are inherited from RelatedMapBase

//   private LinkInfo  linkInfo = null;
//   private OrderInfo orderInfo = null;
//   private XMLName   elementTypeName = null;

   // The following variables are new to RelatedClassMap

   private ClassTableMap        classTableMap = null;
   private ElementInsertionList elementInsertionList = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private RelatedClassTableMap(ClassTableMap classTableMap)
   {
      this.classTableMap = classTableMap;
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new RelatedClassTableMap.
    *
    * @param classTableMap The related ClassTableMap.
    *
    * @return The RelatedClassTableMap.
    */
   public static RelatedClassTableMap create(ClassTableMap classTableMap)
   {
      checkArgNull(classTableMap, ARG_CLASSTABLEMAP);
      return new RelatedClassTableMap(classTableMap);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   /**
    * Get the ClassTableMap of the related class table.
    *
    * @return The ClassTableMap.
    */
   public final ClassTableMap getClassTableMap()
   {
      return classTableMap;
   }

   /**
    * Set the name of the related element type.
    *
    * <p>This can be different from the element type name in the ClassTableMap
    * to which the RelatedClassTableMap points. This allows the same class table
    * to be mapped to different element types depending on the parent table. This
    * is the inverse of the mapping created when a &lt;UseClassMap> element in
    * the XML-DBMS mapping language is used inside a &lt;RelatedClass> element.</p>
    *
    * @param uri The namespace URI of the related element type. May be null.
    * @param localName The local name of the related element type.
    */
   public void setElementTypeName(String uri, String localName)
   {
      setElementTypeName(XMLName.create(uri, localName));
   }

   /**
    * Set the name of the related element type.
    *
    * <p>This can be different from the element type name in the ClassTableMap
    * to which the RelatedClassTableMap points. This allows the same class table
    * to be mapped to different element types depending on the parent table. This
    * is the inverse of the mapping created when a &lt;UseClassMap> element in
    * the XML-DBMS mapping language is used inside a &lt;RelatedClass> element.</p>
    *
    * @param elementTypeName The name of the related element type.
    */
   public void setElementTypeName(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      super.setElementTypeName(elementTypeName);
   }

   // ********************************************************************
   // Element insertion list
   // ********************************************************************

   // ********************************************************************
   // Element insertion list
   // ********************************************************************

   /**
    * Get the list of inserted wrapper elements, if any.
    *
    * <p>The element to which this table is mapped is constructed as a child
    * of the last element in the list.</p>
    *
    * @return The ElementInsertionList. May be null.
    */
   public ElementInsertionList getElementInsertionList()
   {
      return elementInsertionList;
   }

   /**
    * Set the list of inserted wrapper elements, if any.
    *
    * @param elementInsertionList The ElementInsertionList. May be null.
    */
   public void setElementInsertionList(ElementInsertionList elementInsertionList)
   {
      this.elementInsertionList = elementInsertionList;
   }
}
