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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.0: None
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;
import java.util.Hashtable;

/**
 * Maps an element type as a class; <a href="../readme.htm#NotForUse">not for
 * general use</a>.
 *
 * <p>ClassMap contains information about an element type that is viewed as
 * a class and mapped to a class table. It contains the name of the element type
 * and the table, as well as information about a base class map (if any) and
 * maps for attributes, PCDATA, and child element types.</p>
 *
 * <p>ClassMap inherits from ClassMapBase, which provides an accessor for the
 * element type name and accessors and mutators for attribute, PCDATA, and
 * child element type maps. It adds accessors and mutators for class table, base
 * table, and used class map.</p>
 *
 * <p>If this ClassMap uses a different ClassMap (which effectively "casts" the
 * element type mapped by this ClassMap to the element type mapped by the other
 * ClassMap), then all mutator methods except useClassMap() return an
 * IllegalStateException.</p>
 *
 * <p>ClassMaps are stored in the Map class. They are pointed to by ClassMaps,
 * RelatedClassMaps, and InlineClassMaps.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class ClassMap extends ClassMapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   // The following variables are inherited from ClassMapBase

//   private XMLName     elementTypeName = null;
//   private Hashtable   attributeMaps = new Hashtable();
//   private PropertyMap pcdataMap = null;
//   private Hashtable   childMaps = new Hashtable(); // For child element types

   // The following variables are new to ClassMap

   private Table       table = null;
   private ClassMap    baseClassMap = null;
   private LinkInfo    baseLinkInfo = null;
   private ClassMap    useClassMap = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private ClassMap(XMLName elementTypeName)
   {
      super(elementTypeName);
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new ClassMap.
    *
    * @param uri Namespace URI of the element type being mapped. May be null.
    * @param localName Local name of the element type being mapped.
    *
    * @return The ClassMap.
    */
   public static ClassMap create(String uri, String localName)
   {
      // XMLName.create checks that localName is not null.

      return new ClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create a new ClassMap.
    *

    * @param elementTypeName XMLName of the element type being mapped.
    *
    * @return The ClassMap.
    */
   public static ClassMap create(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      return new ClassMap(elementTypeName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Table
   // ********************************************************************

   /**
    * Get the table to which the class is mapped.
    *
    * @return The table.
    */
   public final Table getTable()
   {
      return table;
   }

   /**
    * Set the table to which the class is mapped.
    *
    * @param table The table. May be null.
    */
   public void setTable(Table table)
   {
      this.table = table;
   }

   // ********************************************************************
   // Base table
   // ********************************************************************

   /**
    * Get the name of the base ClassMap, if one is being used.
    *
    * @return The base ClassMap. May be null.
    */
   public final ClassMap getBaseClassMap()
   {
      return baseClassMap;
   }

   /**
    * Set the base ClassMap.
    *
    * @param baseClassMap The base ClassMap. If there is no base ClassMap,
    *    set this to null, in which case the information used to link the
    *    class table to the base table is set to null.
    */
   public void setBaseClassMap(ClassMap baseClassMap)
   {
      this.baseClassMap = baseClassMap;
      if (baseClassMap == null)
      {
         this.baseLinkInfo = null;
      }      
   }

   /**
    * Get the LinkInfo used to link the class table to the base class table.
    *
    * @return The LinkInfo. The "parent" table is the base class table. Null if
    *    the base class table is not used. 
    */
   public final LinkInfo getBaseLinkInfo()
   {
      return baseLinkInfo;
   }

   /**
    * Set the LinkInfo used to link the class table to the base class table.
    *
    * <p>This method may not be called if the base ClassMap is null.</p>
    *
    * <p>Setting the baseLinkInfo argument to null when the base ClassMap is non-null
    * is useful if you want the map objects to preserve inheritance information
    * but want to store the data for the class in a single table, rather than in a
    * base table and a class table. Inheritance information can then be used elsewhere,
    * such as when an XML Schema is generated from a Map.</p>
    *
    * @param baseLinkInfo The LinkInfo. The "parent" table is the base class table.
    *    Null if the base class table is not used.
    */
   public void setBaseLinkInfo(LinkInfo baseLinkInfo)
   {
      if (baseClassMap == null)
         throw new IllegalStateException("Cannot call ClassMap.setUseBaseTable() if the base ClassMap is null.");
      this.baseLinkInfo = baseLinkInfo;
   }

   // ********************************************************************
   // Use another ClassMap
   // ********************************************************************

   /**
    * Get the ClassMap used by this ClassMap.
    *
    * <p>If the returned value is not null, then no other methods may be called
    * to set properties of this ClassMap (except useClassMap()) and the values
    * returned by methods that get properties of this ClassMap are undefined.</p>
    *
    * @return The ClassMap used by this ClassMap. May be null.
    */
   public final ClassMap getUsedClassMap()
   {
      return useClassMap;
   }

   /**
    * Use the ClassMap for a different element type.
    *
    * <p>Using the ClassMap for a different element type effectively "casts" the
    * element type mapped by this ClassMap to the other element type. For more
    * information, see the description of the &lt;UseClassMap> element type in the
    * XML-DBMS mapping language.</p>
    *
    * <p>Calling this method with a non-null useClassMap argument sets all
    * other properties (class table, attribute maps, etc.) to their initial
    * state (null, empty, false, etc.)</p>
    *
    * @param useClassMap The ClassMap to use. Set this to null to use the
    *    current ClassMap.
    */
   public void useClassMap(ClassMap useClassMap)
   {
      ClassMap classMap;
      XMLName  elementTypeName;

      checkArgNull(useClassMap, ARG_USECLASSMAP);

      // Check that there are no circularities in how the ClassMaps are used.
      // For example, suppose the ClassMap for element type A uses the ClassMap
      // for element type B, and the ClassMap for element type B uses the
      // ClassMap for element type C. It would be illegal if the ClassMap for
      // element type C used the ClassMap for element type A.

      classMap = useClassMap;
      elementTypeName = getElementTypeName();
      while (classMap != null)
      {
         if (elementTypeName.equals(classMap.getElementTypeName()))
            throw new IllegalArgumentException("The used class map or a class map it uses maps the same element type as the current class map.");
         classMap = classMap.getUsedClassMap();
      }

      // Remove all the existing attribute maps, PCDATA map, and child element
      // maps. Also remove the class table and base class information. These do
      // not apply when the ClassMap uses another ClassMap.

      removeAllAttributeMaps();
      try
      {
         removePCDATAMap();
      }
      catch (MapException m)
      {
      }
      removeAllChildMaps();
      this.table = null;
      this.baseClassMap = null;
      this.baseLinkInfo = null;

      // Set the used ClassMap.

      this.useClassMap = useClassMap;
   }

   // ********************************************************************
   // Wrappers for mutator methods
   // ********************************************************************

   // These methods simply wrap the mutators in ClassMapBase, checking that
   // this ClassMap does not use another ClassMap.

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
      checkState();
      return super.createAttributeMap(uri, localName);
   }

   /**
    * Create a PropertyMap for an attribute and add it to this map.
    *
    * <p>If the attribute has already been mapped, returns the existing PropertyMap.</p>
    *
    * @param xmlName XMLName of the attribute.
    *
    * @return The PropertyMap for the attribute.
    */
   public PropertyMap createAttributeMap(XMLName xmlName)
   {
      checkState();
      return super.createAttributeMap(xmlName);
   }

   /**
    * Add a PropertyMap for an attribute.
    *
    * @param propMap PropertyMap for the attribute. Must not be null.
    * @exception MapException Thrown if the attribute has already been mapped.
    */
   public void addAttributeMap(PropertyMap propMap)
      throws MapException
   {
      checkState();
      super.addAttributeMap(propMap);
   }

   /**
    * Remove the PropertyMap for an attribute.
    *
    * @param uri Namespace URI of the attribute. May be null.
    * @param localName Local name of the attribute.
    *
    * @exception MapException Thrown if the attribute has not been mapped.
    */
   public void removeAttributeMap(String uri, String localName)
      throws MapException
   {
      checkState();
      super.removeAttributeMap(uri, localName);
   }

   /**
    * Remove the PropertyMap for an attribute.
    *
    * @param universalName Universal name of the attribute.
    *
    * @exception MapException Thrown if the attribute has not been mapped.
    */
   public void removeAttributeMap(String universalName)
      throws MapException
   {
      checkState();
      super.removeAttributeMap(universalName);
   }

   /**
    * Remove the PropertyMaps for all attributes.
    */
   public void removeAllAttributeMaps()
   {
      checkState();
      super.removeAllAttributeMaps();
   }

   // ********************************************************************
   // PCDATA map
   // ********************************************************************

   /**
    * Create a new PropertyMap for PCDATA.
    *
    * <p>If PCDATA has already been mapped, returns the existing PropertyMap.</p>
    *
    * @return A PropertyMap for PCDATA.
    */
   public PropertyMap createPCDATAMap()
   {
      checkState();
      return super.createPCDATAMap();
   }

   /**
    * Add a PropertyMap for PCDATA.
    *
    * @param propMap PropertyMap for PCDATA. Must not be null.
    * @exception MapException Thrown if PCDATA has already been mapped.
    */
   public void addPCDATAMap(PropertyMap propMap)
      throws MapException
   {
      checkState();
      super.addPCDATAMap(propMap);
   }

   /**
    * Remove the PropertyMap for PCDATA.
    *
    * @exception MapException Thrown if PCDATA has not been mapped.
    */
   public void removePCDATAMap()
      throws MapException
   {
      checkState();
      super.removePCDATAMap();
   }

   // ********************************************************************
   // Child element type maps
   // ********************************************************************

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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a related class or inlined class.
    */
   public PropertyMap createChildPropertyMap(String uri, String localName)
      throws MapException
   {
      checkState();
      return super.createChildPropertyMap(uri, localName);
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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a related class or inlined class.
    */
   public PropertyMap createChildPropertyMap(XMLName elementTypeName)
      throws MapException
   {
      checkState();
      return super.createChildPropertyMap(elementTypeName);
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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a property or inlined class.
    */
   public RelatedClassMap createRelatedClassMap(String uri, String localName)
      throws MapException
   {
      checkState();
      return super.createRelatedClassMap(uri, localName);
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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a property or inlined class.
    */
   public RelatedClassMap createRelatedClassMap(XMLName elementTypeName)
      throws MapException
   {
      checkState();
      return super.createRelatedClassMap(elementTypeName);
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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a property or related class.
    */
   public InlineClassMap createInlineClassMap(String uri, String localName)
      throws MapException
   {
      checkState();
      return super.createInlineClassMap(uri, localName);
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
    * @exception MapException Thrown if the child element type is already
    *    mapped as a property or related class.
    */
   public InlineClassMap createInlineClassMap(XMLName elementTypeName)
      throws MapException
   {
      checkState();
      return super.createInlineClassMap(elementTypeName);
   }

   /**
    * Add a PropertyMap for a child element type.
    *
    * @param propMap PropertyMap for the child element type. Must not be null.
    * @exception MapException Thrown if the child element type has already been mapped.
    */
   public void addChildMap(PropertyMap propMap)
      throws MapException
   {
      checkState();
      super.addChildMap(propMap);
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
    * @exception MapException Thrown if the child element type has already been mapped.
    */
   public void addChildMap(RelatedClassMap relatedClassMap)
      throws MapException
   {
      checkState();
      super.addChildMap(relatedClassMap);
   }

   /**
    * Add an InlineClassMap for a child element type.
    *
    * @param inlineClassMap InlineClassMap for the child element type. Must not be null.
    * @exception MapException Throw if the child element type is already mapped.
    */
   public void addChildMap(InlineClassMap inlineClassMap)
      throws MapException
   {
      checkState();
      super.addChildMap(inlineClassMap);
   }

   /**
    * Remove the map for a child element type.
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @exception MapException Thrown if the child element type has not been mapped.
    */
   public void removeChildMap(String uri, String localName)
      throws MapException
   {
      checkState();
      super.removeChildMap(uri, localName);
   }

   /**
    * Remove the map for a child element type.
    *
    * @param universalName Universal name of the child element type.
    *
    * @exception MapException Thrown if the child element type has not been mapped.
    */
   public void removeChildMap(String universalName)
      throws MapException
   {
      checkState();
      super.removeChildMap(universalName);
   }

   /**
    * Remove the maps for all child element types.
    */
   public void removeAllChildMaps()
   {
      checkState();
      super.removeAllChildMaps();
   }

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private void checkState()
   {
      if (useClassMap != null)
         throw new IllegalStateException("Cannot call this method when this class map uses a different class map. Call useClassMap(null) first.");
   }
}