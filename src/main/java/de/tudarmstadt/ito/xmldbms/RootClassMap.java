// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * Maps a root element type as a class; <A HREF="../readme.html#NotForUse">
 * not for general use</A>.
 *
 * <P>RootClassMap is identical to RelatedClassMap, but is used differently
 * and has a separate name to eliminate confusion in the code. It contains
 * information about any element types mapped as classes with a type of
 * TYPE_IGNOREROOT or TYPE_TOROOTTABLE. RootClassMaps are stored in a hash
 * table in the Map class, indexed by element type name.</P>
 *
 * <P>For element types mapped as TYPE_TOROOTTABLE, RootClassMap provides the
 * information needed to retrieve data from the table: the candidate key and
 * order columns. In this case, linkInfo.parentKeyIsCandidate is always false
 * and linkInfo.parentKey is always null. This is because a root table has no
 * parent table. Note that for determining the location of the order column,
 * linkInfo.parentKeyIsCandidate is ignored, as the order column, like the
 * candidate key column, is always in the "child" (root) table.</P>
 *
 * <P>For element types mapped as TYPE_IGNOREROOT, only the classMap variable
 * is used and only its type and subElementTypeMaps variables are used. The
 * purpose is simply to provide a list of legal child element types; the
 * RelatedClassMaps stored in subElementTypeMaps serve the same purpose as
 * the RootClassMap when element types are mapped as TYPE_TOROOTTABLE (see
 * above): to provide the candidate key and order columns used to access the
 * "root" class table. If you find this confusing, you are correct.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class RootClassMap extends RelatedClassMap
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a RootClassMap. */
   public RootClassMap()
   {
	  classMap = null;
	  linkInfo = null;
	  orderInfo = null;
   }   

   /**
	* Construct a RootClassMap from a RelatedClassMap.
	*
	* @param relatedClassMap The RelatedClassMap
	*/
   public RootClassMap(RelatedClassMap relatedClassMap)
   {
	  classMap = relatedClassMap.classMap;
	  orderInfo = relatedClassMap.orderInfo;
	  linkInfo = relatedClassMap.linkInfo;
   }   
}