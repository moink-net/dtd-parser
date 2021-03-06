// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.xslt;

/**
 * Implementation of the ProcessXslt interface for the 
 * Xalan 2 XSLT processor.
 *
 * @author Adam Flinton
 * @version 1.1
 */
// Imported TraX classes
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.*;
// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
// Imported java.io classes
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.util.*;
import de.tudarmstadt.ito.xmldbms.objectcache.ObjectCache;
import java.util.Hashtable;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
public class ProcessXalan2 implements ProcessXslt
{
    private static ObjectCache oc = new ObjectCache();
    /**
     * Create a new ProcessXalan1 object.
     */
    public ProcessXalan2()
    {
        super();
    }
    /**
     * A Method which returns the template
     */
    private static Templates getSheet(String fileName)
        throws
            TransformerException,
            TransformerConfigurationException,
            org.xml.sax.SAXException,
            java.io.FileNotFoundException,
            Exception
    {
        Hashtable h = oc.getMap();
        Templates s = (Templates) h.get(fileName);
        //System.out.println("2");
        if (s == null)
            {
            //System.out.println("3");
            synchronized (ProcessXalan2.class) // make thread safe
            
            {
                s = (Templates) h.get(fileName);
                if (s == null) // make have changed between first if and synch call...
                    {
                    //System.out.println("4");
                    // Compile the stylesheet.
                    GetFileURL gfu = new GetFileURL();
                    String full = gfu.fullqual(fileName);
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    s = tFactory.newTemplates(new StreamSource(full));
                    oc.put(fileName, s);
                }
            }
        }
        return s; // return the cached copy.
    }
    /**
     * Transforms an XML document stored in a file.
     *
     * @param xmlfileName The name of the file
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    public String transformFiles(String xmlfileName, String xslfileName)
        throws Exception
    {
        GetFileURL gfu = new GetFileURL();
        String Result = "";
        String fullfn = gfu.fullqual(xslfileName);
        InputSource src1 = new InputSource(fullfn);
        String SrcURL1 = src1.getSystemId();
        Templates s = getSheet(SrcURL1);
        String full = gfu.fullqual(xmlfileName);
        //System.out.println("XMLFILE fullqual= " +full);
        InputSource src = new InputSource(full);
        String SrcURL = src.getSystemId();
        //	System.out.println("XMLFILE SysID= " +SrcURL);
        java.io.StringWriter sw = new java.io.StringWriter();
        Transformer trans = s.newTransformer();
        trans.transform(new StreamSource(SrcURL), new StreamResult(sw));
        Result = sw.toString();
        return Result;
    }
    /**
     * Transforms an XML document stored in an InputStream.
     *
     * @param srcStream The InputStream
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    public String transformStream(
        java.io.InputStream srcStream,
        String xslfileName)
        throws Exception
    {
        GetFileURL gfu = new GetFileURL();
        String Result = "";
        String fullfn = gfu.fullqual(xslfileName);
        InputSource src1 = new InputSource(fullfn);
        String SrcURL1 = src1.getSystemId();
        Templates s = getSheet(SrcURL1);
        java.io.StringWriter sw = new java.io.StringWriter();
        Transformer trans = s.newTransformer();
        trans.transform(new StreamSource(srcStream), new StreamResult(sw));
        Result = sw.toString();
        return Result;
    }
    /**
     * Transforms an XML document stored in a string.
     *
     * @param inputxml The string
     *
     * @return The output of the transformation
     */
    public static String transformString(String inputxml)
        throws TransformerException, TransformerConfigurationException, Exception
    {
        String media = null, title = null, charset = null;
        String result = "";
        GetFileURL gfu = new GetFileURL();
        String full = gfu.fullqual(inputxml);
        //System.out.println("XMLFILE fullqual= " +full);
        //InputSource src = new InputSource(gfu.getFileURL(xmlFilename));
        InputSource src = new InputSource(full);
        String SrcURL = src.getSystemId();
        //	System.out.println("XMLFILE SysID= " +SrcURL);
        try
            {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Source stylesheet =
                tFactory.getAssociatedStylesheet(
                    new StreamSource(SrcURL),
                    media,
                    title,
                    charset);
            Transformer transformer = tFactory.newTransformer(stylesheet);
            java.io.StringWriter sw = new java.io.StringWriter();
            transformer.transform(new StreamSource(SrcURL), new StreamResult(sw));
            //System.out.println("3");
            result = sw.toString();
            //		System.out.println("************* The result is in foo.out *************");
            //System.out.println("The result string is " + result);
        }
        catch (Exception e)
            {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Transforms an XML document stored in a string.
     *
     * @param inputxml The string
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    public String transformString(String inputxml, String xslfileName)
        throws Exception
    {
        GetFileURL gfu = new GetFileURL();
        String Result = "";
        String fullfn = gfu.fullqual(xslfileName);
        InputSource src1 = new InputSource(fullfn);
        String SrcURL1 = src1.getSystemId();
        Templates s = getSheet(SrcURL1);
        byte[] buf = inputxml.getBytes();
        InputStream is = new ByteArrayInputStream(buf);
        java.io.StringWriter sw = new java.io.StringWriter();
        Transformer trans = s.newTransformer();
        trans.transform(new StreamSource(is), new StreamResult(sw));
        Result = sw.toString();
        return Result;
    }
}