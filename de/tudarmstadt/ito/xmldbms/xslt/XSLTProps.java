// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.xslt;

/**
 * Properties constants for XSLT.
 *
 * <p>Note that these are currently not used directly by XML-DBMS.
 * Instead, they are available for use by property-driven XML-DBMS
 * applications that want to XSLT.</p>
 *
 * @author Adam Flinton
 * @version 1.1
 * @see Transfer
 * @see ProcessProperties
 */
public class XSLTProps
{
    /** Name of the class that implements ProcessXSLT */
    public static String XSLTCLASS = "XSLTClass";

    /** Name of the file to which the XSLT output is to be sent? */
    public static String XSLTOUTPUT = "XSLTOutput";

    /** XSLT stylesheet to use */
    public static String XSLTSCRIPT = "XSLTScript";
}