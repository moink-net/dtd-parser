
/**
 * Title:        StockListenerMDB
 * Description:  This is the listener defined for Softwired's iBus example,
 *               converted to a MessageDrivenBean. In the Bluestone MDB example,
 *               any JMS implementation can be used.
 *
 *               The producer retains use of iBus specific classes, though this
 *               can be normalized to work with any JMS implementation by replacing
 *               the convience classes with standard JNDI lookups. The example is
 *               unchanged to illustrate the ease of moving from a standard JMS
 *               listener to a Message Driven Bean.
 *
 * Copyright:    Copyright (c) 2000
 * Company:      Bluestone
 * @author       Greg
 * @version 1.0
 */
package de.tudarmstadt.ito.xmldbms.jms;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;

public class MDB implements MessageDrivenBean
{

  public MDB()
  {
  }    


  public void onMessage(Message message) {
		// Pull out the quote from the message object.
		// We know that StockQuoteProducer sends TextMessages and
		// cast the object accordingly.
	try {

		
	  String Message = ((TextMessage)message).getText();
	  System.out.println("\nGot a Message: " + Message);
	
	// Put your logic here......	  
	  
	  
	  } catch(JMSException e) {
	  System.out.println("Error processing message: "+message);
	}
  }        

  public void ejbRemove() throws javax.ejb.EJBException
  {
	  System.out.println("\nMDB: ejbRemove called.");
  }      

  public void setMessageDrivenContext(MessageDrivenContext parm1) throws javax.ejb.EJBException
  {
	  System.out.println("\nMDB: setMessageDrivenContext called.");
  }      

  /** ejbCreate with no args required by spec, though not enforced by interface */
  public void ejbCreate()
  {
	  System.out.println("\nMDB: ejbCreate called.");
  }      

}