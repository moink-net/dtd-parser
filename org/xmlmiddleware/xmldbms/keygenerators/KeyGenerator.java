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
 * <p>Applications that want DOMToDBMS to generate key values pass an
 * instance of an object that implements this class to DOMToDBMS. Keys
 * are used to join tables (class table-to-class table or class table-
 * to-property table) and also to retrieve data from root tables.
 * Whether DOMToDBMS generates a key for a given table (or one or more
 * properties are mapped to the key columns) depends on the mapping to
 * that table.</p>
 *
 * <p>Programmers using the lowest level interface to XML-DBMS call
 * initialize before passing a KeyGenerator object to DOMToDBMS and 
 * close after DOMToDBMS returns. They do not call generateKey, which
 * is called only by DOMToDBMS.</p>
 *
 * <p>Programmers calling higher level interfaces to XML-DBMS do not
 * call any of the methods on this interface. Instead, they specify the
 * name of a class that implements this interface and the properties
 * needed to initialize that class.</p>
 *
 * <P>The helper class HighLow provides a sample implementation
 * of this interface.</P>
 *
 * @author Ronald Bourret
 * @version 2.0
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
	* @return The key as an array of Objects.
	* @exception XMLMiddlewareException An error occured while generating the key.
	*/

   public Object[] generateKey() throws XMLMiddlewareException;      

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
