package samples;
/**No copyright, no warranty; use as you will.
* Written by Adam Flinton and Ronald Bourret, 2001
* Version 1.1
* Changes from version 1.01: New in 1.1
* No copyright, no warranty; use as you will.
* Written by Adam Flinton and Ronald Bourret, 2001
* Version 1.1
* Changes from version 1.01: New in 1.1
*/
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
/** This is a default (empty) example Message Driven Bean
 */
public class MDB implements MessageDrivenBean
{
    public MDB()
    {
    }
    public void onMessage(Message message)
    {
        try
            {
            String Message = ((TextMessage) message).getText();
            System.out.println("\nGot a Message: " + Message);
            // Put your logic here......	  
        }
        catch (JMSException e)
            {
            System.out.println("Error processing message: " + message);
        }
    }
    public void ejbRemove() throws javax.ejb.EJBException
    {
        System.out.println("\nMDB: ejbRemove called.");
    }
    public void setMessageDrivenContext(MessageDrivenContext parm1)
        throws javax.ejb.EJBException
    {
        System.out.println("\nMDB: setMessageDrivenContext called.");
    }
    /** ejbCreate with no args required by spec, though not enforced by interface */
    public void ejbCreate()
    {
        System.out.println("\nMDB: ejbCreate called.");
    }
}