// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
// Version 1.1
// Changes from version 1.01: New in 1.1
package de.tudarmstadt.ito.xmldbms.tools;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;
/**
 * Utility for generating Java properties files.
 *
 * <p>GeneratePropFile provides a command line utility for
 * generating Java properties files from a set of property/value pairs.
 * The syntax of GeneratePropFile is:</p>
 *
 * <pre>
 * java GeneratePropFile <property>=<value> [<property>=<value>...]
 * </pre>
 *
 * <p>The name of the output file is specified with the special property
 * OutputFile. To include properties from other property files, use the
 * special property File, whose value is a property file name. The properties
 * are copied to the new file. If any property/value pairs contain spaces,
 * they must be enclosed in quotes.</p>
 *
 * <p>Finally, property/value pairs are read in order. If a property is
 * listed more than once, the last value is used. This can be used to change
 * the value of a property in an existing file. To do this, list the file
 * name first, then the new value. When the file is written out again, the
 * new value will overwrite the old value.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 */
public class GeneratePropFile extends ProcessProperties
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
    	* <p>See introduction for command line syntax.</p>
    	*
    	* @exception FileNotFoundException Thrown if an input file is not found.
    	* @exception IOException Thrown if an error occurs accessing an input
    	*                        or output file.
    	*/
    public static void main(String[] args) throws Exception
    {
        Properties props;
        GeneratePropFile gpf = new GeneratePropFile();
        if (args.length <= 0)
            {
            System.out.println(
                "Usage: GeneratePropFile  <property>=<value> [<property>=<value>...]");
            return;
        }
        gpf.writeProps(gpf.getProperties(args, 0));
    }
}