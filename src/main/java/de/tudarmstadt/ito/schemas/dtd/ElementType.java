// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

import de.tudarmstadt.ito.utils.NSName;
import java.util.Hashtable;

/**
 * Class representing an element type.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class ElementType
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Unknown content type. */
   public static final int CONTENT_UNKNOWN = 0;

   /** Empty content type. */
   public static final int CONTENT_EMPTY = 1;

   /** Any content type. */
   public static final int CONTENT_ANY = 2;

   /** PCDATA-only content type. */
   public static final int CONTENT_PCDATA = 3;

   /**
	* "Mixed" content type. The content model must include at least one
	* child element type.
	*/
   public static final int CONTENT_MIXED = 4;

   /** Element content type. */
   public static final int CONTENT_ELEMENT = 5;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The local, prefixed, and qualified names of the element type. */
   public NSName name;

   /** The type of the content model. Must be one of the CONTENT_* constants. */
   public int contentType = CONTENT_UNKNOWN;

   /**
	* A Group representing the content model. Must be null if the content type
	* is not CONTENT_ELEMENT or CONTENT_MIXED. In the latter case, it must be
	* a choice group with no child Groups.
	*/
   public Group content = null;

   /**
	* A Hashtable of Attributes, keyed by the qualified attribute name
	* (Attribute.name.qualified). May be empty.
	*/
   public Hashtable attributes = new Hashtable();

   /**
	* A Hashtable of child ElementTypes, keyed by the qualified element
	* type name (ElementType.name.qualified). May be empty.
	*/
   public Hashtable children = new Hashtable();

   /**
	* A Hashtable of parent ElementTypes, keyed by the qualified element
	* type name (ElementType.name.qualified). May be empty.
	*/
   public Hashtable parents = new Hashtable();

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new ElementType. */
   public ElementType()
   {
   }   

   /** Construct a new ElementType and set its name. */
   public ElementType(NSName name)
   {
	  this.name = name;
   }   
}