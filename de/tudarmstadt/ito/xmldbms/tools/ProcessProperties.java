// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
import java.util.HashMap;
import de.tudarmstadt.ito.xmldbms.objectcache.ObjectCache;

/**
 * Base class for Transfer, GenerateMap, etc.
 *
 * <p>ProcessProperties provides various methods for handling
 * properties and serves as the base class for Transfer, GenerateMap,
 * etc. XML-DBMS programmers do not need to use it.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 */

public class ProcessProperties
{
   private static String EQUALS = "=";
   private static String UNDERSCORE = "_";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
	* Construct a ProcessProperties object.
	*/
   public ProcessProperties()
   {
   }   

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Constructs properties from a string array of property value pairs.
	*
	* <p>The syntax of each string is</p>
	*
	* <pre>
	*    <property>=<value>
	* </pre>
	*
	* <p>The special property File states that the value is the name
	* of a Java properties file; ProcessProperties reads the properties from
	* this file.</p>
	*
	* @param args String array of property/value pairs.
	* @param start Position to start in the array.
	* @return A Properties object containing the properties and values
	* @exception FileNotFoundException Thrown if an input file is not found.
	* @exception IOException Thrown if an error occurs accessing an input file.
	*/

   public Properties getProperties(String[] args, int start)
	  throws Exception 
   {
	 int        equalsIndex;
	 Properties props = new Properties();
	 String     prop, value;

	 for (int i = start; i < args.length; i++)
	 {
	   equalsIndex = args[i].indexOf(EQUALS);
	   if ((equalsIndex == -1) || (equalsIndex == 0))
		 throw new IllegalArgumentException("Invalid property/value pair: " + args[i]);

	   prop = args[i].substring(0, equalsIndex);
	   value = args[i].substring(equalsIndex + 1);

	  // System.out.println("Debug ProcProp Key = " +prop);
	   // System.out.println("Debug ProcProp val = " +value);

	   if (prop.equals(XMLDBMSProps.FILE))
	   {
		 addPropertiesFromFile(props, value);
	   }
	   else
	   {
		 props.put(prop, value);
	   }
	 }
	

//	 System.out.println("Use Doc Root = " +props.getProperty(XMLDBMSProps.USEDOCROOT));
	 if(props.getProperty(XMLDBMSProps.USEDOCROOT) != null)
	{
	//	 System.out.println("USEDOCROOT IS NOT NULL & = " +props.getProperty(XMLDBMSProps.USEDOCROOT));
	if(getYesNo(props.getProperty(XMLDBMSProps.USEDOCROOT))) 
	{
//		System.out.println("Adding Doc Root");
		addDocRoot(props);
	}
	} 
	 return props;

   }                                                                                                

   /**
	* Adds properties from a Java properties file.
	*
	* @param props Properties object to which the properties are to be added.
	* @param propFilename Name of the properties file.
	* @exception FileNotFoundException Thrown if an input file is not found.
	* @exception IOException Thrown if an error occurs accessing an input file.
	*/
   public void addPropertiesFromFile(Properties props, String propFilename)
	  throws Exception
   {
	 Properties  fileProps = new Properties();
	 Properties  fp = new Properties();
	 Enumeration enum;
	 String      prop, value;
	 GetFileURL gfu = new GetFileURL();

	 if(props.getProperty(XMLDBMSProps.USEDOCROOT) != null)
	{
	//	 System.out.println("USEDOCROOT IS NOT NULL & += " +props.getProperty(XMLDBMSProps.USEDOCROOT));	
	  if(getYesNo(props.getProperty(XMLDBMSProps.USEDOCROOT)))
	{
		propFilename = props.getProperty(XMLDBMSProps.DOCROOT) + propFilename;
	}
	}
	 
//	System.out.println("PFN = " +propFilename);
	 HashMap h = oc.getMap();
		fp = (Properties)h.get(propFilename);
		if (fp != null)
		{
			fileProps = fp;
			
		}
		else 
		{
			synchronized(ProcessProperties.class) // make thread safe
			{
				fp = (Properties)h.get(propFilename);
				
			if (fp != null)
			{fileProps = fp;
			}
			else  // may have changed between first if and synch call...
			{
		//			System.out.println("loading Props " + propFilename);
			//		System.out.println("loading Props " +gfu.fullqual(propFilename));
	 				String[] S = new String[1];
					S[0] = propFilename;
					fileProps.load(gfu.getFile(S));
	 		//		System.out.println("fileProps = " + fileProps);
	 				
						oc.put(propFilename, fileProps);
				
	 					
			}
		}
	 } 
	 
					enum = fileProps.propertyNames();
	 				while (enum.hasMoreElements())
	 					{
	   					prop = (String)enum.nextElement();
	   					value = (String)fileProps.get(prop);
	   					props.put(prop, value);

	 					}						
//	   					System.out.println("Returning Props");
	}                                                               

   /**
	* Returns an array of values corresponding to numbered properties.
	*
	* <p>For example, if the base parameter is Foo, this method returns
	* the values of the properties Foo1, Foo2, Foo3, and so on. Property
	* numbers start with 1 and continue until a property is not found.</p>
	*
	* @param base Base property name.
	* @param props Properties object in which to search.
	* @return An array of Strings containing property values
	*/
   public String[] getNumberedProps(String base, Properties props)
   {
	 String   prop, value;
	 String[] array;
	 Vector   vector = new Vector();

	 // Look for properties named <base>1, <base>2, etc.
	 for (int i = 1; ; i++ )
	 {
	   prop = base + i;
	   value = (String)props.getProperty(prop);
	   if (value != null)
	   {
		 vector.addElement(value);
	   }
	   else
	   {
		 break;
	   }
	 }
	 array = new String[vector.size()];
	 vector.copyInto(array);
	 return array;
   }               

   /**
	* Returns a two-dimensional array of values corresponding to
	* double-numbered properties.
	*
	* <p>For example, if the base parameter is Foo, this method returns
	* the values of the properties Foo1_1, Foo1_2, Foo1_3, ..., Foo2_1, Foo2_2,
	* and so on. Major and minor numbers start with 1 and increase sequentially.
	* When the next minor number is not found, the major number is incremented
	* and the minor number is reset to 1. When the next major number reaches the
	* outer limit, processing stops.</p>
	*
	* <p>NOTE: This should be rewritten so no outer limit is needed.</p>
	*
	* @param base Base property name.
	* @param props Properties object in which to search.
	* @param outerLimit Maximum value of the major numbers.
	* @return A two-dimensional array of Strings containing property values
	*/
   public String[][] getDoubleNumberedProps(String base, Properties props, int outerLimit)
   {
	 String     prop, value;
	 String[]   innerArray;
	 String[][] outerArray = new String[outerLimit][];
	 int        i, j;
	 Vector     innerVector = new Vector();

	 // Look for properties named <base>1_1, <base>1_2, ... <base>2_1, <base>2_2, etc.
	 for (i = 1; i < outerLimit; i++ )
	 {
	   for (j = 1; ; j++ )
	   {
		 prop = base + i + UNDERSCORE + j;
		 value = (String)props.getProperty(prop);
		 if (value != null)
		 {
			innerVector.addElement(value);
		 }
		 else
		 {
			break;
		 }
		 innerArray = new String[innerVector.size()];
		 innerVector.copyInto(innerArray);
		 outerArray[i] = innerArray;
	   }
	 }
	 return outerArray;
   }               

   /**
	* Concatenates the values of numbered properties.
	*
	* <p>For example, if the base parameter is Foo, this method returns the
	* value of Foo1 + the value of Foo2 + ... and so on. The value of Foo may
	* optionally be included at the start of the string. Processing continues
	* until no a property is not found.</p>
	*
	* @param base Base property name.
	* @param props Properties object in which to search.
	* @param includeBase Whether to include the value of the unnumbered
	*                    base property in the output.
	* @return The concatenated values
	*/
   public String concatNumberedProps(String base, Properties props, boolean includeBase)
   {
	  String property, concatValue = null, value;

	  if (includeBase)
	  {
		 concatValue = (String)props.getProperty(base);
	  }

	  if (concatValue == null)
	  {
		 concatValue = "";
	  }
	
	  // Look for properties named <base>1, <base>2, etc.
	  for (int i = 1; ; i++ )
	  {
		 property = base + i;
		 value = (String)props.getProperty(property);
		 if (value != null)
		 {
			concatValue = concatValue + " " + value;
		 }
		 else
		 {
			break;
		 }
	  }

	  return concatValue;
   }   

   /**
	* Whether a String is the value "Yes" or "No". Case sensitive.
	*
	* @param s The String.
	* @return True (for "Yes") or false (for "No")
	* @exception IllegalArgumentException The string was neither "Yes" nor "No".
	*/
   public boolean getYesNo(String s)
   {
	  if(s.equals(XMLDBMSProps.YES))
		 return true;
	  else if (s.equals(XMLDBMSProps.NO))
		 return false;
	  else
		 throw new IllegalArgumentException("Invalid Yes/No value: " + s);
   }   

   /**
	* Writes the properties to an output file.
	*
	* <p>The name of the output file is stored in the OutputFile property.</p>
	*
	* @param props Properties to write.
	* @IOException An error occurred writing the file.
	*/
   public void writeProps(Properties props) throws IOException
   {
	  String           outputFileName;
	  FileOutputStream outputFileStream;

	  outputFileName = props.getProperty(XMLDBMSProps.OUTPUTFILE);
	  if (outputFileName != null)
	  {
		 // Don't write the output file name to the output file
		 props.remove(XMLDBMSProps.OUTPUTFILE);

		 // Write the properties to the output file
		 outputFileStream = new FileOutputStream(outputFileName);
		 props.save(outputFileStream, "Properties for XML-DBMS");
		 outputFileStream.close();

		 // Add the output file name back into the Properties
		 props.put(XMLDBMSProps.OUTPUTFILE, outputFileName);
	  }
	  else
	  {
		 throw new IllegalArgumentException("You must specify a value for the OutputFile property.");
	  }
   }   

   private static ObjectCache oc = new ObjectCache();

/**
 * Insert the method's description here.
 * Creation date: (18/06/01 15:54:16)
 * @return java.util.Properties
 * @param props java.util.Properties
 */
public void addDocRoot(Properties props) {


	//props.getProperty(XMLDBMSProps.USEDOCROOT)

	String Root = props.getProperty(XMLDBMSProps.DOCROOT);

	if (props.getProperty(XMLDBMSProps.XSLTSCRIPT) != null)
	{
	String xsl = Root + props.getProperty(XMLDBMSProps.XSLTSCRIPT);
	props.put(XMLDBMSProps.XSLTSCRIPT,xsl);
	}

	if (props.getProperty(XMLDBMSProps.MAPFILE) != null)
	{
	String map = Root + props.getProperty(XMLDBMSProps.MAPFILE);
	props.put(XMLDBMSProps.MAPFILE,map);
	}
	
//	return props;
}
}