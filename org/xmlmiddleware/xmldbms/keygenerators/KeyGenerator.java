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
// Changes from version 1.01:
// * Added initialize(Properties) and close() methods

package org.xmlmiddleware.xmldbms.keygenerators;

import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.util.*;

/**
 * Interface for a class that generates key values (object IDs).
 *
 * <p>Objects that implement this interface must be able to generate unique
 * keys. The number of columns in the generated key is the choice of the
 * key generator, although single column keys are most common. The generateKey
 * method should have no side effects, as there is no guarantee that all
 * generated keys are actually used. If a key generator is designed to be
 * used with Transfer, it must define any initialization properties it needs
 * and have a zero-argument constructor.</p>
 *
 * <p>Applications that want an external routine to generate the value of
 * a particular key must do two things. First, they must assign a logical
 * name to the key generator in the map document. This is done with the
 * KeyGenerator attribute of the PrimaryKey and UniqueKey elements.</p>
 *
 * <p>Second, they must provide an object that implements the KeyGenerator
 * interface. If the application writes directly to DOMToDBMS, it uses the
 * addKeyGenerator method to pass the KeyGenerator object and its logical
 * name to DOMToDBMS. If the application uses Transfer, it uses the
 * KeyGeneratorName and KeyGeneratorClass properties, plus any properties
 * needed to initialize the key generator.</p>
 *
 * <p>Applications that write directly to DOMToDBMS -- and therefore instantiate
 * KeyGenerator objects themselves -- should use the initialize and close
 * methods to initialize and close the KeyGenerator objects. Applications
 * that use Transfer do not need to use these methods, as Transfer calls
 * them itself. Applications never need to call the generateKey method, as
 * this is called only by DOMToDBMS.</p>
 *
 * <p>The HighLow class provides a sample implementation of the KeyGenerator
 * interface. It uses a high-low algorithm to generate keys.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 * @see HighLow
 */

public interface KeyGenerator
{
   /**
	* Initializes a key generator.
	*
	* <p>This method must be called by applications before the key generator
	* is passed to DOMToDBMS. Applications using DOMToDBMS call this method.</p>
	*
	* @param props A Properties object containing properties to initialize
	*              the key generator. The documentation for the key generator
	*              must give the names and legal values for these properties.
      * @param suffix A numeric suffix to be added to property names. If this
      *     is 0, no suffix is added.
	* @exception XMLMiddlewareException An error occured while initializing the key.
	*/

   public void initialize(Properties props, int suffix) throws XMLMiddlewareException;      

   /**
	* Generates a key.
	*
	* <p>This method is called by DOMToDBMS. Applications using DOMToDBMS
	* do not need to call this method.</p>
	* 
	* @return The key as a Vector of Objects.
	* @exception XMLMiddlewareException An error occured while generating the key.
	*/

   public Vector generateKey() throws XMLMiddlewareException;      

   /**
	* Closes a key generator.
	*
	* <p>This method must be called by applications after the key generator
	* is used by DOMToDBMS. Note that it is generally possible to use the key
	* generator multiple times before it is closed. Programmers using DOMToDBMS
	* call this method.</p>
	*
	* @exception XMLMiddlewareException An error occured while closing the key.
	*/

   public void close() throws XMLMiddlewareException;      
}
