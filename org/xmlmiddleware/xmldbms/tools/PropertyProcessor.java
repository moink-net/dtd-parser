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

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Base class for Transfer and MapGenerator. <b>For internal use only.</b>
 *
 * <p>PropertyProcessor reads properties from a set of property/value pairs or a
 * properties file. It then adds these to an existing Properties object. If
 * requested, PropertyProcessor will perform two additional actions on the newly
 * read properties.</p>
 *
 * <p>First, it prepends the value of the BaseURL property to the value of all
 * filename properties (MapFile, ActionFile, FilterFile, etc.). Second, it
 * recursively replaces all property file properties (File, File1, File2, etc.)
 * with their contents. The latter action makes it possible to distribute
 * properties over a hierarchy of property files.</p>
 *
 * <p>The BaseURL property is applied hierarchically. That is, PropertyProcessor
 * first checks if there is a BaseURL property at the current level (in the current
 * set of property/value pairs or the current property file). If so, it uses this
 * value. If not, it uses the value of the BaseURL from the closest ancestor set
 * of properties. If none of these has a BaseURL property, it uses
 * "file://localhost/"; this treats all file names as local to the current machine.</p>
 *
 * <p>The purpose of the BaseURL property is portability. That is, a set of
 * property files with the same names can be used on different machines simply by
 * changing the value of the BaseURL property.</p>
 *
 * <p>Note that in expanding property files as well as constructing properties
 * from an array of property/value pairs, duplicate properties are silently
 * overwritten. Thus, the last value of a given property that is read is the one
 * that is used.</p>
 *
 * <p>PropertyProcessor is the base class for Transfer and GenerateMap.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 */

public class PropertyProcessor
{
   // ************************************************************************
   // Constants
   // ************************************************************************

   protected static String LOCALFILEURL = "file://localhost/";

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
    *    <property>=<value>
    * </pre>
    *
    * @param props The Properties object to add the new properties to
    * @param pairs String array of property/value pairs.
    * @param start The index to start in the pairs array. 0-based.
    * @param expandFiles Whether to expand property files, etc. See the introduction
    *    for details.
    * @exception FileNotFoundException Thrown if an input file is not found.
    * @exception IOException Thrown if an error occurs accessing an input file.
    */
   public void addPropertiesFromArray(Properties props, String[] pairs, int start, boolean expandFiles)
      throws FileNotFoundException, IOException
   {
      Properties cmdLineProps = new Properties();
      int        equalsIndex;
      String     prop, value, baseURL;

      // Read through the property / value pairs and construct properties. Note that
      // we allow "<property>=" -- a null value. This is useful for turning off the
      // value of the BaseURL property.

      for (int i = start; i < pairs.length; i++)
      {
         equalsIndex = pairs[i].indexOf('=');
         if ((equalsIndex == -1) || (equalsIndex == 0))
            throw new IllegalArgumentException("Invalid property/value pair: " + pairs[i]);
         prop = pairs[i].substring(0, equalsIndex);
         value = pairs[i].substring(equalsIndex + 1);
         cmdLineProps.put(prop, value);
      }

      // Either expand property files and filenames or simply copy the properties. The
      // latter case is useful if you want to serialize properties named File, File1,
      // etc.

      if (expandFiles)
      {
         // Use the base URL from the property / value pairs. If it doesn't exist, use
         // the base URL from props. If it doesn't exist, treat names as local filenames.

         baseURL = getBaseURL(props, null);
         baseURL = getBaseURL(cmdLineProps, baseURL);
         expandFiles(cmdLineProps, props, baseURL);
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
    * @param filename Name of the property file. If this is not a complete
    *    URL, the method checks for a BaseURL property in props. If none is
    *    found, it is treated as a local file name.
    * @param expandFiles Whether to expand property files, etc. See the introduction
    *    for details.
    * @exception FileNotFoundException Thrown if an input file is not found.
    * @exception IOException Thrown if an error occurs accessing an input file.
    */
   public void addPropertiesFromFile(Properties props, String filename, boolean expandFiles)
      throws FileNotFoundException, IOException
   {
      Properties fileProps;
      String     baseURL;

      // Use the base URL from props if it exists. Otherwise, treat the filename
      // as a local filename.

      baseURL = getBaseURL(props, null);

      // Get the properties from the file.

      fileProps = getPropsFromFile(baseURL, filename);

      // Either expand property files and filenames or simply copy the properties. The
      // latter case is useful if you want to serialize properties named File, File1,
      // etc.

      if (expandFiles)
      {
         // Check if the property file contains a base URL. If not, use the
         // previous value.

         expandFiles(fileProps, props, getBaseURL(fileProps, baseURL));
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

   protected URL buildURL(String baseURL, String filename)
      throws FileNotFoundException
   {
      try
      {
         // We don't know if the filename is a URL or not. To find this
         // out, attempt to create a URL over the file name. If there is no
         // protocol, this will throw an exception. Hacky, but it works.

         return new URL(filename);
      }
      catch (MalformedURLException m1)
      {
         try
         {
            if (baseURL == null)
            {
               // If the base URL is null, then assume that the file is on the
               // local machine. In this case, get the path name by creating
               // a File object and asking for the absolute path. Turn this into
               // a URL by prepending file://localhost/.

               File f = new File(filename);
               return new URL(LOCALFILEURL + f.getAbsolutePath());
            }
            else
            {
               // Otherwise, prepend the base URL, which must have a protocol.
               // The combination of base URL and file name must be a valid URL.

               return new URL(baseURL + filename);
            }
         }
         catch (MalformedURLException m2)
         {
            throw new FileNotFoundException(m2.getMessage() + ": " + baseURL + filename);
         }
      }
   }   

   protected String buildURLString(String url, String filename)
      throws FileNotFoundException
   {
      URL realURL;

      // Build a URL over the base URL and filename combination. Serialize this
      // and return it.

      realURL = buildURL(url, filename);
      return realURL.toExternalForm();
   }

   // ************************************************************************
   // Private methods
   // ************************************************************************

   private String getBaseURL(Properties props, String defaultBaseURL)
   {
      String baseURL;

      baseURL = (String)props.get(XMLDBMSProps.BASEURL);
      return (baseURL == null) ? defaultBaseURL : baseURL;
   }

   private void expandFiles(Properties source, Properties target, String defaultBaseURL)
      throws FileNotFoundException, IOException
   {
      Enumeration enum;
      String      prop, value, baseURL;
      Properties  fileProps = new Properties();

      // Check if there is a base URL in the source properties. If so, use it. If not,
      // use the base URL from the parent in the hierarchy.

      baseURL = getBaseURL(source, defaultBaseURL);

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

            fileProps = getPropsFromFile(baseURL, value);
            expandFiles(fileProps, target, baseURL);
         }
         else if (!prop.equals(XMLDBMSProps.BASEURL))
         {
            // If the property is not a property file, copy it over. Note that we
            // don't copy the base URL property and that we prepend the base URL
            // to filenames.

            if (isFilename(prop))
            {
               value = buildURLString(baseURL, value);
            }
            target.put(prop, value);
         }
      }
   }

   private Properties getPropsFromFile(String baseURL, String filename)
      throws FileNotFoundException, IOException
   {
      URL        propFileURL;
      Properties props = new Properties();

      propFileURL = buildURL(baseURL, filename);
      props.load(propFileURL.openStream());
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

   private boolean isFilename(String prop)
   {
      // The following properties contain filenames: MapFile, ActionFile,
      // FilterFile.

      if (prop.equals(XMLDBMSProps.MAPFILE)) return true;
      if (prop.equals(XMLDBMSProps.ACTIONFILE)) return true;
      if (prop.equals(XMLDBMSProps.FILTERFILE)) return true;
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
