// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.xslt;

import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import java.util.Properties;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

/**
 * Loads classes that implement the ProcessXslt interface.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class XSLTLoader extends ProcessProperties
{
    private ProcessXslt processxslt;
    private static String PROCESSXSLT = "ProcessXslt";
    /**
     * Construct a new XSLTLoader.
     */
    public XSLTLoader()
    {
        super();
    }
    /**
    * Set the XSLT Parser to use properties.
    *
    * <p>This method must be called before trying to use the parser</p>
    *
    * <ul>
    * <li>XSLTCLASS. Name of a class that implements the ProcessXslt interface for the parser. </li>
    * </ul>
    *
    * @param props A properties object containing the above properties.
    */
    private void setXSLTProperties(Properties props)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        processxslt =
            (ProcessXslt) instantiateClass(props.getProperty(XSLTProps.XSLTCLASS));
    }
    /**
    * Load the ProcessXslt class.
    *
    * @param props A Properties object that contains an XSLTCLASS property.
    *
    * @return A ProcessXslt object
    */
    public ProcessXslt init(Properties props)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        setXSLTProperties(props);
        checkState(PROCESSXSLT, processxslt);
        return processxslt;
    }
    /** Checks to see that a class has been loaded correctly */
    private void checkState(String interfaceName, Object interfaceObject)
    {
        if (interfaceObject == null)
            throw new IllegalStateException(
                "Name of class that implements " + interfaceName + " not set.");
    }
    /** Instantiates a class from it's (String) Classname */
    private Object instantiateClass(String className)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        if (className == null)
            return null;
        return Class.forName(className).newInstance();
    }
}