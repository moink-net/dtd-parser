// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import java.util.Vector;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.LinkInfo; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempLinkInfo
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   Vector  parentKey = new Vector();
   Vector  childKey = new Vector();
   boolean generateKey = false;
   boolean parentKeyIsCandidate = false;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempLinkInfo()
   {
   }   
}