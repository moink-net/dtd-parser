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
 * Describes the relationship between two classes;
 * <A HREF="../readme.html#NotForUse">not for general use</A>.
 *
 * <P>RelatedClassMaps describe the relationship between a parent
 * element type and a child element type, both of which are mapped as
 * classes. The RelatedClassMap is stored in the subElementTypeMaps
 * variable of the ClassMap for the parent. (A separate ClassMap is
 * needed to describe how the child is mapped.)</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class RelatedClassMap
{
   /** A ClassMap object that maps the child element type-as-class. */
   public ClassMap classMap = null;

   /**
	* A LinkInfo object describing the keys used to link the tables
	* of the parent and child element types-as-classes.
	*/
   public LinkInfo linkInfo = new LinkInfo();

   /**
	* An OrderInfo object describing the column containing the order
	* information for where the child element occurs in the parent. If
	* there is no order information, this object still exists, but has
	* its orderColumn member set to null.
	*/
   public OrderInfo orderInfo = new OrderInfo();

}