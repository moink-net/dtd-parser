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
// Changes from version 1.0: New in 2.0

package org.xmlmiddleware.conversions;

import org.xmlmiddleware.utils.WrappedException;

/**
 * Thrown when a conversion error occurs.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConversionException extends WrappedException
{
   // ********************************************************************
   // Constructors
   // ********************************************************************
   
   /**
     * Create a new ConversionException.
     *
     * @param message The error or warning message.
     */
   public ConversionException(String message) 
   {
      super(message);
   }
   
   /**
     * Create a new ConversionException wrapping an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, and its message will become the default message for
     * the ConversionException.</p>
     *
     * @param e The exception to be wrapped in a ConversionException.
     */
   public ConversionException(Exception e)
   {
      super(e);
   }
   
   /**
     * Create a new ConversionException from an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, but the new exception will have its own message.</p>
     *
     * @param message The detail message.
     * @param e The exception to be wrapped in a ConversionException.
     */
   public ConversionException(String message, Exception e)
   {
      super(message, e);
   }
}