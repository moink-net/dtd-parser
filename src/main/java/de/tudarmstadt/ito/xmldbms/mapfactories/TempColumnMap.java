// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.ColumnMap; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempColumnMap
{
   //***********************************************************************
   // Constructors
   //***********************************************************************

   int         type = 0;
   TempColumn  column = null;
   TempColumn  orderColumn = null;
   String      property = null;
   String      prefixedProperty = null;
   boolean     multiValued = false;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempColumnMap()
   {
   }   

   TempColumnMap(TempColumn column)
   {
	  this.column = column;
   }   
}