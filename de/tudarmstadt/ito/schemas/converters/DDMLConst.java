// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.converters;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * DDML language tokens.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DDMLConst
{
   // *********************************************************************
   // DDML strings
   // *********************************************************************

   // Element names
   public static String ELEM_ANY                = "Any";
   public static String ELEM_ATTDEF             = "AttDef";
   public static String ELEM_ATTGROUP           = "AttGroup";
   public static String ELEM_CHOICE             = "Choice";
   public static String ELEM_DOC                = "Doc";
   public static String ELEM_DOCUMENTDEF        = "DocumentDef";
   public static String ELEM_ELEMENTDECL        = "ElementDecl";
   public static String ELEM_EMPTY              = "Empty";
   public static String ELEM_ENUMERATION        = "Enumeration";
   public static String ELEM_ENUMERATIONVALUE   = "EnumerationValue";
   public static String ELEM_MIXED              = "Mixed";
   public static String ELEM_MODEL              = "Model";
   public static String ELEM_MORE               = "More";
   public static String ELEM_NOTATION           = "Notation";
   public static String ELEM_PCDATA             = "PCData";
   public static String ELEM_REF                = "Ref";
   public static String ELEM_SEQ                = "Seq";
   public static String ELEM_UNPARSEDENTITY     = "UnparsedEntity";
   
   public static final String[] ELEMS = {
										 ELEM_ANY,
										 ELEM_ATTDEF,
										 ELEM_ATTGROUP,
										 ELEM_CHOICE,
										 ELEM_DOC,
										 ELEM_DOCUMENTDEF,
										 ELEM_ELEMENTDECL,
										 ELEM_EMPTY,
										 ELEM_ENUMERATION,
										 ELEM_ENUMERATIONVALUE,
										 ELEM_MIXED,
										 ELEM_MODEL,
										 ELEM_MORE,
										 ELEM_NOTATION,
										 ELEM_PCDATA,
										 ELEM_REF,
										 ELEM_SEQ,
										 ELEM_UNPARSEDENTITY
										};
   
   // Attribute names
   public static String ATTR_ATTVALUE      = "AttValue";
   public static String ATTR_ELEMENT       = "Element";
   public static String ATTR_ELEMENTNS     = "ElementNS";
   public static String ATTR_FILEEXTENSION = "FileExtension";
   public static String ATTR_FREQUENCY     = "Frequency";
   public static String ATTR_ID            = "id";
   public static String ATTR_MIMETYPE      = "MimeType";
   public static String ATTR_NAME          = "Name";
   public static String ATTR_NDATADECL     = "NDataDecl";
   public static String ATTR_NOTATION      = "Notation";
   public static String ATTR_NS            = "ns";
   public static String ATTR_PREFIX        = "prefix";
   public static String ATTR_PUBIDLITERAL  = "PubidLiteral";
   public static String ATTR_REQUIRED      = "Required";
   public static String ATTR_ROOT          = "Root";
   public static String ATTR_SRC           = "Src";
   public static String ATTR_SYSTEMLITERAL = "SystemLiteral";
   public static String ATTR_TYPE          = "Type";
   public static String ATTR_VALUE         = "Value";
   public static String ATTR_VERSION       = "Version";
   
   public static final String[] ATTRS = {
										 ATTR_ATTVALUE,
										 ATTR_ELEMENT,
										 ATTR_ELEMENTNS,
										 ATTR_FILEEXTENSION,
										 ATTR_FREQUENCY,
										 ATTR_ID,
										 ATTR_MIMETYPE,
										 ATTR_NAME,
										 ATTR_NDATADECL,
										 ATTR_NOTATION,
										 ATTR_NS,
										 ATTR_PREFIX,
										 ATTR_PUBIDLITERAL,
										 ATTR_REQUIRED,
										 ATTR_ROOT,
										 ATTR_SRC,
										 ATTR_SYSTEMLITERAL,
										 ATTR_TYPE,
										 ATTR_VALUE,
										 ATTR_VERSION
										};
									 
   // Enumerated attribute values

   // ATTR_REQUIRED:
   public static String ENUM_NO  = "No";
   public static String ENUM_YES = "Yes";

   // ATTR_FREQUENCY:
   public static String ENUM_ONEORMORE  = "OneOrMore";
   public static String ENUM_OPTIONAL   = "Optional";
   public static String ENUM_REQUIRED   = "Required";
   public static String ENUM_ZEROORMORE = "ZeroOrMore";

   // ATTR_ROOT:
   public static String ENUM_POSSIBLE    = "Possible";
   public static String ENUM_RECOMMENDED = "Recommended";
   public static String ENUM_UNLIKELY    = "Unlikely";

   // ATTR_TYPE:
   public static String ENUM_CDATA      = "CData";
   public static String ENUM_ENTITIES   = "Entities";
   public static String ENUM_ENTITY     = "Entity";
   public static String ENUM_ENUMERATED = "Enumerated";
   public static String ENUM_ID         = "ID";
   public static String ENUM_IDREF      = "IDRef";
   public static String ENUM_IDREFS     = "IDRefs";
   public static String ENUM_NMTOKEN    = "Nmtoken";
   public static String ENUM_NMTOKENS   = "Nmtokens";
   public static String ENUM_NOTATION   = "Notation";
   
   public static final String[] ENUMS = {
										 ENUM_NO,
										 ENUM_YES,
										 ENUM_ONEORMORE,
										 ENUM_OPTIONAL,
										 ENUM_REQUIRED,
										 ENUM_ZEROORMORE,
										 ENUM_POSSIBLE,
										 ENUM_RECOMMENDED,
										 ENUM_UNLIKELY,
										 ENUM_CDATA,
										 ENUM_ENTITIES,
										 ENUM_ENTITY,
										 ENUM_ENUMERATED,
										 ENUM_ID,
										 ENUM_IDREF,
										 ENUM_IDREFS,
										 ENUM_NMTOKEN,
										 ENUM_NMTOKENS,
										 ENUM_NOTATION
										};
   
   // Attribute defaults
   public static String DEF_FILEEXTENSION   = "xml";
   public static String DEF_OTHER_FREQUENCY = ENUM_REQUIRED;
   public static String DEF_MIXED_FREQUENCY = ENUM_ZEROORMORE;
   public static String DEF_MIMETYPE        = "application/xml";
   public static String DEF_ROOT            = ENUM_POSSIBLE;
   public static String DEF_REQUIRED        = ENUM_NO;
   public static String DEF_TYPE            = ENUM_CDATA;
   public static String DEF_VERSION         = "1.0";
   
   // DDML namespace
   public static String NAMESPACE = "http://www.purl.org/NET/ddml/v1";

   // XML namespace attribute name
   
   public static String XMLNS_ATTR_XMLNS         = "xmlns";

   // *********************************************************************
   // DDML tokens
   // *********************************************************************

   // Element tokens
   public static final int ELEM_TOKEN_UNKNOWN            = 0;
   public static final int ELEM_TOKEN_ANY                = 1;
   public static final int ELEM_TOKEN_ATTDEF             = 2;
   public static final int ELEM_TOKEN_ATTGROUP           = 3;
   public static final int ELEM_TOKEN_CHOICE             = 4;
   public static final int ELEM_TOKEN_DOC                = 5;
   public static final int ELEM_TOKEN_DOCUMENTDEF        = 6;
   public static final int ELEM_TOKEN_ELEMENTDECL        = 7;
   public static final int ELEM_TOKEN_EMPTY              = 8;
   public static final int ELEM_TOKEN_ENUMERATION        = 9;
   public static final int ELEM_TOKEN_ENUMERATIONVALUE   = 10;
   public static final int ELEM_TOKEN_MIXED              = 11;
   public static final int ELEM_TOKEN_MODEL              = 12;
   public static final int ELEM_TOKEN_MORE               = 13;
   public static final int ELEM_TOKEN_NOTATION           = 14;
   public static final int ELEM_TOKEN_PCDATA             = 15;
   public static final int ELEM_TOKEN_REF                = 16;
   public static final int ELEM_TOKEN_SEQ                = 17;
   public static final int ELEM_TOKEN_UNPARSEDENTITY     = 18;
   
   public static final int[] ELEM_TOKENS = {
											ELEM_TOKEN_ANY,
											ELEM_TOKEN_ATTDEF,
											ELEM_TOKEN_ATTGROUP,
											ELEM_TOKEN_CHOICE,
											ELEM_TOKEN_DOC,
											ELEM_TOKEN_DOCUMENTDEF,
											ELEM_TOKEN_ELEMENTDECL,
											ELEM_TOKEN_EMPTY,
											ELEM_TOKEN_ENUMERATION,
											ELEM_TOKEN_ENUMERATIONVALUE,
											ELEM_TOKEN_MIXED,
											ELEM_TOKEN_MODEL,
											ELEM_TOKEN_MORE,
											ELEM_TOKEN_NOTATION,
											ELEM_TOKEN_PCDATA,
											ELEM_TOKEN_REF,
											ELEM_TOKEN_SEQ,
											ELEM_TOKEN_UNPARSEDENTITY
										   };

   // Attribute tokens
   public static final int ATTR_TOKEN_UNKNOWN       = 0;
   public static final int ATTR_TOKEN_ATTVALUE      = 1;
   public static final int ATTR_TOKEN_ELEMENT       = 2;
   public static final int ATTR_TOKEN_ELEMENTNS     = 3;
   public static final int ATTR_TOKEN_FILEEXTENSION = 4;
   public static final int ATTR_TOKEN_FREQUENCY     = 5;
   public static final int ATTR_TOKEN_ID            = 6;
   public static final int ATTR_TOKEN_MIMETYPE      = 7;
   public static final int ATTR_TOKEN_NAME          = 8;
   public static final int ATTR_TOKEN_NDATADECL     = 9;
   public static final int ATTR_TOKEN_NOTATION      = 10;
   public static final int ATTR_TOKEN_NS            = 11;
   public static final int ATTR_TOKEN_PREFIX        = 12;
   public static final int ATTR_TOKEN_PUBIDLITERAL  = 13;
   public static final int ATTR_TOKEN_REQUIRED      = 14;
   public static final int ATTR_TOKEN_ROOT          = 15;
   public static final int ATTR_TOKEN_SRC           = 16;
   public static final int ATTR_TOKEN_SYSTEMLITERAL = 17;
   public static final int ATTR_TOKEN_TYPE          = 18;
   public static final int ATTR_TOKEN_VALUE         = 19;
   public static final int ATTR_TOKEN_VERSION       = 20;
   
   public static final int[] ATTR_TOKENS = {
											ATTR_TOKEN_ATTVALUE,
											ATTR_TOKEN_ELEMENT,
											ATTR_TOKEN_ELEMENTNS,
											ATTR_TOKEN_FILEEXTENSION,
											ATTR_TOKEN_FREQUENCY,
											ATTR_TOKEN_ID,
											ATTR_TOKEN_MIMETYPE,
											ATTR_TOKEN_NAME,
											ATTR_TOKEN_NDATADECL,
											ATTR_TOKEN_NS,
											ATTR_TOKEN_PUBIDLITERAL,
											ATTR_TOKEN_REQUIRED,
											ATTR_TOKEN_ROOT,
											ATTR_TOKEN_SRC,
											ATTR_TOKEN_SYSTEMLITERAL,
											ATTR_TOKEN_TYPE,
											ATTR_TOKEN_VALUE,
											ATTR_TOKEN_VERSION
										   };
   
   // Enumerated attribute value tokens
   
   // Unknown
   public static final int ENUM_TOKEN_UNKNOWN = 0;

   // Fixed and Required attributes
   public static final int ENUM_TOKEN_NO  = 1;
   public static final int ENUM_TOKEN_YES = 2;

   // Frequency attribute.
   
   public static final int ENUM_TOKEN_ONEORMORE  = 3;
   public static final int ENUM_TOKEN_OPTIONAL   = 4;
   public static final int ENUM_TOKEN_REQUIRED   = 5;
   public static final int ENUM_TOKEN_ZEROORMORE = 6;

   // Root attribute
   public static final int ENUM_TOKEN_POSSIBLE    = 7;
   public static final int ENUM_TOKEN_RECOMMENDED = 8;
   public static final int ENUM_TOKEN_UNLIKELY    = 9;

   // Type attribute
   public static final int ENUM_TOKEN_CDATA      = 10;
   public static final int ENUM_TOKEN_ENTITIES   = 11;
   public static final int ENUM_TOKEN_ENTITY     = 12;
   public static final int ENUM_TOKEN_ENUMERATED = 13;
   public static final int ENUM_TOKEN_ID         = 14;
   public static final int ENUM_TOKEN_IDREF      = 15;
   public static final int ENUM_TOKEN_IDREFS     = 16;
   public static final int ENUM_TOKEN_NMTOKEN    = 17;
   public static final int ENUM_TOKEN_NMTOKENS   = 18;
   public static final int ENUM_TOKEN_NOTATION   = 19;
   
   public static final int[] ENUM_TOKENS = {
											ENUM_TOKEN_NO,
											ENUM_TOKEN_YES,
											ENUM_TOKEN_ONEORMORE,
											ENUM_TOKEN_OPTIONAL,
											ENUM_TOKEN_REQUIRED,
											ENUM_TOKEN_ZEROORMORE,
											ENUM_TOKEN_POSSIBLE,
											ENUM_TOKEN_RECOMMENDED,
											ENUM_TOKEN_UNLIKELY,
											ENUM_TOKEN_CDATA,
											ENUM_TOKEN_ENTITIES,
											ENUM_TOKEN_ENTITY,
											ENUM_TOKEN_ENUMERATED,
											ENUM_TOKEN_ID,
											ENUM_TOKEN_IDREF,
											ENUM_TOKEN_IDREFS,
											ENUM_TOKEN_NMTOKEN,
											ENUM_TOKEN_NMTOKENS,
											ENUM_TOKEN_NOTATION
										   };

}