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

package org.xmlmiddleware.xmldbms.filters;

import org.xmlmiddleware.utils.XMLName;
import org.xmlmiddleware.utils.XMLWriter;

import org.xmlmiddleware.xmldbms.maps.Table;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Serializes a FilterSet object to a character stream.
 *
 * <p>If you want to use a specific encoding, the Writer must be an OutputStreamWriter
 * or a subclass of an OutputStreamWriter. For example, you might use the following
 * code to write a map file with the Shift_JIS encoding:</p>
 *
 * <pre>
 *    // Construct the FileOutputStream.
 *    OutputStream out = new FileOutputStream("sales.map");
 *    <br />
 *    // Construct the OutputStreamWriter with the Shift_JIS encoding. This may
 *    // throw an UnsupportedEncodingException.
 *    Writer writer = new OutputStreamWriter(out, "Shift_JIS");
 *    <br />
 *    // Construct the FilterSerializer.
 *    FilterSerializer serializer = new FilterSerializer(writer);
 *    <br />
 *    // Serialize the filter set.
 *    serializer.serialize(filterSet);
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * <p>If you want to use the default encoding, you can just use a FileWriter. However,
 * no encoding declaration will be written in the XML declaration. For example:</p>
 *
 * <pre>
 *    // Construct a new FileWriter.
 *    Writer writer = new FileWriter("sales.map");
 *    <br />
 *    // Construct the FilterSerializer.
 *    FilterSerializer serializer = new FilterSerializer(writer);
 *    <br />
 *    // Serialize the filter set.
 *    serializer.serialize(filterSet);
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * @author Ronald Bourret, 2002
 * @version 2.0
 */

public class FilterSerializer extends XMLWriter
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String FILTERSDTD = "filters.dtd";

   //**************************************************************************
   // Variables
   //**************************************************************************

   private FilterSet filterSet;
   private Hashtable uris = null;
   private Hashtable prefixes = null;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new FilterSerializer. */
   public FilterSerializer()
   {
      allocateAttrs(5);
   }

   /**
    * Construct a new FilterSerializer and set the Writer.
    *
    * @param writer The writer. The writer must implement the write(String,int,int)
    *    and write(int) methods.
    */
   public FilterSerializer(Writer writer)
   {
      super(writer);
      allocateAttrs(5);
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Serialize a FilterSet to the filter language.
    *
    * <p>No system or public ID is written in the DOCTYPE statement.</p>
    *
    * @param map The FilterSet.
    * @exception IOException Thrown if an I/O exception occurs.
    */
   public void serialize(FilterSet filterSet)
      throws IOException
   {
      this.filterSet = filterSet;
      serialize(null, null);
   }

   /**
    * Serialize a FilterSet to the filter language.
    *
    * @param filterSet The FilterSet.
    * @param systemID System ID of the DTD. If this is null, "filters.dtd" is used.
    * @param publicID Public ID of the DTD. May be null.
    * @exception IOException Thrown if an I/O exception occurs.
    */
   public void serialize(FilterSet filterSet, String systemID, String publicID)
      throws IOException
   {
      this.filterSet = filterSet;
      if (systemID == null)
      {
         systemID = FILTERSDTD;
      }
      serialize(systemID, publicID);
   }

   //**************************************************************************
   // Private methods - serialize()
   //**************************************************************************

   private void serialize(String systemID, String publicID)
      throws IOException
   {
      writeFilterSetStart(systemID, publicID);
      writeOptions();
      writeFilters();
      writeFilterSetEnd();
   }

   //**************************************************************************
   // Private methods - filter set serialization (in alphabetical order)
   //**************************************************************************

   private void writeFilter(FilterBase filterBase)
      throws IOException
   {
      writeElementStart(FilterConst.ELEM_FILTER, 0, false);

      if (filterBase instanceof RootFilter)
      {
         writeRootFilter((RootFilter)filterBase);
      }
      else // if (filterBase instanceof ResultSetFilter)
      {
         writeResultSetFilter((ResultSetFilter)filterBase);
      }

      writeTableFilters(filterBase);

      writeElementEnd(FilterConst.ELEM_FILTER);
   }

   private void writeFilterConditions(FilterConditions fc)
      throws IOException
   {
      Vector conditions;
      String condition;

      writeTable(fc.getTable());
      conditions = fc.getConditions();
      for (int i = 0; i < conditions.size(); i++)
      {
         condition = (String)conditions.elementAt(i);
         writeWhere(condition);
      }
   }

   private void writeFilters()
      throws IOException
   {
      Vector     filters;
      FilterBase filterBase;

      writeElementStart(FilterConst.ELEM_FILTERS, 0, false);

      filters = filterSet.getFilters();
      for (int i = 0; i < filters.size(); i++)
      {
         filterBase = (FilterBase)filters.elementAt(i);
         writeFilter(filterBase);
      }

      writeElementEnd(FilterConst.ELEM_FILTERS);
   }

   private void writeFilterSetEnd()
      throws IOException
   {
      writeElementEnd(FilterConst.ELEM_FILTERSET);
   }

   private void writeFilterSetStart(String systemID, String publicID)
      throws IOException
   {
      attrs[0] = FilterConst.ATTR_VERSION;
      values[0] = FilterConst.DEF_VERSION;
      attrs[1] = "xmlns";
      values[1] = FilterConst.URI_FILTERSV2;

      writeXMLDecl();
      writeDOCTYPE(FilterConst.ELEM_FILTERSET, systemID, publicID);
      writeElementStart(FilterConst.ELEM_FILTERSET, 2, false);
   }

   private void writeNamespaces()
      throws IOException
   {
      Hashtable   uris;
      Enumeration prefixes;
      String      prefix;

      uris = filterSet.getNamespaceURIs();

      // Write out the namespaces.

      prefixes = uris.keys();
      while (prefixes.hasMoreElements())
      {
         prefix = (String)prefixes.nextElement();
         attrs[0] = FilterConst.ATTR_PREFIX;
         values[0] = prefix;
         attrs[1] = FilterConst.ATTR_URI;
         values[1] = (String)uris.get(prefix);
         writeElementStart(FilterConst.ELEM_NAMESPACE, 2, true);
      }
   }

   private void writeOptions()
      throws IOException
   {
      writeElementStart(FilterConst.ELEM_OPTIONS, 0, false);
      writeNamespaces();
      writeWrappers();
      writeElementEnd(FilterConst.ELEM_OPTIONS);
   }

   private void writeRelatedTableFilter(RelatedTableFilter relatedTableFilter)
      throws IOException
   {
      attrs[0] = FilterConst.ATTR_PARENTKEY;
      values[0] = relatedTableFilter.getParentKeyName();
      attrs[1] = FilterConst.ATTR_CHILDKEY;
      values[1] = relatedTableFilter.getChildKeyName();
      writeElementStart(FilterConst.ELEM_RELATEDTABLEFILTER, 2, false);

      writeFilterConditions(relatedTableFilter);

      writeElementEnd(FilterConst.ELEM_RELATEDTABLEFILTER);
   }

   private void writeResultSetFilter(ResultSetFilter rsFilter)
      throws IOException
   {
      int    count = 0;
      String value;

      value = rsFilter.getResultSetName();
      if (value != null)
      {
         if (!value.equals(FilterConst.DEF_NAME))
         {
            attrs[count] = FilterConst.ATTR_NAME;
            values[count++] = value;
         }
      }

      value = rsFilter.getDatabaseName();
      if (value != null)
      {
         if (!value.equals(FilterConst.DEF_DATABASE))
         {
            attrs[count] = FilterConst.ATTR_DATABASE;
            values[count++] = value;
         }
      }

      value = rsFilter.getCatalogName();
      if (value != null)
      {
         attrs[count] = FilterConst.ATTR_CATALOG;
         values[count++] = value;
      }

      value = rsFilter.getSchemaName();
      if (value != null)
      {
         attrs[count] = FilterConst.ATTR_SCHEMA;
         values[count++] = value;
      }

      attrs[count] = FilterConst.ATTR_TABLE;
      values[count++] = rsFilter.getTableName();

      writeElementStart(FilterConst.ELEM_RESULTSETINFO, count, true);
   }

   private void writeRootFilter(RootFilter rootFilter)
      throws IOException
   {
      writeElementStart(FilterConst.ELEM_ROOTFILTER, 0, false);
      writeFilterConditions(rootFilter.getRootFilterConditions());
      writeElementEnd(FilterConst.ELEM_ROOTFILTER);
   }

   private void writeTable(Table table)
      throws IOException
   {
      int    count = 0;
      String value;

      value = table.getDatabaseName();
      if (value != null)
      {
         if (!value.equals(FilterConst.DEF_DATABASE))
         {
            attrs[count] = FilterConst.ATTR_DATABASE;
            values[count++] = value;
         }
      }

      value = table.getCatalogName();
      if (value != null)
      {
         attrs[count] = FilterConst.ATTR_CATALOG;
         values[count++] = value;
      }

      value = table.getSchemaName();
      if (value != null)
      {
         attrs[count] = FilterConst.ATTR_SCHEMA;
         values[count++] = value;
      }

      attrs[count] = FilterConst.ATTR_NAME;
      values[count++] = table.getTableName();

      writeElementStart(FilterConst.ELEM_TABLE, count, true);
   }

   private void writeTableFilter(TableFilter tableFilter)
      throws IOException
   {
      Enumeration relatedTableFilters;

      writeElementStart(FilterConst.ELEM_TABLEFILTER, 0, false);

      writeTable(tableFilter.getTable());

      relatedTableFilters = tableFilter.getRelatedTableFilters();
      while (relatedTableFilters.hasMoreElements())
      {
         writeRelatedTableFilter((RelatedTableFilter)relatedTableFilters.nextElement());
      }

      writeElementEnd(FilterConst.ELEM_TABLEFILTER);
   }

   private void writeTableFilters(FilterBase filterBase)
      throws IOException
   {
      Enumeration tableFilters;

      tableFilters = filterBase.getTableFilters();
      while (tableFilters.hasMoreElements())
      {
         writeTableFilter((TableFilter)tableFilters.nextElement());
      }
   }

   private void writeWhere(String where)
      throws IOException
   {
      attrs[0] = FilterConst.ATTR_CONDITION;
      values[0] = where;

      writeElementStart(FilterConst.ELEM_WHERE, 1, true);
   }

   private void writeWrappers()
      throws IOException
   {
      Vector  wrapperNames;
      XMLName wrapperName;

      wrapperNames = filterSet.getWrapperNames();

      // Write out the wrapper names.

      for (int i = 0; i < wrapperNames.size(); i++)
      {
         wrapperName = (XMLName)wrapperNames.elementAt(i);
         attrs[0] = FilterConst.ATTR_NAME;
         values[0] = wrapperName.getQualifiedName();
         writeElementStart(FilterConst.ELEM_WRAPPER, 1, true);
      }
   }
}
