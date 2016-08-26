// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.utils.NSName;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.PropertyMap; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempPropertyMap
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   NSName        name = null;
   int           type = 0;
   TempTable     table = null;
   TempLinkInfo  linkInfo = null;
   TempColumn    column = null;
   TempOrderInfo orderInfo = new TempOrderInfo();
   boolean       multiValued = false;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempPropertyMap()
   {
   }   
}