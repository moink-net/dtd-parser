// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.xslt;

/**
 * Interface for using XSLT processors.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public interface ProcessXslt
{
    /**
     * Transforms an XML document stored in a file.
     *
     * @param xmlfileName The name of the file
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    String transformFiles(String xmlfileName, String xslfileName) throws Exception;
    /**
     * Transforms an XML document stored in an InputStream.
     *
     * @param srcStream The InputStream
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    String transformStream(java.io.InputStream srcStream, String xslfileName)
        throws Exception;
    /**
     * Transforms an XML document stored in a string.
     *
     * @param inputxml The string
     * @param xslfileName The name of the XSLT stylesheet to use
     *
     * @return The output of the transformation
     */
    String transformString(String inputxml, String xslfileName) throws Exception;
}