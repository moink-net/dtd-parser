// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 2.0

// Changes from version 1.0: None
// Changes from version 1.01:
// * Added initialize(Properties) and close() methods

package org.xmlmiddleware.xmldbms;

import java.util.Properties;

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
 * <P>The helper class KeyGeneratorHighLow provides a sample implementation
 * of this interface.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
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
	* @exception KeyException An error occured while initializing the key.
	*/

   public void initialize(Properties props) throws java.lang.Exception, KeyException;      

   /**
	* Generates a key.
	*
	* <p>This method is called by DOMToDBMS. Applications using DOMToDBMS
	* do not need to call this method.</p>
	* 
	* @return The key as an array of Objects.
	* @exception KeyException An error occured while generating the key.
	*/

   public Object[] generateKey() throws KeyException;      

   /**
	* Closes a key generator.
	*
	* <p>This method must be called by applications after the key generator
	* is used by DOMToDBMS. Note that it is generally possible to use the key
	* generator multiple times before it is closed. Programmers using DOMToDBMS
	* call this method.</p>
	*
	* @exception KeyException An error occured while closing the key.
	*/

   public void close() throws java.lang.Exception, KeyException;      
}