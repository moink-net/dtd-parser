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
 * A Class to test XSLT Scripts.
 * Creation date: (08/05/01 14:45:51)
 * @author: Adam Flinton
 */
package samples;
import de.tudarmstadt.ito.xmldbms.tools.StringStore;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import java.util.Properties;
import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.xslt.*;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
public class XSLTest extends ProcessProperties
{
    /**
     * XSLTest constructor comment.
     */
    public XSLTest()
    {
        super();
    }
    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args)
    {
        try
            {
            Properties props;
            GetFileURL gfu = new GetFileURL();
            XSLTest x = new XSLTest();
            props = x.getProperties(args, 0);
            XSLTLoader a = new XSLTLoader();
            x.processxslt = a.init(props);
            //	x.setXSLTProperties(props);
            //	x.checkState(PROCESSXSLT, x.processxslt);
            String sheet, xmlfile, outputfile;
            sheet = props.getProperty(XSLTProps.XSLTSCRIPT);
            xmlfile = props.getProperty(XMLDBMSProps.XMLFILE);
            outputfile = props.getProperty(XSLTProps.XSLTOUTPUT);
            xmlfile = gfu.fullqual(xmlfile);
            sheet = gfu.fullqual(sheet);
            //sheet = args[0];
            //xmlfile = args[1];
            //outputfile = args[2];
            //		System.out.println("Sheet = " +sheet);
            //		System.out.println("XMLFile = " +xmlfile);
            String output = x.processxslt.transformFiles(xmlfile, sheet);
            System.out.println(output);
            if (outputfile != null)
                {
                StringStore st = new StringStore();
                st.store(outputfile, output);
            }
        }
        catch (Exception e)
            {
            System.out.println("Problem running demo: " + e.toString());
        }
    }
    private ProcessXslt processxslt;
    private static String PROCESSXSLT = "ProcessXslt";
}