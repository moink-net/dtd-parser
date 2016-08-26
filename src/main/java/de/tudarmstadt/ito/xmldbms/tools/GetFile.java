// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

/**
 * Interface for getting an InputStream from a file.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public interface GetFile
{
    /**
     * Get a file as an InputStream.
     *
     * @param args The arguments needed to get the file.
     * @return The InputStream
     */
    java.io.InputStream getFile(String[] args) throws Exception;
}