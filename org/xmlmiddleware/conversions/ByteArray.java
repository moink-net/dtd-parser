// This software is in the public static domain.
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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.conversions;

/**
 * Convenience class so we can represent binary data (a byte array) as an Object.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ByteArray
{
   private byte[] bytes;

   /**
    * Construct a new ByteArray
    *
    * @param bytes The byte array
    */
   public ByteArray(byte[] bytes)
   {
      this.bytes = bytes;
   }

   /**
    * Get the encapsulated byte array
    *
    * @return bytes The byte array
    */
   public byte[] getBytes()
   {
      return bytes;
   }
}