// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import org.w3c.dom.Node;

/**
 * Expands a DOM Node whose value is XML markup.
 *
 * <P>This operation takes place in situ -- that is, the value of the
 * DOM Node after expansion is a DOM sub-tree representing the XML markup.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class NodeExpander
{
   /**
	* Expand a DOM Node whose value is XML markup; <B>not yet implemented</P>.
	*
	* @param node The node to expand.
	*/

   static Node expandNode(Node node)
   {
	  // In the future, this needs to parse the text of the input node and
	  // create a tree with node as its root.
	  return node;
   }   
}