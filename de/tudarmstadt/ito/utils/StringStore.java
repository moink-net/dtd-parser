// No copyright, no warranty; use as you will.
// Written by Adam Flinton

// Version 1.1
// Changes from version 1.0: New in version 1.1

package de.tudarmstadt.ito.utils;

/**
 * Writes a string to a file.
 *
 * @author Adam Flinton
 * @version 1.1
 */

import java.io.*;
 
public class StringStore {
   /**
    * Write a string to a file.
    *
    * @param filename Name of the file.
    * @param fileContent The content of the file.
    */
   public static void store(String filename, String fileContent)
      throws IOException
   {
      FileWriter fw;

      fw = new FileWriter(filename);
      fw.write(fileContent);
      fw.close();
   }
}