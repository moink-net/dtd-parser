// No copyright, no warranty; use as you will.
// Code copied from SAXException, by David Megginson

// Version 1.1
// Changes from version 1.01: New in version 1.1

package de.tudarmstadt.ito.domutils;

/**
 * Exception thrown by methods in the ParserUtils interface.
 *
 * <p>This class can encapsulate another Exception. The code
 * is largely copied from SAXException, by David Megginson.</p>
 *
 * @version 1.1
 */

public class ParserUtilsException extends Exception {

   // ********************************************************************
   // Variables
   // ********************************************************************
   
   private Exception exception;
   
   // ********************************************************************
   // Constructors
   // ********************************************************************
   
   /**
    * Create a new ParserUtilsException.
    *
    * @param message The error or warning message.
    */
   public ParserUtilsException (String message) {
	super(message);
	this.exception = null;
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
   public ParserUtilsException (Exception e)
   {
	super();
	this.exception = e;
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
   public ParserUtilsException (String message, Exception e)
   {
	super(message);
	this.exception = e;
   }
   
   // ********************************************************************
   // Public methods
   // ********************************************************************
   
   /**
    * Return a detail message for this exception.
    *
    * <p>If there is an embedded exception, and if the ParserUtilsException
    * has no detail message of its own, this method will return
    * the detail message from the embedded exception.</p>
    *
    * @return The error or warning message.
    */
   public String getMessage ()
   {
	String message = super.getMessage();
	
	if (message == null && exception != null) {
	   return exception.getMessage();
	} else {
	   return message;
	}
   }
   
   /**
    * Return the embedded exception, if any.
    *
    * @return The embedded exception, or null if there is none.
    */
   public Exception getException ()
   {
	return exception;
   }

   /**
    * Override toString to pick up any embedded exception.
    *
    * @return A string representation of this exception.
    */
   public String toString ()
   {
	if (exception != null) {
	   return exception.toString();
	} else {
	   return super.toString();
	}
   }
}
