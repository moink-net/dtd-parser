// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * Base class for entities.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Entity
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Unknown entity type. */
   public static final int TYPE_UNKNOWN = 0;

   /** Entity is a parsed general entity. */
   public static final int TYPE_PARSEDGENERAL = 1;

   /** Entity is a parameter entity. */
   public static final int TYPE_PARAMETER = 2;

   /** Entity is an unparsed entity. */
   public static final int TYPE_UNPARSED = 3;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The entity type. */
   public int    type = TYPE_UNKNOWN;

   /** The entity name. */
   public String name = null;

   /** The system ID of the entity. May be null. */
   public String systemID = null;

   /** The public ID of the entity. May be null. */
   public String publicID = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************
   /** Construct a new Entity. */
   public Entity()
   {
   }   

   /** Construct a new Entity and set its name. */
   public Entity(String name)
   {
	  this.name = name;
   }   
}