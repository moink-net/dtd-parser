// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Node;
import com.sun.xml.tree.NamespaceScoped;

/**
 * NameQualifier for the Sun DOM implementation, Technology Release 1.
 * Node that this does not support namespace prefixes on attributes.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class NQ_Sun extends NameQualifierImpl
{
   public String getLocalName(Node node)
   {
	  int nodeType = node.getNodeType();

	  if (nodeType == Node.ELEMENT_NODE)
	  {
		 return ((NamespaceScoped)node).getLocalName();
	  }
	  return node.getNodeName();
   }   

   public String getNamespaceURI(Node node)
   {
	  int nodeType = node.getNodeType();

	  if (nodeType == Node.ELEMENT_NODE)
	  {
		 return ((NamespaceScoped)node).getNamespace();
	  }
	  return null;
   }   
}