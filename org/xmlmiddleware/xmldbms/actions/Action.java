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
import org.xmlmiddleware.xmldbms.maps.Column;
import org.xmlmiddleware.xmldbms.maps.PropertyMap;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An action to take on an element type mapped as a class. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class Action
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private XMLName       elementTypeName;
   private ClassMap      classMap;
   private int           action = NONE;
   private Hashtable     updateElements = new Hashtable();
   private Hashtable     updateAttrs = new Hashtable();
   private PropertyMap   pcdataMap = null;
   private Vector        updatePropMaps = null;

   //*********************************************************************
   // Constants
   //*********************************************************************

   /** Do nothing. */
   public static final int NONE           = 0;

   /** Insert a row. If the row already exists, throw an exception. */
   public static final int INSERT         = 1;

   /** Insert a row if it does not already exist. */
   public static final int SOFTINSERT     = 2;

   /** Update a row. If the row does not exist, throw an exception. */
   public static final int UPDATE         = 3;

   /** Update a row. If the row does not exist, insert it. */
   public static final int UPDATEORINSERT = 4;

   /** Delete a row. If the row does not exist, throw an exception. */
   public static final int DELETE         = 5;

   /** Delete a row if it exists, otherwise do nothing. */
   public static final int SOFTDELETE     = 6;

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new Action object.
    *
    * @param elementTypeName The name of the element type to which the action applies.
    * @param classMap The ClassMap that maps the element type.
    */
   protected Action(XMLName elementTypeName, ClassMap classMap)
   {
      if (elementTypeName == null)
         throw new IllegalArgumentException("Element type name must not be null.");
      if (classMap == null)
         throw new IllegalArgumentException("Class map must not be null.");

      this.elementTypeName = elementTypeName;
      this.classMap = classMap;
   }

   /**
    * Construct a default Action object.
    */
   public Action()
   {
      this.elementTypeName = null;
      this.classMap = null;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   /**
    * Get the name of the element type to which the action applies.
    *
    * @return The element type name. Returns null if this is the default action.
    */
   public final XMLName getElementTypeName()
   {
      return elementTypeName;
   }

   /**
    * Get the action to be taken for the element type.
    *
    * @return The action. One of NONE, INSERT, SOFTINSERT, UPDATE, UPDATEORINSERT,
    *    DELETE, or SOFTDELETE.
    */
   public final int getAction()
   {
      return action;
   }

   /**
    * Set the action to be taken for the element type.
    *
    * @param action The action. One of NONE, INSERT, SOFTINSERT, UPDATE, UPDATEORINSERT,
    *    DELETE, or SOFTDELETE.
    */
   public void setAction(int action)
   {
      switch(action)
      {
         case NONE:
         case INSERT:
         case SOFTINSERT:
         case UPDATE:
         case UPDATEORINSERT:
         case DELETE:
         case SOFTDELETE:
            this.action = action;
            break;

         default:
            throw new IllegalArgumentException("Action must be one of NONE, INSERT, SOFTINSERT, UPDATE, UPDATEORINSERT, DELETE, or SOFTDELETE.");
      }

      if (action != UPDATE)
      {
         removeAllUpdateProperties();
      }
   }

   /**
    * Get the names of child elements of the class element type to be updated.
    *
    * @return An Enumeration of XMLNames
    */
   public final Enumeration getUpdateElementTypeNames()
   {
      return updateElements.keys();
   }

   /**
    * Get the names of attributes of the class element type to be updated.
    *
    * @return An Enumeration of XMLNames
    */
   public final Enumeration getUpdateAttributeNames()
   {
      return updateAttrs.keys();
   }

   /**
    * Whether PCDATA is to be updated for the class element type.
    *
    * @return Whether PCDATA is updated
    */
   public final boolean isPCDATAUpdated()
   {
      return (pcdataMap != null);
   }

   public final Vector getUpdatePropertyMaps()
   {
      Enumeration e;

      if (updatePropMaps == null)
      {
         if ((updateElements.size() != 0) || (updateAttrs.size() != 0) || (pcdataMap != null))
         {
            updatePropMaps = new Vector();

            e = updateElements.elements();
            while (e.hasMoreElements())
            {
               updatePropMaps.addElement((PropertyMap)e.nextElement());
            }

            e = updateAttrs.elements();
            while (e.hasMoreElements())
            {
               updatePropMaps.addElement((PropertyMap)e.nextElement());
            }

            if (pcdataMap != null)
            {
               updatePropMaps.addElement(pcdataMap);
            }
         }
         else
         {
//            ??? return all property maps ???
         }
      }
      return updatePropMaps;
   }

   /**
    * Add a property to the update list.
    *
    * @param uri URI of the property. Null for PCDATA.
    * @param localName Local name of the property. Null for PCDATA.
    * @param type PropertyMap.ELEMENTTYPE, .ATTRIBUTE, or .PCDATA
    */
   public void setUpdateProperty(String uri, String localName, int type)
   {
      setUpdateProperty(XMLName.create(uri, localName), type);
   }

   /**
    * Add a property to the update list.
    *
    * @param propName XMLName of the property. Null for PCDATA.
    * @param type PropertyMap.ELEMENTTYPE, .ATTRIBUTE, or .PCDATA
    */
   public void setUpdateProperty(XMLName propName, int type)
   {
      Object o;

      if (classMap == null)
         throw new IllegalStateException("Cannot set update properties for the default action.");

      switch (type)
      {
         case PropertyMap.ELEMENTTYPE:
            o = classMap.getChildMap(propName.getUniversalName());
            if (o != null)
            {
               if (o instanceof PropertyMap)
               {
                  updateElements.put(propName, o);
                  break;
               }
            }
            throw new IllegalArgumentException(propName.getUniversalName() + " not mapped as a property of " + elementTypeName.getUniversalName());

         case PropertyMap.ATTRIBUTE:
            o = classMap.getAttributeMap(propName.getUniversalName());
            if (o == null)
               throw new IllegalArgumentException(propName.getUniversalName() + " not mapped as a property of " + elementTypeName.getUniversalName());
            updateAttrs.put(propName, o);
            break;

         case PropertyMap.PCDATA:
            o = classMap.getPCDATAMap();
            if (o == null)
               throw new IllegalArgumentException("PCDATA not mapped as a property of " + elementTypeName.getUniversalName());
            pcdataMap = (PropertyMap)o;
            break;

         default:
            throw new IllegalArgumentException("Invalid type: " + type);
      }
      updatePropMaps = null;
   }

   /**
    * Remove a property from the update list.
    *
    * @param uri URI of the property. Null for PCDATA.
    * @param localName Local name of the property. Null for PCDATA.
    * @param type PropertyMap.ELEMENTTYPE, .ATTRIBUTE, or .PCDATA
    */
   public void removeUpdateProperty(String uri, String localName, int type)
   {
      removeUpdateProperty(XMLName.create(uri, localName), type);
   }

   /**
    * Remove a property from the update list.
    *
    * @param propName XMLName of the property. Null for PCDATA.
    * @param type PropertyMap.ELEMENTTYPE, .ATTRIBUTE, or .PCDATA
    */
   public void removeUpdateProperty(XMLName propName, int type)
   {
      Object o;

      if (classMap == null)
         throw new IllegalStateException("Cannot remove update properties for the default action.");

      switch (type)
      {
         case PropertyMap.ELEMENTTYPE:
            o = updateElements.remove(propName);
            if (o == null)
               throw new IllegalArgumentException(propName.getUniversalName() + " not an update property.");
            break;

         case PropertyMap.ATTRIBUTE:
            o = updateAttrs.remove(propName);
            if (o == null)
               throw new IllegalArgumentException(propName.getUniversalName() + " not an update property.");
            break;

         case PropertyMap.PCDATA:
            if (pcdataMap == null)
               throw new IllegalArgumentException("PCDATA not an update property.");
            pcdataMap = null;
            break;

         default:
            throw new IllegalArgumentException("Invalid type: " + type);
      }
      updatePropMaps = null;
   }

   /**
    * Remove all properties from the update list.
    */
   public void removeAllUpdateProperties()
   {
      updateElements.clear();
      updateAttrs.clear();
      pcdataMap = null;
      updatePropMaps = null;
   }
}