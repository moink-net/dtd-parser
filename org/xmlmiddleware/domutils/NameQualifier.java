// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9


// Changes from version 1.0: None
// Changes from version 1.01: None

package org.xmlmiddleware.domutils;

import org.w3c.dom.*;

/**
 * Interface encapsulating namespace-qualified names of DOM Nodes.
 *
 * <P>Because the DOM does not yet define how it interacts with namespaces,
 * this interface encapsulates namespace behavior.</P>
 *
 * <P>The 'local name' of a node is its unprefixed name. The 'qualified name'
 * of a node is the namespace URI plus a caret (^) plus the local name; if
 * there is no namespace URI, the qualified name is the same as the local name.
 * For example:</P>
 * <PRE>
 *    &lt;foo:element1 xmlns="http://foo">
 *    Local name: "element1"
 *    Qualified name: "http://foo^element1"<BR />
 *
 *    &lt;element2>
 *    Local name: "element2"
 *    Qualified name: "element2"
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 2.0
 */

public interface NameQualifier
{
   /**
	* Get the URI-qualified name of a DOM Node.
	*
	* @param node The Node for which to get the name.
	* @return The qualified name.
	*/
   public String getQualifiedName(Node node);   

   /**
	* Get the local name of a DOM Node.
	*
	* @param node The Node for which to get the name.
	* @return The local name.
	*/
   public String getLocalName(Node node);   

   /**
	* Get the namespace URI used by a DOM Node.
	*
	* @param node The Node for which to get the URI.
	* @return The namespace URI.
	*/
   public String getNamespaceURI(Node node);   
}