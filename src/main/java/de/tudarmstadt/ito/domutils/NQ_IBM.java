// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Node;
import com.ibm.xml.parser.Namespace;

/**
 * NameQualifier for the IBM DOM implementation, version 2.0.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class NQ_IBM extends NameQualifierImpl
{
   public String getLocalName(Node node)
   {
	  int nodeType = node.getNodeType();

	  if ((nodeType == Node.ELEMENT_NODE) || (nodeType == Node.ATTRIBUTE_NODE))
	  {
		 return ((Namespace)node).getNSLocalName();
	  }
	  return node.getNodeName();
   }   

   public String getNamespaceURI(Node node)
   {
	  int nodeType = node.getNodeType();

	  if ((nodeType == Node.ELEMENT_NODE) || (nodeType == Node.ATTRIBUTE_NODE))
	  {
		 return ((Namespace)node).getNSName();
	  }
	  return null;
   }   
}