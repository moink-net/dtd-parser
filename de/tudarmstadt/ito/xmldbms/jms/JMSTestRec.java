package de.tudarmstadt.ito.xmldbms.jms;

/**
 * Insert the type's description here.
 * Creation date: (16/04/01 17:11:25)
 * @author: Adam Flinton
 */

import javax.jms.*;
import javax.naming.*;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;

import java.util.Properties;
 
public class JMSTestRec extends ProcessProperties{

	
/**
 * JMSTest constructor comment.
 */
public JMSTestRec() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:13:37)
 * @param args java.lang.String[]
 */
public static void main(String[] args) throws Exception {
	
		Properties props = new Properties();
		JMSTest jt = new JMSTest();
		//get the properties file
		props = jt.getProperties(args,0);
		JMSWrapper sm = new JMSWrapper();
		sm.init(props);
		sm.receiveTest();
		
	}
}
