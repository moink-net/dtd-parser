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
// Changes from version 1.01: New in version 1.1
// Changes from version 1.1: Now extends WrappedException

package org.xmlmiddleware.domutils;

import org.xmlmiddleware.utils.WrappedException;

/**
 * Exception thrown by methods in the ParserUtils interface.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ParserUtilsException extends WrappedException
{
   // ********************************************************************
   // Constructors
   // ********************************************************************
   
   /**
     * Create a new ParserUtilsException.
     *
     * @param message The error or warning message.
     */
   public ParserUtilsException(String message) 
   {
      super(message);
   }
   
   /**
     * Create a new ParserUtilsException wrapping an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, and its message will become the default message for
     * the ParserUtilsException.</p>
     *
     * @param e The exception to be wrapped in a ParserUtilsException.
     */
   public ParserUtilsException(Exception e)
   {
      super(e);
   }
   
   /**
     * Create a new ParserUtilsException from an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, but the new exception will have its own message.</p>
     *
     * @param message The detail message.
     * @param e The exception to be wrapped in a ParserUtilsException.
     */
   public ParserUtilsException(String message, Exception e)
   {
      super(message, e);
   }
}