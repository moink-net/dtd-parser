// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.0:
// * Fix violation of DOM spec and infinite loop in expandEntityRef
// Changes from version 1.01: None
// Changes from version 1.1:
// * Moved to xmlutils package
// * General cleanup

package org.xmlmiddleware.xmlutils;

import org.w3c.dom.*;

/**
 * Utility methods that treat a DOM tree as if it consisted only of
 * element, attribute, and text nodes.
 *
 * <p>The methods in this class behave as if the DOM tree consisted only of
 * element, attribute, and fully-normalized text nodes. <b>Except for serialize(),
 * they modify the DOM tree as necessary, replacing entity references with their
 * children, discarding comment and processing instruction nodes, merging adjacent
 * text and CDATA nodes, and so on.</b></p>
 *
 * <p>For example, suppose we have the following DOM tree:</p>
 *
 * <pre>
 *                           ELEMENT(A)
 *                               |
 *             -------------------------------------
 *             |           |           |           |
 *         ELEMENT(B)  TEXT("foo")  ENTITYREF  TEXT("bar")
 *                                     |
 *                         -----------------------
 *                         |           |         |
 *                     CDATA("asdf")   PI     ELEMENT(C)
 * </pre>
 *
 * <p>This class behaves as if the first child of element node A is element
 * node B, the next sibling of element node B is the text node "fooasdf",
 * and the next sibling of the text node "fooasdf" is the element node C.
 * That is, it normalizes the tree to the following:</p>
 *
 * <pre>
 *                           ELEMENT(A)
 *                               |
 *             ----------------------------------------
 *             |              |           |           |
 *         ELEMENT(B)  TEXT("fooasdf")  ELEMENT(C)  TEXT("bar")
 *</pre>
 *
 *<p>The serialized form of this is:</p>
 *
 * <pre>
 *    &lt;A>&lt;B/>fooasdf&lt;C/>bar&lt;/A>
 *</pre>
 *
 * <p>The code assumes that the tree will be traversed in depth-first,
 * width-second order, with the methods in this class, such as getFirstChild,
 * replacing the corresponding methods in DOM's Node interface. This is done
 * so that the tree can be processed in a single pass, rather than a normalization
 * pass and a reading pass. The result of using methods in this class when
 * traversing the tree in any other order is undefined.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DOMNormalizer
{
   // Ideally, these methods would subclass objects that implement existing
   // DOM interfaces, particularly Node. Unfortunately, there is no way to do
   // this in a DOM-implementation-neutral way; we can only subclass a known
   // class, not an interface. The alternative -- encapsulating a class that
   // implements a given interface -- is just too ugly. Therefore, we offer
   // these methods as static utility methods.

   // ********************************************************************
   // Constants
   // ********************************************************************

   private static String AMPENTITY  = "&amp;",
                         LTENTITY   = "&lt;",
                         QUOTENTITY = "&quot;";

   private static final char QUOT   = '"',
                             APOS   = '\'',
                             LT     = '<',
                             GT     = '>',
                             AMP    = '&',
                             SPACE  = ' ',
                             EQUALS = '=',
                             SLASH  = '/';


   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Get the first normalized child node.
    *
    * @param node Parent node.
    * @return First normalized child node or null if there is no normalized
    *    child node.
    */
   public static Node getFirstChild(Node node)
   {
      return normalizeNode(node.getFirstChild());
   }

   /**
    * Get the next normalized sibling node.
    *
    * @param node Starting node.
    * @return First normalized sibling node or null if there is no normalized
    *     sibling node.
    */
   public static Node getNextSibling(Node node)
   {
      return normalizeNode(node.getNextSibling());
   }

   /**
    * Get the first normalized node at or sibling-wise after the input node.
    *
    * <p>This method expands entity references in place, discards processing
    * instruction and comment nodes, and concatenates adjacent text and CDATA
    * nodes.</p>
    *
    * @param node Starting node.
    * @return The node. Null if such a node does not exist or if the type of
    *    the input node is not a type that can legally occur beneath an element node.
    */
   public static Node normalizeNode(Node node)
   {
      short nodeType;
      Node  currNode;

      currNode = node;

      while (currNode != null)
      {
         nodeType = currNode.getNodeType();
         switch (nodeType)
         {
            case Node.ELEMENT_NODE:
               return currNode;

            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
               currNode = normalizeText(currNode);
               return currNode;

            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.COMMENT_NODE:
               currNode = currNode.getNextSibling();
               break;

            case Node.ENTITY_REFERENCE_NODE:
               currNode = expandEntityRef(currNode);
               break;

            default:
               currNode = null;
               break;
         }
      }

      return currNode;
   }

   /**
    * Normalize text nodes, starting with the current node.
    *
    * <p>If the input node is a text or CDATA node, concatenate its value
    * with the values of all immediately following text or CDATA nodes. A
    * following text or CDATA node is considered to be immediately following
    * if the only nodes between it and the input node are text, CDATA,
    * comment, or processing instruction nodes. (Comment and processing
    * instruction nodes are discarded.)</p>
    *
    * <p>If the input node is not a text or CDATA node, or if the input node has no
    * parent, then no normalization takes place and the input node is returned.</p>
    *
    * <p>If the input node is a CDATA node, it is converted to a text node.</p>
    *
    * @param node Text or CDATA node to normalize.
    * @return The normalized node.
    */
   public static Node normalizeText(Node node)
   {
      short nodeType;
      Node  parent, sibling, next;

      nodeType = node.getNodeType();

      if ((nodeType != Node.TEXT_NODE) &&
          (nodeType != Node.CDATA_SECTION_NODE))
      {
         return node;
      }

      parent = node.getParentNode();
      if (parent == null) return node;

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
               // If we hit a comment node or a PI node, just discard it and
               // continue concatenating text.
               sibling = sibling.getNextSibling();
               break;

            default:
               sibling = null;
               break;
         }
      }

      if (nodeType == Node.TEXT_NODE)
      {
         // If the node is a text node, just return it. Note that this now
         // contains all of the normalized text.

         return node;
      }
      else // if (nodeType == Node.CDATA_SECTION_NODE)
      {
         // If the node is a CDATA node, replace it with a text node.

         sibling = node.getOwnerDocument().createTextNode(node.getNodeValue());
         parent.replaceChild(sibling, node);
         return sibling;
      }
   }

   /**
    * Replace an entity reference node by its children.
    *
    * <p>This method returns the first child of the entity reference. This
    * child is now at the same level that the entity reference node was at.
    * If the input node is not an entity reference node, or if the input node
    * does not have a parent, null is returned.</p>
    *
    * <p>This method does not attempt to normalize text, nor does it expand
    * entity reference nodes that are children of the input node. It is
    * normally called only by other methods, which do normalize text and
    * expand nested entity reference nodes.</p>
    *
    * @param node Entity reference node to expand.
    * @return The first child of the entity reference node.
    */
   public static Node expandEntityRef(Node node)
   {
      Node parent, child, next;

      if (node.getNodeType() != Node.ENTITY_REFERENCE_NODE) return null;

      parent = node.getParentNode();
      if (parent == null) return null;

      // Get the next sibling of the entity reference node. We will place
      // children before this. If this is null, the children are simply
      // appended.

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
         // existing nodes before inserting them elsewhere. We solve this by
         // inserting a (deep) clone of the child.
         //
         // Note that we attempted to construct the clone ourselves due
         // to bugs in DOM implementations, but those same bugs prevented
         // us from constructing the clone, so we just use the DOM call, which
         // is what we really want anyway. See clone() for details.
         //
         // The second line is simply wrong: instead of getting the first
         // child of the parent (an infinite loop), it should get the next
         // sibling of the child.

         // Due to bugs and lack of support in various parsers, I am
         // unable to completely test this code. See notes in clone.

//         parent.insertBefore(clone(child, true), next);

         parent.insertBefore(child.cloneNode(true), next);
         child = child.getNextSibling();
      }

      // Get the first child (now sibling) of the entity reference node,
      // remove the entity reference node, and return the child (sibling).

      next = node.getNextSibling();
      parent.removeChild(node);
      return next;
   }

   /**
    * Serialize the normalized version of a node.
    *
    * <p>This method behaves as if entity references were expanded, CDATA
    * sections replaced with text, and comments and PIs didn't exist. It
    * does not actually modify the DOM tree.</p>
    *
    * @param node The node to serialize.
    * @param childrenOnly Whether to serialize the node itself or only its
    *    children. For example, for an element node, this dictates whether a
    *    tag for the element itself is included in the returned value.
    * @param escapeMarkup Whether to replace '&lt;' and '&amp;' with entity references
    *    (&amp;lt;, &amp;amp;) or serialize them literally (&lt;, &amp;).
    * @return The serialized node. If the node is a comment, document type,
    *    entity, notation, or processing instruction node, or if the node is
    *    a text or CDATA node and childrenOnly is true, null is returned.
    */

   public static String serialize(Node node, boolean childrenOnly, boolean escapeMarkup)
   {
      StringBuffer value;
      String       text;
      Node         current, next;

      switch (node.getNodeType())
      {
         case Node.ATTRIBUTE_NODE:
            return (childrenOnly) ? node.getNodeValue() : serializeAttr((Attr)node);

         case Node.CDATA_SECTION_NODE:
         case Node.TEXT_NODE:
            return (childrenOnly) ? null : node.getNodeValue();

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

         case Node.ENTITY_REFERENCE_NODE:
         case Node.DOCUMENT_FRAGMENT_NODE:
         case Node.DOCUMENT_NODE:
            childrenOnly = true;
            break;
      }

      // Build a string from element, text, and CDATA child nodes.

      value = new StringBuffer();

      current = (childrenOnly) ? node.getFirstChild() : node;
      while (current != null)
      {
         // Append the value of the node (if any) to the complete value. We
         // ignore comments and PIs. We "expand" entity references by simply
         // traversing their children.

         switch (current.getNodeType())
         {
            case Node.ELEMENT_NODE:
               value.append(LT);
               value.append(current.getNodeName());
               value.append(serializeAttrs(current.getAttributes()));
               value.append(GT);
               break;

            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
               text = (escapeMarkup) ? escape(current.getNodeValue()) : current.getNodeValue();
               value.append(text);
               break;

            default:
               break;
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
               if (!childrenOnly && (current.getNodeType() == Node.ELEMENT_NODE))
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

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private static String escape(String value)
   {
      StringBuffer newValue = new StringBuffer();
      char[]       chars = value.toCharArray();
      int          start = 0;

      for (int i = 0; i < chars.length; i++)
      {
         switch(chars[i])
         {
            case AMP:
            case LT:
               // When we encounter a character that needs to be escaped as
               // an entity, write out any characters that haven't been written,
               // adjust the save point, and write out the entity reference.
               //
               // Note that we only escape < and &. >, ', and " are all safe in
               // element content.

               newValue.append(chars, start, i - start);
               start = i + 1;

               if (chars[i] == AMP)
               {
                  newValue.append(AMPENTITY);
               }
               else // if (chars[i] == LT)
               {
                  newValue.append(LTENTITY);
               }
            
            default:
               break;
         }
      }
      newValue.append(chars, start, chars.length - start);
      return newValue.toString();
   }

   private static String serializeAttrs(NamedNodeMap attrs)
   {
      StringBuffer value = new StringBuffer();

      for (int i = 0; i < attrs.getLength(); i++)
      {
         value.append(SPACE);
         value.append(serializeAttr((Attr)attrs.item(i)));
      }
      return value.toString();
   }

   private static String serializeAttr(Attr attr)
   {
      StringBuffer value = new StringBuffer();

      value.append(attr.getNodeName());
      value.append(EQUALS);
      value.append(getQuotedValue(attr.getNodeValue()));
      return value.toString();
   }

   private static String getQuotedValue(String value)
   {
      StringBuffer newValue = new StringBuffer();
      char[]       chars;
      int          start;

      // Try to return a value that does not use quot entities.

      if (value.indexOf(QUOT) == -1)
      {
         newValue.append(QUOT);
         newValue.append(value);
         newValue.append(QUOT);
      }
      else if (value.indexOf(APOS) == -1)
      {
         newValue.append(APOS);
         newValue.append(value);
         newValue.append(APOS);
      }
      else
      {
         // Value contains both single and double quotes. Wrap in double quotes
         // and escape any double quotes in the value.

         newValue.append(QUOT);

         chars = value.toCharArray();
         start = 0;
         for (int i = 0; i < chars.length; i++)
         {
            if (chars[i] == QUOT)
            {
               newValue.append(chars, start, i - start);
               newValue.append(QUOTENTITY);
               start = i + 1;
            }
         }
         newValue.append(chars, start, chars.length - start);

         newValue.append(QUOT);
      }
      return newValue.toString();
   }

/*
   /**
    * Clone a Node.
    *
    * @param node Node to clone.
    * @param deep Whether to clone the children or just the node.
    * @return The cloned node.

   private static Node clone(Node node, boolean deep)
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
