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
import java.net.*;

/**
 * Resolves URLs into InputStreams and OutputStreams.
 *
 * @author Ronald Bourret
 * @version 2.0
 */
public class URLResolver implements LocationResolver
{
   /**
    * Get an InputStream over a URL.
    *
    * @param location The URL.
    * @return The InputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Reader, such as the URL is not found.
    */
   public InputStream getInputStream(String location)
      throws XMLMiddlewareException
   {
      URL           url;
      URLConnection conn;

      try
      {
         url = new URL(location);
         conn = url.openConnection();
         conn.connect();
         return conn.getInputStream();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Get an OutputStream over a URL.
    *
    * @param location The URL.
    * @return The OutputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Writer, such as the URL is not found.
    */
   public OutputStream getOutputStream(String location)
      throws XMLMiddlewareException
   {
      URL           url;
      URLConnection conn;

      try
      {
         // Note that this code probably won't let you simply post an XML document
         // to a server by simply using the URL of the XML document you eventually
         // want. Instead, you'll probably have to have a script on the server that
         // can accept the XML document and put it where you want. That will probably
         // require changes to this code as well. For example, location will presumably
         // be the name of the processing script and you'll need to pass the eventual
         // location of the XML document by other means, such as in a form variable.

         url = new URL(location);
         conn = url.openConnection();
         conn.setDoOutput(true);
         return conn.getOutputStream();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Method not supported
    *
    * @param location Ignored.
    * @return Nothing
    * @exception XMLMiddlewareException Always thrown
    */
   public Reader getReader(String location)
      throws XMLMiddlewareException
   {
      throw new XMLMiddlewareException("URLResolver.getReader not supported");
   }

   /**
    * Method not supported
    *
    * @param location Ignored.
    * @param encoding Ignored.
    * @return Nothing
    * @exception XMLMiddlewareException Always thrown
    */
   public Writer getWriter(String location, String encoding)
      throws XMLMiddlewareException
   {
      throw new XMLMiddlewareException("URLResolver.getWriter not supported");
   }

   /**
    * Whether getReader is supported.
    *
    * @return False.
    */
   public boolean supportsReader()
   {
      return false;
   }

   /**
    * Whether getWriter is supported.
    *
    * @return False.
    */
   public boolean supportsWriter()
   {
      return false;
   }
}