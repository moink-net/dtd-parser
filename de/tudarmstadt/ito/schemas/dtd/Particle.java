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
 * Class representing a content particle in a content model. This is
 * the base class for Group and Reference.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Particle
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Content particle type unknown. */
   public static final int PARTICLE_UNKNOWN = 0;

   /** Content particle is a reference to an element type (Reference). */
   public static final int PARTICLE_ELEMENTTYPEREF = 1;

   /** Content particle is a choice group (Group). */
   public static final int PARTICLE_CHOICE = 2;

   /** Content particle is a sequence group (Group). */
   public static final int PARTICLE_SEQUENCE = 3;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** Content particle type. */
   public int     type = PARTICLE_UNKNOWN;

   // The following table shows how these map to *, +, and ?
   //
   //                      isRequired
   // isRepeatable  |   true    |    false
   // --------------|-----------|--------------
   //         true  |     +     |      *
   // --------------|-----------|--------------
   //        false  |     --    |      ?
   //
   // Note that the defaults map to the required/not repeatable
   // (i.e. no operator) case.

   /**
	* Whether the particle is required. By default, this is true. The
	* following table shows how isRequired and isRepeatable map to the
	* *, +, and ? qualifiers:
	*
	* <pre>
	*
	*                         isRequired
	*    isRepeatable  |   true    |    false
	*    --------------|-----------|--------------
	*            true  |     +     |      *
	*    --------------|-----------|--------------
	*           false  |     --    |      ?
	*
	* </pre>
	*
	* <p>Note that the defaults of isRequired and isRepeatable map to
	* the required/not repeatable (i.e. no operator) case.</p>
	*/
   public boolean isRequired = true;

   /** Whether the particle may be repeated. By default, this is false. */
   public boolean isRepeatable = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Particle. */
   public Particle()
   {
   }   
}