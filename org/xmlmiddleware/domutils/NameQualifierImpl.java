// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 2.0
// Changes from version 1.0: None
// Changes from version 1.01: None

package org.xmlmiddleware.utils;

import org.w3c.dom.Node;

/**
 * Default implementation of the NameQualifier interface.
 *
 * <P>This is usually the base class for other objects that implement
 * NameQualifier. Normally, these objects override getLocalName() and
 * getNamespaceURI() but not getQualifiedName().</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class NameQualifierImpl implements NameQualifier
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new NameQualifierImpl object. */
   public NameQualifierImpl()
   {
   }   

   // ********************************************************************
   // Methods
   // ********************************************************************

   /**
	* Default implementation of getQualifiedName.
	*
	* @param node The Node for which to get the name.
	* @return The qualified name.
	*/
   public String getQualifiedName(Node node)
   {
	  String uri, localName;

	  localName = getLocalName(node);
	  uri = getNamespaceURI(node);
	  return XMLName.getQualifiedName(localName, uri);
   }   

   /**
	* Default implementation of getLocalName. Always returns the Node name.
	*
	* @param node The Node for which to get the name.
	* @return The (possibly prefixed) Node name.
	*/
   public String getLocalName(Node node)
   {
	  return node.getNodeName();
   }   

   /**
	* Default implementation of getNamespaceURI. Always returns null.
	*
	* @param node The Node for which to get the URI.
	* @return Always null.
	*/
   public String getNamespaceURI(Node node)
   {
	  return null;
   }   
}