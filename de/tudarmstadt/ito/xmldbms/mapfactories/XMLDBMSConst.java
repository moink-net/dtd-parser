// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * XML-DBMS mapping language tokens; <B>used only by map factories and
 * the Map class</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class XMLDBMSConst
{
   //*********************************************************************
   // Mapping language strings
   //*********************************************************************

   // Element names

   public static String ELEM_ATTRIBUTE         = "Attribute";
   public static String ELEM_CANDIDATEKEY      = "CandidateKey";
   public static String ELEM_CLASSMAP          = "ClassMap";
   public static String ELEM_COLUMN            = "Column";
   public static String ELEM_DATETIMEFORMATS   = "DateTimeFormats";
   public static String ELEM_ELEMENTTYPE       = "ElementType";
   public static String ELEM_EMPTYSTRINGISNULL = "EmptyStringIsNull";
   public static String ELEM_FOREIGNKEY        = "ForeignKey";
   public static String ELEM_IGNOREROOT        = "IgnoreRoot";
   public static String ELEM_LOCALE            = "Locale";
   public static String ELEM_MAPS              = "Maps";
   public static String ELEM_NAMESPACE         = "Namespace";
   public static String ELEM_OPTIONS           = "Options";
   public static String ELEM_ORDERCOLUMN       = "OrderColumn";
   public static String ELEM_PASSTHROUGH       = "PassThrough";
   public static String ELEM_PATTERNS          = "Patterns";
   public static String ELEM_PCDATA            = "PCDATA";
   public static String ELEM_PROPERTYMAP       = "PropertyMap";
   public static String ELEM_PSEUDOROOT        = "PseudoRoot";
   public static String ELEM_RELATEDCLASS      = "RelatedClass";
   public static String ELEM_TABLE             = "Table";
   public static String ELEM_TOCLASSTABLE      = "ToClassTable";
   public static String ELEM_TOCOLUMN          = "ToColumn";
   public static String ELEM_TOPROPERTYTABLE   = "ToPropertyTable";
   public static String ELEM_TOROOTTABLE       = "ToRootTable";
   public static String ELEM_XMLTODBMS         = "XMLToDBMS";
   
   public static final String[] ELEMS = {
										 ELEM_ATTRIBUTE,
										 ELEM_CANDIDATEKEY,
										 ELEM_CLASSMAP,
										 ELEM_COLUMN,
										 ELEM_DATETIMEFORMATS,
										 ELEM_ELEMENTTYPE,
										 ELEM_EMPTYSTRINGISNULL,
										 ELEM_FOREIGNKEY,
										 ELEM_IGNOREROOT,
										 ELEM_LOCALE,
										 ELEM_MAPS,
										 ELEM_NAMESPACE,
										 ELEM_OPTIONS,
										 ELEM_ORDERCOLUMN,
										 ELEM_PASSTHROUGH,
										 ELEM_PATTERNS,
										 ELEM_PCDATA,
										 ELEM_PROPERTYMAP,
										 ELEM_PSEUDOROOT,
										 ELEM_RELATEDCLASS,
										 ELEM_TABLE,
										 ELEM_TOCLASSTABLE,
										 ELEM_TOCOLUMN,
										 ELEM_TOPROPERTYTABLE,
										 ELEM_TOROOTTABLE,
										 ELEM_XMLTODBMS
										};
   
   // Attribute names

   public static String ATTR_COUNTRY          = "Country";
   public static String ATTR_DATE             = "Date";
   public static String ATTR_FILEEXTENSION    = "FileExtension";
   public static String ATTR_GENERATE         = "Generate";
   public static String ATTR_KEYINPARENTTABLE = "KeyInParentTable";
   public static String ATTR_LANGUAGE         = "Language";
   public static String ATTR_MULTIVALUED      = "MultiValued";
   public static String ATTR_NAME             = "Name";
   public static String ATTR_PREFIX           = "Prefix";
   public static String ATTR_SCHEMA           = "Schema";
   public static String ATTR_TIME             = "Time";
   public static String ATTR_TIMESTAMP        = "Timestamp";
   public static String ATTR_URI              = "URI";
   public static String ATTR_VERSION          = "Version";
   
   public static final String[] ATTRS = {
										 ATTR_COUNTRY,
										 ATTR_DATE,
										 ATTR_FILEEXTENSION,
										 ATTR_GENERATE,
										 ATTR_KEYINPARENTTABLE,
										 ATTR_LANGUAGE,
										 ATTR_MULTIVALUED,
										 ATTR_NAME,
										 ATTR_PREFIX,
										 ATTR_SCHEMA,
										 ATTR_TIME,
										 ATTR_TIMESTAMP,
										 ATTR_URI,
										 ATTR_VERSION
										};

   // Enumerated attribute values

   // ATTR_MULTIVALUED, ATTR_GENERATE
   public static String ENUM_NO  = "No";
   public static String ENUM_YES = "Yes";

   // ATTR_KEYINCLASSTABLE:
   public static String ENUM_CANDIDATE = "Candidate";
   public static String ENUM_FOREIGN   = "Foreign";
   
   public static final String[] ENUMS = {
										 ENUM_NO,
										 ENUM_YES,
										 ENUM_CANDIDATE,
										 ENUM_FOREIGN
										};
   
   // Attribute defaults
   public static String DEF_MULTIVALUED = ENUM_NO;
   public static String DEF_VERSION     = "1.0";

   //*********************************************************************
   // Mapping language tokens
   //*********************************************************************

   // Element tokens
   public static final int ELEM_TOKEN_INVALID           = -999;
   public static final int ELEM_TOKEN_ATTRIBUTE         = 0;
   public static final int ELEM_TOKEN_CANDIDATEKEY      = 1;
   public static final int ELEM_TOKEN_CLASSMAP          = 2;
   public static final int ELEM_TOKEN_COLUMN            = 3;
   public static final int ELEM_TOKEN_DATETIMEFORMATS   = 4;
   public static final int ELEM_TOKEN_ELEMENTTYPE       = 5;
   public static final int ELEM_TOKEN_EMPTYSTRINGISNULL = 6;
   public static final int ELEM_TOKEN_FOREIGNKEY        = 7;
   public static final int ELEM_TOKEN_IGNOREROOT        = 8;
   public static final int ELEM_TOKEN_LOCALE            = 9;
   public static final int ELEM_TOKEN_MAPS              = 10;
   public static final int ELEM_TOKEN_NAMESPACE         = 11;
   public static final int ELEM_TOKEN_OPTIONS           = 12;
   public static final int ELEM_TOKEN_ORDERCOLUMN       = 13;
   public static final int ELEM_TOKEN_PASSTHROUGH       = 14;
   public static final int ELEM_TOKEN_PATTERNS          = 15;
   public static final int ELEM_TOKEN_PCDATA            = 16;
   public static final int ELEM_TOKEN_PROPERTYMAP       = 17;
   public static final int ELEM_TOKEN_PSEUDOROOT        = 18;
   public static final int ELEM_TOKEN_RELATEDCLASS      = 19;
   public static final int ELEM_TOKEN_TABLE             = 20;
   public static final int ELEM_TOKEN_TOCLASSTABLE      = 21;
   public static final int ELEM_TOKEN_TOCOLUMN          = 22;
   public static final int ELEM_TOKEN_TOPROPERTYTABLE   = 23;
   public static final int ELEM_TOKEN_TOROOTTABLE       = 24;
   public static final int ELEM_TOKEN_XMLTODBMS         = 25;
   
   public static final int[] ELEM_TOKENS = {
											ELEM_TOKEN_ATTRIBUTE,
											ELEM_TOKEN_CANDIDATEKEY,
											ELEM_TOKEN_CLASSMAP,
											ELEM_TOKEN_COLUMN,
											ELEM_TOKEN_DATETIMEFORMATS,
											ELEM_TOKEN_ELEMENTTYPE,
											ELEM_TOKEN_EMPTYSTRINGISNULL,
											ELEM_TOKEN_FOREIGNKEY,
											ELEM_TOKEN_IGNOREROOT,
											ELEM_TOKEN_LOCALE,
											ELEM_TOKEN_MAPS,
											ELEM_TOKEN_NAMESPACE,
											ELEM_TOKEN_OPTIONS,
											ELEM_TOKEN_ORDERCOLUMN,
											ELEM_TOKEN_PASSTHROUGH,
											ELEM_TOKEN_PATTERNS,
											ELEM_TOKEN_PCDATA,
											ELEM_TOKEN_PROPERTYMAP,
											ELEM_TOKEN_PSEUDOROOT,
											ELEM_TOKEN_RELATEDCLASS,
											ELEM_TOKEN_TABLE,
											ELEM_TOKEN_TOCLASSTABLE,
											ELEM_TOKEN_TOCOLUMN,
											ELEM_TOKEN_TOPROPERTYTABLE,
											ELEM_TOKEN_TOROOTTABLE,
											ELEM_TOKEN_XMLTODBMS
										   };

   // Attribute tokens
   public static final int ATTR_TOKEN_INVALID          = -999;
   public static final int ATTR_TOKEN_COUNTRY          = 0;
   public static final int ATTR_TOKEN_DATE             = 1;
   public static final int ATTR_TOKEN_FILEEXTENSION    = 2;
   public static final int ATTR_TOKEN_GENERATE         = 3;
   public static final int ATTR_TOKEN_KEYINPARENTTABLE = 4;
   public static final int ATTR_TOKEN_LANGUAGE         = 5;
   public static final int ATTR_TOKEN_MULTIVALUED      = 6;
   public static final int ATTR_TOKEN_NAME             = 7;
   public static final int ATTR_TOKEN_PREFIX           = 8;
   public static final int ATTR_TOKEN_SCHEMA           = 9;
   public static final int ATTR_TOKEN_TIME             = 10;
   public static final int ATTR_TOKEN_TIMESTAMP        = 11;
   public static final int ATTR_TOKEN_URI              = 12;
   public static final int ATTR_TOKEN_VERSION          = 13;

   public static final int[] ATTR_TOKENS = {
											ATTR_TOKEN_COUNTRY,
											ATTR_TOKEN_DATE,
											ATTR_TOKEN_FILEEXTENSION,
											ATTR_TOKEN_GENERATE,
											ATTR_TOKEN_KEYINPARENTTABLE,
											ATTR_TOKEN_LANGUAGE,
											ATTR_TOKEN_MULTIVALUED,
											ATTR_TOKEN_NAME,
											ATTR_TOKEN_PREFIX,
											ATTR_TOKEN_SCHEMA,
											ATTR_TOKEN_TIME,
											ATTR_TOKEN_TIMESTAMP,
											ATTR_TOKEN_URI,
											ATTR_TOKEN_VERSION
										   };
   
   // Enumerated attribute value tokens
   
   // ATTR_MULTIVALUED, ATTR_GENERATE
   public static final int ENUM_TOKEN_NO  = 0;
   public static final int ENUM_TOKEN_YES = 1;

   // ATTR_KEYINTHISTABLE:
   public static final int ENUM_TOKEN_CANDIDATE = 0;
   public static final int ENUM_TOKEN_FOREIGN   = 1;
   
   public static final int[] ENUM_TOKENS = {
											ENUM_TOKEN_NO,
											ENUM_TOKEN_YES,
											ENUM_TOKEN_CANDIDATE,
											ENUM_TOKEN_FOREIGN
										   };
}