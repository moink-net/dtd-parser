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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.utils;

/**
 * Provides simple sort routines.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class Sort
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Sort an array of objects by string.
    *
    * <p>All sorting is done in the array passed to the method.</p>
    *
    * @param keys The strings to sort on.
    * @param values The objects to sort.
    */
   public static void sort(String[] keys, Object[] values)
   {
      String s;
      Object o;

      // Sort an array of values by the specified (String) keys.

      for (int i = 0; i < keys.length; i++)
      {
         for (int j = i + 1; j < keys.length; j++)
         { 
            if (keys[i].compareTo(keys[j]) > 0)
            {
               s = keys[i];
               o = values[i];
               keys[i] = keys[j];
               values[i] = values[j];
               keys[j] = s;
               values[j] = o;
            }
         }
      }
   }

   /**
    * Sort an array of objects by long.
    *
    * <p>All sorting is done in the array passed to the method.</p>
    *
    * @param keys The longs to sort on.
    * @param values The objects to sort.
    */
   public static void sort(long[] keys, Object[] values)
   {
      long   key;
      Object o;

      // Sort an array of values by the specified (String) keys.

      for (int i = 0; i < keys.length; i++)
      {
         for (int j = i + 1; j < keys.length; j++)
         { 
            if (keys[i] > keys[j])
            {
               key = keys[i];
               o = values[i];
               keys[i] = keys[j];
               values[i] = values[j];
               keys[j] = key;
               values[j] = o;
            }
         }
      }
   }
}
