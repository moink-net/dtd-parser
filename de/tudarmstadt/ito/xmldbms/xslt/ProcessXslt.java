package de.tudarmstadt.ito.xmldbms.xslt;

/**
 * Interface for using XSLT processors.
 * Creation date: (08/05/01 16:45:37)
 * @author: Adam Flinton
 */
public interface ProcessXslt {
/**
 * Insert the method's description here.
 * Creation date: (08/05/01 16:46:33)
 * @return java.lang.String
 */
String transformFiles(String xmlfileName, String xslfileName) throws Exception;
/**
 * Insert the method's description here.
 * Creation date: (08/05/01 16:47:42)
 * @return java.lang.String
 */
String transformStream(java.io.InputStream srcStream, String xslfileName) throws Exception;
/**
 * Insert the method's description here.
 * Creation date: (08/05/01 16:48:29)
 * @return java.lang.String
 */
String transformString(String inputxml, String xslfileName) throws Exception;
}
