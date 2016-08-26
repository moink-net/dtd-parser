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
 * Temporary version of de.tudarmstadt.ito.xmldbms.OrderInfo; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempOrderInfo
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   TempColumn orderColumn = null;
   boolean    generateOrder = false;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempOrderInfo()
   {
   }   

   TempOrderInfo(TempColumn orderColumn, boolean generateOrder)
   {
	  this.orderColumn = orderColumn;
	  this.generateOrder = generateOrder;
   }   
}