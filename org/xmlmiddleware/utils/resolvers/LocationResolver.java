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

package org.xmlmiddleware.utils.resolvers;

import org.xmlmiddleware.utils.*;

import java.io.*;

/**
 * Resolve resource names into IO objects.
 *
 * <p>Support for the methods to get Readers and Writers is optional. If
 * implementations do support these methods, they must be able to accurately
 * determine the character encoding that is used, even if they do not support
 * that encoding.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */
public interface LocationResolver
{
   /**
    * Get an InputStream over the resource at a given location.
    *
    * @param location The name of the location.
    * @return The InputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Reader, such as the resource is not found.
    */
   public InputStream getInputStream(String location)
      throws XMLMiddlewareException;

   /**
    * Get an OutputStream over the resource at a given location.
    *
    * @param location The name of the location.
    * @return The OutputStream.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Writer, such as the resource is not found.
    */
   public OutputStream getOutputStream(String location)
      throws XMLMiddlewareException;

   /**
    * Get a Reader over the resource at a given location.
    *
    * @param location The name of the location.
    * @return The Reader.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Reader, such as the resource is not found or the encoding is not
    *    supported.
    */
   public Reader getReader(String location)
      throws XMLMiddlewareException;

   /**
    * Get a Writer over the resource at a given location.
    *
    * @param location The name of the location.
    * @param encoding The encoding to use.
    * @return The Writer.
    * @exception XMLMiddlewareException Thrown if an error occurs retrieving the
    *    Writer, such as the resource is not found or the encoding is not
    *    supported.
    */
   public Writer getWriter(String location, String encoding)
      throws XMLMiddlewareException;

   /**
    * Whether getReader is supported.
    *
    * @return Whether getReader is supported.
    */
   public boolean supportsReader();

   /**
    * Whether getWriter is supported.
    *
    * @return Whether getWriter is supported.
    */
   public boolean supportsWriter();
}