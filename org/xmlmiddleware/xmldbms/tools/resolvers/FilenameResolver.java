// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.01: New in 2.0

package org.xmlmiddleware.xmldbms.tools.resolvers;

import org.xmlmiddleware.utils.*;

import java.io.*;

/**
 * Resolves filenames into InputStreams, OutputStreams, Readers, and Writers.
 *
 * @author Ronald Bourret
 * @version 2.0
 */
public class FilenameResolver implements LocationResolver
{
   /**
    * Get an InputStream over a file.
    *
    * @param location The filename.
    * @return The InputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Reader, such as the file is not found.
    */
   public InputStream getInputStream(String location)
      throws XMLMiddlewareException
   {
      try
      {
         return new FileInputStream(location);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Get an OutputStream over a file.
    *
    * @param location The filename.
    * @return The OutputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Writer, such as the file is not found.
    */
   public OutputStream getOutputStream(String location)
      throws XMLMiddlewareException
   {
      try
      {
         return new FileOutputStream(location);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Get a Reader over a file.
    *
    * @param location The filename.
    * @return The Reader.
    * @exception XMLMiddlewareException Thrown if the file is not found, etc.
    */
   public Reader getReader(String location)
      throws XMLMiddlewareException
   {
      try
      {
         return new FileReader(location);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Get a Writer over a file.
    *
    * @param location The filename.
    * @param encoding The encoding to use.
    * @return The Writer.
    * @exception XMLMiddlewareException Thrown if the file is not found, etc.
    */
   public Writer getWriter(String location, String encoding)
      throws XMLMiddlewareException
   {
      try
      {
         if (encoding == null)
         {
            return new FileWriter(location);
         }
         else
         {
            OutputStream stream;
            stream = new FileOutputStream(location);
            return new OutputStreamWriter(stream, encoding);
         }
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }
   /**
    * Whether getReader is supported.
    *
    * @return True.
    */
   public boolean supportsReader()
   {
      return true;
   }

   /**
    * Whether getWriter is supported.
    *
    * @return True.
    */
   public boolean supportsWriter()
   {
      return true;
   }
}