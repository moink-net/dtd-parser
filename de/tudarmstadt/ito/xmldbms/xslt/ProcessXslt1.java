package de.tudarmstadt.ito.xmldbms.xslt;

/**
 * XSLT Wrapper. Use the Object cache to store compiled Stylesheets.
 * @author: Adam Flinton
 */
import org.apache.xalan.xslt.*;

import de.tudarmstadt.ito.xmldbms.objectcache.ObjectCache;


import java.util.HashMap;import java.io.*;import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;import org.xml.sax.InputSource;public class ProcessXslt1 implements ProcessXslt {

private static ObjectCache oc = new ObjectCache();

	
/**
 * This does nothing.
 */
public ProcessXslt1() {
	super();
}

/**
 * This returns the compiled stylesheet for the given name.
 * Stylesheets are compiled only once and then cached for the duration of the application.
*/
	private static StylesheetRoot getSheet(String fileName)
		throws org.xml.sax.SAXException, Exception
	{
		HashMap h = oc.getMap();
		StylesheetRoot s = (StylesheetRoot)h.get(fileName);
		if(s == null)
		{
			synchronized(ProcessXslt1.class) // make thread safe
			{
				s = (StylesheetRoot)h.get(fileName);
				if(s == null) // may have changed between first if and synch call...
				{
					// Compile the stylesheet.
					GetFileURL gfu = new GetFileURL();


					String full = gfu.fullqual(fileName);
					//System.out.println("XMLFILE fullqual= " +full);
					//InputSource src = new InputSource(gfu.getFileURL(xmlFilename));
					InputSource Isrc = new InputSource(full);
					String SrcURL = Isrc.getSystemId();
					//System.out.println("XMLFILE SysID= " +SrcURL);		
					XSLTProcessor p = XSLTProcessorFactory.getProcessor();
					XSLTInputSource src = new XSLTInputSource(SrcURL);	
					s = p.processStylesheet(src);
					oc.put(fileName, s);
				}
			}
		}
		return s; // return the cached copy.
	}
/**
 * This processes the input using the stylesheet in file filename.
 * The transformed string is returned.
*/
	public String transformStream(java.io.InputStream srcStream, String xslfileName)
		throws 	org.xml.sax.SAXException, java.io.FileNotFoundException, 
				Exception, java.net.MalformedURLException
	{

		GetFileURL gfu = new GetFileURL();
		String Result = "";

		String fullfn = gfu.fullqual(xslfileName);
		InputSource src1 = new InputSource(fullfn);
		String SrcURL1 = src1.getSystemId();

		
		StylesheetRoot s = getSheet(SrcURL1);
		XSLTInputSource i = new XSLTInputSource(srcStream);
		java.io.StringWriter sw = new java.io.StringWriter();
		XSLTResultTarget t = new XSLTResultTarget(sw);
		s.process(i, t);
		return sw.toString();
	}
/**
 * This processes the input using the stylesheet in file filename.
 * The transformed string is returned.
*/
	 public String transformString(String inputxml, String xslfileName)
		throws 	org.xml.sax.SAXException, java.io.FileNotFoundException, 
				Exception, java.net.MalformedURLException
	{
		GetFileURL gfu = new GetFileURL();
	//	System.out.println("XSLFIlename = " +xslfileName);
		String fullfn = gfu.fullqual(xslfileName);
		InputSource src1 = new InputSource(fullfn);
		String SrcURL1 = src1.getSystemId();
		
		StylesheetRoot s = getSheet(SrcURL1);

		byte[] buf = inputxml.getBytes();
		InputStream is = new ByteArrayInputStream(buf);
		
		XSLTInputSource i = new XSLTInputSource(is);
		java.io.StringWriter sw = new java.io.StringWriter();
		XSLTResultTarget t = new XSLTResultTarget(sw);
		s.process(i, t);
		return sw.toString();
	}
	/**
 * This processes the input using the stylesheet in file filename.
 * The transformed string is returned.
*/
	public String transformFiles(String xmlfileName, String xslfileName)
		throws 	org.xml.sax.SAXException, java.io.FileNotFoundException, 
				Exception, java.net.MalformedURLException
	{
		GetFileURL gfu = new GetFileURL();
		String fullfn = gfu.fullqual(xslfileName);
		InputSource src1 = new InputSource(fullfn);
		String SrcURL1 = src1.getSystemId();
		StylesheetRoot s = getSheet(SrcURL1);

		String full = gfu.fullqual(xmlfileName);
		System.out.println("XMLFILE fullqual= " +full);
	//InputSource src = new InputSource(gfu.getFileURL(xmlFilename));
		InputSource src = new InputSource(full);
		String SrcURL = src.getSystemId();
		System.out.println("XMLFILE SysID= " +SrcURL);
			
		XSLTInputSource i = new XSLTInputSource(SrcURL);
		java.io.StringWriter sw = new java.io.StringWriter();
		XSLTResultTarget t = new XSLTResultTarget(sw);
		s.process(i, t);
		return sw.toString();
	}}