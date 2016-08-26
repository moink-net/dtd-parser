// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, 2001

// The tree traversal routine was adapted from code
// written by John Cowan.

// Version 1.1
// Changes from version 1.0: New in version 1.1

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.*;

/**
 * Base class for some ParserUtils implementations.
 *
 * <p>This class provides a method for serializing a DOM document
 * that contains only elements, attributes, and text nodes to a string.</p>
 *
 * @author Ronald Bourret
 * @version 1.1
 */

public class ParserUtilsBase
{
   private static String AMPENTITY    = "&amp;",
                         LTENTITY     = "&lt;",
                         GTENTITY     = "&gt;",
                         APOSENTITY   = "&apos;",
                         QUOTENTITY   = "&quot;";

   /**
    * Return a Document as a string.
    *
    * <p>This method can only handle Element, Attr, and Text nodes.</p>
    *
    * @param doc The Document.
    *
    * @return The Document serialized as a string.
    */
   public String serializeDocument(Document doc)
   {
      return traverse(doc.getDocumentElement());
   }

   private String traverse(Node node)
   {
      StringBuffer buf = new StringBuffer();
      Node currentNode = node;

      while (currentNode != null)
      {
         visit(currentNode, buf);

         // Move down to first child
         Node nextNode = currentNode.getFirstChild();
         if (nextNode != null)
         {
            currentNode = nextNode;
            continue;
         }

         // No child nodes, so walk tree
         while (currentNode != null)
         {
            // do end-of-node processing, if any
            revisit(currentNode, buf);

            // Move to sibling if possible.
            nextNode = currentNode.getNextSibling();
            if (nextNode != null)
            {
               currentNode = nextNode;
               break;
            }

            // Move up
            if (currentNode == node)
               currentNode = null;
            else
               currentNode = currentNode.getParentNode();
         }
      }
      return buf.toString();
   }

   private void visit(Node node, StringBuffer buf)
   {
      int type = node.getNodeType();

      //Node.ELEMENT, Node.TEXT
      switch (type)
      {
         case Node.ELEMENT_NODE:
            buf.append("<");
            buf.append(node.getNodeName());
            processAttributes(node, buf);
            buf.append(">");
            break;

         case Node.TEXT_NODE:
            buf.append(replaceEntityChars(node.getNodeValue()));
            break;

         default:
            break;
      }
   }

   private void processAttributes(Node elem, StringBuffer buf)
   {
      NamedNodeMap attrs = elem.getAttributes();

      for (int i = 0; i < attrs.getLength(); i++)
      {
          Node attr = attrs.item(i);
          buf.append(" '");
          buf.append(replaceEntityChars(attr.getNodeValue()));
          buf.append("'");
      }
   }

   private void revisit(Node node, StringBuffer buf)
   {
      int type = node.getNodeType();
      switch (type)
      {
         case Node.ELEMENT_NODE:
            buf.append("</");
            buf.append(node.getNodeName());
            buf.append(">");
            break;

         default:
            break;
      }
   }

   private String replaceEntityChars(String s)
   {
      StringBuffer sb = new StringBuffer();
      int          save;
      char[]       buf;

      buf = s.toCharArray();

      save = 0;
      for (int i = 0; i < buf.length; i++)
      {
         switch(buf[i])
         {
            case '&':
            case '<':
            case '>':
            case '\'':
            case '"':
               // When we encounter a character that needs to be
               // escaped as an entity, write out any characters
               // that haven't been written, adjust the save point,
               // and write out the entity reference.

               if (save < i)
               {
                  sb.append(buf, save, i - save);
               }
               save = i + 1;

               switch(buf[i])
               {
                  case '&':
                     sb.append(AMPENTITY);
                     break;
            
                  case '<':
                     sb.append(LTENTITY);
                     break;
            
                  case '>':
                     sb.append(GTENTITY);
                     break;
            
                  case '\'':
                     sb.append(APOSENTITY);
                     break;
            
                  case '"':
                     sb.append(QUOTENTITY);
                     break;
               }
            
            default:
               break;
         }
      }

      // If there are any characters that haven't yet been
      // written, write them out now.
      
      if (save < buf.length)
      {
         sb.append(buf, save, buf.length - save);
      }

      return sb.toString();
   }
}
