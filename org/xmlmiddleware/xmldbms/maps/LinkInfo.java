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
// Changes from version 1.0: None
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

/**
 * Provides information needed to link two tables;
 *  <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>LinkInfo contains the keys needed to link one class table to another
 * class table or a class table to a property table.  LinkInfo classes are
 * used in RelatedClassMaps, PropertyMaps, and RelatedTableMaps.</p>
 *
 * <p>In PropertyMaps and RelatedClassMaps, "parent" and "child" refer to the
 * relationship between the corresponding structures in the XML document. In a
 * class / property relationship, such as an element type and an attribute, the
 * parent is the class (mapped by a ClassMap) and child is the property (mapped
 * by a PropertyMap). In a class / related class relationship, such as between
 * an element type and a child element type mapped as a class, the parent is the
 * class (mapped by a ClassMap) and the child is the related class (mapped by a
 * RelatedClassMap).</p>
 *
 * <p>In RelatedTableMaps, "parent" and "child" refer to the relationship between
 * the corresponding structures in the database. That is, the parent is the table
 * encountered first in the database hierarchy (mapped by a ClassTableMap) and the
 * child is the table encountered second (mapped by a RelatedClassTableMap).</p>
 *
 * <p>The parent / child relationship is independent of where the primary /
 * unique key is located. That is, it could be in the table of the parent
 * or the child. Which table contains the primary / unique key is determined
 * by the types of the parent and child keys.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class LinkInfo extends MapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private Key parentKey = null;
   private Key childKey = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private LinkInfo(Key parentKey, Key childKey)
   {
      this.parentKey = parentKey;
      this.childKey = childKey;
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new LinkInfo object.
    *
    * <p>If the parent key is a primary or unique key, the child key must be
    * a foreign key and vice versa.</p>
    *
    * @param parentKey The key in the parent table.
    * @param childKey The key in the child table.
    *
    * @return The LinkInfo object.
    */
   public static LinkInfo create(Key parentKey, Key childKey)
   {
      int parentType, childType;

      checkArgNull(parentKey, ARG_PARENTKEY);
      checkArgNull(childKey, ARG_CHILDKEY);

      parentType = parentKey.getType();
      childType = childKey.getType();

      if (((parentType == Key.FOREIGN_KEY) &&
           ((childType == Key.PRIMARY_KEY) || (childType == Key.UNIQUE_KEY))) ||
          ((childType == Key.FOREIGN_KEY) &&
           ((parentType == Key.PRIMARY_KEY) || (parentType == Key.UNIQUE_KEY))))
      {
         return new LinkInfo(parentKey, childKey);
      }
      else
         throw new IllegalArgumentException("Parent key must have type FOREIGN_KEY and child key must have type PRIMARY_KEY or UNIQUE_KEY, or vice versa.");
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   /**
    * Get the parent key.
    *
    * @return The parent key.
    */
   public final Key getParentKey()
   {
      return parentKey;
   }

   /**
    * Get the child key.
    *
    * @return The child key.
    */
   public final Key getChildKey()
   {
      return childKey;
   }

   /**
    * Whether the parent key is unique (or primary).
    *
    * @return Whether the parent key is unique (or primary).
    */
   public final boolean parentKeyIsUnique()
   {
      // If the child key is foreign, the parent key must be unique / primary.

      return (childKey.getType() == Key.FOREIGN_KEY);
   }
}
