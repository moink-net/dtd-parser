// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.jms;

/**
 * Properties constants for JMSWrapper.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class JMSProps
{
    /** The JMS acknowledgement mode. */
    public static String JMSACKMODE = "JMSAckMode";

    /** The JMS context. */
    public static String JMSCONTEXT = "JMSContext";

    /** A sample JMS message. */
    public static String JMSMESSAGE = "JMSMessage";

    /** JMS password. Required if your JMS server is set up to use
        user names and passwords. */
    public static String JMSPASSWORD = "JMSPassword";

    /** The provider URL of your JMS server. */
    public static String JMSPROVIDERURL = "JMSProviderURL";

    /** Whether to print debugging messages. Yes or No. */
    public static String JMSSILENT = "JMSSilent";

    /** The name of the JMS TopicConnectionFactory to use. */
    public static String JMSTCF = "JMSTCF";

    /** The JMS Topic to use. */
    public static String JMSTOPIC = "JMSTopic";

    /** JMS user name. Needed if your JMS server is set up to use
        user names and passwords. */
    public static String JMSUSER = "JMSUser";
}