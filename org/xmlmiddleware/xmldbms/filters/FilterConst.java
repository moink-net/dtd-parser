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

/**
 * Filter language strings and tokens. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class FilterConst
{
   public static String URI_FILTERSV2 = "http://www.xmlmiddleware.org/xmldbms/filters/v2";

   //*********************************************************************
   // Filter language element strings and tokens
   //*********************************************************************

   public static String ELEM_FILTER             = "Filter";
   public static String ELEM_FILTERS            = "Filters";
   public static String ELEM_FILTERSET          = "FilterSet";
   public static String ELEM_NAMESPACE          = "Namespace";
   public static String ELEM_OPTIONS            = "Options";
   public static String ELEM_RESULTSETINFO      = "ResultSetInfo";
   public static String ELEM_ROOTFILTER         = "RootFilter";
   public static String ELEM_TABLE              = "Table";
   public static String ELEM_TABLEFILTER        = "TableFilter";
   public static String ELEM_RELATEDTABLEFILTER = "RelatedTableFilter";
   public static String ELEM_WHERE              = "Where";
   public static String ELEM_WRAPPER            = "Wrapper";

   public static final String[] ELEMS = {
                                         ELEM_FILTER,
                                         ELEM_FILTERS,
                                         ELEM_FILTERSET,
                                         ELEM_NAMESPACE,
                                         ELEM_OPTIONS,
                                         ELEM_RESULTSETINFO,
                                         ELEM_ROOTFILTER,
                                         ELEM_TABLE,
                                         ELEM_TABLEFILTER,
                                         ELEM_RELATEDTABLEFILTER,
                                         ELEM_WHERE,
                                         ELEM_WRAPPER
                                        };
   
   // Element type tokens

   public static final int ELEM_TOKEN_INVALID            = -999;
   public static final int ELEM_TOKEN_FILTER             = 1;
   public static final int ELEM_TOKEN_FILTERS            = 2;
   public static final int ELEM_TOKEN_FILTERSET          = 3;
   public static final int ELEM_TOKEN_NAMESPACE          = 4;
   public static final int ELEM_TOKEN_OPTIONS            = 5;
   public static final int ELEM_TOKEN_RESULTSETINFO      = 6;
   public static final int ELEM_TOKEN_ROOTFILTER         = 7;
   public static final int ELEM_TOKEN_TABLE              = 8;
   public static final int ELEM_TOKEN_TABLEFILTER        = 9;
   public static final int ELEM_TOKEN_RELATEDTABLEFILTER = 10;
   public static final int ELEM_TOKEN_WHERE              = 11;
   public static final int ELEM_TOKEN_WRAPPER            = 12;
   
   public static final int[] ELEM_TOKENS = {
                                            ELEM_TOKEN_FILTER,
                                            ELEM_TOKEN_FILTERS,
                                            ELEM_TOKEN_FILTERSET,
                                            ELEM_TOKEN_NAMESPACE,
                                            ELEM_TOKEN_OPTIONS,
                                            ELEM_TOKEN_RESULTSETINFO,
                                            ELEM_TOKEN_ROOTFILTER,
                                            ELEM_TOKEN_TABLE,
                                            ELEM_TOKEN_TABLEFILTER,
                                            ELEM_TOKEN_RELATEDTABLEFILTER,
                                            ELEM_TOKEN_WHERE,
                                            ELEM_TOKEN_WRAPPER
                                           };

   //*********************************************************************
   // Filter language attribute strings and tokens
   //*********************************************************************

   // Attribute names

   public static String ATTR_CATALOG          = "Catalog";
   public static String ATTR_CHILDKEY         = "ChildKey";
   public static String ATTR_CONDITION        = "Condition";
   public static String ATTR_DATABASE         = "Database";
   public static String ATTR_NAME             = "Name";
   public static String ATTR_NUMBER           = "Number";
   public static String ATTR_PARENTKEY        = "ParentKey";
   public static String ATTR_PREFIX           = "Prefix";
   public static String ATTR_SCHEMA           = "Schema";
   public static String ATTR_TABLE            = "Table";
   public static String ATTR_URI              = "URI";
   public static String ATTR_VERSION          = "Version";
   
   public static final String[] ATTRS = {
                                         ATTR_CATALOG,
                                         ATTR_CHILDKEY,
                                         ATTR_CONDITION,
                                         ATTR_DATABASE,
                                         ATTR_NAME,
                                         ATTR_NUMBER,
                                         ATTR_PARENTKEY,
                                         ATTR_PREFIX,
                                         ATTR_SCHEMA,
                                         ATTR_TABLE,
                                         ATTR_URI,
                                         ATTR_VERSION
                                        };

   // Attribute tokens

   public static final int ATTR_TOKEN_INVALID   = -999;
   public static final int ATTR_TOKEN_CATALOG   = 1;
   public static final int ATTR_TOKEN_CHILDKEY  = 2;
   public static final int ATTR_TOKEN_CONDITION = 3;
   public static final int ATTR_TOKEN_DATABASE  = 4;
   public static final int ATTR_TOKEN_NAME      = 5;
   public static final int ATTR_TOKEN_NUMBER    = 6;
   public static final int ATTR_TOKEN_PARENTKEY = 7;
   public static final int ATTR_TOKEN_PREFIX    = 8;
   public static final int ATTR_TOKEN_SCHEMA    = 9;
   public static final int ATTR_TOKEN_TABLE     = 10;
   public static final int ATTR_TOKEN_URI       = 11;
   public static final int ATTR_TOKEN_VERSION   = 12;

   public static final int[] ATTR_TOKENS = {
                                            ATTR_TOKEN_CATALOG,
                                            ATTR_TOKEN_CHILDKEY,
                                            ATTR_TOKEN_CONDITION,
                                            ATTR_TOKEN_DATABASE,
                                            ATTR_TOKEN_NAME,
                                            ATTR_TOKEN_NUMBER,
                                            ATTR_TOKEN_PARENTKEY,
                                            ATTR_TOKEN_PREFIX,
                                            ATTR_TOKEN_SCHEMA,
                                            ATTR_TOKEN_TABLE,
                                            ATTR_TOKEN_URI,
                                            ATTR_TOKEN_VERSION
                                           };

   // Attribute defaults

   public static String DEF_DATABASE = "Default";
   public static String DEF_NUMBER = "0";
   public static String DEF_VERSION = "2.0";
}