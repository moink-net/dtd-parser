// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;
import java.util.Hashtable;
import javax.naming.*;
import java.io.*;

/**
 * Implements GetFile for JNDI
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class GetFileJNDI implements GetFile
{
    /**
     * Construct a new GetFileJNDI object.
     */
    public GetFileJNDI()
    {
        super();
    }
    /**
     * Get a file as an InputStream.
     *
     * @param args args[0] is the filename and args[1] is the name
     *             of the initial context.
     * @return The InputStream
     */
    public java.io.InputStream getFile(String[] args)
        throws GetFileException, java.io.FileNotFoundException
    {
        Hashtable env = new Hashtable();
        String InitContext, ic, Filename;
        Context ctx;
        InputStream fins = null;
        ic = args[1];
        Filename = args[0];
        InitContext = ic.trim();
        try
            {
            // Create the initial context
            if (InitContext != null)
                {
                env.put(Context.INITIAL_CONTEXT_FACTORY, InitContext);
                System.out.println(" InitC =  " + InitContext);
                ctx = new InitialContext(env);
            }
            else
                {
                ctx = new InitialContext();
            }
            File f = (File) ctx.lookup(Filename);
            fins = new FileInputStream(f);
            // Close the context when we're done
            ctx.close();
        }
        catch (NamingException e)
            {
            System.err.println("Problem looking up " + Filename + ": " + e);
        }
        return fins;
    }
}