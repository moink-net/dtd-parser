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
 * Thrown when a KeyGenerator encounters an error.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 * @see KeyGenerator
 */

public class KeyException extends Exception
{
   public KeyException (String message)
   {
	  super(message);
   }   
}