// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Fix violation of DOM spec and infinite loop in expandEntityRef
// Changes from version 1.01: None

package org.xmlmiddleware.domutils;

import org.w3c.dom.*;

/**
 * Utilities for manipulating a DOM tree as a tree of logical nodes.
 *
 * <P>The methods in this class behave as if the DOM tree consisted only of
 * element, attribute, and fully-normalized text nodes. The methods modify
 * the DOM tree as necessary. For example, they discard comment and
 * processing instruction nodes, merge adjacent text and CDATA nodes,
 * and replace entity references with their children.</P>
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
 * <P>The first logical child of element node A is element node B, the
 * logical sibling of element B is the text node "fooasdf", and the
 * logical sibling of the text node "fooasdf" is element node C.
 * Furthermore, calling methods to get these logical children and
 * siblings modifies the DOM tree so it looks as follows:</P>
 *
 * <PRE>
 *                           ELEMENT(A)
 *                               |
 *             ----------------------------------------
 *             |              |           |           |
 *         ELEMENT(B)  TEXT("fooasdf")  ELEMENT(C)  TEXT("bar")
 *</PRE>
 *
 * <P>The code is not particularly robust and assumes that the tree will
 * be traversed in depth-first, width-second order. If this is not done,
 * results are unpredictable. For example, it is not clear what the next
 * logical sibling of text node "foo" is if getting the next logical sibling
 * of element node B has not merged it with CDATA node "asdf".</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class LogicalNodeUtils
{
/*
   /**
	* Clone a Node.
	*
	* @param node Node to clone.
	* @param deep Whether to clone the children or just the node.
	* @return The cloned node.

   static Node clone(Node node, boolean deep)
   {
	  // 6/13/00, Ronald Bourret
	  // New method.

	  // The only parser I can find that constructs EntityReference nodes (Xerces)
	  // appears to have a bug that doesn't allow it to clone EntityReference
	  // nodes in some cases. Thus, I am unable to fully test expandEntityReference.
	  // I wrote this method because I was hoping to work around the bug, but hit a
	  // variant of the same bug that doesn't even allow me to get the children of
	  // an EntityReference node in the same cases. In any case, this method is
	  // available if we need it in the future...

	  Node parentClone, currentClone, current, next;

	  parentClone = node.cloneNode(false);
	  if (!deep) return parentClone;

	  current = node.getFirstChild();
	  while (current != null)
	  {
		 // Clone the child and insert it in the parent clone
		 // as the last child.

		 currentClone = current.cloneNode(false);
		 parentClone.insertBefore(currentClone, null);

		 // Depth first -- get children

		 next = current.getFirstChild();
		 if (next != null)
		 {
			parentClone = currentClone;
			current = next;
			continue;
		 }

		 // Width second -- get siblings

		 while (current != null)
		 {
			// Get the next sibling and process it.

			next = current.getNextSibling();
			if (next != null)
			{
			   current = next;
			   break;
			}

			// If there are no siblings, move up one level
			// and check if we are back at the top level.

			current = current.getParentNode();
			if (current == node) current = null;
			parentClone = parentClone.getParentNode();
		 }
	  }

	  return parentClone;
   }
*/
   // Ideally, these methods would subclass objects that implement existing
   // DOM interfaces, particularly Node. Unfortunately, there is no way to do
   // this in a DOM-implementation-neutral way; we can only subclass a known
   // class, not an interface. The alternative -- encapsulating a class that
   // implements a given interface -- is just too ugly. Therefore, we offer
   // these methods as static utility methods.

   // ********************************************************************
   // Methods
   // ********************************************************************

   /**
	* Get the first logical child node.
	*
	* @param node Parent node.
	* @return First logical child node or null if there is no logical child
	*  node.
	*/
   public static Node getFirstChild(Node node)
   {
	  return getLogicalNode(node.getFirstChild());
   }   

   /**
	* Get the next logical sibling node.
	*
	* @param node Starting node.
	* @return First logical sibling node or null if there is no logical
	*  sibling node.
	*/
   public static Node getNextSibling(Node node)
   {
	  return getLogicalNode(node.getNextSibling());
   }   

   /**
	* Get the first logical node at or sibling-wise after the the
	* input node.
	*
	* @param node Starting node.
	* @return First logical node at or sibling-wise after the input node.
	*  Null if such a node does not exist or if the type of the input node
	*  is not a type that can legally occur beneath an element node.
	*/
   public static Node getLogicalNode(Node node)
   {
	  short nodeType;

	  // Returns the first "logical" (element or text) node that is at or after
	  // the current node. Text nodes are normalized to include all immediately
	  // following text or CDATA nodes. Entity references are "expanded" in
	  // place.

	  while (node != null)
	  {
		 nodeType = node.getNodeType();
		 switch (nodeType)
		 {
			case Node.ELEMENT_NODE:
			   return node;

			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
			   node = normalizeText(node);
			   return node;

			case Node.PROCESSING_INSTRUCTION_NODE:
			case Node.COMMENT_NODE:
			   node = node.getNextSibling();
			   break;

			case Node.ENTITY_REFERENCE_NODE:
			   node = expandEntityRef(node);
			   break;

			default:
			   node = null;
			   break;
		 }
	  }

	  return node;
   }   

   /**
	* Concatenate the character data in the current node and all immediately
	* following text or CDATA nodes. The result is placed in the current node.
	* If the current node is a CDATA node, it is converted to a text node. No
	* action is taken if the input node is not text or CDATA or if the input
	* node has no parent.
	*
	* @param node Text or CDATA node to normalize.
	* @return The modified node.
	*/
   static Node normalizeText(Node node)
   {
	  Node parent, sibling, next;

	  if ((node.getNodeType() != Node.TEXT_NODE) &&
		  (node.getNodeType() != Node.CDATA_SECTION_NODE))
	  {
		 return node;
	  }

	  if ((parent = node.getParentNode()) == null) return node;
	  sibling = node.getNextSibling();

	  while (sibling != null)
	  {
		 switch (sibling.getNodeType())
		 {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
			   ((CharacterData)node).appendData(sibling.getNodeValue());
			   next = sibling.getNextSibling();
			   parent.removeChild(sibling);
			   sibling = next;
			   break;
			   
			case Node.ENTITY_REFERENCE_NODE:
			   sibling = expandEntityRef(sibling);
			   break;

			case Node.COMMENT_NODE:
			case Node.PROCESSING_INSTRUCTION_NODE:
			   // It is not entirely clear what to do when we hit a PI: stop
			   // or continue concatenating text. Currently, we continue.
			   sibling = sibling.getNextSibling();
			   break;

			default:
			   sibling = null;
			   break;
		 }
	  }

	  // If the node is a text node, just return it.
	  if (node.getNodeType() == Node.TEXT_NODE)
	  {
		 return node;
	  }

	  // If the node is a CDATA node, replace it with a text node.
	  sibling = node.getOwnerDocument().createTextNode(node.getNodeValue());
	  parent.replaceChild(sibling, node);
	  return sibling;
   }   

   /**
	* Replace an entity reference node by its children. Note that this
	* method does not attempt to normalize text, nor does it expand child
	* entity reference nodes.
	*
	* @param node Entity reference node to expand.
	* @return The first child of the entity reference node. This child is
	*  now at the same level that the entity reference node was at.
	*/
   static Node expandEntityRef(Node node)
   {
	  Node parent, child, next;

	  if (node.getNodeType() != Node.ENTITY_REFERENCE_NODE) return null;

	  if ((parent = node.getParentNode()) == null) return null;

	  // Get the next sibling of the entity reference node. We will place
	  // children before this.

	  next = node.getNextSibling();

	  // Get the first child, then move it to its parent's level. Continue
	  // the process as long as the entity reference node has children.

	  child = node.getFirstChild();
	  while (child != null)
	  {
		 // 6/13/00, Ronald Bourret
		 // Previously, this code was:
		 //
		 //    parent.insertBefore(child, next);
		 //    child = node.getFirstChild();
		 //
		 // The first line violates the DOM spec because children of
		 // EntityReference nodes are read-only and insertBefore removes the
		 // existing nodes before inserting them elsewhere. Solve this by
		 // inserting a (deep) clone of the child. Note that we construct
		 // the clone ourselves -- see clone for details.
		 //
		 // The second line is simply wrong: instead of getting the first
		 // child of the parent (an infinite loop), it should get the next
		 // sibling of the child.

		 // Due to bugs and lack of support in various parsers, I am
		 // unable to completely test this code. See notes in clone.

		 parent.insertBefore(child.cloneNode(true), next);
//         parent.insertBefore(clone(child, true), next);
		 child = child.getNextSibling();
	  }

	  // Get the first child (now sibling) of the entity reference node,
	  // remove the entity reference node, and return the child (sibling).

	  next = node.getNextSibling();
	  parent.removeChild(node);
	  return next;
   }   

/*
   /**
	* Clone a Node.
	*
	* @param node Node to clone.
	* @param deep Whether to clone the children or just the node.
	* @return The cloned node.

   static Node clone(Node node, boolean deep)
   {
	  // 6/13/00, Ronald Bourret
	  // New method.

	  // The only parser I can find that constructs EntityReference nodes (Xerces)
	  // appears to have a bug that doesn't allow it to clone EntityReference
	  // nodes in some cases. Thus, I am unable to fully test expandEntityReference.
	  // I wrote this method because I was hoping to work around the bug, but hit a
	  // variant of the same bug that doesn't even allow me to get the children of
	  // an EntityReference node in the same cases. In any case, this method is
	  // available if we need it in the future...

	  Node parentClone, currentClone, current, next;

	  parentClone = node.cloneNode(false);
	  if (!deep) return parentClone;

	  current = node.getFirstChild();
	  while (current != null)
	  {
		 // Clone the child and insert it in the parent clone
		 // as the last child.

		 currentClone = current.cloneNode(false);
		 parentClone.insertBefore(currentClone, null);

		 // Depth first -- get children

		 next = current.getFirstChild();
		 if (next != null)
		 {
			parentClone = currentClone;
			current = next;
			continue;
		 }

		 // Width second -- get siblings

		 while (current != null)
		 {
			// Get the next sibling and process it.

			next = current.getNextSibling();
			if (next != null)
			{
			   current = next;
			   break;
			}

			// If there are no siblings, move up one level
			// and check if we are back at the top level.

			current = current.getParentNode();
			if (current == node) current = null;
			parentClone = parentClone.getParentNode();
		 }
	  }

	  return parentClone;
   }
*/
}