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
 * Class representing an unparsed entity.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class UnparsedEntity extends Entity
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The notation used by the entity. */
   public String notation = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new UnparsedEntity. */
   public UnparsedEntity()
   {
	  this.type = Entity.TYPE_UNPARSED;
   }   


   /** Construct a new UnparsedEntity and set its name. */
   public UnparsedEntity(String name)
   {
	  super(name);
	  this.type = Entity.TYPE_UNPARSED;
   }   
}