
/**
 * A Class to test XSLT Scripts.
 * Creation date: (08/05/01 14:45:51)
 * @author: Adam Flinton
 */


package de.tudarmstadt.ito.xmldbms.xslt;

import de.tudarmstadt.ito.xmldbms.tools.StringStore;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import java.util.Properties;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;public class XSLTLoader extends ProcessProperties {

/**
 * XSLTest constructor comment.
 */
public XSLTLoader() {
	super();
}

	private ProcessXslt processxslt;	private static String PROCESSXSLT = "ProcessXslt";   private void checkState(String interfaceName, Object interfaceObject)
   {
	 if (interfaceObject == null)
	   throw new IllegalStateException("Name of class that implements " + interfaceName + " not set.");
   }                     private Object instantiateClass(String className)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 if (className == null) return null;
	 return Class.forName(className).newInstance();
   }            /**
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

   public void setXSLTProperties(Properties props)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 processxslt = (ProcessXslt)instantiateClass((String)props.getProperty(XMLDBMSProps.XSLTCLASS));
   }                     
   
   
   /**
 * Insert the method's description here.
 * Creation date: (10/05/01 15:11:40)
 * @return de.tudarmstadt.ito.xmldbms.xslt.ProcessXslt
 * @param props java.util.Properties
 */
public ProcessXslt init(Properties props) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

	setXSLTProperties(props);
	checkState(PROCESSXSLT, processxslt);
	
	return processxslt;
}}