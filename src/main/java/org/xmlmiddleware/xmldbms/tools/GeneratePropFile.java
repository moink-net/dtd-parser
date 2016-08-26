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
//   http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.01: New in 1.1
// Changes from version 1.1: Updated for version 2.0

package org.xmlmiddleware.xmldbms.tools;

import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.io.*;
import java.util.*;

/**
 * Utility for generating Java properties files.
 *
 * <p>GeneratePropFile provides a command line utility for
 * generating Java properties files from a set of property/value pairs.
 * The command line syntax is:</p>
 *
 * <pre>
 * java GeneratePropFile &lt;property file name> &lt;property>=&lt;value> [&lt;property>=&lt;value>...]
 * </pre>
 *
 * <p>If any property/value pairs contain spaces, they must be enclosed in double
 * quotes. For example:</p>
 *
 * <pre>
 * java GeneratePropFile MyPropFile.props "MapFile=My Map File.map"
 * </pre>
 *
 * <p>Property/value pairs are read in order. If a property is listed more than
 * once, the last value is used.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 */
public class GeneratePropFile extends PropertyProcessor
{
   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
    * Construct a GeneratePropFile object.
    */
   public GeneratePropFile()
   {
      super();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Run GeneratePropFile from a command line.
    *
    * <p>See the introduction for the command line syntax.</p>
    *
    * @param args Property/value pairs.
    */
   public static void main(String[] args)
   {
      GeneratePropFile generator = new GeneratePropFile();
      Properties       props = new Properties();

      if (args.length < 2)
      {
         System.out.println("Usage: java GeneratePropFile <property file name> <property>=<value> [<property>=<value>...]>");
      }

      try
      {
         generator.addPropertiesFromArray(props, args, 1, false);
         props.save(new FileOutputStream(args[0]), null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
