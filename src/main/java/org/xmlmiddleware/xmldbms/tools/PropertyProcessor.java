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
// Changes from version 1.01: New in 1.1
// Changes from version 1.1: Updated for version 2.0

package org.xmlmiddleware.xmldbms.tools;

import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Base class for Transfer and MapManager. <b>For internal use only.</b>
 *
 * <p>PropertyProcessor reads properties from a set of property/value pairs or a
 * properties file. It then adds these to an existing Properties object. If
 * requested, PropertyProcessor will recursively replace all property file
 * properties (File, File1, File2, etc.) with their contents. This makes it
 * possible to distribute properties over a hierarchy of property files.</p>
 *
 * <p>Note that in expanding property files as well as constructing properties
 * from an array of property/value pairs, duplicate properties are silently
 * overwritten. Thus, the last value of a given property that is read is the one
 * that is used.</p>
 *
 * <p>PropertyProcessor is the base class for Transfer and MapManager.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 */

public class PropertyProcessor
{
   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
    * Construct a PropertyProcessor object.
    */
   public PropertyProcessor()
   {
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Add properties to a Properties object from an array of property/value pairs.
    *
    * <p>The syntax of each string is:</p>
    *
    * <pre>
    *    &lt;property>=&lt;value>
    * </pre>
    *
    * @param props The Properties object to add the new properties to
    * @param pairs String array of property/value pairs.
    * @param start The index to start in the pairs array. 0-based.
    * @param expandFiles Whether to expand property files, etc. See the introduction
    *    for details.
    * @exception XMLMiddlewareException Thrown if an input file is not found or
    *    if an error occurs accessing an input file.
    */
   public void addPropertiesFromArray(Properties props, String[] pairs, int start, boolean expandFiles)
      throws XMLMiddlewareException
   {
      Properties cmdLineProps = new Properties();
      int        equalsIndex;
      String     prop, value;

      // Read through the property / value pairs and construct properties. Note that
      // we allow "<property>=" -- a null value.

      for (int i = start; i < pairs.length; i++)
      {
         equalsIndex = pairs[i].indexOf('=');
         if ((equalsIndex == -1) || (equalsIndex == 0))
            throw new IllegalArgumentException("Invalid property/value pair: " + pairs[i]);
         prop = pairs[i].substring(0, equalsIndex);
         value = pairs[i].substring(equalsIndex + 1);
         cmdLineProps.put(prop, value);
      }

      // Either expand property files or simply copy the properties. The latter case
      // is useful if you want to serialize properties named File, File1, etc.

      if (expandFiles)
      {
         expandFiles(cmdLineProps, props);
      }
      else
      {
         copyProps(cmdLineProps, props);
      }
   }

   /**
    * Add properties to a Properties object from a property file.
    *
    * @param props The Properties object to add the new properties to
    * @param filename Name of the property file.
    * @param expandFiles Whether to expand property files, etc. See the introduction
    *    for details.
    * @exception XMLMiddlewareException Thrown if an input file is not found or
    *    if an error occurs accessing an input file.
    */
   public void addPropertiesFromFile(Properties props, String filename, boolean expandFiles)
      throws XMLMiddlewareException
   {
      Properties fileProps;

      // Get the properties from the file.

      fileProps = getPropsFromFile(filename);

      // Either expand property files or simply copy the properties. The latter case
      // is useful if you want to serialize properties named File, File1, etc.

      if (expandFiles)
      {
         expandFiles(fileProps, props);
      }
      else
      {
         copyProps(fileProps, props);
      }
   }

   // ************************************************************************
   // Protected methods
   // ************************************************************************

   /**
    * Returns an array of values corresponding to numbered properties.
    *
    * <p>For example, if the base parameter is Foo, this method returns
    * the values of the properties Foo1, Foo2, Foo3, and so on. Property
    * numbers start with 1 and continue until a property is not found.</p>
    *
    * @param base Base property name.
    * @param props Properties object in which to search.
    * @return An array of Strings containing property values. Null if no
    *         properties were found.
    */
   protected String[] getNumberedProps(String base, Properties props)
   {
      String   prop, value;
      String[] array = null;
      Vector   vector = new Vector();
      int      size;

      // Look for properties named <base>1, <base>2, etc.

      for (int i = 1;; i++)
      {
         prop = base + i;
         value = props.getProperty(prop);
         if (value != null)
         {
            vector.addElement(value);
         }
         else
         {
            break;
         }
      }

      // Convert the vector to an array.

      size = vector.size();
      if (size > 0)
      {
         array = new String[size];
         vector.copyInto(array);
      }
      return array;
    }

   // ************************************************************************
   // Private methods
   // ************************************************************************

   private void expandFiles(Properties source, Properties target)
      throws XMLMiddlewareException
   {
      Enumeration enum;
      String      prop, value;
      Properties  fileProps = new Properties();

      // Enumerate and process the properties.

      enum = source.propertyNames();
      while (enum.hasMoreElements())
      {
         // Get the property and its value.

         prop = (String)enum.nextElement();
         value = source.getProperty(prop);

         if (isPropFile(prop))
         {
            // If the property is a property file, recursively expand it.

            fileProps = getPropsFromFile(value);
            expandFiles(fileProps, target);
         }
         else
         {
            // If the property is not a property file, copy it over.

            target.put(prop, value);
         }
      }
   }

   private Properties getPropsFromFile(String filename)
      throws XMLMiddlewareException
   {
      Properties props = new Properties();

      try
      {
         props.load(new FileInputStream(filename));
      }
      catch (IOException e)
      {
         throw new XMLMiddlewareException(e);
      }
      return props;
   }

   private boolean isPropFile(String prop)
   {
      // A property represents a property file if its name is File or
      // File[n...], where n is one or more digits.

      if (prop.startsWith(XMLDBMSProps.FILE))
      {
         if (prop.length() != XMLDBMSProps.FILE.length())
         {
            char[] buf = prop.substring(XMLDBMSProps.FILE.length()).toCharArray();
            for (int j = 0; j < buf.length; j++)
            {
               if ((buf[j] < '0') || (buf[j] > '9')) return false;
            }
         }
         return true;
      }
      return false;
   }

   private void copyProps(Properties source, Properties target)
   {
      Enumeration enum;
      Object      prop;

      enum = source.propertyNames();
      while (enum.hasMoreElements())
      {
         prop = enum.nextElement();
         target.put(prop, source.get(prop));
      }
   }
}
