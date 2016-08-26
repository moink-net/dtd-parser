// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Node;
import oracle.xml.parser.NSAttr;
import oracle.xml.parser.NSElement;

/**
 * NameQualifier for the Oracle DOM implementation, version 1.0.1.2.0.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class NQ_Oracle extends NameQualifierImpl
{
   public String getLocalName(Node node)
   {
	  int nodeType = node.getNodeType();

	  if (nodeType == Node.ELEMENT_NODE)
	  {
		 return ((NSElement)node).getLocalName();
	  }
	  else if (nodeType == Node.ATTRIBUTE_NODE)
	  {
		 return ((NSAttr)node).getLocalName();
	  }
	  else
	  {
		 return node.getNodeName();
	  }
   }   

   public String getNamespaceURI(Node node)
   {
	  int nodeType = node.getNodeType();

	  if (nodeType == Node.ELEMENT_NODE)
	  {
		 return ((NSElement)node).getNamespace();
	  }
	  else if (nodeType == Node.ATTRIBUTE_NODE)
	  {
		 return ((NSAttr)node).getNamespace();
	  }
	  else
	  {
		 return null;
	  }
   }   
}