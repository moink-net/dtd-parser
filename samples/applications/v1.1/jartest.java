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
 * A Class to test loading files from a Jar (e.g.
 * XSLT Sheets, Properties files,mapfiles
 * Creation date: (18/06/01 13:05:40)
 * @author: Adam Flinton
 */
import de.tudarmstadt.ito.xmldbms.tools.*;
import de.tudarmstadt.ito.domutils.*;
import org.w3c.dom.Document;
public class jartest
{
    /**
     * jartest constructor comment.
     */
    public jartest()
    {
        super();
    }
    /**
     * Insert the method's description here.
     * Creation date: (18/06/01 13:06:11)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) throws java.lang.Exception
    {
        String fileref = new String();
        fileref = args[0];
        System.out.println("FileRef 1 = " + fileref);
        GetFileURL gfu = new GetFileURL();
        String fq = gfu.fullqual(fileref);
        System.out.println("FQ 1 = " + fq);
        jartest a = new jartest();
        System.out.println("The class is " + a.getClass().getName());
        //java.net.URL u2 = gfu.getFileJarURL(fq);
        //String jq = u2.toString();
        //System.out.println("U2 = 2 "+jq);
        ParserUtilsXerces x = new ParserUtilsXerces();
        Document Doc = x.openDocument(fq);
        String ret = x.returnString(Doc);
        System.out.println("Return = " + ret);
    }
}