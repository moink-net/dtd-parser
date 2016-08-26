// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import java.util.Vector;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.RootTableMap; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempRootTableMap
{
   // This class contains additional information needed by root tables: the
   // type of an ignored parent root (if any), the candidate key, and the
   // order column (if any).

   //***********************************************************************
   // Variables
   //***********************************************************************

   TempTableMap tableMap = null;
   String       ignoredRootType = null;
   String       prefixedIgnoredRootType = null;
   Vector       candidateKey = null;    // Contains TempColumns
   TempColumn   orderColumn = null;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempRootTableMap()
   {
   }   
}