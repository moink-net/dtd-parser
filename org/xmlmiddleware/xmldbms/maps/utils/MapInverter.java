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

import org.xmlmiddleware.utils.XMLName;

import org.xmlmiddleware.xmldbms.maps.ClassMap;
import org.xmlmiddleware.xmldbms.maps.ClassMapBase;
import org.xmlmiddleware.xmldbms.maps.ClassTableMap;
import org.xmlmiddleware.xmldbms.maps.ColumnMap;
import org.xmlmiddleware.xmldbms.maps.ElementInsertionList;
import org.xmlmiddleware.xmldbms.maps.ElementInsertionMap;
import org.xmlmiddleware.xmldbms.maps.InlineClassMap;
import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.MapException;
import org.xmlmiddleware.xmldbms.maps.OrderInfo;
import org.xmlmiddleware.xmldbms.maps.PropertyMap;
import org.xmlmiddleware.xmldbms.maps.PropertyMapBase;
import org.xmlmiddleware.xmldbms.maps.PropertyTableMap;
import org.xmlmiddleware.xmldbms.maps.RelatedClassMap;
import org.xmlmiddleware.xmldbms.maps.RelatedClassTableMap;
import org.xmlmiddleware.xmldbms.maps.Table;

import java.util.Enumeration;

/**
 * Invert the database-centric or XML-centric map objects in a Map.
 *
 * <p>A Map is the root of a graph of map objects. ClassMap, PropertyMap,
 * InlineClassMap, and RelatedClassMap objects form an XML-centric view of the map.
 * That is, they map XML elements, attributes, and PCDATA to database structures.
 * ClassTableMap, PropertyTableMap, ColumnMap, ElementInsertionList, ElementInsertionMap,
 * and RelatedClassTableMap objects form a database-centric view of the map. That is,
 * they map tables and columns to XML structures.</p>
 *
 * <p>MapInverter provides two methods. createXMLView() takes the XML-centric
 * objects and creates database-centric objects. createDatabaseView() takes the
 * database-centric objects and creates XML-centric objects. Note that neither method
 * modifies the objects it inverts.</p>
 *
 * <p>The primary users of MapInverter are map factories, which usually create one
 * view and use the MapInverter to create the other view. For example, MapFactory_MapDocument
 * and MapFactory_DTD create XML-centric objects, then use MapInverter to create the
 * database-centric objects. MapFactory_Database creates database-centric objects, then
 * uses MapInverter to create the XML-centric objects.</p>
 *
 * <p>The correspondence between the XML-centric and database-centric objects is as
 * follows. ClassMap corresponds to ClassTableMap. PropertyMap corresponds to ColumnMap
 * and PropertyTableMap. InlineClassMap corresponds to ElementInsertionList and
 * ElementInsertionMap. And RelatedClassMap corresponds to RelatedClassTableMap.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class MapInverter
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new MapInverter. */
   public MapInverter()
   {
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Create the database-centric view.
    *
    * <p>Warning: This method deletes any existing objects that form part of the
    * database-centric view of the map.</p>
    *
    * <p>Warning: If one ClassMap uses another ClassMap, this information is partially
    * lost in the inversion process. The reason for this is that there is no place to
    * store this information in a ClassTableMap. For example, suppose element types Book
    * and Essay both use the ClassMap for element type Writing, which is mapped to the
    * Writings table. A ClassTableMap can only be mapped to a single element type, so it
    * is mapped to Writing, not Book or Essay.</p>
    *
    * <p>Fortunately, this is not as bad as it sounds. If an element type that uses the
    * ClassMap for another element type is mapped as a related class, then the usage
    * information is retained. The reason for this is that the RelatedClassMap is inverted
    * to a RelatedClassTableMap and the RelatedClassTableMap can store the name of the
    * mapped element type.</p>
    *
    * <p>Thus, the only real drawback at a mapping level is that used ClassMaps can't
    * be round-tripped. That is, calling createDatabaseView() and then calling
    * createXMLView() results in the loss of these ClassMaps. Fortunately, there
    * seems to be no reason to round-trip such maps.</p>
    *
    * <p>(At a data level, this means that root elements that use the ClassMaps of other
    * elements can't be reconstructed -- the element type of the used ClassMap is reconstructed
    * instead -- but that child elements that use the ClassMaps of other element types
    * can be reconstructed.)</p>
    *
    * @exception MapException Thrown if an error occurs creating the database-centric
    *    objects. This indicates a programming error, as it means the XML-centric objects
    *    are invalid.
    */
   public void createDatabaseView(Map map)
      throws MapException
   {
      if (map == null)
         throw new IllegalStateException("Map object must be set before the database view can be created.");

      map.removeAllClassTableMaps();
      invertClassMaps(map);
   }

   /**
    * Create the XML-centric view.
    *
    * <p>Warning: This method deletes any existing objects that form part of the
    * XML-centric view of the map.</p>
    *
    * @exception MapException Thrown if an error occurs creating the XML-centric objects.
    *    This indicates a programming error, as it means the database-centric objects
    *    are invalid.
    */
   public void createXMLView(Map map)
      throws MapException
   {
      if (map == null)
         throw new IllegalStateException("Map object must be set before the XML view can be created.");

      map.removeAllClassMaps();
      invertClassTableMaps(map);
   }

   //**************************************************************************
   // Private methods -- inverting XML view
   //**************************************************************************

   private void invertClassMaps(Map map)
      throws MapException
   {
      Enumeration classMaps;

      classMaps = map.getClassMaps();
      while (classMaps.hasMoreElements())
      {
         invertClassMap(map, (ClassMap)classMaps.nextElement());
      }
   }

   private void invertClassMap(Map map, ClassMap classMap)
      throws MapException
   {
      // Create a ClassTableMap from a ClassMap.

      ClassTableMap newClassTableMap;
      ClassMap      baseClassMap;

      if (classMap.getUsedClassMap() != null)
      {
         // There is nothing to do here -- used class maps are not invertible at
         // this level. For more information, see the comments for createDatabaseView().

         return;
      }

      // Get/create the ClassTableMap for the table. Note that this might already
      // have been created when inverting a RelatedClassMap, so we use
      // Map.createClassTableMap().

      newClassTableMap = map.createClassTableMap(classMap.getTable());

      // Set the element type name.

      newClassTableMap.setElementTypeName(classMap.getElementTypeName());

      // Set the base table, if any.

      baseClassMap = classMap.getBaseClassMap();
      if (baseClassMap != null)
      {
         newClassTableMap.setBaseTable(baseClassMap.getTable());
         newClassTableMap.setBaseLinkInfo(classMap.getBaseLinkInfo());
      }

      // Invert the maps for attributes, PCDATA, and child elements.

      invertMarkupMaps(map, newClassTableMap, classMap, ElementInsertionList.create());
   }

   private void invertMarkupMaps(Map map, ClassTableMap newClassTableMap, ClassMapBase classMapBase, ElementInsertionList elementInsertionList)
      throws MapException
   {
      // Process the child maps of a ClassMap or InlineClassMap.

      // This method uses ClassMapBase because it can be used to invert child maps from
      // both ClassMaps and InlineClassMaps.

      Enumeration e;
      PropertyMap propMap;
      Object      o;

      // Invert the attribute maps.

      e = classMapBase.getAttributeMaps();
      while (e.hasMoreElements())
      {
         propMap = (PropertyMap)e.nextElement();
         invertPropertyMap(newClassTableMap, propMap, elementInsertionList);
      }

      // Invert the PCDATA map.

      propMap = classMapBase.getPCDATAMap();
      if (propMap != null)
      {
         invertPropertyMap(newClassTableMap, propMap, elementInsertionList);
      }

      // Invert the child element maps.

      e = classMapBase.getChildMaps();
      while (e.hasMoreElements())
      {
         // Get the next child element map and determine whether it is a
         // PropertyMap, RelatedClassMap, or InlineClassMap.

         o = e.nextElement();
         if (o instanceof PropertyMap)
         {
            invertPropertyMap(newClassTableMap, (PropertyMap)o, elementInsertionList);
         }
         else if (o instanceof RelatedClassMap)
         {
            invertRelatedClassMap(map, newClassTableMap, (RelatedClassMap)o, elementInsertionList);
         }
         else // if (o instanceof InlineClassMap)
         {
            invertInlineClassMap(map, newClassTableMap, (InlineClassMap)o, elementInsertionList);
         }
      }
   }

   private void invertPropertyMap(ClassTableMap newClassTableMap, PropertyMap propMap, ElementInsertionList elementInsertionList)
      throws MapException
   {
      // Create a ColumnMap or a PropertyTableMap from a PropertyMap.

      ColumnMap        newColumnMap;
      PropertyTableMap newPropTableMap;
      PropertyMapBase  newPropMapBase;
      Table            propTable;
      boolean          isTokenList;

      // Check if the property is mapped to a column in the parent table or a property
      // table. In the former case, create a ColumnMap; in the latter case, create a
      // PropertyTableMap.

      propTable = propMap.getTable();
      if (propTable == null)
      {
         // Create a ColumnMap and add it to the ClassTableMap/ElementInsertionMap.

         newColumnMap = ColumnMap.create(propMap.getColumn());
         newColumnMap.setXMLName(propMap.getXMLName(), propMap.getType());
         if (elementInsertionList.size() != 0)
         {
            newColumnMap.setElementInsertionList((ElementInsertionList)elementInsertionList.clone());
         }
         newClassTableMap.addColumnMap(newColumnMap);
         newPropMapBase = newColumnMap;
      }
      else
      {
         // Create a PropertyTableMap and add it to the ClassTableMap/ElementInsertionMap.

         newPropTableMap = PropertyTableMap.create(propTable);
         newPropTableMap.setColumn(propMap.getColumn());
         newPropTableMap.setLinkInfo(propMap.getLinkInfo());
         newPropTableMap.setXMLName(propMap.getXMLName(), propMap.getType());
         if (elementInsertionList.size() != 0)
         {
            newPropTableMap.setElementInsertionList((ElementInsertionList)elementInsertionList.clone());
         }
         newClassTableMap.addPropertyTableMap(newPropTableMap);
         newPropMapBase = newPropTableMap;
      }

      // Set the common properties.

      isTokenList = propMap.isTokenList();
      newPropMapBase.setIsTokenList(isTokenList);
      if (isTokenList)
      {
         newPropMapBase.setTokenListOrderInfo(propMap.getTokenListOrderInfo());
      }
      if (propMap.getType() != PropertyMapBase.ATTRIBUTE)
      {
         newPropMapBase.setOrderInfo(propMap.getOrderInfo());
      }

      if (newPropMapBase.getType() == PropertyMapBase.ELEMENTTYPE)
      {
         newPropMapBase.setContainsXML(propMap.containsXML());
      }
   }

   private void invertRelatedClassMap(Map map, ClassTableMap newClassTableMap, RelatedClassMap relatedClassMap, ElementInsertionList elementInsertionList)
      throws MapException
   {
      // Create a RelatedClassTableMap from a RelatedClassMap.

      ClassTableMap        newChildClassTableMap;
      RelatedClassTableMap newRelatedClassTableMap;

      // Get/create the ClassTableMap for the related table. Note that this might already
      // have been created when inverting a ClassMap, so we use Map.createClassTableMap(). 

      newChildClassTableMap = map.createClassTableMap(relatedClassMap.getClassMap().getTable());

      // Create the RelatedClassTableMap and add it to the ClassTableMap/ElementInsertionMap.

      newRelatedClassTableMap = RelatedClassTableMap.create(newChildClassTableMap);
      newClassTableMap.addRelatedClassTableMap(newRelatedClassTableMap);

      // Set the other properties. Note that the element type name might be different
      // from the element type name found in the ClassTableMap. This occurs if the
      // element type is mapped using the ClassMap for another element type.

      newRelatedClassTableMap.setElementTypeName(relatedClassMap.getElementTypeName());
      newRelatedClassTableMap.setLinkInfo(relatedClassMap.getLinkInfo());
      newRelatedClassTableMap.setOrderInfo(relatedClassMap.getOrderInfo());
      if (elementInsertionList.size() != 0)
      {
         newRelatedClassTableMap.setElementInsertionList((ElementInsertionList)elementInsertionList.clone());
      }
   }

   private void invertInlineClassMap(Map map, ClassTableMap newClassTableMap, InlineClassMap inlineClassMap, ElementInsertionList elementInsertionList)
      throws MapException
   {
      // The InlineClassMaps in a ClassMap form a tree. That is, a given ClassMap or
      // InlineClassMap can have more than one InlineClassMap as a child. The path of
      // InlineClassMaps that lead to a given PropertyMap or RelatedTableMap is
      // linear. Thus, we can construct a list (ElementInsertionList) of the inlined
      // elements that are the ancestors of a given PropertyMap or RelatedTableMap.
      //
      // We do this by creating a new ElementInsertionList when processing a ClassMap.
      // We pass this to each child map. When we process an InlineClassMap, we add
      // a ElementInsertionMap to the list; this gives the name of the inlined element
      // and the order in which it is to appear in its parent (if any). When we process
      // a PropertyMap or RelatedClassMap, we check if the list is non-empty -- that is,
      // if it has any inlined elements as ancestors -- and add the list to the newly
      // created ColumnMap, PropertyTableMap, or RelatedClassTableMap if it is non-empty.
      //
      // When we reach a PropertyMap or RelatedClassMap, we have reached a leaf node in
      // the map tree. Thus, we return up one level and process the next sibling. When
      // we do this, we need to remove the ElementInsertionMap from the list, since it
      // does not apply to the branch of the tree we will process next.

      ElementInsertionMap newElementInsertionMap;

      // Create a new ElementInsertionMap and add it to the ElementInsertionList.

      newElementInsertionMap = ElementInsertionMap.create(inlineClassMap.getElementTypeName());
      elementInsertionList.addElementInsertionMap(newElementInsertionMap);

      // Set the properties.

      newElementInsertionMap.setOrderInfo(inlineClassMap.getOrderInfo());

      // Process the children of the InlineClassMap

      invertMarkupMaps(map, newClassTableMap, inlineClassMap, elementInsertionList);

      // Remove the ElementInsertionMap that was added earlier.

      elementInsertionList.removeElementInsertionMap(elementInsertionList.size() - 1);
   }

   //**************************************************************************
   // Private methods -- inverting database view
   //**************************************************************************

   private void invertClassTableMaps(Map map)
      throws MapException
   {
      Enumeration classTableMaps;

      classTableMaps = map.getClassTableMaps();
      while (classTableMaps.hasMoreElements())
      {
         invertClassTableMap(map, (ClassTableMap)classTableMaps.nextElement());
      }
   }

   private void invertClassTableMap(Map map, ClassTableMap classTableMap)
      throws MapException
   {
      // Create a ClassMap from a ClassTableMap.

      ClassMap      newClassMap, baseClassMap;
      Table         baseTable;
      ClassTableMap baseClassTableMap;

      // Get/create the ClassMap for the element type. Note that this might already
      // have been created when inverting a RelatedClassTableMap, so we use
      // Map.createClassMap().

      newClassMap = map.createClassMap(classTableMap.getElementTypeName());

      // Set the table.

      newClassMap.setTable(classTableMap.getTable());

      // Set the base ClassMap, if any. This requires us to:
      // 1) Get the ClassTableMap for the base table. We need this to:
      // 2) Get the element type to which the base table is mapped. We need this to:
      // 3) Get/create the ClassMap for the base table element type. Note that this
      //    ClassMap might already have been created, so we use map.createClassMap().

      baseTable = classTableMap.getBaseTable();
      if (baseTable != null)
      {
         baseClassTableMap = map.getClassTableMap(baseTable.getDatabaseName(),
                                                  baseTable.getCatalogName(),
                                                  baseTable.getSchemaName(),
                                                  baseTable.getTableName());
         baseClassMap = map.createClassMap(baseClassTableMap.getElementTypeName());
         newClassMap.setBaseClassMap(baseClassMap);
         newClassMap.setBaseLinkInfo(classTableMap.getBaseLinkInfo());
      }

      // Invert the maps for columns, related property tables, and related class tables.

      invertColumnMaps(newClassMap, classTableMap.getColumnMaps());
      invertPropertyTableMaps(newClassMap, classTableMap.getPropertyTableMaps());
      invertRelatedClassTableMaps(map, newClassMap, classTableMap.getRelatedClassTableMaps());
   }

   private void invertColumnMaps(ClassMap newClassMap, Enumeration columnMaps)
      throws MapException
   {
      ColumnMap      columnMap;
      InlineClassMap newInlineClassMap;
      ClassMapBase   newClassMapBase;

      while (columnMaps.hasMoreElements())
      {
         // Get the next ColumnMap.

         columnMap = (ColumnMap)columnMaps.nextElement();

         // Construct the InlineClassMaps above the ColumnMap. The last
         // InlineClassMap in the list (if any) is returned.

         newInlineClassMap = constructInlineClassMaps(newClassMap, columnMap.getElementInsertionList());

         // Invert the ColumnMap. The PropertyMap that is created will be added either
         // to the last InlineClassMap in the list (if it exists) or the ClassMap (if
         // it doesn't).

         newClassMapBase = (newInlineClassMap != null) ? (ClassMapBase)newInlineClassMap : (ClassMapBase)newClassMap;
         invertPropertyMapBase(newClassMapBase, columnMap);
      }
   }

   private void invertPropertyTableMaps(ClassMap newClassMap, Enumeration propTableMaps)
      throws MapException
   {
      PropertyTableMap  propTableMap;
      InlineClassMap    newInlineClassMap;
      ClassMapBase      newClassMapBase;

      while (propTableMaps.hasMoreElements())
      {
         // Get the next PropertyTableMap.

         propTableMap = (PropertyTableMap)propTableMaps.nextElement();

         // Construct the InlineClassMaps above the PropertyTableMap. The last
         // InlineClassMap in the list (if any) is returned.

         newInlineClassMap = constructInlineClassMaps(newClassMap, propTableMap.getElementInsertionList());

         // Invert the PropertyTableMap. The PropertyMap that is created will be added either
         // to the last InlineClassMap in the list (if it exists) or the ClassMap (if
         // it doesn't).

         newClassMapBase = (newInlineClassMap != null) ? (ClassMapBase)newInlineClassMap : (ClassMapBase)newClassMap;
         invertPropertyMapBase(newClassMapBase, propTableMap);
      }
   }

   private void invertPropertyMapBase(ClassMapBase newClassMapBase, PropertyMapBase propMapBase)
      throws MapException
   {
      // Create a PropertyMap from a ColumnMap or a PropertyTableMap.

      // This method uses ClassMapBase because the inverted PropertyMapBase can
      // be added to either a ClassMap or an InlineClassMap.

      PropertyMap newPropMap;
      int         type;
      boolean     isTokenList;

      // Create a new PropertyMap and add it to the appropriate list.

      type = propMapBase.getType();
      newPropMap = PropertyMap.create(propMapBase.getXMLName(), type);
      switch(type)
      {
         case PropertyMapBase.ATTRIBUTE:
            newClassMapBase.addAttributeMap(newPropMap);
            break;

         case PropertyMapBase.PCDATA:
            newClassMapBase.addPCDATAMap(newPropMap);
            break;

         case PropertyMapBase.ELEMENTTYPE:
            newClassMapBase.addChildMap(newPropMap);
            break;
      }

      // Set the column, table, and OrderInfo.

      newPropMap.setColumn(propMapBase.getColumn());

      if (propMapBase instanceof PropertyTableMap)
      {
         newPropMap.setTable(((PropertyTableMap)propMapBase).getTable(),
                          ((PropertyTableMap)propMapBase).getLinkInfo());
      }

      isTokenList = propMapBase.isTokenList();
      newPropMap.setIsTokenList(isTokenList);
      if (isTokenList)
      {
         newPropMap.setTokenListOrderInfo(propMapBase.getTokenListOrderInfo());
      }
      if (type != PropertyMapBase.ATTRIBUTE)
      {
         newPropMap.setOrderInfo(propMapBase.getOrderInfo());
      }
      if (type == PropertyMapBase.ELEMENTTYPE)
      {
         newPropMap.setContainsXML(propMapBase.containsXML());
      }
   }

   private void invertRelatedClassTableMaps(Map map, ClassMap newClassMap, Enumeration relatedClassTableMaps)
      throws MapException
   {
      RelatedClassTableMap  relatedClassTableMap;
      InlineClassMap        newInlineClassMap;
      ClassMapBase          newClassMapBase;

      while (relatedClassTableMaps.hasMoreElements())
      {
         // Get the next PropertyTableMap.

         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();

         // Construct the InlineClassMaps above the RelatedClassTableMap. The last
         // InlineClassMap in the list (if any) is returned.

         newInlineClassMap = constructInlineClassMaps(newClassMap, relatedClassTableMap.getElementInsertionList());

         // Invert the RelatedClassTableMap. The RelatedClassMap that is created will
         // be added either to the last InlineClassMap in the list (if it exists) or
         // the ClassMap (if it doesn't).

         newClassMapBase = (newInlineClassMap != null) ? (ClassMapBase)newInlineClassMap : (ClassMapBase)newClassMap;
         invertRelatedClassTableMap(map, newClassMapBase, relatedClassTableMap);
      }
   }

   private void invertRelatedClassTableMap(Map map, ClassMapBase newClassMapBase, RelatedClassTableMap relatedClassTableMap)
      throws MapException
   {
      // Create a RelatedClassMap from a RelatedClassTableMap.

      // This method uses ClassMapBase because the inverted RelatedClassTableMap can
      // be added to either a ClassMap or an InlineClassMap.

      ClassMap        usedClassMap;
      RelatedClassMap newRelatedClassMap;

      // Create the RelatedClassMap and add it to the ClassMap/InlineClassMap.

      newRelatedClassMap = RelatedClassMap.create(relatedClassTableMap.getElementTypeName());
      newClassMapBase.addChildMap(newRelatedClassMap);

      // Get the ClassMap for the element type to which the related class table is mapped.
      // This might be different from the element type in the RelatedClassTableMap object.
      // That is, relatedClassTableMap.getElementTypeName() might be different from
      // relatedClassTableMap.getClassTableMap().getElementTypeName(). For example, this
      // occurs when one element type (in the RelatedClassTableMap object) uses the
      // ClassMap for another element type (in the pointed-to ClassTableMap object). We
      // use Map.createClassMap() to get/create the ClassMap, since it might already have
      // been created when inverting a ClassTableMap.

      usedClassMap = map.createClassMap(relatedClassTableMap.getClassTableMap().getElementTypeName());
      newRelatedClassMap.setClassMap(usedClassMap);

      // Set the common properties.

      newRelatedClassMap.setLinkInfo(relatedClassTableMap.getLinkInfo());
      newRelatedClassMap.setOrderInfo(relatedClassTableMap.getOrderInfo());
   }

   private InlineClassMap constructInlineClassMaps(ClassMap newClassMap, ElementInsertionList elementInsertionList)
      throws MapException
   {
      // Construct a chain of InlineClassMaps from an ElementInsertionList.
      //
      // An ElementInsertionList represents a path of inlined elements from a
      // ClassMap to a ColumnMap, PropertyTableMap, or RelatedClassTableMap.
      // Each ElementInsertionMap in the list corresponds to one InlineClassMap.
      //
      // When inverting the list, it is necessary to remember that the InlineClassMaps
      // have a tree structure, not a linear structure. That is, some InlineClassMaps
      // may be ancestors of more than one ColumnMap, PropertyTableMap, or
      // RelatedClassTableMap. Thus, some ElementInsertionMaps are repeated in the
      // ElementInsertionLists of more than one ColumnMap, PropertyTableMap, or
      // RelatedClassTableMap.
      //
      // For this reason, createInlineClassMap is used. This method returns an existing
      // InlineClassMap (if it exists) and creates a new one (if it doesn't).

      ClassMapBase        classMapBase;
      ElementInsertionMap elementInsertionMap;
      InlineClassMap      inlineClassMap = null;

      // Just return null if the ElementInsertionList is null.

      if (elementInsertionList == null) return null;

      // Set the classMapBase to the parent ClassMap. As they are created, new
      // InlineClassMaps are added to the current classMapBase and become the new
      // classMapBase. That is, the first InlineClassMap is added to the parent
      // ClassMap, the second InlineClassMap is added to the first InlineClassMap, and
      // so on. ClassMapBase is used because both ClassMap and InlineClassMap inherit
      // from it.

      classMapBase = newClassMap;

      for (int i = 0; i < elementInsertionList.size(); i++)
      {
         // Get the next ElementInsertionMap from the list.

         elementInsertionMap = (ElementInsertionMap)elementInsertionList.getElementInsertionMap(i);

         // Get/create a new InlineClassMap with the same element type name as
         // the ElementInsertionMap. Set the OrderInfo on the new InlineClassMap.

         inlineClassMap = classMapBase.createInlineClassMap(elementInsertionMap.getElementTypeName());
         inlineClassMap.setOrderInfo(elementInsertionMap.getOrderInfo());

         // Set classMapBase to the new InlineClassMap. This means that the next
         // InlineClassMap that is created will be added to this InlineClassMap.

         classMapBase = inlineClassMap;
      }

      // Return the last InlineClassMap in the chain.

      return inlineClassMap;
   }
}
