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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.xmldbms.maps;

/**
 * MapBase is the base class for all map objects.
 *
 * <p>It provides a few constants and methods for argument checking.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class MapBase
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   // Argument names

   static final String ARG_CHILDKEY             = "childKey";
   static final String ARG_CLASSMAP             = "classMap";
   static final String ARG_CLASSTABLEMAP        = "classTableMap";
   static final String ARG_COLUMN               = "column";
   static final String ARG_COLUMNMAP            = "columnMap";
   static final String ARG_COLUMNNAME           = "columnName";
   static final String ARG_ELEMENTINSERTIONMAP  = "elementInsertionMap";
   static final String ARG_ELEMENTTYPENAME      = "elementTypeName";
   static final String ARG_FORMAT               = "format";
   static final String ARG_INLINECLASSMAP       = "inlineClassMap";
   static final String ARG_KEY                  = "key";
   static final String ARG_KEYNAME              = "keyName";
   static final String ARG_LOCALNAME            = "localName";
   static final String ARG_MAP                  = "map";
   static final String ARG_NAME                 = "name";
   static final String ARG_ORDERCOLUMN          = "orderColumn";
   static final String ARG_PARENTKEY            = "parentKey";
   static final String ARG_PREFIX               = "prefix";
   static final String ARG_PROPMAP              = "propMap";
   static final String ARG_PROPTABLEMAP         = "propTableMap";
   static final String ARG_RELATEDCLASSMAP      = "relatedClassMap";
   static final String ARG_RELATEDCLASSTABLEMAP = "relatedClassTableMap";
   static final String ARG_TABLE                = "table";
   static final String ARG_TABLENAME            = "tableName";
   static final String ARG_UNIVERSALNAME        = "universalName";
   static final String ARG_URI                  = "uri";
   static final String ARG_USECLASSMAP          = "useClassMap";
   static final String ARG_USECLASSTABLEMAP     = "useClassTableMap";
   static final String ARG_XMLNAME              = "xmlName";

   // Default format names

   static final String DEFAULT         = "Default";
   static final String DEFAULTDATE     = "DefaultDate";
   static final String DEFAULTTIME     = "DefaultTime";
   static final String DEFAULTDATETIME = "DefaultDateTime";
   static final String DEFAULTNUMBER   = "DefaultNumber";

   // Error messages

   private static final String ARGUMENT = "Programming error. Argument ";
   private static final String NOTNULL  = " must not be null.";

   // ********************************************************************
   // Methods
   // ********************************************************************


   // Checks if an argument value is null.

   static void checkArgNull(Object o, String argName)
   {
      if (o == null) throw new IllegalArgumentException (ARGUMENT + argName + NOTNULL);
   }
}
