// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Changed columnNumber to rowObjectIndex in convertTable
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.domutils.NameQualifier;
import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.xmldbms.ClassMap;
import de.tudarmstadt.ito.xmldbms.Column;
import de.tudarmstadt.ito.xmldbms.ColumnMap;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.LinkInfo;
import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.OrderInfo;
import de.tudarmstadt.ito.xmldbms.PropertyMap;
import de.tudarmstadt.ito.xmldbms.RelatedClassMap;
import de.tudarmstadt.ito.xmldbms.RootClassMap;
import de.tudarmstadt.ito.xmldbms.RootTableMap;
import de.tudarmstadt.ito.xmldbms.Table;
import de.tudarmstadt.ito.xmldbms.TableMap;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.Map; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempMap
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   // Variables used while constructing Temp maps

   private Hashtable  propertyTables = new Hashtable(),
					  classTablesByElementType = new Hashtable(),
					  classTablesByName = new Hashtable(),
					  classMaps = new Hashtable(),
					  rootClassMaps = new Hashtable(),
					  mappedClasses = new Hashtable(),
					  tableMaps = new Hashtable(),
					  rootTableMaps = new Hashtable(),
					  mappedTables = new Hashtable(),
					  namespaceURIs = null;
   private Object     obj = new Object();
   boolean            generateKeys = false, // Are any keys generated?
					  emptyStringIsNull = false; // Are empty strings treated as NULLs?

   // Variables used while constructing real maps

   private Hashtable  tableInfos = new Hashtable(),
					  map_rootClassMaps = new Hashtable(),
					  map_classMaps = new Hashtable(),
					  map_rootTableMaps = new Hashtable();
   private Table[]    map_tables = null;
   private TableMap[] map_tableMaps = null;
   private String[]   map_prefixes = null, map_uris = null;
   DateFormat         dateFormatter, timeFormatter, timestampFormatter;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempMap()
   {
   }   

   //***********************************************************************
   // Methods for adding and getting temporary class maps
   //
   // The difference between "adding" and "getting" is that adding assumes that
   // the requested map does not already exist and returns an error if it
   // does. Getting assumes that the map might exist and returns it if it does;
   // otherwise, it adds the map.
   //***********************************************************************

   TempRootClassMap addTempRootClassMap(TempClassMap classMap)
   {
	  TempRootClassMap rootClassMap;

	  // Create a new TempRootClassMap and store this in the list of
	  // root class maps.

	  rootClassMap = new TempRootClassMap();
	  rootClassMap.classMap = classMap;
	  rootClassMaps.put(classMap.name.qualified, rootClassMap);
	  return rootClassMap;
   }   

   TempClassMap addTempClassMap(NSName elementType)
	  throws InvalidMapException
   {
	  // Check if the element type has already been mapped as a class. Note
	  // that this is different from whether the TempClassMap for the element
	  // type already exists: a TempClassMap might have already been created
	  // when a different class mapped this class as a related class. If it
	  // has not been mapped, store a dummy Object using the element type
	  // name as a key.

	  if (mappedClasses.containsKey(elementType.qualified))
		 throw new InvalidMapException("Element type " + elementType.qualified + " mapped more than once.");
	  mappedClasses.put(elementType.qualified, obj);
	  return getTempClassMap(elementType);
   }   

   TempClassMap getTempClassMap(NSName elementType)
   {
	  TempClassMap classMap;

	  // Return the TempClassMap if it already exists. Otherwise, create it.

	  if ((classMap = (TempClassMap)classMaps.get(elementType.qualified)) == null)
	  {
		 classMap = new TempClassMap(elementType);
		 classMaps.put(elementType.qualified, classMap);
	  }
	  return classMap;
   }   

   //***********************************************************************
   // Methods for adding and getting temporary tables
   //***********************************************************************

   TempTable getTempClassTable(String qualifiedElementType)
	  throws InvalidMapException
   {
	  // Get the class table for the specified element type and add it if it
	  // doesn't exist. (See also comments in addTempClassTable().)

	  TempTable table;

	  // Check if a table has already been defined for the element type. If not,
	  // create a new table and add it to the list of tables by element type.

	  table = (TempTable)classTablesByElementType.get(qualifiedElementType);
	  if (table == null)
	  {
		 table = new TempTable();
		 classTablesByElementType.put(qualifiedElementType, table);
	  }
	  return table;
   }   

   TempTable addTempClassTable(String qualifiedElementType, String tableName)
	  throws InvalidMapException
   {
	  // Add a new table for the specified class (element type).
	  //
	  // Note that the specified table might already exist but not have a name.
	  // This occurs when one class refers to a second class and the second
	  // class hasn't yet been defined, but we need to add primary or foreign
	  // keys or order columns to the table of the second class.
	  //
	  // To solve this problem, we store tables keyed both by element type and
	  // by table name. The list of tables by element type is a (possibly equal)
	  // superset of the list of tables by table name. We store tables by name
	  // to prevent two properties or element types from using the same table.

	  TempTable table;

	  // Check if the table has already been defined. If so, throw an error, as
	  // it means that another class or property is already using the table.

	  if (classTablesByName.get(tableName) != null)
		 throw new InvalidMapException("More than one class mapped to the table: " + tableName);

	  if (propertyTables.get(tableName) != null)
		 throw new InvalidMapException("The table " + tableName + " is used as both a property table and a class table.");

	  // Check if a table has been defined for an element type and, if so,
	  // set the name and add it to the list of tables by name. Otherwise,
	  // create a new table and add it to both lists.

	  table = (TempTable)classTablesByElementType.get(qualifiedElementType);
	  if (table != null)
	  {
		 table.name = tableName;
	  }
	  else
	  {
		 table = new TempTable(tableName);
		 classTablesByElementType.put(qualifiedElementType, table);
	  }
	  classTablesByName.put(tableName, table);
	  return table;
   }   

   TempTable addTempPropertyTable(String tableName) throws InvalidMapException
   {
	  TempTable table;

	  // Check that the table isn't already being used.

	  if (classTablesByName.get(tableName) != null)
		 throw new InvalidMapException("The table " + tableName + " is used as both a property table and a class table.");

	  if (propertyTables.get(tableName) != null)
		 throw new InvalidMapException("More than one property is mapped to the table " + tableName);

	  // Add a new table.

	  table = new TempTable(tableName);
	  propertyTables.put(tableName, table);
	  return table;
   }   

   //***********************************************************************
   // Methods for adding and getting temporary table maps
   //***********************************************************************

   TempRootTableMap addTempRootTableMap(TempTable table)
	  throws InvalidMapException
   {
	  TempRootTableMap rootTableMap;

	  if (rootTableMaps.get(table) != null)
		 throw new InvalidMapException("Table mapped as a root table more than once: " + table.name);
	  rootTableMap = new TempRootTableMap();
	  rootTableMaps.put(table, rootTableMap);
	  return rootTableMap;
   }   

   TempTableMap addTempClassTableMap(TempClassMap classMap)
	  throws InvalidMapException
   {
	  TempTableMap tableMap;

	  tableMap = addTempTableMap(classMap.table);
	  tableMap.type = TableMap.TYPE_CLASSTABLE;
	  tableMap.elementType = classMap.name.local;
	  tableMap.prefixedElementType = classMap.name.prefixed;
	  return tableMap;
   }   

   TempTableMap addTempPropTableMap(TempPropertyMap propMap)
	  throws InvalidMapException
   {
	  TempTableMap tableMap;

	  tableMap = addTempTableMap(propMap.table);
	  tableMap.type = TableMap.TYPE_PROPERTYTABLE;
	  return tableMap;
   }   

   TempTableMap addTempTableMap(TempTable table) throws InvalidMapException
   {
	  if (mappedTables.containsKey(table))
		 throw new InvalidMapException("More than one class or property mapped to the table " + table.name);
	  mappedTables.put(table, obj);
	  return getTempTableMap(table);
   }   

   TempTableMap getTempTableMap(TempTable table)
   {
	  TempTableMap tableMap;

	  if ((tableMap = (TempTableMap)tableMaps.get(table)) == null)
	  {
		 tableMap = new TempTableMap(table);
		 tableMaps.put(table, tableMap);
	  }
	  return tableMap;
   }   

   //***********************************************************************
   // Namespace methods
   //***********************************************************************

   void addNamespace(String prefix, String uri) throws InvalidMapException
   {
	  // Allocate a new Hashtable if this is the first namespace declaration.
	  // Note that namespaceURIs is used as a flag for whether namespaces are
	  // being used.

	  if (namespaceURIs == null)
	  {
		 namespaceURIs = new Hashtable();
	  }

	  // Check that the prefix and URI haven't already been mapped, then add
	  // them to the list of namespace prefixes and URIs.

	  if (namespaceURIs.containsKey(prefix))
		 throw new InvalidMapException ("Prefix " + prefix + " mapped more than once.");

	  if (namespaceURIs.contains(uri))
		 throw new InvalidMapException ("URI " + uri + " mapped more than once.");

	  namespaceURIs.put(prefix, uri);
   }   

   NSName getNSName(String prefixedName)
   {
	  return NSName.getNSName(prefixedName, namespaceURIs);
   }   

   //***********************************************************************
   // Methods for generating table and column maps from class maps
   //***********************************************************************

   void createTableMapsFromClassMaps() throws InvalidMapException
   {
	  // To create the table maps from the class maps, we do the following:
	  // 1) Check that each related class was mapped separately.
	  // 2) Process the class maps. This creates the table and column maps.
	  // 3) Process the root class maps. This creates the root table maps.

	  checkRelatedClasses();
	  processClassMaps();
	  processRootClassMaps();
   }   

   //***********************************************************************
   // Private methods for generating table and column maps from class maps
   //***********************************************************************

   void checkRelatedClasses() throws InvalidMapException
   {
	  // For each TempClassMap that was created (listed in classMaps),
	  // check that it was also mapped (listed in mappedClasses). A
	  // TempClassMap could have been created but not mapped if it was
	  // listed as a related class of a mapped class.

	  Enumeration     e;
	  TempClassMap    classMap;

	  e = classMaps.elements();
	  while (e.hasMoreElements())
	  {
		 classMap = (TempClassMap)e.nextElement();
		 if (!mappedClasses.containsKey(classMap.name.qualified))
			throw new InvalidMapException("Element type " + classMap.name.qualified + " was listed as a related class but was never mapped.");
	  }
   }   

   void processClassMaps() throws InvalidMapException
   {
	  Enumeration  e;
	  TempTableMap tableMap;
	  TempClassMap classMap;

	  // For each element type mapped to a class table or root table, create
	  // a map for the table and then process the maps for the attributes,
	  // subelements, and PCDATA of the element type.

	  e = classMaps.elements();
	  while (e.hasMoreElements())
	  {
		 classMap = (TempClassMap)e.nextElement();
		 if ((classMap.type == ClassMap.TYPE_IGNOREROOT) ||
			 (classMap.type == ClassMap.TYPE_PASSTHROUGH)) continue;
		 tableMap = addTempClassTableMap(classMap);
		 processSubMaps(classMap.attributeMaps, tableMap, ColumnMap.TYPE_TOATTRIBUTE);
		 processSubMaps(classMap.subElementTypeMaps, tableMap, ColumnMap.TYPE_TOELEMENTTYPE);
		 if (classMap.pcdataMap != null)
		 {
			processPropertyMap(classMap.pcdataMap, tableMap, ColumnMap.TYPE_TOPCDATA);
		 }
	  }
   }   

   void processSubMaps(Hashtable subMaps, TempTableMap classTableMap, int columnType)
	  throws InvalidMapException
   {
	  // This method processes hashtables containing maps subordinate to the
	  // class map. These hashtables can contain either property maps only
	  // (the hashtable for attributes) or a mixture of property maps and
	  // related class maps (the hashtable for subelement types).

	  Enumeration   e;
	  Object        map;

	  e = subMaps.elements();
	  while (e.hasMoreElements())
	  {
		 map = e.nextElement();
		 if (map instanceof TempPropertyMap)
		 {
			processPropertyMap((TempPropertyMap)map, classTableMap, columnType);
		 }
		 else // if (map instanceof TempRelatedClassMap)
		 {
			processRelatedClassMap((TempRelatedClassMap)map, classTableMap);
		 }
	  }
   }   

   void processPropertyMap(TempPropertyMap propMap, TempTableMap classTableMap, int columnType)
	  throws InvalidMapException
   {
	  TempTableMap propTableMap;

	  // If the property is mapped to a column, then create the corresponding
	  // column map. Otherwise, create the property table map and column map.

	  if (propMap.type == PropertyMap.TYPE_TOCOLUMN)
	  {
		 createPropColumnMap(classTableMap, propMap, columnType);
	  }
	  else // if (propMap.type == PropertyMap.TYPE_TOPROPERTYTABLE)
	  {
		 propTableMap = createPropTableMap(classTableMap, propMap);
		 createPropColumnMap(propTableMap, propMap, columnType);
	  }
   }   

   TempTableMap createPropTableMap(TempTableMap parentTableMap, TempPropertyMap propMap)
	  throws InvalidMapException
   {
	  TempTableMap propTableMap;

	  // Create a table map for the property
	  propTableMap = addTempPropTableMap(propMap);

	  // Link the parent table map to the property table map

	  parentTableMap.relatedTables.addElement(propTableMap);
	  parentTableMap.parentKeyIsCandidate.addElement(
							new Boolean(propMap.linkInfo.parentKeyIsCandidate));
	  parentTableMap.parentKeys.addElement(propMap.linkInfo.parentKey);
	  parentTableMap.childKeys.addElement(propMap.linkInfo.childKey);

	  // Add the order column information, if any. Note that even if there is
	  // no order column, we still add a null object because we keep parallel
	  // lists of information about related tables.

	  parentTableMap.orderColumns.addElement(propMap.orderInfo.orderColumn);

	  return propTableMap;
   }   

   void createPropColumnMap(TempTableMap tableMap, TempPropertyMap propMap, int columnType)
	  throws InvalidMapException
   {
	  TempColumnMap columnMap;

	  if (columnType == ColumnMap.TYPE_TOELEMENTTYPE)
	  {
		 columnMap = tableMap.addElementTypeColumnMap(propMap.column);
	  }
	  else // TOATTRIBUTE, TOPCDATA
	  {
		 columnMap = tableMap.addPropertyColumnMap(propMap.column);
	  }
	  columnMap.type = columnType;
	  columnMap.property = propMap.name.local;
	  columnMap.prefixedProperty = propMap.name.prefixed;
	  columnMap.multiValued = propMap.multiValued;
	  columnMap.orderColumn = propMap.orderInfo.orderColumn;
   }   

   void processRelatedClassMap(TempRelatedClassMap relatedMap, TempTableMap classTableMap)
	  throws InvalidMapException
   {
	  TempTableMap relatedTableMap;

	  switch (relatedMap.classMap.type)
	  {
		 case ClassMap.TYPE_TOCLASSTABLE:
		 case ClassMap.TYPE_TOROOTTABLE:

			// Get the table map for the related class. Add this table map
			// and its corresponding information (link info, order info) to
			// the map of the current table.

			relatedTableMap = getTempTableMap(relatedMap.classMap.table);
			classTableMap.relatedTables.addElement(relatedTableMap);
			classTableMap.parentKeyIsCandidate.addElement(
						 new Boolean(relatedMap.linkInfo.parentKeyIsCandidate));
			classTableMap.parentKeys.addElement(relatedMap.linkInfo.parentKey);
			classTableMap.childKeys.addElement(relatedMap.linkInfo.childKey);
			classTableMap.orderColumns.addElement(
											  relatedMap.orderInfo.orderColumn);
			break;

		 case ClassMap.TYPE_IGNOREROOT:
			throw new InvalidMapException("The element type " + relatedMap.classMap.name.qualified + " was mapped as an ignored root, but listed as a related class.");

		 case ClassMap.TYPE_PASSTHROUGH:
			throw new InvalidMapException("Class mapped as pass-through: " + relatedMap.classMap.name.qualified);
	  }
   }   

   void processRootClassMaps() throws InvalidMapException
   {
	  // Process each root class map and either:
	  // a) Create the corresponding root table map, or
	  // b) Add the root class as an ignored element type to all of
	  //    the table maps of its children.

	  Enumeration      e;
	  TempRootClassMap rootClassMap;

	  e = rootClassMaps.elements();
	  while (e.hasMoreElements())
	  {
		 rootClassMap = (TempRootClassMap)e.nextElement();
		 if (rootClassMap.classMap.type == ClassMap.TYPE_TOROOTTABLE)
		 {
			processRootTableClassMap(rootClassMap);
		 }
		 else if (rootClassMap.classMap.type == ClassMap.TYPE_IGNOREROOT)
		 {
			processIgnoreRootClassMap(rootClassMap);
		 }
		 else // TYPE_TOCLASSTABLE, TYPE_PASSTHROUGH
		 {
			throw new InvalidMapException("Root classes must be mapped to root tables or ignored: " + rootClassMap.classMap.name.qualified);
		 }
	  }
   }   

   void processRootTableClassMap(TempRootClassMap rootClassMap)
	  throws InvalidMapException
   {
	  TempRootTableMap rootTableMap;
	  TempTableMap     tableMap;

	  // Get the table map for the table to which the root element is mapped.

	  tableMap = (TempTableMap)tableMaps.get(rootClassMap.classMap.table);
	  if (tableMap == null)
		 // We shouldn't hit this...
		 throw new InvalidMapException("Surprise! Root element map points to non-existent table: " + rootClassMap.classMap.name.qualified);

	  // Create a root table map and set the table map, candidate key, and
	  // order column.

	  rootTableMap = addTempRootTableMap(rootClassMap.classMap.table);
	  rootTableMap.tableMap = tableMap;
	  if (rootTableMap.tableMap.type != TableMap.TYPE_CLASSTABLE)
		 throw new InvalidMapException("Root table must be mapped as TableMap.TYPE_CLASSTABLE.");
	  if (rootClassMap.linkInfo != null)
	  {
		 rootTableMap.candidateKey = rootClassMap.linkInfo.childKey;
	  }
	  rootTableMap.orderColumn = rootClassMap.orderInfo.orderColumn;
   }   

   void processIgnoreRootClassMap(TempRootClassMap rootClassMap)
	  throws InvalidMapException
   {
	  TempRootTableMap    rootTableMap;
	  TempRelatedClassMap relatedMap;
	  Object              tempMap;
	  Enumeration         e;

	  // Get the children of the ignored root element and process as follows:
	  // 1) Check that each was mapped as a class, not a property.
	  // 2) Create a root table map for the child class table.
	  // 3) In the root table map, set the ignored root type.

	  e = rootClassMap.classMap.subElementTypeMaps.elements();
	  while (e.hasMoreElements())
	  {
		 tempMap = e.nextElement();
		 if (tempMap instanceof TempRelatedClassMap)
		 {
			relatedMap = (TempRelatedClassMap)tempMap;
			rootTableMap = addTempRootTableMap(relatedMap.classMap.table);
			rootTableMap.tableMap = (TempTableMap)tableMaps.get(relatedMap.classMap.table);
			if (rootTableMap.tableMap.type != TableMap.TYPE_CLASSTABLE)
			   throw new InvalidMapException("Root table must be mapped as TableMap.TYPE_CLASSTABLE.");
			if (relatedMap.linkInfo != null)
			{
			   rootTableMap.candidateKey = relatedMap.linkInfo.childKey;
			}
			rootTableMap.orderColumn = relatedMap.orderInfo.orderColumn;
			rootTableMap.ignoredRootType = rootClassMap.classMap.name.local;
			rootTableMap.prefixedIgnoredRootType = rootClassMap.classMap.name.prefixed;
		 }
		 else // if (tempMap instanceof TempPropertyMap)
		 {
			throw new InvalidMapException("The ignored root element type " + rootClassMap.classMap.name.qualified + " has a child element type that is mapped as a property.");
		 }
	  }
   }   

   //***********************************************************************
   // methods for creating a Map from temporary structures
   //***********************************************************************

   Map createMapFromTemp() throws InvalidMapException
   {
	  convertTemp();
	  return new Map(map_tables,
					 map_tableMaps,
					 map_rootTableMaps,
					 map_classMaps,
					 map_rootClassMaps,
					 map_prefixes,
					 map_uris,
					 generateKeys,
					 emptyStringIsNull,
					 dateFormatter,
					 timeFormatter,
					 timestampFormatter);
   }   

   //***********************************************************************
   // Private methods for creating a Map from temporary structures
   //***********************************************************************

   void convertTemp() throws InvalidMapException
   {
	  convertTables();
	  convertTableMaps();
	  convertClassMaps();
	  convertNamespaces();
   }   

   void convertTables()
	  throws InvalidMapException
   {
	  Enumeration      e;
	  Table            table;
	  int              tableNumber = 0; // Table numbers are 0-based.

	  // Create a new array to hold the Tables.

	  map_tables = new Table[classTablesByName.size() + propertyTables.size()];

	  // Convert the class tables first, then the property tables.

	  e = classTablesByName.elements();
	  while (e.hasMoreElements())
	  {
		 convertTable((TempTable)e.nextElement(), tableNumber++);
	  }

	  e = propertyTables.elements();
	  while (e.hasMoreElements())
	  {
		 convertTable((TempTable)e.nextElement(), tableNumber++);
	  }
   }   

   void convertTable(TempTable tempTable, int tableNumber)
	  throws InvalidMapException
   {
	  // 5/29/00, Ronald Bourret
	  // Due to changes in Column, changed columnNumber to
	  // rowObjectIndex and used 0-based numbers.

	  Enumeration e;
	  Column[]    columns;
	  TempColumn  tempColumn;
	  int         rowObjectIndex = -1; // Values stored in 0-based array in Row

	  // Create an array for the columns in the table.
	  columns = new Column[tempTable.columns.size()];

	  // Create a new Table and add it to the list of tables.
	  map_tables[tableNumber] = new Table(columns, tempTable.name, tableNumber);

	  // Convert the columns in the table.
	  e = tempTable.columns.elements();
	  while (e.hasMoreElements())
	  {
		 rowObjectIndex++;
		 tempColumn = (TempColumn)e.nextElement();
		 columns[rowObjectIndex] = new Column(tempColumn.name, rowObjectIndex, tempColumn.type, tempColumn.length);
	  }
   }   

   void convertTableMaps()
   {
	  // Converting the TempTableMaps is a multi-part process:
	  // 1) Build a Hashtable of TableInfo structures.
	  // 2) Convert all table map information except for the information about
	  //    the related tables. This builds the TableMap objects.
	  // 3) Convert the related table information, using the TableMaps built
	  //    in step 2.
	  // 4) Convert the root table information.

	  buildTableInfos();
	  convertTableMaps1();
	  convertTableMaps2();
	  convertRootTableMaps();
   }   

   void buildTableInfos()
   {
	  Table     table;
	  TableInfo tableInfo;

	  // For each table, create a TableInfo object and store it in a hashtable
	  // that is keyed by table name. Each TableInfo contains the Table object
	  // and a Hashtable of Columns, keyed by column name.

	  for (int i = 0; i < map_tables.length; i++)
	  {
		 table = map_tables[i];
		 tableInfo = new TableInfo(table);
		 tableInfos.put(table.name, tableInfo);
		 for (int j = 0; j < table.columns.length; j++)
		 {
			tableInfo.columns.put(table.columns[j].name, table.columns[j]);
		 }
	  }
   }   

   void convertTableMaps1()
   {
	  Enumeration  e;
	  TableInfo    tableInfo;
	  TempTableMap tempTableMap;
	  TableMap     tableMap;
	  ColumnMap[]  columnMaps;
	  int          mapNumber;

	  // Allocate an array of TableMaps. There is an implicit assumption here
	  // that there is a 1-to-1 mapping between Tables and TableMaps. If this
	  // is not true, something else will blow up and catch the error.

	  map_tableMaps = new TableMap[map_tables.length];

	  // Get an enumeration over the Hashtable of TempTableMaps and process
	  // them one by one.

	  e = tableMaps.elements();
	  while (e.hasMoreElements())
	  {
		 // Get the next TempTableMap and its corresponding TableInfo.

		 tempTableMap = (TempTableMap)e.nextElement();
		 tableInfo = (TableInfo)tableInfos.get(tempTableMap.table.name);

		 // Convert the TempColumnMaps

		 columnMaps = new ColumnMap[tempTableMap.elementTypeColumnMaps.size() +
									tempTableMap.propertyColumnMaps.size()];
		 processColumnMaps(tempTableMap.elementTypeColumnMaps, columnMaps, tableInfo.columns, 0);
		 processColumnMaps(tempTableMap.propertyColumnMaps, columnMaps, tableInfo.columns, tempTableMap.elementTypeColumnMaps.size());

		 // Create a new TableMap and store it for later use.

		 tableMap = new TableMap(tableInfo.table,
								 tempTableMap.type,
								 tempTableMap.elementType,
								 tempTableMap.prefixedElementType,
								 columnMaps,
								 tempTableMap.relatedTables.size());
		 map_tableMaps[tableInfo.table.number] = tableMap;
	  }
   }   

   void processColumnMaps(Hashtable tempColumnMaps, ColumnMap[] columnMaps, Hashtable columns, int mapNumber)
   {
	  Enumeration   e;
	  TempColumnMap tempColumnMap;
	  Column        column, orderColumn;

	  // Get an enumeration over the hashtable of TempColumnMaps and
	  // create a ColumnMap for each one.

	  e = tempColumnMaps.elements();
	  while (e.hasMoreElements())
	  {
		 tempColumnMap = (TempColumnMap)e.nextElement();
		 column = (Column)columns.get(tempColumnMap.column.name);
		 orderColumn = (tempColumnMap.orderColumn == null) ? null :
							(Column)columns.get(tempColumnMap.orderColumn.name);
		 columnMaps[mapNumber++] = new ColumnMap(tempColumnMap.type,
												 column,
												 orderColumn,
												 tempColumnMap.property,
												 tempColumnMap.prefixedProperty,
												 tempColumnMap.multiValued);
	  }
   }   

   void convertTableMaps2()
   {
	  Enumeration  e;
	  TempTableMap tempTableMap, relatedMap;
	  TempColumn   tempOrderColumn;
	  Vector       parentKey, childKey;
	  TableMap     tableMap;
	  TableInfo    tableInfo, relatedInfo;

	  // Process the related tables in each table map. We process these in a
	  // separate step because it guarantees that the map for each related table
	  // already exists, which simplifies the code.

	  e = tableMaps.elements();
	  while (e.hasMoreElements())
	  {
		 // Get the next TempTableMap and its TableInfo.
		 tempTableMap = (TempTableMap)e.nextElement();
		 tableInfo = (TableInfo)tableInfos.get(tempTableMap.table.name);
		 tableMap = map_tableMaps[tableInfo.table.number];

		 // Process the related table information.
		 for (int i = 0; i < tableMap.relatedTables.length; i++)
		 {
			// Get the TempTableMap of the related table and its TableInfo, then
			// set the information about related tables in the current TableMap.
			relatedMap = (TempTableMap)tempTableMap.relatedTables.elementAt(i);
			relatedInfo = (TableInfo)tableInfos.get(relatedMap.table.name);

			// Set the TableMap of the related table.
			tableMap.relatedTables[i] = map_tableMaps[relatedInfo.table.number];

			// Transfer the information about which key is the candidate
			tableMap.parentKeyIsCandidate[i] = ((Boolean)tempTableMap.parentKeyIsCandidate.elementAt(i)).booleanValue();

			// Allocate the arrays for the key columns and convert them. The
			// parent key is always in the table whose map is being converted
			// and the child key is always in the related table.

			parentKey = (Vector)tempTableMap.parentKeys.elementAt(i);
			childKey = (Vector)tempTableMap.childKeys.elementAt(i);
			tableMap.parentKeys[i] = new Column[parentKey.size()];
			tableMap.childKeys[i] = new Column[childKey.size()];
			convertKeyColumns(tableMap.parentKeys[i],
							  parentKey,
							  tableInfo.columns);
			convertKeyColumns(tableMap.childKeys[i],
							  childKey,
							  relatedInfo.columns);

			// Get the name of the order column and get the actual Column from
			// either the table whose map is being converted or from the related
			// table. Remember that the order column is in the same table as the
			// child key.

			tempOrderColumn = (TempColumn)tempTableMap.orderColumns.elementAt(i);
			if (tempOrderColumn == null)
			{
			   tableMap.orderColumns[i] = null;
			}
			else if (tableMap.parentKeyIsCandidate[i])
			{
			   tableMap.orderColumns[i] = (Column)relatedInfo.columns.get(tempOrderColumn.name);
			}
			else
			{
			   tableMap.orderColumns[i] = (Column)tableInfo.columns.get(tempOrderColumn.name);
			}
		 }
	  }
   }   

   void convertKeyColumns(Column[] keyColumns, Vector tempColumns, Hashtable columns)
   {
	  TempColumn tempColumn;

	  // Convert a Vector of TempColumns to an array of Columns.

	  for (int i = 0; i < keyColumns.length; i++)
	  {
		 tempColumn = (TempColumn)tempColumns.elementAt(i);
		 keyColumns[i] = (Column)columns.get(tempColumn.name);
	  }
   }   

   void convertRootTableMaps()
   {
	  Enumeration      e;
	  TempRootTableMap tempRootTableMap;
	  RootTableMap     rootTableMap;
	  TableInfo        tableInfo;
	  TempColumn       tempColumn;
	  Column[]         candidateKey;
	  Column           orderColumn;
	  String           tableName;

	  e = rootTableMaps.elements();
	  while (e.hasMoreElements())
	  {
		 // Get the next TempRootTableMap and its TableInfo
		 tempRootTableMap = (TempRootTableMap)e.nextElement();
		 tableName = tempRootTableMap.tableMap.table.name;
		 tableInfo = (TableInfo)tableInfos.get(tableName);

		 // Build the candidate key
		 candidateKey = new Column[tempRootTableMap.candidateKey.size()];
		 convertKeyColumns(candidateKey,
						   tempRootTableMap.candidateKey,
						   tableInfo.columns);

		 // Get the order column
		 orderColumn = (tempRootTableMap.orderColumn == null) ? null :
			   (Column)tableInfo.columns.get(tempRootTableMap.orderColumn.name);

		 // Build and store the new RootTableMap
		 rootTableMap = new RootTableMap(map_tableMaps[tableInfo.table.number],
										 tempRootTableMap.ignoredRootType,
									   tempRootTableMap.prefixedIgnoredRootType,
										 candidateKey,
										 orderColumn);
		 map_rootTableMaps.put(tableName, rootTableMap);
	  }
   }   

   void convertClassMaps()
   {
	  Enumeration  e;

	  e = classMaps.elements();
	  while (e.hasMoreElements())
	  {
		 convertClassMap((TempClassMap)e.nextElement());
	  }
	  convertRootClassMaps();
   }   

   void convertClassMap(TempClassMap tempClassMap)
   {
	  ClassMap     classMap;
	  TableInfo    tableInfo;

	  // Allocate a corresponding ClassMap.
	  classMap = getClassMap(tempClassMap.name.qualified);
	  classMap.name = tempClassMap.name;
	  classMap.type = tempClassMap.type;

	  if (tempClassMap.type != ClassMap.TYPE_IGNOREROOT)
	  {
		 // If the element type is not ignored, then set the table. Also
		 // set the attribute, PCDATA, and sub-element type maps, if any.

		 tableInfo = (TableInfo)tableInfos.get(tempClassMap.table.name);
		 classMap.table = tableInfo.table;

		 if (tempClassMap.attributeMaps.size() > 0)
		 {
			convertSubMaps(classMap.attributeMaps,
						   tempClassMap.attributeMaps,
						   tableInfo);
		 }

		 if (tempClassMap.pcdataMap != null)
		 {
			classMap.pcdataMap = convertPropertyMap(tempClassMap.pcdataMap, tableInfo);
		 }

		 if (tempClassMap.subElementTypeMaps.size() > 0)
		 {
			convertSubMaps(classMap.subElementTypeMaps,
						   tempClassMap.subElementTypeMaps,
						   tableInfo);
		 }
	  }
	  else // if (tempClassMap.type == ClassMap.TYPE_IGNOREROOT)
	  {
		 // If the element type was ignored, then it must have one or more
		 // sub-element type maps. Convert these. Note that there is no
		 // class (parent) table/table info in this case.

		 convertSubMaps(classMap.subElementTypeMaps,
						tempClassMap.subElementTypeMaps,
						null);
	  }
   }   

   void convertSubMaps(Hashtable dest, Hashtable src, TableInfo parentTableInfo)
   {
	  // This method converts hashtables containing maps subordinate to the
	  // class map. These hashtables can contain either property maps only
	  // (the hashtable maps for attributes) or a mixture of property maps and
	  // related class maps (the hashtable for subelement types).

	  Enumeration         e;
	  Object              tempMap;
	  TempPropertyMap     tempPropMap;
	  PropertyMap         propMap;
	  TempRelatedClassMap tempRelatedClassMap;
	  RelatedClassMap     relatedClassMap;

	  e = src.elements();
	  while (e.hasMoreElements())
	  {
		 tempMap = e.nextElement();

		 if (tempMap instanceof TempPropertyMap)
		 {
			tempPropMap = (TempPropertyMap)tempMap;
			propMap = convertPropertyMap(tempPropMap, parentTableInfo);
			dest.put(tempPropMap.name.qualified, propMap);
		 }
		 else // if (tempMap instanceof TempRelatedClassMap)
		 {
			tempRelatedClassMap = (TempRelatedClassMap)tempMap;
			relatedClassMap = convertRelatedClassMap(tempRelatedClassMap, parentTableInfo);
			dest.put(tempRelatedClassMap.classMap.name.qualified, relatedClassMap);
		 }
	  }
   }   

   PropertyMap convertPropertyMap(TempPropertyMap tempPropMap, TableInfo parentTableInfo)
   {
	  PropertyMap propMap;
	  TableInfo   propTableInfo;

	  // Create a new property map and set its type.

	  propMap = new PropertyMap();
	  if (tempPropMap.name.local != null)
	  {
		 propMap.name = tempPropMap.name;
	  }
	  propMap.type = tempPropMap.type;
	  propMap.multiValued = tempPropMap.multiValued;

	  if (tempPropMap.table != null)
	  {
		 // If the property is mapped to a table, get the TableInfo for that
		 // table and set the table, column, link, and order information. Note
		 // that the column occurs in the property table, not the parent table
		 // and that the order column occurs in the table with the child key.

		 propTableInfo = (TableInfo)tableInfos.get(tempPropMap.table.name);
		 propMap.table = propTableInfo.table;
		 propMap.column = (Column)propTableInfo.columns.get(tempPropMap.column.name);
		 propMap.linkInfo = convertLinkInfo(tempPropMap.linkInfo, parentTableInfo, propTableInfo);
		 if (propMap.linkInfo.parentKeyIsCandidate)
		 {
			propMap.orderInfo = convertOrderInfo(tempPropMap.orderInfo, propTableInfo);
		 }
		 else
		 {
			propMap.orderInfo = convertOrderInfo(tempPropMap.orderInfo, parentTableInfo);
		 }
	  }
	  else
	  {
		 // If the property is mapped to a column, set the column and order
		 // information. Note that these occur in the parent table.

		 propMap.column = (Column)parentTableInfo.columns.get(tempPropMap.column.name);
		 propMap.orderInfo = convertOrderInfo(tempPropMap.orderInfo, parentTableInfo);
	  }
	  return propMap;
   }   

   RelatedClassMap convertRelatedClassMap(TempRelatedClassMap tempRelatedMap, TableInfo parentTableInfo)
   {
	  RelatedClassMap relatedMap;
	  TableInfo       relatedInfo;

	  // Create a new RelatedClassMap and set the ClassMap. Note that
	  // getClassMap() might create the map.

	  relatedMap = new RelatedClassMap();
	  relatedMap.classMap = (ClassMap)getClassMap(tempRelatedMap.classMap.name.qualified);

	  // Get the TableInfo for the related class' table.
	  relatedInfo = (TableInfo)tableInfos.get(tempRelatedMap.classMap.table.name);

	  // Convert the link info.

	  relatedMap.linkInfo = convertLinkInfo(tempRelatedMap.linkInfo, parentTableInfo, relatedInfo);

	  // Convert the order info.

	  if (relatedMap.linkInfo.parentKeyIsCandidate)
	  {
		 relatedMap.orderInfo = convertOrderInfo(tempRelatedMap.orderInfo, relatedInfo);
	  }
	  else
	  {
		 relatedMap.orderInfo = convertOrderInfo(tempRelatedMap.orderInfo, parentTableInfo);
	  }

	  return relatedMap;
   }   

   LinkInfo convertLinkInfo(TempLinkInfo tempLinkInfo, TableInfo parentInfo, TableInfo childInfo)
   {
	  boolean  parentKeyExists;
	  LinkInfo linkInfo;

	  // Create a new LinkInfo and copy the easy stuff over.

	  linkInfo = new LinkInfo();
	  linkInfo.generateKey = tempLinkInfo.generateKey;
	  linkInfo.parentKeyIsCandidate = tempLinkInfo.parentKeyIsCandidate;

	  // Create the parent and child key arrays. Note that we do not create
	  // create the parent key array when the size of the Vector is 0. This
	  // is the special case when LinkInfo is used in RootClassMap.

	  parentKeyExists = (tempLinkInfo.parentKey.size() > 0);
	  if (parentKeyExists)
	  {
		 linkInfo.parentKey = new Column[tempLinkInfo.parentKey.size()];
	  }
	  linkInfo.childKey = new Column[tempLinkInfo.childKey.size()];

	  // Convert the key columns. Note that we only convert the parent key
	  // columns if they exist, which they don't in the special case where
	  // LinkInfo is used in RootClassMap.

	  if (parentKeyExists)
	  {
		 convertKeyColumns(linkInfo.parentKey,
						   tempLinkInfo.parentKey,
						   parentInfo.columns);
	  }
	  convertKeyColumns(linkInfo.childKey,
						tempLinkInfo.childKey,
						childInfo.columns);

	  return linkInfo;
   }   

   OrderInfo convertOrderInfo(TempOrderInfo tempOrderInfo, TableInfo tableInfo)
   {
	  OrderInfo orderInfo;

	  // Create a new OrderInfo and transfer the order information.
	  orderInfo = new OrderInfo();
	  orderInfo.generateOrder = tempOrderInfo.generateOrder;
	  if (tempOrderInfo.orderColumn != null)
	  {
		 orderInfo.orderColumn = (Column)tableInfo.columns.get(tempOrderInfo.orderColumn.name);
	  }

	  return orderInfo;
   }   

   void convertRootClassMaps()
   {
	  Enumeration      e;
	  TempRootClassMap tempRootClassMap;
	  RelatedClassMap  relatedClassMap;
	  RootClassMap     rootClassMap;
	  TableInfo        rootTableInfo;

	  // Get an enumeration over the root class maps and convert these. Remember
	  // that RootClassMaps are just RelatedClassMaps in disguise, so we can
	  // convert them with convertRelatedClassMap().

	  e = rootClassMaps.elements();
	  while (e.hasMoreElements())
	  {
		 tempRootClassMap = (TempRootClassMap)e.nextElement();
		 rootClassMap = convertRootClassMap(tempRootClassMap);
		 map_rootClassMaps.put(tempRootClassMap.classMap.name.qualified, rootClassMap);
	  }
   }   

   RootClassMap convertRootClassMap(TempRootClassMap tempRootMap)
   {
	  RootClassMap rootMap;
	  TableInfo    rootInfo;

	  // Create a new RootClassMap and set the ClassMap. Note that
	  // getClassMap() might create the map.

	  rootMap = new RootClassMap();
	  rootMap.classMap = (ClassMap)getClassMap(tempRootMap.classMap.name.qualified);

	  // Convert the link info and order info. Note that link info can only
	  // be null in the case where the root element type is mapped as
	  // IGNOREROOT. In this case, the order info is always null. 

	  if (tempRootMap.linkInfo != null)
	  {
		 // Get the TableInfo for the related class' table.
		 rootInfo = (TableInfo)tableInfos.get(tempRootMap.classMap.table.name);

		 // Convert the link and order info. Note that the order column is
		 // always in the "child" (root) table, regardless of the value of
		 // parentKeyIsCandidate. This is because there is no parent table.

		 rootMap.linkInfo = convertLinkInfo(tempRootMap.linkInfo, null, rootInfo);
		 rootMap.orderInfo = convertOrderInfo(tempRootMap.orderInfo, rootInfo);
	  }

	  return rootMap;
   }   



   ClassMap getClassMap(String qualifiedElementType)
   {
	  ClassMap classMap;

	  if ((classMap = (ClassMap)map_classMaps.get(qualifiedElementType)) == null)
	  {
		 classMap = new ClassMap();
		 map_classMaps.put(qualifiedElementType, classMap);
	  }

	  return classMap;
   }   

   void convertNamespaces()
   {
	  int         size, i = 0;
	  Enumeration e;
	  String      prefix, uri;

	  if (namespaceURIs == null) return;
	  size = namespaceURIs.size();
	  if (size == 0) return;

	  map_prefixes = new String[size];
	  map_uris = new String[size];

	  e = namespaceURIs.keys();
	  while (e.hasMoreElements())
	  {
		 prefix = (String)e.nextElement();
		 uri = (String)namespaceURIs.get(prefix);
		 map_prefixes[i] = prefix;
		 map_uris[i] = uri;
		 i++;
	  }
   }   

   // *************************************************************************
   // Inner classes
   // *************************************************************************

   class TableInfo
   {
	  Table     table;
	  Hashtable columns = new Hashtable();

	  TableInfo(Table table)
	  {
		 this.table = table;
	  }
   }
}