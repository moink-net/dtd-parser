// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 2000

// Version 1.1
// Changes from version 1.0:
// * New in version 1.01
// Changes from version 1.01: None

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Node;

/**
* Implementation of NameQualifier for any DOM level 2 implementation.
*
* @author Ronald Bourret, Technical University of Darmstadt
* @version 1.1
*/

public class NQ_DOM2 extends NameQualifierImpl
{
   /**
	* Get the local name of a DOM Node.
	*
	* @param node The Node for which to get the name.
	* @return The local name.
	*/
   public String getLocalName(Node node)
   {
	  String localName = null;

	  int nodeType = node.getNodeType();

	  // For element and attribute nodes, get the local name. If
	  // namespaces are not being used, this will be null, so we
	  // need to get the node name instead. For nodes other than
	  // element and attribute nodes, always get the node name.

	  if ((nodeType == Node.ELEMENT_NODE) || (nodeType == 
Node.ATTRIBUTE_NODE))
	  {
		 localName = node.getLocalName();
	  }
	  if (localName == null) localName = node.getNodeName();
	  return localName;
   }   


   /**
	* Get the namespace URI used by a DOM Node.
	*
	* @param node The Node for which to get the URI.
	* @return The namespace URI.
	*/
   public String getNamespaceURI(Node node)
   {
	  return node.getNamespaceURI();
   }   
}