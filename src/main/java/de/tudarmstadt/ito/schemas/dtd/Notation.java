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
 * Class representing a notation.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Notation
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The name of the notation. */
   public String name = null;

   /**
	* The public ID of the notation. Either this or systemID must be
	* non-null.
	*/
   public String publicID = null;

   /**
	* The system ID of the notation. Either this or publicID must be
	* non-null.
	*/
   public String systemID = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Notation. */
   public Notation()
   {
   }   
}