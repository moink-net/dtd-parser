// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.0: None
// Changes from version 1.01: None

package org.xmlmiddleware.xmldbms.keygenerators;

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
