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
import org.xmlmiddleware.xmldbms.maps.Column;

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
   private String[]  parsedConditions = null;
   private Vector    paramNames = new Vector(), paramColumns = new Vector();
   private Object[]  paramValues;
   private Column[]  columns;
   private Hashtable vectorConditions = new Hashtable(), params = new Hashtable();
   private String    whereCondition;
   private boolean   parseConditions = true, parseVectorConditions = true;

   //*********************************************************************
   // Constants
   //*********************************************************************

   private static final int FINDDOLLAR  = 0;
   private static final int DOLLARFOUND = 1;

   private static final int WHITESPACEAFTERPAREN  = 0;
   private static final int PAREN                 = 1;
   private static final int WHITESPACEBEFOREPAREN = 2;
   private static final int N                     = 3;
   private static final int I                     = 4;
   private static final int WHITESPACEBEFOREIN    = 5;

   private static String AND = " AND ";

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
    * <p>If the conditions have any named parameters, this must be called
    * before calling getWhereCondition(), getParameterValues(), or getColumns().
    * Note that calling this method causes conditions with IN parameters to
    * be reparsed, even if the Hashtable of parameters has not changed. Thus,
    * it is best to call it as little as possible.</p>
    *
    * @param params A Hashtable containing parameter names and values. Parameter
    *    names must start with a dollar sign ($). If a parameter value is null,
    *    do not included it in the Hashtable. If there are no parameters or
    *    all parameters are null, pass a null value.
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
    *    a condition but no corresponding column is found in the table.
    */
   public final String getWhereCondition()
   {
      parse();
      return whereCondition;
   }

   /**
    * Get a list of Column objects corresponding to the parameters.
    *
    * <p>These occur in the order the corresponding names appear in the WHERE condition.</p>
    *
    * @return The list. May be null.
    * @exception IllegalArgumentException Thrown if a named parameter is found in
    *    a condition but no corresponding column is found in the table.
    */
   public final Column[] getColumns()
   {
      parse();
      return columns;
   }

   /**
    * Get a list of parameter values.
    *
    * <p>These occur in the order the corresponding names appear in the WHERE condition.</p>
    *
    * @return The list. May be null.
    * @exception IllegalArgumentException Thrown if a named parameter is found in
    *    a condition but no corresponding column is found in the table.
    */
   public final Object[] getParameterValues()
   {
      parse();
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

         parsedConditions = new String[conditions.size()];

         paramNames.removeAllElements();
         paramColumns.removeAllElements();
         vectorConditions.clear();

         parseConditions();
         appendConditions();
         buildParameterValues();
      }
      else if (parseVectorConditions)
      {
         parseVectorConditions();
         appendConditions();
         buildParameterValues();
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
      // Reparse those conditions that have IN operators / use Vectors. This is
      // necessary because the number of parameters in the IN operator must
      // correspond to the number of values in the Vector.

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
      int          save = 0, dollar = 0, state = FINDDOLLAR, numParams, lastDollar;
      Object       paramValue;
      boolean      inOperator = false;
      String       columnName;
      Column       column;

      // Convert the condition string to a character array. Note that we add a
      // trailing space to simplify the parsing code in the case where a parameter
      // name ends the condition string. That is, we change the case "Foo=$Foo" to
      // "Foo=$Foo " so we can stop parsing the parameter name at the space, rather
      // than the end of the string.

      condition = (String)conditions.elementAt(index) + " ";
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
                        dest.append(src, save, i - save - 1);
                        save = i;
                        break;
                     }
                  }

                  // Check if the parameter is the target of an IN operator.

                  inOperator = checkINOperator(src, dollar);

                  // Start parsing the parameter name.

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
               case ')':

                  // Parse until we hit whitespace or a closing parenthesis.

                  // Construct the parameter name (including the dollar sign)
                  // and get the parameter value.

                  paramName = new String(src, dollar, i - dollar);
                  paramValue = params.get(paramName);

                  // Append the text up to (but not including) the dollar
                  // sign to the destination string.

                  dest.append(src, save, dollar - save);

                  // If the parameter is the target of the IN operator and is not null,
                  // it must be a Vector. In this case, replace it with Vector.size() parameter
                  // markers (?). Otherwise, replace it with a single parameter marker.

                  numParams = (inOperator && (paramValue != null)) ? ((Vector)paramValue).size() : 1;
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
                     if (inOperator)
                     {
                        // Save a list of the numbers of conditions that have Vector
                        // parameters. We use a Hashtable so that conditions with more
                        // than one Vector parameter appear only once in the list.

                        vectorConditions.put(new Integer(index), O);
                     }

                     // Save the names of parameters. We use these later to extract 
                     // parameter values from the params Hashtable.

                     paramNames.addElement(paramName);

                     // Save the Column corresponding to the parameter. We need this
                     // to retrieve type information when setting parameters.

                     lastDollar = paramName.lastIndexOf('$');
                     columnName = (lastDollar > 0) ? paramName.substring(1, lastDollar) : paramName.substring(1);
                     column = table.getColumn(columnName);
                     if (column == null)
                        throw new IllegalArgumentException("Filter parameter names must be of the form $Column[$Suffix], where Column matches the name of the column in the table to which the parameter applies. No column was found in table " + table.getUniversalName() + " corresponding to the parameter name " + paramName);
                     paramColumns.addElement(column);
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
      parsedConditions[index] = dest.toString();
   }

   private boolean checkINOperator(char[] src, int pos)
   {
      int state;

      // Parse backwards and see if we are using an IN operator.

      state = WHITESPACEAFTERPAREN;
      for (int i = pos - 1; i >= 0; i--)
      {
         switch (state)
         {
            case WHITESPACEAFTERPAREN:
               // If the current character is whitespace, continue parsing.
               // Otherwise, fall through to check if we have an opening parenthesis.

               if ((src[i] == 0x20) || (src[i] == 0x0d)) break;
               state = PAREN;
               // WARNING! This falls through to the next case.

            case PAREN:
               if (src[i] != '(') return false;
               state = WHITESPACEBEFOREPAREN;
               break;

            case WHITESPACEBEFOREPAREN:
               // If the current character is whitespace, continue parsing.
               // Otherwise, fall through to check if we have an 'N'.

               if ((src[i] == 0x20) || (src[i] == 0x0d)) break;
               state = N;
               // WARNING! This falls through to the next case.

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
      StringBuffer sb = new StringBuffer();

      sb.append('(');
      for (int i = 0; i < parsedConditions.length; i++)
      {
         if (parsedConditions[i] != null)
         {
            if (i != 0) sb.append(AND);
            sb.append(parsedConditions[i]);
            sb.append(' ');
         }
      }
      sb.append(')');
      whereCondition = sb.toString();
   }

   private void buildParameterValues()
   {
      // Build parallel lists of parameter values and Column objects.

      Vector v, values = new Vector(), cols = new Vector();
      Object paramValue;
      Column column;
      int    numParams;

      for (int i = 0; i < paramNames.size(); i++)
      {
         paramValue = params.get(paramNames.elementAt(i));
         column = (Column)paramColumns.elementAt(i);
         if (paramValue instanceof Vector)
         {
            v = (Vector)paramValue;
            for (int j = 0; j < v.size(); j++)
            {
               values.addElement(v.elementAt(j));
               cols.addElement(column);
            }
         }
         else
         {
            values.addElement(paramValue);
            cols.addElement(column);
         }
      }

      numParams = values.size();
      if (numParams > 0)
      {
         paramValues = new Object[numParams];
         values.copyInto(paramValues);
         columns = new Column[numParams];
         cols.copyInto(columns);
      }
      else
      {
         paramValues = null;
         columns = null;
      }
   }
}