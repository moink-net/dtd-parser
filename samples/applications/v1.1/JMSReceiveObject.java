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
/**
 * Receives an Object via JMS (as opposed to a text message)
 * .
 * Creation date: (16/04/01 17:11:25)
 * @author: Adam Flinton
 * This is a simple test receive for Objects rather than text / string
 */
import javax.jms.*;
import javax.naming.*;
import de.tudarmstadt.ito.xmldbms.jms.JMSWrapper;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import java.util.Properties;
public class JMSReceiveObject extends ProcessProperties
{
    /**
     * JMSTest constructor comment.
     */
    public JMSReceiveObject()
    {
        super();
    }
    /**
     * Sets up a JMS Connection and calls receive.
     * Creation date: (16/04/01 17:13:37)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        JMSReceiveObject jt = new JMSReceiveObject();
        //get the properties file
        props = jt.getProperties(args, 0);
        JMSWrapper sm = new JMSWrapper();
        sm.init(props);
        sm.receiveTestObj();
    }
}