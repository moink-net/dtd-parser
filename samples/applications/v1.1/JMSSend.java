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
 * A Class to use to send JMS Messages.
 * Creation date: (16/04/01 17:11:25)
 * @author: Adam Flinton
 */
import java.io.*;
import javax.jms.*;
import javax.naming.*;
import de.tudarmstadt.ito.xmldbms.jms.JMSWrapper;
import de.tudarmstadt.ito.xmldbms.jms.JMSProps;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
import java.util.Properties;
public class JMSSend extends ProcessProperties
{
    /**
     * JMSTest constructor comment.
     */
    public JMSSend()
    {
        super();
    }
    /**
     * Takes in a set of args & uses it to build a properties file.
     * It uses setting in the properties file (the JMSMESSAGE setting)
     * to then load up the message and then send it.
     * You can set the number of times you want the message sending
     * via the JMSTESTNUM setting.
     * Creation date: (16/04/01 17:13:37)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) throws Exception
    {
        GetFileURL gfu = new GetFileURL();
        Properties props = new Properties();
        JMSSend jt = new JMSSend();
        //get the properties file
        props = jt.getProperties(args, 0);
        String s = "You forgot to give me a message file..never mind";
        if (props.getProperty(JMSProps.JMSMESSAGE) != null)
            {
            s = props.getProperty(JMSProps.JMSMESSAGE);
        }
        int i = 1;
        if (props.getProperty(JMSProps.JMSTESTNUM) != null)
            {
            i = Integer.parseInt(props.getProperty(JMSProps.JMSTESTNUM));
        }
        String[] S = new String[1];
        S[0] = s;
        InputStream is = gfu.getFile(S);
        //String s = (String)is;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String files = "";
        StringBuffer sb = new StringBuffer();
        while ((files = br.readLine()) != null)
            {
            sb.append(files);
        }
        // print the contents of xml file in String 
        //System.out.println("the content is"+sb.toString());
        String Message = sb.toString();
        JMSWrapper sm = new JMSWrapper();
        sm.init(props);
        //	sm.send(props, Message);
        System.out.println("Sending " + i + " Messages");
        int pushCounter = 0;
        // That's it, the TopicPublisher is now ready for use
        // Publish stock quotes now
        while (pushCounter < i)
            {
            //String Message2 = Message + ++pushCounter;
            sm.send(Message);
            pushCounter++;
        }
    }
}