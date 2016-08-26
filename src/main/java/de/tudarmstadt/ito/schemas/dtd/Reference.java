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
 * Class representing a reference to an ElementType. Used in the
 * members of a Group.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Reference extends Particle
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The referred-to element typo. */
   public ElementType elementType = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Reference. */
   public Reference()
   {
	  this.type = Particle.PARTICLE_ELEMENTTYPEREF;
   }   

   /** Construct a new Reference and set the element type. */
   public Reference(ElementType elementType)
   {
	  this.type = Particle.PARTICLE_ELEMENTTYPEREF;
	  this.elementType = elementType;
   }   
}