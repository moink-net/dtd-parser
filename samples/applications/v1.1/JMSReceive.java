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
 * Receives JMS Messages.
 * Creation date: (16/04/01 17:11:25)
 * @author: Adam Flinton
 */
import javax.jms.*;
import javax.naming.*;
import de.tudarmstadt.ito.xmldbms.jms.JMSWrapper;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import java.util.Properties;
public class JMSReceive extends ProcessProperties
{
    /**
     * JMSTest constructor comment.
     */
    public JMSReceive()
    {
        super();
    }
    /**
     * Sets up a JMS Connection & then calls receive on it..
     * Creation date: (16/04/01 17:13:37)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        JMSReceive jt = new JMSReceive();
        //get the properties file
        props = jt.getProperties(args, 0);
        JMSWrapper sm = new JMSWrapper();
        sm.init(props);
        sm.receiveTest();
    }
}