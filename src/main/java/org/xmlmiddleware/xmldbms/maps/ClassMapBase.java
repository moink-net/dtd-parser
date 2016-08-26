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
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmlutils.*;

import java.util.*;

/**
 * Base class for ClassMap and InlineClassMap; <a href="../readme.htm#NotForUse">not for
 * general use</a>.
 *
 * <p>ClassMapBase provides accessors for element type name and accessors and
 * mutators for attribute, PCDATA, and child element type maps. It is the base
 * class for ClassMap and InlineClassMap.</p>
 *
 * <p>ClassMapBase is never used directly. It is only used through
 * ClassMap and InlineClassMap.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 * @see ClassMap
 * @see InlineClassMap
 */

public class ClassMapBase extends MapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private XMLName     elementTypeName = null;
   private Hashtable   attributeMaps = new Hashtable();
   private PropertyMap pcdataMap = null;
   private Hashtable   childMaps = new Hashtable(); // For child element types

   // ********************************************************************
   // Constructors
   // ********************************************************************

   ClassMapBase(XMLName elementTypeName)
   {
      this.elementTypeName = elementTypeName;
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Element type name
   // ********************************************************************

   /**
    * Get the name of the element type being mapped.
    *
    * @return The element type name, as an XMLName.
    */
   public final XMLName getElementTypeName()
   {
      return elementTypeName;
   }

   // ********************************************************************
   // Attribute maps
   // ********************************************************************

   /**
    * Get the PropertyMap for an attribute.
    *
    * @param uri Namespace URI of the attribute. May be null.
    * @param localName Local name of the attribute.
    *
    * @return A PropertyMap for the attribute. Null if the attribute
    *  is not mapped.
    */
   public final PropertyMap getAttributeMap(String uri, String localName)
   {
      // create checks if localName is null.

      return (PropertyMap)attributeMaps.get(XMLName.create(uri, localName));
   }

   /**
    * Get the PropertyMap for an attribute.
    *
    * @param attributeName XMLName of the attribute.
    *
    * @return A PropertyMap for the attribute. Null if the attribute
    *  is not mapped.
    */
   public final PropertyMap getAttributeMap(XMLName attributeName)
   {
      checkArgNull(attributeName, ARG_ATTRIBUTENAME);
      return (PropertyMap)attributeMaps.get(attributeName);
   }

   /**
    * Get the PropertyMaps for all attributes.
    *
    * @return An Enumeration of the PropertyMaps for all attributes. May be empty.
    */
   public final Enumeration getAttributeMaps()
   {
      return attributeMaps.elements();
   }

   /**
    * Create a PropertyMap for an attribute and add it to this map.
    *
    * <p>If the attribute has already been mapped, returns the existing PropertyMap.</p>
    *
    * @param uri Namespace URI of the attribute. May be null.
    * @param localName Local name of the attribute.
    *
    * @return The PropertyMap for the attribute.
    */
   public PropertyMap createAttributeMap(String uri, String localName)
   {
      // XMLName.create checks if localName is null.

      return createAttributeMap(XMLName.create(uri, localName));
   }

   /**
    * Create a PropertyMap for an attribute and add it to this map.
    *
    * <p>If the attribute has already been mapped, returns the existing PropertyMap.</p>
    *
    * @param attributeName XMLName of the attribute.
    *
    * @return The PropertyMap for the attribute.
    */
   public PropertyMap createAttributeMap(XMLName attributeName)
   {
      PropertyMap propMap;

      checkArgNull(attributeName, ARG_ATTRIBUTENAME);
      propMap = (PropertyMap)attributeMaps.get(attributeName);
      if (propMap == null)
      {
         propMap = PropertyMap.create(attributeName, PropertyMap.ATTRIBUTE);
         attributeMaps.put(attributeName, propMap);
      }
      return propMap;
   }

   /**
    * Add a PropertyMap for an attribute.
    *
    * @param propMap PropertyMap for the attribute. Must not be null.
    * @exception XMLMiddlewareException Thrown if the attribute has already been mapped.
    */
   public void addAttributeMap(PropertyMap propMap)
      throws XMLMiddlewareException
   {
      Object  o;
      XMLName attributeName;

      checkArgNull(propMap, ARG_PROPMAP);
      if (propMap.getType() != PropertyMap.ATTRIBUTE)
         throw new IllegalArgumentException("PropertyMap does not map an attribute.");

      attributeName = propMap.getXMLName();
      o = attributeMaps.get(attributeName);
      if (o != null)
         throw new XMLMiddlewareException("Attribute " + attributeName.getUniversalName() + " already mapped for element type " + elementTypeName.getUniversalName() + ".");
      attributeMaps.put(attributeName, propMap);
   }

   /**
    * Remove the PropertyMap for an attribute.
    *
    * @param uri Namespace URI of the attribute. May be null.
    * @param localName Local name of the attribute.
    *
    * @exception XMLMiddlewareException Thrown if the attribute has not been mapped.
    */
   public void removeAttributeMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      // create checks if localName is null.

      removeAttributeMap(XMLName.create(uri, localName));
   }

   /**
    * Remove the PropertyMap for an attribute.
    *
    * @param attributeName XMLName of the attribute.
    *
    * @exception XMLMiddlewareException Thrown if the attribute has not been mapped.
    */
   public void removeAttributeMap(XMLName attributeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(attributeName, ARG_ATTRIBUTENAME);
      o = attributeMaps.remove(attributeName);
      if (o == null)
         throw new XMLMiddlewareException("Attribute " + attributeName.getUniversalName() + " not mapped for element type " + elementTypeName.getUniversalName() + ".");
   }

   /**
    * Remove the PropertyMaps for all attributes.
    */
   public void removeAllAttributeMaps()
   {
      attributeMaps.clear();
   }

   // ********************************************************************
   // PCDATA map
   // ********************************************************************

   /**
    * Get the PropertyMap for PCDATA.
    *
    * @return A PropertyMap for PCDATA. Null if PCDATA is not mapped.
    */
   public final PropertyMap getPCDATAMap()
   {
      return pcdataMap;
   }

   /**
    * Create a new PropertyMap for PCDATA.
    *
    * <p>If PCDATA has already been mapped, returns the existing PropertyMap.</p>
    *
    * @return A PropertyMap for PCDATA.
    */
   public PropertyMap createPCDATAMap()
   {
      PropertyMap propMap;

      if (pcdataMap == null)
      {
         pcdataMap = PropertyMap.create(null, null, PropertyMap.PCDATA);
      }
      return pcdataMap;
   }

   /**
    * Add a PropertyMap for PCDATA.
    *
    * @param propMap PropertyMap for PCDATA. Must not be null.
    * @exception XMLMiddlewareException Thrown if PCDATA has already been mapped.
    */
   public void addPCDATAMap(PropertyMap propMap)
      throws XMLMiddlewareException
   {
      checkArgNull(propMap, ARG_PROPMAP);
      if (propMap.getType() != PropertyMap.PCDATA)
         throw new IllegalArgumentException("PropertyMap does not map PCDATA.");
      if (pcdataMap != null)
         throw new XMLMiddlewareException("PCDATA already mapped for " + elementTypeName.getUniversalName() + ".");
      pcdataMap = propMap;
   }

   /**
    * Remove the PropertyMap for PCDATA.
    *
    * @exception XMLMiddlewareException Thrown if PCDATA has not been mapped.
    */
   public void removePCDATAMap()
      throws XMLMiddlewareException
   {
      if (pcdataMap == null)
         throw new XMLMiddlewareException("PCDATA not mapped for " + elementTypeName.getUniversalName() + ".");
      pcdataMap = null;
   }

   // ********************************************************************
   // Child element type maps
   // ********************************************************************

   /**
    * Get the map for a child element type.
    *
    * <p>Returns a PropertyMap, RelatedClassMap, or InlineClassMap, depending
    * on how the child element type is mapped. The calling method must determine
    * the class of the returned Object.</p>
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return The returned map (as an Object) or null if the child element type
    *    is not mapped.
    */
   public final Object getChildMap(String uri, String localName)
   {
      // create checks if localName is null.

      return childMaps.get(XMLName.create(uri, localName));
   }

   /**
    * Get the map for a child element type.
    *
    * <p>Returns a PropertyMap, RelatedClassMap, or InlineClassMap, depending
    * on how the child element type is mapped. The calling method must determine
    * the class of the returned Object.</p>
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @return The returned map (as an Object) or null if the child element type
    *    is not mapped.
    */
   public final Object getChildMap(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      return childMaps.get(elementTypeName);
   }

   /**
    * Get the maps for all element types.
    *
    * <p>The Enumeration can contain PropertyMaps, RelatedClassMaps, and InlineClassMaps.
    * The calling method must determine the class of Objects returned by the Enumeration.</p>
    *
    * @return An Enumeration of the maps for all child element types. May be empty.
    */
   public final Enumeration getChildMaps()
   {
      return childMaps.elements();
   }

   /**
    * Create a PropertyMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as a property, returns the
    * existing PropertyMap.</p>
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return The PropertyMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a related class or inlined class.
    */
   public PropertyMap createChildPropertyMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      return createChildPropertyMap(XMLName.create(uri, localName));
   }

   /**
    * Create a PropertyMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as a property, returns the
    * existing PropertyMap.</p>
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @return The PropertyMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a related class or inlined class.
    */
   public PropertyMap createChildPropertyMap(XMLName elementTypeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      o = childMaps.get(elementTypeName);
      if (o == null)
      {
         o = PropertyMap.create(elementTypeName, PropertyMap.ELEMENTTYPE);
         childMaps.put(elementTypeName, o);
         return (PropertyMap)o;
      }
      else if (o instanceof PropertyMap)
      {
         return (PropertyMap)o;
      }
      else if (o instanceof RelatedClassMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as a related class of element type " + this.elementTypeName.getUniversalName() + ".");
      else // if (o instanceof InlineClassMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as an inlined child class of element type " + this.elementTypeName.getUniversalName() + ".");
   }

   /**
    * Create a RelatedClassMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as a related class, returns the
    * existing RelatedClassMap.</p>
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return The RelatedClassMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a property or inlined class.
    */
   public RelatedClassMap createRelatedClassMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      return createRelatedClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create a RelatedClassMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as a related class, returns the
    * existing RelatedClassMap.</p>
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @return The RelatedClassMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a property or inlined class.
    */
   public RelatedClassMap createRelatedClassMap(XMLName elementTypeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      o = childMaps.get(elementTypeName);
      if (o == null)
      {
         o = RelatedClassMap.create(elementTypeName);
         childMaps.put(elementTypeName, o);
         return (RelatedClassMap)o;
      }
      else if (o instanceof RelatedClassMap)
      {
         return (RelatedClassMap)o;
      }
      else if (o instanceof PropertyMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as a property of element type " + this.elementTypeName.getUniversalName() + ".");
      else // if (o instanceof InlineClassMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as an inlined child class of element type " + this.elementTypeName.getUniversalName() + ".");
   }

   /**
    * Create an InlineClassMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as an inlined class, returns the
    * existing InlineClassMap.</p>
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return The InlineClassMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a property or related class.
    */
   public InlineClassMap createInlineClassMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      return createInlineClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create an InlineClassMap for a child element type and add it to this map.
    *
    * <p>If the child element type has already been mapped as an inlined class, returns the
    * existing InlineClassMap.</p>
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @return The InlineClassMap for the child element type.
    * @exception XMLMiddlewareException Thrown if the child element type is already
    *    mapped as a property or related class.
    */
   public InlineClassMap createInlineClassMap(XMLName elementTypeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      o = childMaps.get(elementTypeName);
      if (o == null)
      {
         o = InlineClassMap.create(elementTypeName);
         childMaps.put(elementTypeName, o);
         return (InlineClassMap)o;
      }
      else if (o instanceof InlineClassMap)
      {
         return (InlineClassMap)o;
      }
      else if (o instanceof PropertyMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as a property of element type " + this.elementTypeName.getUniversalName() + ".");
      else // if (o instanceof RelatedClassMap)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped as a related child class of element type " + this.elementTypeName.getUniversalName() + ".");
   }

   /**
    * Add a PropertyMap for a child element type.
    *
    * @param propMap PropertyMap for the child element type. Must not be null.
    * @exception XMLMiddlewareException Thrown if the child element type has already been mapped.
    */
   public void addChildMap(PropertyMap propMap)
      throws XMLMiddlewareException
   {
      Object  o;
      XMLName elementTypeName;

      checkArgNull(propMap, ARG_PROPMAP);
      if (propMap.getType() != PropertyMap.ELEMENTTYPE)
         throw new IllegalArgumentException("PropertyMap does not map an element type.");

      elementTypeName = propMap.getXMLName();
      o = childMaps.get(elementTypeName);
      if (o != null)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped for element type " + this.elementTypeName.getUniversalName() + ".");
      childMaps.put(elementTypeName, propMap);
   }

   /**
    * Add a RelatedClassMap for a child element type.
    *
    * <p>The RelatedClassMap is added under the name returned by
    * RelatedClassMap.getElementTypeName(). This may be different from the name returned
    * by RelatedClassMap.getClassMap().getElementTypeName(). That is, the referenced
    * element type might be mapped using the ClassMap for another element type. This
    * effectively "casts" the reference to the second element type. For more
    * information, see the description of the &lt;UseClassMap> element type in the
    * XML-DBMS mapping language.</p>
    *
    * @param relatedClassMap RelatedClassMap for the child element type. Must not be null.
    * @exception XMLMiddlewareException Thrown if the child element type has already been mapped.
    */
   public void addChildMap(RelatedClassMap relatedClassMap)
      throws XMLMiddlewareException
   {
      Object  o;
      XMLName elementTypeName;

      checkArgNull(relatedClassMap, ARG_RELATEDCLASSMAP);

      // Get the name of the element type being mapped and check if it has already
      // been mapped. If not, add the RelatedClassMap.

      elementTypeName = relatedClassMap.getElementTypeName();
      o = childMaps.get(elementTypeName);
      if (o != null)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped for element type " + this.elementTypeName.getUniversalName() + ".");
      childMaps.put(elementTypeName, relatedClassMap);
   }

   /**
    * Add an InlineClassMap for a child element type.
    *
    * @param inlineClassMap InlineClassMap for the child element type. Must not be null.
    * @exception XMLMiddlewareException Throw if the child element type is already mapped.
    */
   public void addChildMap(InlineClassMap inlineClassMap)
      throws XMLMiddlewareException
   {
      Object  o;
      XMLName elementTypeName;

      checkArgNull(inlineClassMap, ARG_INLINECLASSMAP);

      elementTypeName = inlineClassMap.getElementTypeName();
      o = childMaps.get(elementTypeName);
      if (o != null)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " already mapped for element type " + this.elementTypeName.getUniversalName() + ".");
      childMaps.put(elementTypeName, inlineClassMap);
   }

   /**
    * Remove the map for a child element type.
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @exception XMLMiddlewareException Thrown if the child element type has not been mapped.
    */
   public void removeChildMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      // create checks if localName is null.

      removeChildMap(XMLName.create(uri, localName));
   }

   /**
    * Remove the map for a child element type.
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @exception XMLMiddlewareException Thrown if the child element type has not been mapped.
    */
   public void removeChildMap(XMLName elementTypeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      o = childMaps.remove(elementTypeName);
      if (o == null)
         throw new XMLMiddlewareException("Child element type " + elementTypeName.getUniversalName() + " not mapped for element type " + this.elementTypeName.getUniversalName() + ".");
   }

   /**
    * Remove the maps for all child element types.
    */
   public void removeAllChildMaps()
   {
      childMaps.clear();
   }
}
