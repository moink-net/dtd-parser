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

package org.xmlmiddleware.xmldbms;

/**
 * Action language strings and tokens. <b>Only used internally.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ActionConst
{
   //*********************************************************************
   // Action attributes and values
   //*********************************************************************

   public static String URI_ACTION = "http://www.xmlmiddleware.org/xmldbms/actions/v2";
   public static String ATTR_ACTION = "Action";
   public static String ATTR_UPDATEPROPERTIES = "UpdateProperties";

   public static String VALUE_NONE = "None";
   public static String VALUE_INSERT = "Insert";
   public static String VALUE_SOFTINSERT = "SoftInsert";
   public static String VALUE_UPDATEORINSERT = "UpdateOrInsert";
   public static String VALUE_DELETE = "Delete";
   public static String VALUE_SOFTDELETE = "SoftDelete";

   public static String VALUE_ALL = "#All";
   public static String VALUE_PCDATA = "#PCDATA";

   //*********************************************************************
   // Action language element strings and tokens
   //*********************************************************************

   public static String ELEM_ACTION         = "Action";
   public static String ELEM_ACTIONS        = "Actions";
   public static String ELEM_ALL            = "All";
   public static String ELEM_ATTRIBUTE      = "Attribute";
   public static String ELEM_DELETE         = "Delete";
   public static String ELEM_ELEMENTTYPE    = "ElementType";
   public static String ELEM_INSERT         = "Insert";
   public static String ELEM_NONE           = "None";
   public static String ELEM_PCDATA         = "PCDATA";
   public static String ELEM_SOFTDELETE     = "SoftDelete";
   public static String ELEM_SOFTINSERT     = "SoftInsert";
   public static String ELEM_UPDATE         = "Update";
   public static String ELEM_UPDATEORINSERT = "UpdateOrInsert";
   
   public static final String[] ELEMS = {
                                         ELEM_ACTION,
                                         ELEM_ACTIONS,
                                         ELEM_ALL,
                                         ELEM_ATTRIBUTE,
                                         ELEM_DELETE,
                                         ELEM_ELEMENTTYPE,
                                         ELEM_INSERT,
                                         ELEM_NONE,
                                         ELEM_PCDATA,
                                         ELEM_SOFTDELETE,
                                         ELEM_SOFTINSERT,
                                         ELEM_UPDATE,
                                         ELEM_UPDATEORINSERT
                                        };
   
   // Element type tokens

   public static final int ELEM_TOKEN_INVALID        = -999;
   public static final int ELEM_TOKEN_ACTION         = 0;
   public static final int ELEM_TOKEN_ACTIONS        = 1;
   public static final int ELEM_TOKEN_ALL            = 2;
   public static final int ELEM_TOKEN_ATTRIBUTE      = 3;
   public static final int ELEM_TOKEN_DELETE         = 4;
   public static final int ELEM_TOKEN_ELEMENTTYPE    = 5;
   public static final int ELEM_TOKEN_INSERT         = 6;
   public static final int ELEM_TOKEN_NONE           = 7;
   public static final int ELEM_TOKEN_PCDATA         = 8;
   public static final int ELEM_TOKEN_SOFTDELETE     = 9;
   public static final int ELEM_TOKEN_SOFTINSERT     = 10;
   public static final int ELEM_TOKEN_UPDATE         = 11;
   public static final int ELEM_TOKEN_UPDATEORINSERT = 12;
   
   public static final int[] ELEM_TOKENS = {
                                            ELEM_TOKEN_ACTION,
                                            ELEM_TOKEN_ACTIONS,
                                            ELEM_TOKEN_ALL,
                                            ELEM_TOKEN_ATTRIBUTE,
                                            ELEM_TOKEN_DELETE,
                                            ELEM_TOKEN_ELEMENTTYPE,
                                            ELEM_TOKEN_INSERT,
                                            ELEM_TOKEN_NONE,
                                            ELEM_TOKEN_PCDATA,
                                            ELEM_TOKEN_SOFTDELETE,
                                            ELEM_TOKEN_SOFTINSERT,
                                            ELEM_TOKEN_UPDATE,
                                            ELEM_TOKEN_UPDATEORINSERT
                                           };

   //*********************************************************************
   // Action language attribute strings and tokens
   //*********************************************************************

   // Attribute names

   public static String ATTR_NAME             = "Name";
   public static String ATTR_VERSION          = "Version";
   
   public static final String[] ATTRS = {
                                         ATTR_NAME,
                                         ATTR_VERSION
                                        };

   // Attribute tokens

   public static final int ATTR_TOKEN_INVALID = -999;
   public static final int ATTR_TOKEN_NAME    = 1;
   public static final int ATTR_TOKEN_VERSION = 2;

   public static final int[] ATTR_TOKENS = {
                                            ATTR_TOKEN_NAME,
                                            ATTR_TOKEN_VERSION
                                           };

   // Attribute defaults

   public static String DEF_VERSION = "2.0";
}
