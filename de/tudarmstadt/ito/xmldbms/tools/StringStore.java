package de.tudarmstadt.ito.xmldbms.tools;

/**
 * Insert the type's description here.
 * Creation date: (01/05/01 12:47:38)
 * @author: Adam Flinton
 */

import java.io.*;
 
public class StringStore {
/**
 * StringStore constructor comment.
 */
public StringStore() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (01/05/01 12:48:13)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	
	      String s = "hello world from XMLDBMS";
	  store("c:\\samplefile.txt",s);
	  
	  
	  }  
/**
 * Insert the method's description here.
 * Creation date: (01/05/01 12:48:55)
 */
public static void store(String sfilepath,String sfileContent) {
	
	String scontent = sfileContent;
	  String spath = sfilepath;
	  char[] buf = new char[scontent.length()];
	  scontent.getChars(0,scontent.length(),buf,0);
	  try
	  {
	  	// if append to be done just add true to the consructor say 
	  	// FileWriter f1 = new FileWriter(spath,true);
	  	
		FileWriter f1 = new FileWriter(spath);
		f1.write(buf);
		f1.close();
	  }
	  catch(Exception e)
	  {
	 	System.out.println("String Writing Error Occured Filepath = "+spath +"Error = " +e); 
	  } 
	}


	  
}
