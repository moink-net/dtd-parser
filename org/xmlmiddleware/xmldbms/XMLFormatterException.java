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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.0: New in 2.0

package org.xmlmiddleware.xmldbms;

/**
 * Thrown when a error occurs using the XMLFormatter interface.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class XMLFormatterException extends Exception
{
   /**
    * Construct a XMLFormatterException.
    *
    * @param message The error message.
    */
   public XMLFormatterException(String message)
   {
      super(message);
   }
}
