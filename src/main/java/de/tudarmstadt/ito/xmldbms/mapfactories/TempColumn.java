// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import java.sql.Types;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.Column; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempColumn
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   String name = null;
   int    type = Types.NULL;
   int    length = -1;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempColumn()
   {
   }   

   TempColumn(String name)
   {
	  this.name = name;
   }   
}