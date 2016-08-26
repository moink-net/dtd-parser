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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.schemas.dtds.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmlutils.*;

import java.sql.*;
import java.util.*;

/**
 * Generate a DTD object.
 *
 * <p>This class generates a DTD object capable of representing documents
 * that can use the map. The DTD is generated as follows:</p>
 *
 * <ul>
 * <li><p>For each ClassMap, an element type is generated. If a ClassMap does not
 *        have a property map for PCDATA but does have property maps, related class
 *        maps, or inline maps for child elements, the content model is a sequence.
 *        Otherwise, empty, mixed, or PCDATA-only content models are used as
 *        appropriate.</p></li>
 * <li><p>For each PropertyMap for a child element, a reference to that element type
 *        is added to the content model. If the PropertyMap uses a property table, the
 *        * modifier is added to the reference.</p></li>
 * <li><p>For each PropertyMap for an attribute, an attribute is created. The type
 *        is CDATA unless the TokenList attribute is of the PropertyMap is set, in
 *        which case the type is NMTOKENS.</p></li>
 * <li><p>For each PropertyMap for PCDATA, PCDATA is added to the content model. If
 *        the content model also contains element references, it is made into a mixed
 *        content model.</p></li>
 * <li><p>The ContainsXML attribute of PropertyMap cannot be handled correctly, as
 *        there is no way to know what children the referenced element type has.</p></li>
 * <li><p>For each InlineMap, a reference to the inlined element type is added to
 *        the content model. The element type is then processed under the same rules
 *        as element types mapped using ClassMap.</p></li>
 * <li><p>For each RelatedClass, a reference to the element type is added to the
 *        content model. If the unique key is in the parent, the * modifier is added
 *        to the reference.</p></li>
 * <li><p>FixedOrder, when present, is used to specify the order of references to
 *        child elements in the content model.</p></li>
 * <li><p>Extends and UseClassMap cause exceptions to be thrown, as these model
 *        structures not handled by DTDs.</p></li>
 * </ul>
 *
 * @author Ronald Bourret, 2002
 * @version 2.0
 */

public class DTDGenerator
{
   //**************************************************************************
   // Class variables
   //**************************************************************************

   private DTD        dtd;
   private XMLDBMSMap map;
   private Hashtable  order = new Hashtable(); // Indexed by element type name

   //**************************************************************************
   // Constants
   //**************************************************************************

   private final static Long MAXLONG = new Long(Long.MAX_VALUE);

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DTDGenerator.
    *
    */
   public DTDGenerator()
   {
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Gets a DTD that can be used with instance documents that use the map.
    *
    * @param map The map
    * @return A DTD object
    * @exception XMLMiddlewareException Thrown if the XMLDBMSMap needs structures
    *    that cannot be represented in a DTD.
    */
   public DTD getDTD(XMLDBMSMap map)
      throws XMLMiddlewareException
   {
      // Initialize the global variables.

      dtd = new DTD();
      this.map = map;
      order.clear();

      // Process the class maps and sort the content models of element types
      // with element content.

      processClassMaps();
      sortContentModels();

      // Return the new DTD.

      return dtd;
   }

   //**************************************************************************
   // Private methods -- main methods
   //**************************************************************************

   private void processClassMaps()
      throws XMLMiddlewareException
   {
      Enumeration classMaps;
      ClassMap    classMap;

      classMaps = map.getClassMaps();
      while (classMaps.hasMoreElements())
      {
         classMap = (ClassMap)classMaps.nextElement();
         processClassMapBase(classMap);
      }
   }

   private void processClassMapBase(ClassMapBase classMapBase)
      throws XMLMiddlewareException
   {
      // This method processes both ClassMaps and InlineClassMaps.

      ElementType elementType;
      PropertyMap pcdataMap;
      Enumeration childMaps, attrMaps;

      // Check for mappings that require structures not supported by DTD.

      if (classMapBase instanceof ClassMap)
      {
         checkUnhandledStructures((ClassMap)classMapBase);
      }

      // Create a new ElementType or get an existing one.

      elementType = dtd.createElementType(classMapBase.getElementTypeName());

      // Get the child element maps and set the content type.

      pcdataMap = classMapBase.getPCDATAMap();
      childMaps = classMapBase.getChildMaps();
      setContentType(elementType, pcdataMap, childMaps);

      // Process the child maps (PropertyMaps, RelatedClassMaps, and InlineClassMaps).

      processChildMaps(elementType, childMaps);

      // Process the attribute maps

      attrMaps = classMapBase.getAttributeMaps();
      processAttributeMaps(elementType, attrMaps);
   }

   private void processChildMaps(ElementType parent, Enumeration childMaps)
      throws XMLMiddlewareException
   {
      Object childMap;

      while (childMaps.hasMoreElements())
      {
         childMap = childMaps.nextElement();
         if (childMap instanceof PropertyMap)
         {
            processPropertyMap(parent, (PropertyMap)childMap);
         }
         else if (childMap instanceof RelatedClassMap)
         {
            processRelatedClassMap(parent, (RelatedClassMap)childMap);
         }
         else // if (childMap instanceof InlineClassMap)
         {
            processInlineClassMap(parent, (InlineClassMap)childMap);
         }
      }
   }

   private void processPropertyMap(ElementType parent, PropertyMap propMap)
   {
      XMLName     elementTypeName;
      ElementType elementType;
      Reference   reference;

      // Create/get the specified element type.

      elementTypeName = propMap.getXMLName();
      elementType = dtd.createElementType(elementTypeName);

      // Set the content type. Note that the element type might already have
      // been created. This is especially true if its content is PCDATA-only
      // and is mapped as a property of a different class element type, but
      // it could also be true in silly cases such as being mapped beneath one
      // class element type as a related class and beneath this class element
      // type as a property.

      switch (elementType.contentType)
      {
         case ElementType.CONTENT_UNKNOWN:
            // This is the most common case. Assume that the content is PCDATA.

            elementType.contentType = ElementType.CONTENT_PCDATA;
            break;

         case ElementType.CONTENT_PCDATA:
            // Another common case -- the element type is mapped as a property
            // of multiple class element types. Nothing to do here.

            break;

         case ElementType.CONTENT_EMPTY:
            // Assume that the element type is mapped elsewhere as a related
            // class and, in that case, only has attributes. Since it is mapped
            // here as a property and property elements ignore attributes, assume
            // that it in fact has useful PCDATA content and set the model accordingly.

            elementType.contentType = ElementType.CONTENT_PCDATA;
            break;

         case ElementType.CONTENT_ELEMENT:
            // Assume that the element type is mapped elsewhere as a related
            // class and, in that case, has child elements. Assume that in this
            // case, the user has chosen to serialize the contents as XML and treat
            // them like a property. Nothing to do here, since it would be a poor
            // assumption to assume that the contents also include PCDATA (are mixed).

            break;

         case ElementType.CONTENT_MIXED:
            // Assume that the element type is mapped elsewhere as a related
            // class and, in that case, has mixed content. Assume that in this
            // case, the user has chosen to serialize the contents as XML and treat
            // them like a property. Nothing to do here.

            break;
      }

      // Add the element type to the hashtable of children in the parent.

      parent.children.put(elementTypeName, elementType);

      // Add a reference to the element type to the content model of its parent.

      reference = new Reference(elementType);
      parent.content.members.addElement(reference);

      // If the property is stored in a separate table, this means that either
      // the reference is optional and repeatable (*) or the referenced element
      // type contains a token list. If the property is stored in the class table,
      // determine whether it is optional (?) from the nullability of the column.
      // Note that this logic only applies to element content; in mixed content,
      // all references are required and singleton.

      if (parent.contentType == ElementType.CONTENT_ELEMENT)
      {
         if (propMap.getTable() != null)
         {
            if (!propMap.isTokenList())
            {
               reference.isRepeatable = true;
               reference.isRequired = false;
            }
         }
         else
         {
            if (propMap.getColumn().getNullability() == DatabaseMetaData.columnNullable)
            {
               reference.isRequired = false;
            }
         }
      }

      // If the parent has element content, add the order value for the property.

      addOrderValue(parent, elementTypeName, propMap.getOrderInfo());

      // Add the parent of the element type to the hashtable of parents.

      elementType.parents.put(parent.name, parent);
   }

   private void processRelatedClassMap(ElementType parent, RelatedClassMap relatedClassMap)
   {
      XMLName     elementTypeName;
      ElementType elementType;
      Reference   reference;

      // For a related class map, add a reference to the content model of the parent.

      elementTypeName = relatedClassMap.getElementTypeName();
      elementType = dtd.createElementType(elementTypeName);
      reference = new Reference(elementType);
      parent.content.members.addElement(reference);

      // If the parent has element content, then the cardinality of the reference depends
      // on who has the primary key. If the parent table has the primary key, the reference
      // is optional and repeatable. If the child table has the primary key, the reference
      // is optional but not repeatable.

      if (parent.contentType == ElementType.CONTENT_ELEMENT)
      {
         reference.isRequired = false;
         reference.isRepeatable = relatedClassMap.getLinkInfo().parentKeyIsUnique();
      }

      // If the parent has element content, add the order value for the related class.

      addOrderValue(parent, elementTypeName, relatedClassMap.getOrderInfo());
   }

   private void processInlineClassMap(ElementType parent, InlineClassMap inlineClassMap)
      throws XMLMiddlewareException
   {
      XMLName     elementTypeName;
      ElementType elementType;
      Reference   reference;

      // For an inline class map, add a reference to the content model of the parent.

      elementTypeName = inlineClassMap.getElementTypeName();
      elementType = dtd.createElementType(elementTypeName);
      reference = new Reference(elementType);
      parent.content.members.addElement(reference);

      // If the parent has element content, add the order value for the class.

      addOrderValue(parent, elementTypeName, inlineClassMap.getOrderInfo());

      // Process the inline class map recursively to build its content model, etc.

      processClassMapBase(inlineClassMap);
   }

   private void processAttributeMaps(ElementType parent, Enumeration attrMaps)
   {
      PropertyMap attrMap;

      while (attrMaps.hasMoreElements())
      {
         attrMap = (PropertyMap)attrMaps.nextElement();
         processAttributeMap(parent, attrMap);
      }
   }

   private void processAttributeMap(ElementType parent, PropertyMap attrMap)
   {
      XMLName   attrName;
      Attribute attr;

      // Create a new attribute and add it to the parent element type's
      // hashtable of attributes.

      attrName = attrMap.getXMLName();
      attr = new Attribute(attrName);
      parent.attributes.put(attrName, attr);

      // Set the attribute type. This is NMTOKENS if the attribute contains
      // a token list and CDATA otherwise.

      attr.type = (attrMap.isTokenList()) ? Attribute.TYPE_NMTOKENS : Attribute.TYPE_CDATA;

      // If the attribute is stored in a separate table, assume it is optional.
      // If it is stored in the class table, determine whether it is optional from
      // the column's nullability.

      if (attrMap.getTable() != null)
      {
         attr.required = Attribute.REQUIRED_OPTIONAL;
      }
      else if (attrMap.getColumn().getNullability() == DatabaseMetaData.columnNullable)
      {
         attr.required = Attribute.REQUIRED_OPTIONAL;
      }
      else
      {
         attr.required = Attribute.REQUIRED_REQUIRED;
      }
   }

   //**************************************************************************
   // Private methods -- helpers
   //**************************************************************************

   private void checkUnhandledStructures(ClassMap classMap)
      throws XMLMiddlewareException
   {
      // Check for structures we can't handle.

      if (classMap.getBaseClassMap() != null)
         throw new XMLMiddlewareException("The class map for the " + classMap.getElementTypeName().getUniversalName() + " uses base class maps (the Extends element). This structure cannot be represented in a DTD.");

      if (classMap.getUsedClassMap() != null)
         throw new XMLMiddlewareException("The class map for the " + classMap.getElementTypeName().getUniversalName() + " uses the class map for another element type (the UseClassMap element). This structure cannot be represented in a DTD.");
   }

   private void setContentType(ElementType elementType, PropertyMap pcdataMap, Enumeration childMaps)
   {
      // We switch on the content type because the element type might have already
      // been processed. This is the case if an element type is mapped twice -- for
      // example, it is mapped as a related class in one element type and an inline
      // class in another element type (weird but legal). In this case, the content
      // model is a composite of all the mappings.

      switch (elementType.contentType)
      {
         case ElementType.CONTENT_UNKNOWN:
         case ElementType.CONTENT_EMPTY:
            // If the content model has not been created or is empty, figure out its type
            // in a straightforward way depending on what types of children are present.

            if (childMaps.hasMoreElements())
            {
               if (pcdataMap == null)
               {
                  elementType.contentType = ElementType.CONTENT_ELEMENT;
               }
               else
               {
                  elementType.contentType = ElementType.CONTENT_MIXED;
               }
            }
            else // No child elements mapped
            {
               if (pcdataMap == null)
               {
                  elementType.contentType = ElementType.CONTENT_EMPTY;
               }
               else
               {
                  elementType.contentType = ElementType.CONTENT_PCDATA;
               }
            }
            break;

         case ElementType.CONTENT_ELEMENT:
            // If a content model already contains elements, it can only change to mixed.

            if (pcdataMap != null)
            {
               elementType.contentType = ElementType.CONTENT_MIXED;
               updateContentModel(elementType);
            }
            break;

         case ElementType.CONTENT_PCDATA:
            // If a content model already contains PCDATA, it can only change to mixed.

            if (childMaps.hasMoreElements())
            {
               elementType.contentType = ElementType.CONTENT_MIXED;
            }
            break;
      }

      // If the content group has not yet been set, set it now. We can do this safely
      // because, for element and mixed content groups, we know that there are children.
      // We use a sequence for element content and a choice for mixed content.

      if ((elementType.contentType == ElementType.CONTENT_ELEMENT) ||
          (elementType.contentType == ElementType.CONTENT_MIXED))
      {
         if (elementType.content == null)
         {
            elementType.content = new Group();
            elementType.content.members = new Vector();
            if (elementType.contentType == ElementType.CONTENT_ELEMENT)
            {
               elementType.content.type = Particle.TYPE_SEQUENCE;
            }
            else // (elementType.contentType == ElementType.CONTENT_MIXED)
            {
               elementType.content.type = Particle.TYPE_CHOICE;
               elementType.content.isRepeatable = true;
               elementType.content.isRequired = false;
            }
         }
      }
   }

   private void updateContentModel(ElementType elementType)
   {
      // If we change the content model from element to mixed, we need to update the
      // ElementType.content variable. In particular, this must (a) be a choice and
      // (b) contain only references to required, singleton child elements.

      Reference reference;

      // Change the content type to choice and repeatability/required to *.

      elementType.content.type = Particle.TYPE_CHOICE;
      elementType.content.isRepeatable = true;
      elementType.content.isRequired = false;

      // Change the element type references to required, singleton children.

      for (int i = 0; i < elementType.content.members.size(); i++)
      {
         reference = (Reference)elementType.content.members.elementAt(i);
         reference.isRepeatable = false;
         reference.isRequired = true;
      }

      // Remove information about the order of children in the content model.

      order.remove(elementType.name);
   }

   private void addOrderValue(ElementType parent, XMLName elementTypeName, OrderInfo orderInfo)
   {
      Hashtable childOrder;

      // If the parent does not have an element content model, just return --
      // we don't care about order.

      if (parent.contentType != ElementType.CONTENT_ELEMENT) return;

      // If we haven't started saving order values for this content
      // model, allocate a new hashtable now.

      childOrder = (Hashtable)order.get(parent.name);
      if (childOrder == null)
      {
         childOrder = new Hashtable();
         order.put(parent.name, childOrder);
      }

      // If the order is fixed, store that number now. Otherwise, store the max.

      if ((orderInfo != null) && (orderInfo.orderValueIsFixed()))
      {
         childOrder.put(elementTypeName, new Long(orderInfo.getFixedOrderValue()));
      }
      else
      {
         childOrder.put(elementTypeName, MAXLONG);
      }
   }

   private void sortContentModels()
   {
      Enumeration elementTypes;
      ElementType elementType;

      elementTypes = dtd.elementTypes.elements();
      while (elementTypes.hasMoreElements())
      {
         elementType = (ElementType)elementTypes.nextElement();
         if (elementType.contentType == ElementType.CONTENT_ELEMENT)
         {
            sortContentModel(elementType);
         }
      }
   }

   private void sortContentModel(ElementType elementType)
   {
      Object[]  refs;
      long[]    keys;
      Hashtable childOrder;
      XMLName   elementTypeName;
      int       i;

      // Get the hashtable containing order values for this element type's content model.

      childOrder = (Hashtable)order.get(elementType.name);

      // Build an array of References to child element types.

      refs = new Object[elementType.content.members.size()];
      elementType.content.members.copyInto(refs);

      // Get the order values for the references and build a second array.

      keys = new long[elementType.content.members.size()];
      for (i = 0; i < keys.length; i++)
      {
         elementTypeName = ((Reference)refs[i]).elementType.name;
         keys[i] = (((Long)childOrder.get(elementTypeName)).longValue());
      }

      // Sort the references by order value.

      Sort.sort(keys, refs);

      // Clear the original content model Vector and rebuild it in the correct order.

      elementType.content.members.removeAllElements();
      for (i = 0; i < refs.length; i++)
      {
         elementType.content.members.addElement(refs[i]);
      }
   }

}
