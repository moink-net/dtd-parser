package de.tudarmstadt.ito.xmldbms.tools;



import java.io.*;
import java.net.*;
  
public class GetFileURL implements de.tudarmstadt.ito.xmldbms.tools.GetFile {
  public static void main(String args[])
  {
	 GetFileURL fileobj = new GetFileURL();
	 String fabspath = "";
	try {

	   //   fabspath = fileobj.getFileURL(args[0]);
	   //   System.out.println("the absolute path of the file"+fabspath);
			InputStream temp = fileobj.getFile(args);

/*
	this is done for testing whether the stream has been returned or not
*/

			BufferedReader br = new BufferedReader(new InputStreamReader(temp));
			int c;
			while (( c = br.read()) != -1)
			{
			  System.out.print((char)c);
			}
		} 
	catch (GetFileException e) {
	  System.err.println(e);


	}// end of checking for exception

	catch (IOException e) {
	   System.err.println(e);


	}// end of checking for exception
   

 
  }// end of main method                  





public InputStream getFile(String[] args) throws GetFileException {
	String fname = args[0];
	String fpath = "";

	File f1;
	f1 = new File(fname);
	InputStream fins = null;
	InputStream fins1 = getClass().getResourceAsStream(fname);

	if (fins1 != null) {
		fins = fins1;
	} else
		if (f1.exists()) {
			// System.out.println("the file exists");
			if (f1.canWrite()) {
				// System.out.println("the file is writable");
				if (f1.canRead()) {
					//System.out.println("the file is readable");
					fpath = f1.getAbsolutePath();
					//System.out.println("the file is " + fpath);
					try {
						fins = new FileInputStream(fname);
					} catch (IOException e) {
						System.err.println(e);

					} // end of checking for exception

				} // end of if for canread 

				else {

					throw new GetFileException("the file is not readable");

				} // end of else for canread 
			} // end of if for canWrite 

			else {
				throw new GetFileException("the file is not writable");

			} // end of if for canWrite 

		} // end of if for file exist

	else {
		try {
			URL fileobj = new URL(fname);
			URLConnection hpCon = fileobj.openConnection();
			int urlfilelen = hpCon.getContentLength();
			if (urlfilelen > 0) {
				InputStream f1s = hpCon.getInputStream();
				fins = f1s;

			} else
				throw new GetFileException("the file DOES NOT EXIST");
		} catch (MalformedURLException e) {
			throw new GetFileException(" url is invalid");
			//System.err.println(e);

		} // end of checking for exception
		catch (IOException e) {
			System.err.println(e);

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


   if(f1.exists())
  {
	   fname = fpath + f1.getAbsolutePath();
		return fname;

	   }// end checking if local file or URL

   else if(fins != null)
  	
	{//System.out.println("Fins = " + fins.toString());
		return fins.toString();
		
	}
  	 	
   else
   { 
	   return fname;
   }
  }                                                  

}// end of class url