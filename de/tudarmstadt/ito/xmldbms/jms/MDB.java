

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