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

import org.xmlmiddleware.xmldbms.maps.Table;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Container for the WHERE clause conditions of a filter. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class FilterConditions
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private Table     table;
   private Vector    conditions = new Vector();
   private Vector    expandedConditions = new Vector();
   private Vector    paramNames = new Vector();
   private Hashtable vectorConditions = new Hashtable(), params = null;
   private String    whereCondition;
   private boolean   parseConditions = true, parseVectorConditions = true;

   //*********************************************************************
   // Constants
   //*********************************************************************

   private static final int FINDDOLLAR = 0;
   private static final int DOLLARFOUND = 1;

   private static final int WHITESPACEAFTERPAREN  = 0;
   private static final int PAREN                 = 1;
   private static final int WHITESPACEBEFOREPAREN = 2;
   private static final int N                     = 3;
   private static final int I                     = 4;
   private static final int WHITESPACEBEFOREIN    = 5;

   private static String EMPTYSTRING = new String();
   private static Object O = new Object();

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new FilterConditions object.
    *
    * @param table The Table to which the conditions apply.
    */
   protected FilterConditions(Table table)
   {
      this.table = table;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Table
   //*********************************************************************

   /**
    * Get the table to which the filter conditions apply.
    *
    * @return The table.
    */
   public final Table getTable()
   {
      return table;
   }

   //*********************************************************************
   // Conditions
   //*********************************************************************

   /**
    * Get a specific condition.
    *
    * @param index The index of the condition. 0-based.
    *
    * @return The condition
    */
   public final String getCondition(int index)
   {
      return (String)conditions.elementAt(index);
   }

   /**
    * Get the conditions.
    *
    * @return A Vector containing the conditions as Strings.
    */
   public Vector getConditions()
   {
      return (Vector)conditions.clone();
   }

   /**
    * Add a condition to the end of the list.
    *
    * @param condition The condition
    */
   public void addCondition(String condition)
   {
      conditions.addElement(condition);
      parseConditions = true;
   }

   /**
    * Remove the specified condition.
    *
    * <p>This method shifts conditions at or above the specified index
    * downward one position.</p>
    *
    * @param index Index of the condition. 0-based.
    * @exception IllegalArgumentException Thrown if the index is invalid.
    */
   public void removeCondition(int index)
   {
      if ((index >= 0) && (index < conditions.size()))
      {
         conditions.removeElementAt(index);
      }
      else
         throw new IllegalArgumentException("Invalid index: " + index);

      parseConditions = true;
   }

   /**
    * Remove all wrapper conditions.
    */
   public void removeAllConditions()
   {
      conditions.removeAllElements();
      parseConditions = true;
   }

   //*********************************************************************
   // WHERE clause and parameters
   //*********************************************************************

   /**
    * Set the parameters to be used with the conditions.
    *
    * <p>If the conditions have any parameter arguments, this must be
    * called before calling getWhereCondition() or getParameterValues().</p>
    *
    * @param params A Hashtable containing parameter names and values. Parameter
    *    names must start with a dollar sign ($). Null if there are no parameters.
    */
   public void setParameters(Hashtable params)
   {
      this.params = (params == null) ? new Hashtable() : params;
      parseVectorConditions = true;
   }

   /**
    * Get the condition that can be appended to the WHERE clause.
    *
    * <p>This condition has parameter names replaced with ?'s and all
    * conditions AND'ed together.</p>
    *
    * @return The condition. This may be zero-length but is never null.
    * @exception IllegalArgumentException Thrown if a named parameter is found in
    *    a condition but no corresponding parameter value is provided.
    */
   public final String getWhereCondition()
   {
      parse();
      return whereCondition;
   }

   /**
    * Get a list of parameter values.
    *
    * <p>These occur in the order the corresponding names appear in the WHERE condition.</p>
    *
    * @return The list. May be empty.
    * @exception IllegalArgumentException Thrown if a named parameter is found in
    *    a condition but no corresponding parameter value is provided.
    */
   public final Vector getParameterValues()
   {
      Vector v, paramValues = new Vector();
      Object paramValue;

      parse();

      for (int i = 0; i < paramNames.size(); i++)
      {
         paramValue = params.get(paramNames.elementAt(i));
         if (paramValue instanceof Vector)
         {
            v = (Vector)paramValue;
            for (int j = 0; j < v.size(); j++)
            {
               paramValues.addElement(v.elementAt(j));
            }
         }
         else
         {
            paramValues.addElement(paramValue);
         }
      }

      return paramValues;
   }

   //*********************************************************************
   // Protected methods
   //*********************************************************************

   //*********************************************************************
   // Private methods
   //*********************************************************************

   private void parse()
   {
      if (parseConditions)
      {
         // Initialize global arrays.

         expandedConditions.removeAllElements();
         for (int i = 0; i < conditions.size(); i++)
         {
            expandedConditions.addElement(EMPTYSTRING);
         }

         paramNames.removeAllElements();
         vectorConditions.clear();

         parseConditions();
         appendConditions();
      }
      else if (parseVectorConditions)
      {
         parseVectorConditions();
         appendConditions();
      }
      parseConditions = false;
      parseVectorConditions = false;
   }

   private void parseConditions()
   {
      for (int i = 0; i < conditions.size(); i++)
      {
         parseCondition(i);
      }
   }

   private void parseVectorConditions()
   {
      Enumeration e;

      e = vectorConditions.keys();
      while (e.hasMoreElements())
      {
         parseCondition(((Integer)e.nextElement()).intValue());
      }
   }

   private void parseCondition(int index)
   {
      // WARNING! The index parameter is fragile. In particular, if parseConditions
      // is true, then parseCondition(index) must be called with values of index that
      // increase by 1. This is so we can build the list of parameter names in the
      // order they occur in the conditions. This, in turn, allows us to build the
      // list of parameter values in the order the parameter markers occur in the
      // conditions.

      String       condition, paramName;
      char[]       src;
      StringBuffer dest = new StringBuffer();
      int          save = 0, dollar = 0, state = FINDDOLLAR, numParams;
      Object       paramValue;
      boolean      inClause = false;

      condition = (String)conditions.elementAt(index);
      src = condition.toCharArray();

      for (int i = 0; i < src.length; i++)
      {
         if (state == FINDDOLLAR)
         {
            switch (src[i])
            {
               case '$':
                  // If the parameter name is escaped, remove the escape
                  // character and ignore the parameter.

                  if (i > 0)
                  {
                     if (src[i - 1] == '\\')
                     {
                        dest.append(src, save, i - save);
                        save = i;
                        break;
                     }
                  }

                  // Check if the parameter occurs in an IN clause.

                  inClause = checkINClause(src, dollar);

                  // Otherwise, start parsing the parameter name.

                  state = DOLLARFOUND;
                  dollar = i;
                  break;

               default:
                  break;

            } // switch
         } // if (state == FINDDOLLAR)
         else // if (state == DOLLARFOUND)
         {
            switch (src[i])
            {
               case 0x20:   // Space
               case 0x0d:   // Tab

                  // If the parameter name is zero-length -- that is, we
                  // have a standalone dollar sign, ignore it.

                  if ((i - dollar) == 0)
                  {
                     state = FINDDOLLAR;
                     break;
                  }

                  // Construct the parameter name (including the dollar sign)
                  // and get the parameter value.

                  paramName = new String(src, dollar, i - dollar);
                  paramValue = params.get(paramName);

                  // Otherwise, append the text up to (but not including) the dollar
                  // sign to the destination string.

                  dest.append(src, save, dollar - save);

                  // If the parameter is in an IN clause and is not null, it must be
                  // a Vector. In this case, replace it with Vector.size() parameter
                  // markers (?). Otherwise, replace it with a single parameter marker.

                  numParams = (inClause && (paramValue != null)) ? ((Vector)paramValue).size() : 1;
                  for (int j = 0; j < numParams; j++)
                  {
                     if (j != 0) dest.append(',');
                     dest.append('?');
                  }

                  // Save information about the parameters to optimize later parsing.
                  // Note we only do this on the initial parse, not when reparsing
                  // conditions with Vectors.

                  if (parseConditions)
                  {
                     if (inClause)
                     {
                        // Save a list of the numbers of conditions that have Vector
                        // parameters. We use a Hashtable so that conditions with more
                        // than one Vector parameter appear only once in the list.

                        vectorConditions.put(new Integer(index), O);
                     }

                     // Save the names of parameters. We use these later to extract 
                     // parameter values from the params Hashtable.

                     paramNames.addElement(paramName);
                  }

                  // Start all over...

                  save = i;
                  state = FINDDOLLAR;
                  break;

               default:
                  break;

            } // switch
         } // if (state == DOLLARFOUND)
      } // for

      // Append the rest of the source string to the destination string.

      dest.append(src, save, src.length - save);
      expandedConditions.setElementAt(dest.toString(), index);
   }

   private boolean checkINClause(char[] src, int pos)
   {
      int state;

      // Parse backwards and see if we are in an IN clause.

      state = WHITESPACEAFTERPAREN;
      for (int i = pos - 1; i >= 0; i--)
      {
         switch (state)
         {
            case WHITESPACEAFTERPAREN:
               if ((src[i] != 0x20) && (src[i] != 0x0d)) state = PAREN;
               break;

            case PAREN:
               if (src[i] != '(') return false;
               state = WHITESPACEBEFOREPAREN;
               break;

            case WHITESPACEBEFOREPAREN:
               if ((src[i] != 0x20) && (src[i] != 0x0d)) state = N;
               break;

            case N:
               if ((src[i] != 'n') && (src[i] != 'N')) return false;
               break;

            case I:
               if ((src[i] != 'i') && (src[i] != 'I')) return false;
               state = WHITESPACEBEFOREIN;
               break;

            case WHITESPACEBEFOREIN:
               if ((src[i] == 0x20) || (src[i] == 0x0d)) return true;
               return false;
         }
      }
      return false;
   }

   private void appendConditions()
   {
      StringBuffer sb = new StringBuffer('(');

      for (int i = 0; i < expandedConditions.size(); i++)
      {
         sb.append((String)expandedConditions.elementAt(i));
         sb.append(' ');
      }
      sb.append(')');
      whereCondition = sb.toString();
   }
}