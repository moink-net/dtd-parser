// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Serialize the logical nodes of a DOM tree.
 *
 * <P>The methods in this class behave as if the DOM tree consisted only of
 * element, attribute, and fully-normalized text nodes. They ignore comment
 * and processing instruction nodes and expand entity references, but do not
 * modify the DOM tree itself.</P>
 *
 * <P>For example, suppose we have the following DOM tree:</P>
 *
 * <PRE>
 *                           ELEMENT(A)
 *                               |
 *             -------------------------------------
 *             |           |           |           |
 *         ELEMENT(B)  TEXT("foo")  ENTITYREF  TEXT("bar")
 *                                     |
 *                         -----------------------
 *                         |           |         |
 *                     CDATA("asdf")   PI     ELEMENT(C)
 * </PRE>
 *
 * <P>The logical serialized form of this is:</P>
 *
 * <PRE>
 *    <A><B><B/>fooasdf<C><C/>bar</A>
 *</PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class LogicalNodeSerializer
{
   // This class returns the "data" -- elements, attributes, and PCDATA -- in a
   // particular node as a string. It expands entity references, eliminates
   // CDATA sections, and ignores comments, PIs, and the DTD.

   // Constants
   private static final String LT = "<";
   private static final String GT = ">";
   private static final String SLASH = "/";
   private static final String SPACE = " ";
   private static final String EQUALS = "=";
   private static final String DOUBLEQUOTE = "\"";

   // ********************************************************************
   // methods
   // ********************************************************************

   /**
	* Serialize a node and its child elements and PCDATA.
	*
	* <P>This method expands entity references, eliminates CDATA sections,
	* and ignores comments, PIs, and the DTD.</P>
	*
	* <P>This method currently has a bug in that it does does not correctly
	* escape markup that occurs as text or in CDATA sections. How to handle
	* this bug is not yet clear, as the initial target of the serialized XML
	* is a database, where people are likely to want to search for the markup,
	* not CDATA sections or entity references.</P>
	*
	* @param node The node to serialize.
	* @param childrenOnly Whether to serialize the node itself or only its
	*  children. For example, for an element node, this dictates whether a
	*  tag for the element itself is included in the returned value. Note
	*  that attribute, text, and CDATA nodes have no &quot;logical&quot;
	*  children, so returning children-only for these nodes returns a null.
	* @return The serialized node and its children. Null is returned for
	*  comment, document type, entity, notation, and processing instruction
	*  nodes.
	*/

   static String serializeNode(Node node, boolean childrenOnly)
   {
	  short nodeType = node.getNodeType();

	  switch (nodeType)
	  {
		 case Node.ATTRIBUTE_NODE:
		 case Node.CDATA_SECTION_NODE:
		 case Node.TEXT_NODE:
			if (childrenOnly)
			{
			   return null;
			}
			else
			{
			   return node.getNodeValue();
			}

		 case Node.COMMENT_NODE:
		 case Node.DOCUMENT_TYPE_NODE:
		 case Node.ENTITY_NODE:
		 case Node.NOTATION_NODE:
		 case Node.PROCESSING_INSTRUCTION_NODE:
			// These nodes are not part of the "data" of a document. Therefore
			// return null;
			return null;

		 case Node.ELEMENT_NODE:
			break;

		 case Node.DOCUMENT_FRAGMENT_NODE:
			childrenOnly = true;
			break;

		 case Node.DOCUMENT_NODE:
			node = ((Document)node).getDocumentElement();
			childrenOnly = false;
			break;
	  }

	  // Build a string from element, text, and CDATA child nodes. Elements
	  // are represented by tags; note that there is a problem here, as it is
	  // impossible to tell a real element from a tag constructed with lt/gt
	  // entities or CDATA.

	  StringBuffer value = new StringBuffer();
	  Node         current, next;

	  if (childrenOnly)
	  {
		 current = node.getFirstChild();
	  }
	  else
	  {
		 current = node;
	  }

	  while (current != null)
	  {
		 // Append the value of the node (if any) to the complete value. We
		 // ignore comments and PIs. We "expand" entity references by simply
		 // traversing their children.

		 nodeType = current.getNodeType();
		 if (nodeType == Node.ELEMENT_NODE)
		 {
			value.append(LT);
			value.append(current.getNodeName());
			value.append(serializeAttrs(current.getAttributes()));
			value.append(GT);
		 }
		 else if ((nodeType == Node.TEXT_NODE) ||
				  (nodeType == Node.CDATA_SECTION_NODE))
		 {
			value.append(current.getNodeValue());
		 }

		 // Get the first child of the node.

		 next = current.getFirstChild();
		 if (next != null)
		 {
			current = next;
			continue;
		 }

		 // If the node has no children, then get the sibling of the node.

		 while (current != null)
		 {
			// Close the current node.
			if (current.getNodeType() == Node.ELEMENT_NODE)
			{
			   value.append(LT);
			   value.append(SLASH);
			   value.append(current.getNodeName());
			   value.append(GT);
			}

			// Get the next sibling. If there is a next sibling, then go to
			// the outer while loop to process it and get its children. If
			// there is no next sibling, then go back up a level. If we get
			// back to the original node, stop processing.

			next = current.getNextSibling();
			if (next != null)
			{
			   current = next;
			   break;
			}

			current = current.getParentNode();
			if (current == node)
			{
			   if (!childrenOnly &&
				   (current.getNodeType() == Node.ELEMENT_NODE))
			   {
				  value.append(LT);
				  value.append(SLASH);
				  value.append(current.getNodeName());
				  value.append(GT);
			   }
			   current = null;
			}
		 }
	  }
	  return value.toString();
   }   

   /**
	* Serialize a NamedNodeMap of attributes.
	*
	* <P>This method expands entity references, eliminates CDATA sections,
	* and ignores comments, PIs, and the DTD. It currently has a bug in
	* that it does not correctly escape double quotes. How to handle this
	* bug is not yet clear, as the initial target of the serialized XML
	* is a database, where people are likely to want to search for the
	* attribute values, not references to the quot entity.</P>
	*
	* @param attrs The NamedNodeMap containing the attributes.
	* @return The serialized attributes.
	*/

   static String serializeAttrs(NamedNodeMap attrs)
   {
	  // This method serializes attributes. It currently has a bug in that it
	  // does not correctly escape double quotes. How to handle this bug is
	  // not yet clear, as the initial target of the serialized XML is a
	  // database, where people are likely to want to search for the attribute
	  // values, not entity references.

	  StringBuffer value = new StringBuffer();
	  int          i;
	  Attr         attr;

	  for (i = 0; i < attrs.getLength(); i++)
	  {
		 attr = (Attr) attrs.item(i);
		 value.append(SPACE);
		 value.append(attr.getNodeName());
		 value.append(EQUALS);
		 value.append(DOUBLEQUOTE);
		 value.append(attr.getNodeValue());
		 value.append(DOUBLEQUOTE);
	  }
	  return value.toString();
   }   
}