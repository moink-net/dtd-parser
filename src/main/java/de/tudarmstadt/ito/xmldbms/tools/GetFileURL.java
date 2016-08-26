// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;
import java.io.*;
import java.net.*;

/**
 * Implements GetFile for URIs
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class GetFileURL implements GetFile
{
    public static void main(String args[])
    {
        GetFileURL fileobj = new GetFileURL();
        String fabspath = "";
        try
            {
            //   fabspath = fileobj.getFileURL(args[0]);
            //   System.out.println("the absolute path of the file"+fabspath);
            InputStream temp = fileobj.getFile(args);
            /*
            	this is done for testing whether the stream has been returned or not
            */
            BufferedReader br = new BufferedReader(new InputStreamReader(temp));
            int c;
            while ((c = br.read()) != -1)
                {
                System.out.print((char) c);
            }
        }
        catch (GetFileException e)
            {
            System.err.println(e);
        } // end of checking for exception
        catch (IOException e)
            {
            System.err.println(e);
        } // end of checking for exception
    } // end of main method                  
    /**
     * Get a file as an InputStream.
     *
     * @param args args[0] is the filename.
     * @return The InputStream
     */
    public InputStream getFile(String[] args) throws GetFileException
    {
        String fname = args[0];
        String fpath = "";
        File f1;
        f1 = new File(fname);
        InputStream fins = null;
        InputStream fins1 = getClass().getResourceAsStream(fname);
        if (fins1 != null)
            {
            fins = fins1;
        }
        else if (f1.exists())
            {
            // System.out.println("the file exists");
            if (f1.canWrite())
                {
                // System.out.println("the file is writable");
                if (f1.canRead())
                    {
                    //System.out.println("the file is readable");
                    fpath = f1.getAbsolutePath();
                    //System.out.println("the file is " + fpath);
                    try
                        {
                        fins = new FileInputStream(fname);
                    }
                    catch (IOException e)
                        {
                        throw new GetFileException(
                            "FileName = " + fname + " IOException " + e.toString());
                    } // end of checking for exception
                } // end of if for canread 
                else
                    {
                    throw new GetFileException("the file is not readable " + fname);
                } // end of else for canread 
            } // end of if for canWrite 
            else
                {
                throw new GetFileException("the file is not writable " + fname);
            } // end of if for canWrite 
        } // end of if for file exist
        else
            {
            try
                {
                URL fileobj = new URL(fname);
                URLConnection hpCon = fileobj.openConnection();
                int urlfilelen = hpCon.getContentLength();
                if (urlfilelen > 0)
                    {
                    InputStream f1s = hpCon.getInputStream();
                    fins = f1s;
                }
                else
                    throw new GetFileException("the file DOES NOT EXIST" + fname);
            }
            catch (MalformedURLException e)
                {
                throw new GetFileException(
                    "FileName = " + fname + " url is invalid" + e.toString());
                //System.err.println(e);
            } // end of checking for exception
            catch (IOException e)
                {
                throw new GetFileException(
                    "FileName = " + fname + " IOException " + e.toString());
            } // end of checking for exception
        } // end of else for fileexist
        return fins;
    } // // end of if for fileurl method                              
    public String fullqual(String filename) throws GetFileException
    {
        String fname = filename;
        String fpath = "file:///";
        java.net.URL fins = getClass().getResource(filename);
        File f1;
        f1 = new File(fname);
     //   System.out.println("Filename passed in  = " + filename);
     //   System.out.println("fname now  = " + fname);
        if (f1.exists())
            {
            fname = fpath + f1.getAbsolutePath();
      //      System.out.println("File found thus fname now  = " + fname);
            return fname;
        } // end checking if local file or URL
        else if (fins != null)
            {
            //System.out.println("Fins = " + fins.toString());
            return fins.toString();
        }
        else
            {
            System.out.println(
                "File not in a jar nor a local file thus fname now  = " + fname);
            return fname;
        }
    }
}