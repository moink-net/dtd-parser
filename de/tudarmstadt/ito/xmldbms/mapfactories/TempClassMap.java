// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import java.util.Hashtable;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.ClassMap; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempClassMap
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   NSName          name = null;

   int             type = 0;
   TempTable       table = null;

   Hashtable       attributeMaps = new Hashtable();
   TempPropertyMap pcdataMap = null;
   Hashtable       subElementTypeMaps = new Hashtable();

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempClassMap()
   {
   }   

   TempClassMap(NSName name)
   {
	  this.name = name;
   }   

   //***********************************************************************
   // methods
   //***********************************************************************

   void addElementPropertyMap(TempPropertyMap propMap)
	  throws InvalidMapException
   {
	  if (subElementTypeMaps.get(propMap.name.qualified) != null)
		 throw new InvalidMapException("Element type " + propMap.name.qualified + " mapped more than once as a related class or property of " + name.qualified);
	  subElementTypeMaps.put(propMap.name.qualified, propMap);
   }   

   void addAttributePropertyMap(TempPropertyMap propMap)
	  throws InvalidMapException
   {
	  if (attributeMaps.get(propMap.name.qualified) != null)
		 throw new InvalidMapException("Attribute " + propMap.name.qualified + " mapped more than once as a property of " + name.qualified);
	  attributeMaps.put(propMap.name.qualified, propMap);
   }   

   void addPCDATAPropertyMap(TempPropertyMap propMap)
	  throws InvalidMapException
   {
	  if (pcdataMap != null)
		 throw new InvalidMapException("PCDATA for " + name.qualified + " mapped more than once.");
	  pcdataMap = propMap;
   }   

   void addRelatedClassMap(TempRelatedClassMap relatedMap)
	  throws InvalidMapException
   {
	  if (subElementTypeMaps.get(relatedMap.classMap.name.qualified) != null)
		 throw new InvalidMapException("Element type " + relatedMap.classMap.name.qualified + " mapped more than once as a related class or property of " + name.qualified);
	  subElementTypeMaps.put(relatedMap.classMap.name.qualified, relatedMap);
   }   
}