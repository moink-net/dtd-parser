package de.tudarmstadt.ito.xmldbms.tools;

/**
 * Insert the type's description here.
 * Creation date: (18/04/01 14:40:04)
 * @author: Adam Flinton
 */

import java.util.Hashtable;
import javax.naming.*;
import java.io.*;
 
public class GetFileJNDI implements GetFile {
/**
 * GetFileJNDI constructor comment.
 */
public GetFileJNDI() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (18/04/01 14:40:04)
 * @return java.io.InputStream
 * @param filename java.lang.String
 */
public java.io.InputStream getFile(String[] args) throws GetFileException,java.io.FileNotFoundException {
	
	Hashtable env = new Hashtable();
	String      InitContext,ic, Filename;
	Context ctx;
	InputStream fins = null;

	ic = args[1];
	Filename =  args[0];
	InitContext = ic.trim();
	try {

	    // Create the initial context

	 
	    if(InitContext != null)      
		    {	env.put(Context.INITIAL_CONTEXT_FACTORY,InitContext);
			    System.out.println(" InitC =  "+InitContext);
			    ctx = new InitialContext(env);}
		    else {ctx = new InitialContext();}



		 File f = (File)ctx.lookup(Filename);
		
		fins = new FileInputStream(f);
		
	    
	    // Close the context when we're done
	    ctx.close();
	} catch (NamingException e) {
	    System.err.println("Problem looking up " + Filename + ": " + e);
	}
return fins;	
}
}
