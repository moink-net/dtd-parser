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
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Lists wrapper elements that need to be inserted;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>ElementInsertionList contains a list of ElementInsertionMaps, each of which
 * describes a wrapper element. When data is retrieved from the database, the
 * wrapper elements are constructed beneath the class element. The elements are
 * constructed in ascending order. That is, the first element in the list is
 * the first child of the class element.</p>
 *
 * <p>An ElementInsertionList describes a single chain of wrapper elements. Because
 * wrapper elements can form a tree -- which occurs when a WrapperClassMap contains
 * more than one child WrapperClassMap -- the ElementInsertionMaps in an
 * ElementInsertionList can appear in more than one ElementInsertionList. When this
 * occurs, DBMSToDOM is careful not to construct duplicate wrapper elements. In
 * particular, a class table or wrapper element can have at most one child wrapper
 * element with a particular name.</p>
 *
 * <p>ElementInsertionLists are stored in ColumnMaps, PropertyMaps, and
 * RelatedClassTableMaps.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class ElementInsertionList extends MapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Vector list = new Vector();

   // ********************************************************************
   // Constructor
   // ********************************************************************

   private ElementInsertionList()
   {
   }

   /**
    * Create a new ElementInsertionList.
    *
    * @return The ElementInsertionList.
    */
   public static ElementInsertionList create()
   {
      return new ElementInsertionList();
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Clone an ElementInsertionList.
    *
    * <p>This method performs a "shallow" clone. That is, the ElementInsertionMaps
    * in the list are not cloned.</p>
    *
    * @return The cloned ElementInsertionList
   */
   public Object clone()
   {
      ElementInsertionList clone = new ElementInsertionList();

      for (int i = 0; i < list.size(); i++)
      {
         clone.addElementInsertionMap((ElementInsertionMap)list.elementAt(i));
      }

      return clone;
   }

   // ********************************************************************
   // Public methods -- element insertion maps
   // ********************************************************************

   /**
    * Get an ElementInsertionMap.
    *
    * @param position The position of the ElementInsertionMap in the list.
    *    The position is 0-based.
    *
    * @return The ElementInsertionMap.
    */
   public ElementInsertionMap getElementInsertionMap(int position)
   {
      return (ElementInsertionMap)list.elementAt(position);
   }

   /**
    * Get all ElementInsertionMaps.
    *
    * @return An Enumeration of ElementInsertionMaps. May be empty.
    */
   public Enumeration getElementInsertionMaps()
   {
      return list.elements();
   }

   /**
    * Add an ElementInsertionMap to the end of the list.
    *
    * @param elementInsertionMap The ElementInsertionMap.
    */
   public void addElementInsertionMap(ElementInsertionMap elementInsertionMap)
   {
      checkArgNull(elementInsertionMap, ARG_ELEMENTINSERTIONMAP);
      list.addElement(elementInsertionMap);
   }

   /**
    * Insert an ElementInsertionMap at the specified position.
    *
    * <p>All ElementInsertionMaps at or after the specified position are
    * moved one position later in the list.</p>
    *
    * @param elementInsertionMap The ElementInsertionMap.
    * @param position The position of the ElementInsertionMap in the list.
    *    The position is 0-based.
    */
   public void insertElementInsertionMap(ElementInsertionMap elementInsertionMap, int position)
   {
      checkArgNull(elementInsertionMap, ARG_ELEMENTINSERTIONMAP);
      list.insertElementAt(elementInsertionMap, position);
   }

   /**
    * Overwrite the ElementInsertionMap at the specified position.
    *
    * @param elementInsertionMap The ElementInsertionMap.
    * @param position The position of the ElementInsertionMap in the list.
    *    The position is 0-based.
    */
   public void setElementInsertionMap(ElementInsertionMap elementInsertionMap, int position)
   {
      checkArgNull(elementInsertionMap, ARG_ELEMENTINSERTIONMAP);
      list.setElementAt(elementInsertionMap, position);
   }

   /**
    * Remove the ElementInsertionMap at the specified position.
    *
    * <p>All ElementInsertionMaps after the specified position are
    * moved one position earlier in the list.</p>
    *
    * @param position The position of the ElementInsertionMap in the list.
    *    The position is 0-based.
    */
   public void removeElementInsertionMap(int position)
   {
      list.removeElementAt(position);
   }

   /**
    * Remove all ElementInsertionMaps from the list.
    */
   public void removeAllElementInsertionMaps()
   {
      list.removeAllElements();
   }

   /**
    * Get the number of ElementInsertionMaps in the list.
    *
    * @return The number of ElementInsertionMaps in the list.
    */
   public int size()
   {
      return list.size();
   }
}
