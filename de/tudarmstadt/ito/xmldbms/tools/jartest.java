package de.tudarmstadt.ito.xmldbms.tools;

/**
 * Insert the type's description here.
 * Creation date: (18/06/01 13:05:40)
 * @author: Adam Flinton
 */

import de.tudarmstadt.ito.domutils.*;
import org.w3c.dom.Document;
 
public class jartest {
/**
 * jartest constructor comment.
 */
public jartest() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (18/06/01 13:06:11)
 * @param args java.lang.String[]
 */
public static void main(String[] args) throws java.lang.Exception{
	
	String fileref = new String();

	fileref = args[0];
	System.out.println("FileRef 1 = " +fileref);
	GetFileURL gfu = new GetFileURL();
	String fq = gfu.fullqual(fileref);
	System.out.println("FQ 1 = " +fq);
	jartest a = new jartest();
	System.out.println("The class is " + a.getClass().getName());
	 
	
	//java.net.URL u2 = gfu.getFileJarURL(fq);
	//String jq = u2.toString();
	//System.out.println("U2 = 2 "+jq);

	
	

	
	ParserUtilsXerces x = new ParserUtilsXerces();
	Document Doc = x.openDocument(fq);
	String ret = x.returnString(Doc);
	System.out.println("Return = " +ret); 
	
	}
}
