package de.tudarmstadt.ito.xmldbms.jms;

import javax.jms.*;


import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;import java.util.Properties;import javax.naming.*;public class JMSWrapper extends ProcessProperties implements javax.jms.MessageListener{

 		
	public static void main(String [] args) throws Exception
	{

		JMSWrapper sm = new JMSWrapper();
 		Properties props = new Properties();
	
		//get the properties file
		props = sm.getProperties(args,0);
		System.out.println("Props in sendMessage = " + props);
		//Get JMS specific settings
		try {
		sm.ic = (String)props.getProperty(XMLDBMSProps.JMSCONTEXT).trim();
		//System.out.println("JMS Initial Context = " + ic);	
		sm.prov_url = (String)props.getProperty(XMLDBMSProps.JMSPROVIDERURL).trim();
		//System.out.println("prov_url = " + prov_url);
		sm.JMSTopic = (String)props.getProperty(XMLDBMSProps.JMSTOPIC).trim();
		//System.out.println("JMSTopic = " + JMSTopic);
		//Get Acknowledgement Code (assume AUTO_ACKNOWLEDGE (i.e i = 1)
		} catch(Exception e) {
				System.out.println("One of JMSContext,JMSProviderURL or JMSTopic Not set");
			}
		if (props.getProperty(XMLDBMSProps.JMSACKMODE) != null)
		{ sm.setAkMode(props.getProperty(XMLDBMSProps.JMSACKMODE));		
		}
  
		if (sm.ic.equalsIgnoreCase(sm.SonicMQ))
		{sm.isSonic = true;}

		//Now set the base level JMS Info open a connection & setup the session & the topic
		
		sm.setJMS(sm.ic,sm.prov_url,sm.JMSTopic);
		

		// Next, create the TopicPublisher. This is the class that
		// assists with publishing events.
		TopicPublisher p = sm.session.createPublisher(sm.Topic1);		

		// There is a local counter to tag the postings
		int pushCounter = 0;

		// That's it, the TopicPublisher is now ready for use
		// Publish test messages now
		while(true)
		{
			// Pack a string into a message.  JMS insists on
			// having a StringBuffer, so do the conversion.
			TextMessage m=sm.session.createTextMessage();
			m.setText(props.getProperty("JMSMessage") + ++pushCounter  );
			try
			{
				// This simple call handles the rest.
				p.publish(m);
				System.out.println("Said Hello " + pushCounter + " times");
			} catch(JMSException e) {
				System.out.println("Error publishing quote# " + pushCounter);
			}
			//Thread.currentThread().sleep(1000);
			/*try
			{
				sm.connection.close();
			}
			catch (javax.jms.JMSException jmse)
			{
			jmse.printStackTrace();
			}
*/		}
			
	}
	private int ak = 1; 			private TopicConnection connection; 			private javax.naming.InitialContext context;			private String ic =null;	 		private boolean isSonic = false;			private String JMSTopic =null;			private String password =null;			private String prov_url =null; 			private TopicSession session; 			private String SonicMQ = "SONICMQ"; 			private TopicConnectionFactory tcf = null; 			private Topic Topic1;			private String user =null;
public JMSWrapper() {}	/** Cleanup resources and then exit. */
	private void close()
	{
		try
		{
			connection.close();
		}
		catch (javax.jms.JMSException jmse)
		{
			jmse.printStackTrace();
		}

//		System.exit(0);
	}
	
	/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */
public Topic getTopic(String Context, String prov_url, String Topic) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup("TopicConnectionFactory");
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection();
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);




	
	return Topic1;
}	/**
	 * Handle the message
	 * (as specified in the javax.jms.MessageListener interface).
	 */
	public void onMessage( javax.jms.Message aMessage)
	{
		try
		{
			System.out.println("FOUND A MESSAGE!!");
		   // Cast the message as a text message.
		   javax.jms.TextMessage textMessage = (javax.jms.TextMessage) aMessage;
		   // This handler reads a single String from the
		   // message and prints it to the standard output.
			try
			{
				String string = textMessage.getText();
				System.out.println( string );
			}
			catch (javax.jms.JMSException jmse)
			{
				jmse.printStackTrace();
			}
		}
		catch (java.lang.RuntimeException rte)
		{
			rte.printStackTrace();
		}            
	}// end of onMessage method
	


/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:02:44)
 * @param props java.util.Properties
 * @param message java.lang.String
 */

 public void send(Properties props, String message) throws javax.naming.NamingException,javax.jms.JMSException {


		//System.out.println("Props in sendMessage = " + props);
		//Get JMS specific settings
		try {
		ic = (String)props.getProperty(XMLDBMSProps.JMSCONTEXT).trim();
		//System.out.println("JMS Initial Context = " + ic);	
		prov_url = (String)props.getProperty(XMLDBMSProps.JMSPROVIDERURL).trim();
		//System.out.println("prov_url = " + prov_url);
		JMSTopic = (String)props.getProperty(XMLDBMSProps.JMSTOPIC).trim();
		//System.out.println("JMSTopic = " + JMSTopic);
			if (props.getProperty(XMLDBMSProps.JMSTCF) != null)
		{//System.out.println("Setting TCF = " +TCF);
			TCF = (String)props.getProperty(XMLDBMSProps.JMSTCF).trim();
			//System.out.println("Set TCF = " +TCF);
			}
		
		} catch(Exception e) {
				System.out.println("One of JMSContext,JMSProviderURL or JMSTopic Not set");
			}
		if (props.getProperty(XMLDBMSProps.JMSACKMODE) != null)
		{ setAkMode(props.getProperty(XMLDBMSProps.JMSACKMODE));		
		}
  
		if (ic.equalsIgnoreCase(SonicMQ))
		{isSonic = true;}

		//Now set the base level JMS Info open a connection & setup the session & the topic




		if (props.getProperty(XMLDBMSProps.JMSUSER) != null && props.getProperty(XMLDBMSProps.JMSPASSWORD) != null)
		{
		user = props.getProperty(XMLDBMSProps.JMSUSER);
		password = props.getProperty(XMLDBMSProps.JMSPASSWORD) ;

				setJMS(ic,prov_url,JMSTopic,TCF,user,password);
		
		}
		else {	setJMS(ic,prov_url,JMSTopic,TCF); }
		

		// Next, create the TopicPublisher. This is the class that
		// assists with publishing events.
		TopicPublisher p = session.createPublisher(Topic1);


		TextMessage m=session.createTextMessage();
		m.setText(message);
			try
			{
				// This simple call handles the rest.
				p.publish(m);
				System.out.println("Published " + message );
			} catch(JMSException e) {
				System.out.println("Error publishing Message " + message);
				//Thread.currentThread().sleep(1000);
			}
					
			try
			{
				connection.close();
			}
			catch (javax.jms.JMSException jmse)
			{
			jmse.printStackTrace();
			}
			
		
	
	
	}

/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:23:06)
 * @return int
 * @param Ack java.lang.String
 */

 public int setAkMode(String Ack) {
		//Get Acknowledgement Code (assume AUTO_ACKNOWLEDGE (i.e i = 1)
		
		if (Ack.trim().equalsIgnoreCase("CLIENT_ACKNOWLEDGE"))
		{ak = 2;}
		else if (Ack.trim().equalsIgnoreCase("DUPS_OK_ACKNOWLEDGE"))
		{ak = 3;}
		else {ak = ak;}
	return ak;
}

/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */

 public void setJMS(String Context, String prov_url, String Topic) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup("TopicConnectionFactory");
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection();
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);
}

	private String TCF ="TopicConnectionFactory";	/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */
public Topic getTopic(String Context, String prov_url, String Topic, String TCF) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup(TCF);
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection();
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);




	
	return Topic1;
}	/**
	 * Handle the message
	 * (as specified in the javax.jms.MessageListener interface).
	 */	/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */
public Topic getTopic(String Context, String prov_url, String Topic, String TCF, String User, String Password) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup(TCF);
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection(User, Password);
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);




	
	return Topic1;
}	/**
	 * Handle the message
	 * (as specified in the javax.jms.MessageListener interface).
	 */	/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:02:44)
 * @param props java.util.Properties
 * @param message java.lang.String
 */
public String receive(Properties props) throws javax.naming.NamingException,javax.jms.JMSException {


	String msg = null;
	
		//System.out.println("Props in sendMessage = " + props);
		//Get JMS specific settings
		try {
		ic = (String)props.getProperty(XMLDBMSProps.JMSCONTEXT).trim();
		//System.out.println("JMS Initial Context = " + ic);	
		prov_url = (String)props.getProperty(XMLDBMSProps.JMSPROVIDERURL).trim();
		//System.out.println("prov_url = " + prov_url);
		JMSTopic = (String)props.getProperty(XMLDBMSProps.JMSTOPIC).trim();
		//System.out.println("JMSTopic = " + JMSTopic);
				if (props.getProperty(XMLDBMSProps.JMSTCF) != null)
		{TCF = (String)props.getProperty(XMLDBMSProps.JMSTCF).trim();}
		
		} catch(Exception e) {
				System.out.println("One of JMSContext,JMSProviderURL or JMSTopic Not set");
			}
		if (props.getProperty(XMLDBMSProps.JMSACKMODE) != null)
		{ setAkMode(props.getProperty(XMLDBMSProps.JMSACKMODE));		
		}
  
		if (ic.equalsIgnoreCase(SonicMQ))
		{isSonic = true;}

		//Now set the base level JMS Info open a connection & setup the session & the topic

		if (props.getProperty(XMLDBMSProps.JMSUSER) != null && props.getProperty(XMLDBMSProps.JMSPASSWORD) != null)
		{
		user = props.getProperty(XMLDBMSProps.JMSUSER);
		password = props.getProperty(XMLDBMSProps.JMSPASSWORD) ;

		setJMS(ic,prov_url,JMSTopic,TCF,user,password);
		
		}
		else {	setJMS(ic,prov_url,JMSTopic,TCF); }
		

		
try
		{
			javax.jms.TopicSubscriber subscriber = session.createSubscriber(Topic1);
			System.out.println("Subscriber = " + subscriber);
		//	subscriber.setMessageListener(this);
			// Now that setup is complete, start the Connection
			connection.start();
		System.out.println ("Connection Started.Ready to listen for messages");

	  TextMessage textmsg2 = (TextMessage)subscriber.receive();
	  
	  msg = textmsg2.getText();
	 // System.out.println("Received : " + textmsg2.getText() );




	   
		}
		catch (javax.jms.JMSException jmse)
		{
			jmse.printStackTrace();
		}

			try
			{
				connection.close();
			}
			catch (javax.jms.JMSException jmse)
			{
			jmse.printStackTrace();
			}
			
		
return msg;	
	
	}	/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:02:44)
 * @param props java.util.Properties
 * @param message java.lang.String
 */
public void receiveTest(Properties props) throws javax.naming.NamingException,javax.jms.JMSException {



	
		//System.out.println("Props in sendMessage = " + props);
		//Get JMS specific settings
		try {
		ic = (String)props.getProperty(XMLDBMSProps.JMSCONTEXT).trim();
		//System.out.println("JMS Initial Context = " + ic);	
		prov_url = (String)props.getProperty(XMLDBMSProps.JMSPROVIDERURL).trim();
		//System.out.println("prov_url = " + prov_url);
		JMSTopic = (String)props.getProperty(XMLDBMSProps.JMSTOPIC).trim();
		//System.out.println("JMSTopic = " + JMSTopic);
		if (props.getProperty(XMLDBMSProps.JMSTCF) != null)
		{TCF = (String)props.getProperty(XMLDBMSProps.JMSTCF).trim();}
		
		} catch(Exception e) {
				System.out.println("One of JMSContext,JMSProviderURL or JMSTopic Not set");
			}
		if (props.getProperty(XMLDBMSProps.JMSACKMODE) != null)
		{ setAkMode(props.getProperty(XMLDBMSProps.JMSACKMODE));		
		}
  
		if (ic.equalsIgnoreCase(SonicMQ))
		{isSonic = true;}

		//Now set the base level JMS Info open a connection & setup the session & the topic
	
		if (props.getProperty(XMLDBMSProps.JMSUSER) != null && props.getProperty(XMLDBMSProps.JMSPASSWORD) != null)
		{
		user = props.getProperty(XMLDBMSProps.JMSUSER);
		password = props.getProperty(XMLDBMSProps.JMSPASSWORD) ;

				setJMS(ic,prov_url,JMSTopic,TCF,user,password);
		
		}
		else {	setJMS(ic,prov_url,JMSTopic,TCF); }			

		
try
		{
			javax.jms.TopicSubscriber subscriber = session.createSubscriber(Topic1);
			System.out.println("Subscriber = " + subscriber);
		//	subscriber.setMessageListener(this);
			// Now that setup is complete, start the Connection
			connection.start();
		System.out.println ("Connection Started.Ready to listen for messages");
	while (true)
	{
	  TextMessage textmsg2 = (TextMessage)subscriber.receive();
	  
	  System.out.println( textmsg2.getText() );
	}



	   
		}
		catch (javax.jms.JMSException jmse)
		{
			jmse.printStackTrace();
		}

			try
			{
				connection.close();
			}
			catch (javax.jms.JMSException jmse)
			{
			jmse.printStackTrace();
			}
			
	
	}/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */

 public void setJMS(String Context, String prov_url, String Topic,String TCF) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup(TCF.trim());
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection();
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);
}/**
 * Insert the method's description here.
 * Creation date: (16/04/01 20:32:04)
 * @return javax.jms.Topic
 * @param Context java.lang.String
 * @param Prov_URL java.lang.String
 * @param Topic java.lang.String
 */

 public void setJMS(String Context, String prov_url, String Topic,String TCF,String User, String Password) throws javax.naming.NamingException,javax.jms.JMSException {


if (!isSonic)
		{
			
			java.util.Hashtable env1 = new java.util.Hashtable();
			env1.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, Context);			
			env1.put(javax.naming.Context.PROVIDER_URL, prov_url);
			//System.out.println("ENV = " +env1);
		 	context = new javax.naming.InitialContext(env1);	
			tcf = (TopicConnectionFactory)context.lookup(TCF.trim());
		}

		else
		{
			//For SonicMQ
			tcf = new progress.message.jclient.TopicConnectionFactory (prov_url);
		}

		// First, create the TopicSession, which is used to create
		// both publishers and their message objects.
		// We choose a non-transacted session with automatic
		// message acknowledgment for simplicity.  See the programmer's
		// manual for more details on these parameters.

		connection = tcf.createTopicConnection(User, Password);
		//TopicSession session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		session = connection.createTopicSession(false, ak);
		//System.out.println("Ack int = " + ak);

		// Next convert the topic name into the corresponding
		// object, encapsulating the "radio station" that the
		// stock quote consumer can "tune in" to.
		Topic1 = session.createTopic(Topic);
}/**
 * Insert the method's description here.
 * Creation date: (16/04/01 17:15:34)
 */
}