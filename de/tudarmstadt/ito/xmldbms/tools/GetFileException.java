// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

/**
 * Thrown if an error occurs getting the content of a file.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class GetFileException extends Exception
{
    private String detail;

    /**
     * Construct a new GetFileException
     *
     * @param s The exception message
     */

    GetFileException(String s)
    {
        detail = s;
    } // end of constructor               

    /**
     * Get the exception message
     *
     * @return The exception message
     */
    public String toString()
    {
        return "Encountered Exception" + detail;
    } // end of toString method          
}