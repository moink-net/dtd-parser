// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import de.tudarmstadt.ito.utils.NSName;
import java.util.Hashtable;

/**
 * Maps an element type as a class; <A HREF="../readme.html#NotForUse">not for
 * general use</A>.
 *
 * <P>ClassMap contains information about an element type-as-class:
 * how it is mapped, what table it is mapped to, maps for attributes,
 * PCDATA, and child element types, etc. ClassMaps
 * are stored in RootClassMaps and RelatedClassMaps, which are
 * accessed through hash tables keyed by element type name.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class ClassMap
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Map the element type to a root table. */
   public static final int TYPE_TOROOTTABLE = 1;

   /** Map the element type to a class table. */
   public static final int TYPE_TOCLASSTABLE = 2;

   /** Ignore the element type. */
   public static final int TYPE_IGNOREROOT = 3;

   /** Map the element type as pass-through; not implemented yet. */
   public static final int TYPE_PASSTHROUGH = 4;  // Not implemented yet

   // ********************************************************************
   // Variables
   // ********************************************************************

   /**
	* Name of the class (element type). This name includes full namespace
	* information.
	*/
   public NSName name = null;

   /**
	* How the class is mapped: to a root table, to a class table,
	* ignored, or passed-through.
	*/
   public int type;

   /**
	* The table to which the class is mapped. Null if the class is
	* ignored or passed through.
	*/
   public Table table = null;

   /**
	* A Hashtable containing PropertyMaps for attributes. The Hashtable
	* is keyed by the namespace-qualified attribute name. Null if no
	* attributes are mapped.
	*/
   public Hashtable attributeMaps = new Hashtable();

   /**
	* The PropertyMap for the PCDATA. Null if the PCDATA is not mapped.
	*/
   public PropertyMap pcdataMap = null;

   /**
	* A Hashtable containing maps for child element types. The Hashtable
	* is keyed by the namespace-qualified element type name. It can
	* contain RelatedClassMaps (for child element types-as-classes) or
	* PropertyMaps (for child element types-as-properties). In the
	* future, it will also contain PassThroughMaps (for passed-through
	* child element types). Null if no child element types are mapped.
	*/
   public Hashtable subElementTypeMaps = new Hashtable();

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
	* Construct a ClassMap.
	*/
   public ClassMap()
   {
   }   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
	* Get a map for PCDATA.
	*
	* @return The PropertyMap for the class' PCDATA. Null if the PCDATA
	*  is not mapped.
	*/
   public PropertyMap getPCDATAMap()
   {
	  return pcdataMap;
   }   

   /**
	* Get a map for a child element.
	*
	* @param qualifiedElementName The namespace-qualified name of the
	*  element.
	*
	* @return A PropertyMap (for element type-as-property children) or
	*  RelatedClassMap (for element type-as-class children). Null if the
	*  child element is not mapped.
	*/
   public Object getElementTypeMap(String qualifiedElementName)
   {
	  return subElementTypeMaps.get(qualifiedElementName);
   }   

   /**
	* Get a map for an attribute.
	*
	* @param qualifiedAttrName The namespace-qualified name of the
	*  attribute.
	*
	* @return A PropertyMap for the attribute. Null if the attribute
	*  is not mapped.
	*/
   public PropertyMap getAttributeMap(String qualifiedAttrName)
   {
	  return (PropertyMap)attributeMaps.get(qualifiedAttrName);
   }   
}