// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * DTD tokens.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DTDConst
{
   // *********************************************************************
   // DTD Strings
   // *********************************************************************

   public static String KEYWD_ANY      = "ANY";
   public static String KEYWD_ATTLIST  = "ATTLIST";
   public static String KEYWD_CDATA    = "CDATA";
   public static String KEYWD_ELEMENT  = "ELEMENT";
   public static String KEYWD_EMPTY    = "EMPTY";
   public static String KEYWD_ENTITY   = "ENTITY";
   public static String KEYWD_ENTITIES = "ENTITIES";
   public static String KEYWD_FIXED    = "FIXED";
   public static String KEYWD_ID       = "ID";
   public static String KEYWD_IDREF    = "IDREF";
   public static String KEYWD_IDREFS   = "IDREFS";
   public static String KEYWD_IMPLIED  = "IMPLIED";
   public static String KEYWD_NDATA    = "NDATA";
   public static String KEYWD_NMTOKEN  = "NMTOKEN";
   public static String KEYWD_NMTOKENS = "NMTOKENS";
   public static String KEYWD_NOTATION = "NOTATION";
   public static String KEYWD_PCDATA   = "PCDATA";
   public static String KEYWD_PUBLIC   = "PUBLIC";
   public static String KEYWD_REQUIRED = "REQUIRED";
   public static String KEYWD_SYSTEM   = "SYSTEM";
   
   public static final String[] KEYWDS = {
											  KEYWD_ANY,
											  KEYWD_ATTLIST,
											  KEYWD_CDATA,
											  KEYWD_ELEMENT,
											  KEYWD_EMPTY,
											  KEYWD_ENTITY,
											  KEYWD_ENTITIES,
											  KEYWD_FIXED,
											  KEYWD_ID,
											  KEYWD_IDREF,
											  KEYWD_IDREFS,
											  KEYWD_IMPLIED,
											  KEYWD_NDATA,
											  KEYWD_NMTOKEN,
											  KEYWD_NMTOKENS,
											  KEYWD_NOTATION,
											  KEYWD_PCDATA,
											  KEYWD_PUBLIC,
											  KEYWD_REQUIRED,
											  KEYWD_SYSTEM
											 };
   
   // *********************************************************************
   // DTD Tokens
   // *********************************************************************

   public static final int KEYWD_TOKEN_UNKNOWN  = 0;
   public static final int KEYWD_TOKEN_ANY      = 1;
   public static final int KEYWD_TOKEN_ATTLIST  = 2;
   public static final int KEYWD_TOKEN_CDATA    = 3;
   public static final int KEYWD_TOKEN_ELEMENT  = 4;
   public static final int KEYWD_TOKEN_EMPTY    = 5;
   public static final int KEYWD_TOKEN_ENTITY   = 6;
   public static final int KEYWD_TOKEN_ENTITIES = 7;
   public static final int KEYWD_TOKEN_FIXED    = 8;
   public static final int KEYWD_TOKEN_ID       = 9;
   public static final int KEYWD_TOKEN_IDREF    = 10;
   public static final int KEYWD_TOKEN_IDREFS   = 11;
   public static final int KEYWD_TOKEN_IMPLIED  = 12;
   public static final int KEYWD_TOKEN_NDATA    = 13;
   public static final int KEYWD_TOKEN_NMTOKEN  = 14;
   public static final int KEYWD_TOKEN_NMTOKENS = 15;
   public static final int KEYWD_TOKEN_NOTATION = 16;
   public static final int KEYWD_TOKEN_PCDATA   = 17;
   public static final int KEYWD_TOKEN_PUBLIC   = 18;
   public static final int KEYWD_TOKEN_REQUIRED = 19;
   public static final int KEYWD_TOKEN_SYSTEM   = 20;
   
   public static final int[] KEYWD_TOKENS = {
												 KEYWD_TOKEN_ANY,
												 KEYWD_TOKEN_ATTLIST,
												 KEYWD_TOKEN_CDATA,
												 KEYWD_TOKEN_ELEMENT,
												 KEYWD_TOKEN_EMPTY,
												 KEYWD_TOKEN_ENTITY,
												 KEYWD_TOKEN_ENTITIES,
												 KEYWD_TOKEN_FIXED,
												 KEYWD_TOKEN_ID,
												 KEYWD_TOKEN_IDREF,
												 KEYWD_TOKEN_IDREFS,
												 KEYWD_TOKEN_IMPLIED,
												 KEYWD_TOKEN_NDATA,
												 KEYWD_TOKEN_NMTOKEN,
												 KEYWD_TOKEN_NMTOKENS,
												 KEYWD_TOKEN_NOTATION,
												 KEYWD_TOKEN_PCDATA,
												 KEYWD_TOKEN_PUBLIC,
												 KEYWD_TOKEN_REQUIRED,
												 KEYWD_TOKEN_SYSTEM
												};
}