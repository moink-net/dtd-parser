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
 * Class representing a parsed general entity.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class ParsedGeneralEntity extends Entity
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /**
	* The value of the parsed general entity. This also serves as a flag
	* that the entity is an internal entity. It must be null if the
	* systemID or publicID variables inherited from Entity are non-null.
	*/
   public String value = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a ParsedGeneralEntity. */
   public ParsedGeneralEntity()
   {
	  this.type = Entity.TYPE_PARSEDGENERAL;
   }   

   /** Construct a ParsedGeneralEntity and set its name. */
   public ParsedGeneralEntity(String name)
   {
	  super(name);
	  this.type = Entity.TYPE_PARSEDGENERAL;
   }   
}