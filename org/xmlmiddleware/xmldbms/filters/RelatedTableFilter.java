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

import org.xmlmiddleware.xmldbms.maps.Table;

/**
 * Identifies a particular use of a related table. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class RelatedTableFilter extends FilterConditions
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private String parentKeyName;
   private String childKeyName;

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new RelatedTableFilter object.
    *
    * @param table The related table.
    * @param parentKeyName Name of the parent key.
    * @param childKeyName Name of the child key.
    */
   protected RelatedTableFilter(Table table, String parentKeyName, String childKeyName)
   {
      super(table);
      this.parentKeyName = parentKeyName;
      this.childKeyName = childKeyName;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Key names
   //*********************************************************************

   /**
    * Get the name of the parent key.
    *
    * @return The name.
    */
   public final String getParentKeyName()
   {
      return parentKeyName;
   }

   /**
    * Get the name of the child key.
    *
    * @return The name.
    */
   public final String getChildKeyName()
   {
      return childKeyName;
   }
}