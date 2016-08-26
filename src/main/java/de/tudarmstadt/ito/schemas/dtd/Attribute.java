// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

import de.tudarmstadt.ito.utils.NSName;
import java.util.Vector;

/**
 * Class representing an attribute.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Attribute
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Attribute type unknown. */
   public static final int TYPE_UNKNOWN = 0;

   /** Attribute type CDATA. */
   public static final int TYPE_CDATA = 1;

   /** Attribute type ID. */
   public static final int TYPE_ID = 2;

   /** Attribute type IDREF. */
   public static final int TYPE_IDREF = 3;

   /** Attribute type IDREFS. */
   public static final int TYPE_IDREFS = 4;

   /** Attribute type ENTITY. */
   public static final int TYPE_ENTITY = 5;

   /** Attribute type ENTITIES. */
   public static final int TYPE_ENTITIES = 6;

   /** Attribute type NMTOKEN. */
   public static final int TYPE_NMTOKEN = 7;

   /** Attribute type NMTOKENS. */
   public static final int TYPE_NMTOKENS = 8;

   /** Enumerated attribute type. */
   public static final int TYPE_ENUMERATED = 9;

   /** Notation attribute type. */
   public static final int TYPE_NOTATION = 10;

   /** Default type unknown. */
   public static final int REQUIRED_UNKNOWN = 0;

   /** Attribute is required, no default. Corresponds to #REQUIRED. */
   public static final int REQUIRED_REQUIRED = 1;

   /** Attribute is optional, no default. Corresponds to #IMPLIED. */
   public static final int REQUIRED_OPTIONAL = 2;

   /** Attribute has a fixed default. Corresponds to #FIXED <default>. */
   public static final int REQUIRED_FIXED = 3;

   /** Attribute is optional and has a default. Corresponds to <default>. */
   public static final int REQUIRED_DEFAULT = 4;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The local, prefixed, and qualified names of the attribute. */
   public NSName name = null;

   /** The attribute type. */
   public int type = TYPE_UNKNOWN;

   /** Whether the attribute is required and has a default. */
   public int required = REQUIRED_UNKNOWN;

   /** The attribute's default value. May be null. */
   public String defaultValue = null;

   /**
	* The legal values for attributes with a type of TYPE_ENUMERATED or
	* TYPE_NOTATION. Otherwise null.
	*/
   public Vector enums = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Attribute. */
   public Attribute()
   {
   }   

   /**
	* Construct a new Attribute from its local name, prefix, and namespace URI.
	*/
   public Attribute(String local, String prefix, String uri)
   {
	  name = new NSName(local, prefix, uri);
   }   

   /**
	* Construct a new Attribute from its local, prefixed and qualified names.
	*/
   public Attribute(NSName name)
   {
	  this.name = name;
   }   
}