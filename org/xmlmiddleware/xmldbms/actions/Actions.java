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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.actions;

import org.xmlmiddleware.utils.XMLName;
import org.xmlmiddleware.xmldbms.maps.ClassMap;
import org.xmlmiddleware.xmldbms.maps.Map;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A set of Actions to be taken on an XML document.
 *
 * <p>The normal way to construct an Actions object is to write an XML
 * document using the action language (actions.dtd), then compile it into
 * an Actions object. This is then passed to DOMToDBMS. For example:</p>
 *
 * <pre>
 *    // Compile an actions document.
 *    compiler = new ActionCompiler(parserUtils);
 *    actions = compiler.compile(map, new InputSource(new FileReader("salesactions.act")));
 *
 *    // Use a user-defined function to create a DOM tree over sales_in.xml
 *    doc = openDocument("sales_in.xml");
 *
 *    // Process the document according to the actions.
 *    domToDBMS.processDocument(transferInfo, doc, actions);
 * </pre>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class Actions
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private Hashtable actions;
   private Map       map;
   private Action    defaultAction = null;

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new Actions object.
    *
    * @param map The Map to which the actions apply.
    */
   public Actions(Map map)
   {
      actions = new Hashtable();
      this.map = map;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   /**
    * Get the default action for a document.
    *
    * @return The default action or null if no default action was found.
    */
   public final Action getDefaultAction()
   {
      return defaultAction;
   }

   /**
    * Set the default action for a document.
    *
    * @param defaultAction The default action. Null if there is no default action.
    */
   public void setDefaultAction(Action defaultAction)
   {
      this.defaultAction = defaultAction;
   }

   /**
    * Get the action for an element type mapped as a class.
    *
    * @param elementTypeName The element type name. Must not be null.
    * @return The action or null if no action was found.
    */
   public final Action getAction(XMLName elementTypeName)
   {
      return (Action)actions.get(elementTypeName);
   }

   /**
    * Get the action for an element type mapped as a class.
    *
    * @param uri The uri of the element type.
    * @param localName The local name of the element type. Must not be null.
    * @return The action or null if no action was found.
    */
   public final Action getAction(String uri, String localName)
   {
      return (Action)actions.get(XMLName.create(uri, localName));
   }

   /**
    * Gets an Enumeration of all actions.
    *
    * @return The Enumeration. May be empty.
    */
   public final Enumeration getActions()
   {
      return actions.elements();
   }

   /**
    * Create an action for an element type and add it to the set of actions.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type. Must not be null.
    *
    * @return The action for the element type.
    * @exception IllegalArgumentException Thrown if an action has already been
    *    created for the element type.
    */
   public Action createAction(String uri, String localName)
   {
      return createAction(XMLName.create(uri, localName));
   }

   /**
    * Create an action for an element type and add it to the set of actions.
    *
    * @param elementTypeName The element type name. Must not be null.
    *
    * @return The action for the element type.
    * @exception IllegalArgumentException Thrown if an action has already been
    *    created for the element type or the element type is not mapped as a class.
    */
   public Action createAction(XMLName elementTypeName)
   {
      Action   action;
      ClassMap classMap;

      if (elementTypeName == null)
         throw new IllegalArgumentException("Element type name may not be null.");

      // Check if an action has already been added for this element type.

      action = getAction(elementTypeName);
      if (action != null)
         throw new IllegalArgumentException("Action already created for " + elementTypeName.getUniversalName());

      // Check that the element type is mapped as a class.

      classMap = map.getClassMap(elementTypeName.getUniversalName());
      if (classMap == null)
         throw new IllegalArgumentException("Element type not mapped as a class: " + elementTypeName.getUniversalName());

      // Create the new Action, add it to the hashtable, and return it.

      action = new Action(elementTypeName, classMap);
      actions.put(elementTypeName, action);
      return action;
   }

   /**
    * Remove the action for an element type name.
    *
    * @param elementTypeName The element type name. Must not be null.
    *
    * @exception IllegalArgumentException Thrown if no action has been specified
    *    for the element type.
    */
   public void removeAction(XMLName elementTypeName)
      throws IllegalArgumentException
   {
      Object o;

      o = actions.remove(elementTypeName);
      if (o == null)
         throw new IllegalArgumentException("No action specified for element type " + elementTypeName.getUniversalName());
   }

   /**
    * Remove the action for an element type name.
    *
    * @param uri The uri of the element type.
    * @param localName The local name of the element type. Must not be null.
    *
    * @exception IllegalArgumentException Thrown if no action has been specified
    *    for the element type.
    */
   public void removeAction(String uri, String localName)
      throws IllegalArgumentException
   {
      removeAction(XMLName.create(uri, localName));
   }

   /**
    * Remove all actions.
    */
   public void removeAllActions()
   {
      actions.clear();
   }
}