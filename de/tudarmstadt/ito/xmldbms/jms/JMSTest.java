package de.tudarmstadt.ito.xmldbms.jms;

/**
 * Insert the type's description here.
 * Creation date: (16/04/01 17:11:25)
 * @author: Adam Flinton
 */

 import java.io.*;
import javax.jms.*;
import javax.naming.*;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

import java.util.Properties;
 
public class JMSTest extends ProcessProperties{

	
/**
 * JMSTest constructor comment.
 */
public JMSTest() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:13:37)
 * @param args java.lang.String[]
 */
public static void main(String[] args) throws Exception {

		GetFileURL gfu = new GetFileURL();
		Properties props = new Properties();
		JMSTest jt = new JMSTest();
		//get the properties file
		props = jt.getProperties(args,0);

		String s = "You forgot to give me a message file..never mind";

		if(props.getProperty(XMLDBMSProps.JMSMESSAGE) != null)
		{
		s = props.getProperty(XMLDBMSProps.JMSMESSAGE);
		}

		int i = 10000000;

		if (props.getProperty(XMLDBMSProps.JMSTESTNUM) != null)
			{i = Integer.parseInt(props.getProperty(XMLDBMSProps.JMSTESTNUM));}

			String[] S = new String[1];
			S[0] = s;
		
		InputStream is = gfu.getFile(S);
		
		 //String s = (String)is;
		 
		 BufferedReader br = new BufferedReader(new InputStreamReader(is));
		 String files = "";
		 StringBuffer sb = new StringBuffer();
		 while((files = br.readLine()) != null)
		 {
		 	
		 	sb.append(files);
		 	
		 }
		 // print the contents of xml file in String 
		 
		 //System.out.println("the content is"+sb.toString());
		String Message = sb.toString();
		JMSWrapper sm = new JMSWrapper();
		sm.init(props);
	//	sm.send(props, Message);
		System.out.println("Sending " + i +" Messages");
		int pushCounter = 0;

		// That's it, the TopicPublisher is now ready for use
		// Publish stock quotes now
		while(pushCounter < i)
		{
			//String Message2 = Message + ++pushCounter;
			sm.send(Message);
			pushCounter++;
			
		}


		
	}
}
