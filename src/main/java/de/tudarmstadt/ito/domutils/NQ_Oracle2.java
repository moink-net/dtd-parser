// No copyright, no warranty; use as you will.

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Node;
import oracle.xml.parser.v2.NSName;

/**
 * NameQualifier for the Oracle DOM implementation, version 2.0.2.8.0.
 *
 * @author Alf Hogemark
 * @version 1.1
 */

public class NQ_Oracle2 extends NameQualifierImpl
{
   public String getLocalName(Node node)
   {
	  int nodeType = node.getNodeType();

	  if (nodeType == Node.ELEMENT_NODE)
	  {
		 return ((NSName)node).getLocalName();
	  }
	  else if (nodeType == Node.ATTRIBUTE_NODE)
	  {
		 return ((NSName)node).getLocalName();
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
		 return ((NSName)node).getNamespace();
	  }
	  else if (nodeType == Node.ATTRIBUTE_NODE)
	  {
		 return ((NSName)node).getNamespace();
	  }
	  else
	  {
		 return null;
	  }
   }   
}