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
// Changes from version 1.01:
// * Update for version 2.0 DTD.

package org.xmlmiddleware.xmldbms.maps.factories;

import java.text.DateFormat;

/**
 * XML-DBMS mapping language strings and tokens; <b>used only by map factories and
 * map utilities.</b>.
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class XMLDBMSConst
{
   //*********************************************************************
   // Element type names: strings and tokens
   //*********************************************************************

   // Element type names

   public static String ELEM_ATTRIBUTE         = "Attribute";
   public static String ELEM_CATALOG           = "Catalog";
   public static String ELEM_CLASSMAP          = "ClassMap";
   public static String ELEM_COLUMN            = "Column";
   public static String ELEM_DATABASE          = "Database";
   public static String ELEM_DATABASES         = "Databases";
   public static String ELEM_DATEFORMAT        = "DateFormat";
   public static String ELEM_DATETIMEFORMAT    = "DateTimeFormat";
   public static String ELEM_DECIMALFORMAT     = "DecimalFormat";
   public static String ELEM_ELEMENTTYPE       = "ElementType";
   public static String ELEM_EMPTYSTRINGISNULL = "EmptyStringIsNull";
   public static String ELEM_EXTENDS           = "Extends";
   public static String ELEM_FIXEDORDER        = "FixedOrder";
   public static String ELEM_FOREIGNKEY        = "ForeignKey";
   public static String ELEM_FORMATCLASS       = "FormatClass";
   public static String ELEM_INLINEMAP         = "InlineMap";
   public static String ELEM_LOCALE            = "Locale";
   public static String ELEM_MAPS              = "Maps";
   public static String ELEM_MVORDERCOLUMN     = "MVOrderColumn";
   public static String ELEM_NAMESPACE         = "Namespace";
   public static String ELEM_NUMBERFORMAT      = "NumberFormat";
   public static String ELEM_OPTIONS           = "Options";
   public static String ELEM_ORDERCOLUMN       = "OrderColumn";
   public static String ELEM_PCDATA            = "PCDATA";
   public static String ELEM_PRIMARYKEY        = "PrimaryKey";
   public static String ELEM_PROPERTYMAP       = "PropertyMap";
   public static String ELEM_RELATEDCLASS      = "RelatedClass";
   public static String ELEM_SCHEMA            = "Schema";
   public static String ELEM_SIMPLEDATEFORMAT  = "SimpleDateFormat";
   public static String ELEM_TABLE             = "Table";
   public static String ELEM_TIMEFORMAT        = "TimeFormat";
   public static String ELEM_TOCLASSTABLE      = "ToClassTable";
   public static String ELEM_TOCOLUMN          = "ToColumn";
   public static String ELEM_TOPROPERTYTABLE   = "ToPropertyTable";
   public static String ELEM_UNIQUEKEY         = "UniqueKey";
   public static String ELEM_USEBASETABLE      = "UseBaseTable";
   public static String ELEM_USECLASSMAP       = "UseClassMap";
   public static String ELEM_USECOLUMN         = "UseColumn";
   public static String ELEM_USEFOREIGNKEY     = "UseForeignKey";
   public static String ELEM_USETABLE          = "UseTable";
   public static String ELEM_USEUNIQUEKEY      = "UseUniqueKey";
   public static String ELEM_XMLTODBMS         = "XMLToDBMS";
   
   public static final String[] ELEMS = {
                                         ELEM_ATTRIBUTE,
                                         ELEM_CATALOG,
                                         ELEM_CLASSMAP,
                                         ELEM_COLUMN,
                                         ELEM_DATABASE,
                                         ELEM_DATABASES,
                                         ELEM_DATEFORMAT,
                                         ELEM_DATETIMEFORMAT,
                                         ELEM_DECIMALFORMAT,
                                         ELEM_ELEMENTTYPE,
                                         ELEM_EMPTYSTRINGISNULL,
                                         ELEM_EXTENDS,
                                         ELEM_FIXEDORDER,
                                         ELEM_FOREIGNKEY,
                                         ELEM_FORMATCLASS,
                                         ELEM_INLINEMAP,
                                         ELEM_LOCALE,
                                         ELEM_MAPS,
                                         ELEM_MVORDERCOLUMN,
                                         ELEM_NAMESPACE,
                                         ELEM_NUMBERFORMAT,
                                         ELEM_OPTIONS,
                                         ELEM_ORDERCOLUMN,
                                         ELEM_PCDATA,
                                         ELEM_PRIMARYKEY,
                                         ELEM_PROPERTYMAP,
                                         ELEM_RELATEDCLASS,
                                         ELEM_SCHEMA,
                                         ELEM_SIMPLEDATEFORMAT,
                                         ELEM_TABLE,
                                         ELEM_TIMEFORMAT,
                                         ELEM_TOCLASSTABLE,
                                         ELEM_TOCOLUMN,
                                         ELEM_TOPROPERTYTABLE,
                                         ELEM_UNIQUEKEY,
                                         ELEM_USEBASETABLE,
                                         ELEM_USECLASSMAP,
                                         ELEM_USECOLUMN,
                                         ELEM_USEFOREIGNKEY,
                                         ELEM_USETABLE,
                                         ELEM_USEUNIQUEKEY,
                                         ELEM_XMLTODBMS
                                        };
   
   // Element type tokens

   public static final int ELEM_TOKEN_INVALID           = -999;
   public static final int ELEM_TOKEN_ATTRIBUTE         = 0;
   public static final int ELEM_TOKEN_CATALOG           = 1;
   public static final int ELEM_TOKEN_CLASSMAP          = 2;
   public static final int ELEM_TOKEN_COLUMN            = 3;
   public static final int ELEM_TOKEN_DATABASE          = 4;
   public static final int ELEM_TOKEN_DATABASES         = 5;
   public static final int ELEM_TOKEN_DATEFORMAT        = 6;
   public static final int ELEM_TOKEN_DATETIMEFORMAT    = 7;
   public static final int ELEM_TOKEN_DECIMALFORMAT     = 8;
   public static final int ELEM_TOKEN_ELEMENTTYPE       = 9;
   public static final int ELEM_TOKEN_EMPTYSTRINGISNULL = 10;
   public static final int ELEM_TOKEN_EXTENDS           = 11;
   public static final int ELEM_TOKEN_FIXEDORDER        = 12;
   public static final int ELEM_TOKEN_FOREIGNKEY        = 13;
   public static final int ELEM_TOKEN_FORMATCLASS       = 14;
   public static final int ELEM_TOKEN_INLINEMAP         = 15;
   public static final int ELEM_TOKEN_LOCALE            = 16;
   public static final int ELEM_TOKEN_MAPS              = 17;
   public static final int ELEM_TOKEN_MVORDERCOLUMN     = 18;
   public static final int ELEM_TOKEN_NAMESPACE         = 19;
   public static final int ELEM_TOKEN_NUMBERFORMAT      = 20;
   public static final int ELEM_TOKEN_OPTIONS           = 21;
   public static final int ELEM_TOKEN_ORDERCOLUMN       = 22;
   public static final int ELEM_TOKEN_PCDATA            = 23;
   public static final int ELEM_TOKEN_PRIMARYKEY        = 24;
   public static final int ELEM_TOKEN_PROPERTYMAP       = 25;
   public static final int ELEM_TOKEN_RELATEDCLASS      = 26;
   public static final int ELEM_TOKEN_SCHEMA            = 27;
   public static final int ELEM_TOKEN_SIMPLEDATEFORMAT  = 28;
   public static final int ELEM_TOKEN_TABLE             = 29;
   public static final int ELEM_TOKEN_TIMEFORMAT        = 30;
   public static final int ELEM_TOKEN_TOCLASSTABLE      = 31;
   public static final int ELEM_TOKEN_TOCOLUMN          = 32;
   public static final int ELEM_TOKEN_TOPROPERTYTABLE   = 33;
   public static final int ELEM_TOKEN_UNIQUEKEY         = 34;
   public static final int ELEM_TOKEN_USEBASETABLE      = 35;
   public static final int ELEM_TOKEN_USECLASSMAP       = 36;
   public static final int ELEM_TOKEN_USECOLUMN         = 37;
   public static final int ELEM_TOKEN_USEFOREIGNKEY     = 38;
   public static final int ELEM_TOKEN_USETABLE          = 39;
   public static final int ELEM_TOKEN_USEUNIQUEKEY      = 40;
   public static final int ELEM_TOKEN_XMLTODBMS         = 41;
   
   public static final int[] ELEM_TOKENS = {
                                            ELEM_TOKEN_ATTRIBUTE,
                                            ELEM_TOKEN_CATALOG,
                                            ELEM_TOKEN_CLASSMAP,
                                            ELEM_TOKEN_COLUMN,
                                            ELEM_TOKEN_DATABASE,
                                            ELEM_TOKEN_DATABASES,
                                            ELEM_TOKEN_DATEFORMAT,
                                            ELEM_TOKEN_DATETIMEFORMAT,
                                            ELEM_TOKEN_DECIMALFORMAT,
                                            ELEM_TOKEN_ELEMENTTYPE,
                                            ELEM_TOKEN_EMPTYSTRINGISNULL,
                                            ELEM_TOKEN_EXTENDS,
                                            ELEM_TOKEN_FIXEDORDER,
                                            ELEM_TOKEN_FOREIGNKEY,
                                            ELEM_TOKEN_FORMATCLASS,
                                            ELEM_TOKEN_INLINEMAP,
                                            ELEM_TOKEN_LOCALE,
                                            ELEM_TOKEN_MAPS,
                                            ELEM_TOKEN_MVORDERCOLUMN,
                                            ELEM_TOKEN_NAMESPACE,
                                            ELEM_TOKEN_NUMBERFORMAT,
                                            ELEM_TOKEN_OPTIONS,
                                            ELEM_TOKEN_ORDERCOLUMN,
                                            ELEM_TOKEN_PCDATA,
                                            ELEM_TOKEN_PRIMARYKEY,
                                            ELEM_TOKEN_PROPERTYMAP,
                                            ELEM_TOKEN_RELATEDCLASS,
                                            ELEM_TOKEN_SCHEMA,
                                            ELEM_TOKEN_SIMPLEDATEFORMAT,
                                            ELEM_TOKEN_TABLE,
                                            ELEM_TOKEN_TIMEFORMAT,
                                            ELEM_TOKEN_TOCLASSTABLE,
                                            ELEM_TOKEN_TOCOLUMN,
                                            ELEM_TOKEN_TOPROPERTYTABLE,
                                            ELEM_TOKEN_UNIQUEKEY,
                                            ELEM_TOKEN_USEBASETABLE,
                                            ELEM_TOKEN_USECLASSMAP,
                                            ELEM_TOKEN_USECOLUMN,
                                            ELEM_TOKEN_USEFOREIGNKEY,
                                            ELEM_TOKEN_USETABLE,
                                            ELEM_TOKEN_USEUNIQUEKEY,
                                            ELEM_TOKEN_XMLTODBMS
                                           };

   //*********************************************************************
   // Enumeration values: strings and tokens
   //*********************************************************************

   // Enumeration value strings

   // ATTR_MULTIVALUED, ATTR_USEBASETABLE, ATTR_GENERATE, ATTR_NULLABLE
   public static String ENUM_NO      = "No";
   public static String ENUM_YES     = "Yes";
   public static String ENUM_UNKNOWN = "Unknown"; // ATTR_NULLABLE only

   // ATTR_KEYINPARENTTABLE:
   public static String ENUM_UNIQUE  = "Unique";
   public static String ENUM_FOREIGN = "Foreign";
   
   // ATTR_DIRECTION:
   public static String ENUM_ASCENDING  = "Ascending";
   public static String ENUM_DESCENDING = "Descending";

   // ATTR_DATESTYLE, ATTR_TIMESTYLE:
   public static String ENUM_FULL   = "FULL";
   public static String ENUM_LONG   = "LONG";
   public static String ENUM_MEDIUM = "MEDIUM";
   public static String ENUM_SHORT  = "SHORT";

   // ATTR_DATATYPE:
   // See JDBCTypes.java

   public static String[] ENUMS = {
                                   ENUM_NO,
                                   ENUM_YES,
                                   ENUM_UNKNOWN,
                                   ENUM_UNIQUE,
                                   ENUM_FOREIGN,
                                   ENUM_ASCENDING,
                                   ENUM_DESCENDING,
                                   ENUM_FULL,
                                   ENUM_LONG,
                                   ENUM_MEDIUM,
                                   ENUM_SHORT
                                  };

   // Enumeration value tokens
   
   public static final int ENUM_TOKEN_INVALID = -999;

   // ATTR_MULTIVALUED, ATTR_USEBASETABLE, ATTR_NULLABLE
   public static final int ENUM_TOKEN_NO  = 0;
   public static final int ENUM_TOKEN_YES = 1;
   public static final int ENUM_TOKEN_UNKNOWN = 2; // ATTR_NULLABLE only

   // ATTR_KEYINPARENTTABLE:
   public static final int ENUM_TOKEN_UNIQUE  = 0;
   public static final int ENUM_TOKEN_FOREIGN = 1;
   
   // ATTR_DIRECTION:
   public static final int ENUM_TOKEN_ASCENDING = 0;
   public static final int ENUM_TOKEN_DESCENDING = 1;

   // ATTR_DATESTYLE, ATTR_TIMESTYLE:
   public static final int ENUM_TOKEN_FULL   = DateFormat.FULL;
   public static final int ENUM_TOKEN_LONG   = DateFormat.LONG;
   public static final int ENUM_TOKEN_MEDIUM = DateFormat.MEDIUM;
   public static final int ENUM_TOKEN_SHORT  = DateFormat.SHORT;

   // ATTR_DATATYPE:
   // See JDBCTypes.java

   public static final int[] ENUM_TOKENS = {
                                            ENUM_TOKEN_NO,
                                            ENUM_TOKEN_YES,
                                            ENUM_TOKEN_UNKNOWN,
                                            ENUM_TOKEN_UNIQUE,
                                            ENUM_TOKEN_FOREIGN,
                                            ENUM_TOKEN_ASCENDING,
                                            ENUM_TOKEN_DESCENDING,
                                            ENUM_TOKEN_FULL,
                                            ENUM_TOKEN_LONG,
                                            ENUM_TOKEN_MEDIUM,
                                            ENUM_TOKEN_SHORT
                                           };

   //*********************************************************************
   // Attribute names and defaults: strings and tokens
   //*********************************************************************

   // Attribute names

   public static String ATTR_CATALOG          = "Catalog";
   public static String ATTR_CLASS            = "Class";
   public static String ATTR_COUNTRY          = "Country";
   public static String ATTR_DATABASE         = "Database";
   public static String ATTR_DATATYPE         = "DataType";
   public static String ATTR_DATESTYLE        = "DateStyle";
   public static String ATTR_DEFAULTFORTYPES  = "DefaultForTypes";
   public static String ATTR_DIRECTION        = "Direction";
   public static String ATTR_ELEMENTTYPE      = "ElementType";
   public static String ATTR_FORMAT           = "Format";
   public static String ATTR_GENERATE         = "Generate";
   public static String ATTR_KEYGENERATOR     = "KeyGenerator";
   public static String ATTR_KEYINBASETABLE   = "KeyInBaseTable";
   public static String ATTR_KEYINPARENTTABLE = "KeyInParentTable";
   public static String ATTR_LANGUAGE         = "Language";
   public static String ATTR_LENGTH           = "Length";
   public static String ATTR_MULTIVALUED      = "MultiValued";
   public static String ATTR_NAME             = "Name";
   public static String ATTR_NULLABLE         = "Nullable";
   public static String ATTR_PATTERN          = "Pattern";
   public static String ATTR_PRECISION        = "Precision";
   public static String ATTR_PREFIX           = "Prefix";
   public static String ATTR_SCALE            = "Scale";
   public static String ATTR_SCHEMA           = "Schema";
   public static String ATTR_TIMESTYLE        = "TimeStyle";
   public static String ATTR_URI              = "URI";
   public static String ATTR_USEBASETABLE     = "UseBaseTable";
   public static String ATTR_VALUE            = "Value";
   public static String ATTR_VERSION          = "Version";
   
   public static final String[] ATTRS = {
                                         ATTR_CATALOG,
                                         ATTR_CLASS,
                                         ATTR_COUNTRY,
                                         ATTR_DATABASE,
                                         ATTR_DATATYPE,
                                         ATTR_DATESTYLE,
                                         ATTR_DEFAULTFORTYPES,
                                         ATTR_DIRECTION,
                                         ATTR_ELEMENTTYPE,
                                         ATTR_FORMAT,
                                         ATTR_GENERATE,
                                         ATTR_KEYGENERATOR,
                                         ATTR_KEYINBASETABLE,
                                         ATTR_KEYINPARENTTABLE,
                                         ATTR_LANGUAGE,
                                         ATTR_LENGTH,
                                         ATTR_MULTIVALUED,
                                         ATTR_NAME,
                                         ATTR_NULLABLE,
                                         ATTR_PATTERN,
                                         ATTR_PRECISION,
                                         ATTR_PREFIX,
                                         ATTR_SCALE,
                                         ATTR_SCHEMA,
                                         ATTR_TIMESTYLE,
                                         ATTR_URI,
                                         ATTR_USEBASETABLE,
                                         ATTR_VALUE,
                                         ATTR_VERSION
                                        };

   // Attribute tokens

   public static final int ATTR_TOKEN_INVALID          = -999;
   public static final int ATTR_TOKEN_CATALOG          = 0;
   public static final int ATTR_TOKEN_CLASS            = 1;
   public static final int ATTR_TOKEN_COUNTRY          = 2;
   public static final int ATTR_TOKEN_DATABASE         = 3;
   public static final int ATTR_TOKEN_DATATYPE         = 4;
   public static final int ATTR_TOKEN_DATESTYLE        = 5;
   public static final int ATTR_TOKEN_DEFAULTFORTYPES  = 6;
   public static final int ATTR_TOKEN_DIRECTION        = 7;
   public static final int ATTR_TOKEN_ELEMENTTYPE      = 8;
   public static final int ATTR_TOKEN_FORMAT           = 9;
   public static final int ATTR_TOKEN_GENERATE         = 10;
   public static final int ATTR_TOKEN_KEYGENERATOR     = 11;
   public static final int ATTR_TOKEN_KEYINBASETABLE   = 12;
   public static final int ATTR_TOKEN_KEYINPARENTTABLE = 13;
   public static final int ATTR_TOKEN_LANGUAGE         = 14;
   public static final int ATTR_TOKEN_LENGTH           = 15;
   public static final int ATTR_TOKEN_MULTIVALUED      = 16;
   public static final int ATTR_TOKEN_NAME             = 17;
   public static final int ATTR_TOKEN_NULLABLE         = 18;
   public static final int ATTR_TOKEN_PATTERN          = 19;
   public static final int ATTR_TOKEN_PRECISION        = 20;
   public static final int ATTR_TOKEN_PREFIX           = 21;
   public static final int ATTR_TOKEN_SCALE            = 22;
   public static final int ATTR_TOKEN_SCHEMA           = 23;
   public static final int ATTR_TOKEN_TIMESTYLE        = 24;
   public static final int ATTR_TOKEN_URI              = 25;
   public static final int ATTR_TOKEN_USEBASETABLE     = 26;
   public static final int ATTR_TOKEN_VALUE            = 27;
   public static final int ATTR_TOKEN_VERSION          = 28;

   public static final int[] ATTR_TOKENS = {
                                            ATTR_TOKEN_CATALOG,
                                            ATTR_TOKEN_CLASS,
                                            ATTR_TOKEN_COUNTRY,
                                            ATTR_TOKEN_DATABASE,
                                            ATTR_TOKEN_DATATYPE,
                                            ATTR_TOKEN_DATESTYLE,
                                            ATTR_TOKEN_DEFAULTFORTYPES,
                                            ATTR_TOKEN_DIRECTION,
                                            ATTR_TOKEN_ELEMENTTYPE,
                                            ATTR_TOKEN_FORMAT,
                                            ATTR_TOKEN_GENERATE,
                                            ATTR_TOKEN_KEYGENERATOR,
                                            ATTR_TOKEN_KEYINBASETABLE,
                                            ATTR_TOKEN_KEYINPARENTTABLE,
                                            ATTR_TOKEN_LANGUAGE,
                                            ATTR_TOKEN_LENGTH,
                                            ATTR_TOKEN_MULTIVALUED,
                                            ATTR_TOKEN_NAME,
                                            ATTR_TOKEN_NULLABLE,
                                            ATTR_TOKEN_PATTERN,
                                            ATTR_TOKEN_PRECISION,
                                            ATTR_TOKEN_PREFIX,
                                            ATTR_TOKEN_SCALE,
                                            ATTR_TOKEN_SCHEMA,
                                            ATTR_TOKEN_TIMESTYLE,
                                            ATTR_TOKEN_URI,
                                            ATTR_TOKEN_USEBASETABLE,
                                            ATTR_TOKEN_VALUE,
                                            ATTR_TOKEN_VERSION
                                           };

   // Attribute defaults

   public static String DEF_MULTIVALUED      = ENUM_NO;
   public static String DEF_GENERATE         = ENUM_NO;
   public static String DEF_DATABASENAME     = "Default";
   public static String DEF_VERSION          = "2.0";
   public static String DEF_DIRECTION        = ENUM_ASCENDING;

   // Special attribute values

   public static String VALUE_DATABASE = "Database";
   public static String VALUE_PRIMARYKEY = "PrimaryKey";

   public static String URI_XMLDBMSV2 = "http://www.xmlmiddleware.org/xmldbms/v2";
}