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

   private Table   table;
   private Vector  conditions = new Vector();
   private Vector  expandedConditions = new Vector();
   private String  whereCondition;
   private boolean reparse = true;

   //*********************************************************************
   // Constants
   //*********************************************************************

   private static final int FINDDOLLAR = 0;
   private static final int DOLLARFOUND = 1;

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
      reparse = true;
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

      reparse = true;
   }

   /**
    * Remove all wrapper conditions.
    */
   public void removeAllConditions()
   {
      conditions.removeAllElements();
      reparse = true;
   }

   /**
    * Get the condition that can be appended to the WHERE clause.
    *
    * <p>This condition has parameter names replaced with ?'s, multi-valued
    * parameters expanded into OR'ed conditions, and all conditions AND'ed
    * together.</p>
    *
    * @param params A Hashtable containing parameter names and values. Parameter
    *    names must start with a dollar sign ($).
    * @return The condition. This may be zero-length but is never null.
    */
   public final String getWhereCondition(Hashtable params)
   {
      if (reparse)
      {
         expandConditions(params);
         appendConditions();
      }
      return whereCondition;
   }

   //*********************************************************************
   // Protected methods
   //*********************************************************************

   //*********************************************************************
   // Private methods
   //*********************************************************************

   private void expandConditions(Hashtable params)
   {
      expandedConditions.removeAllElements();
      for (int i = 0; i < conditions.size(); i++)
      {
         expandCondition(i, params);
      }
   }

   private void expandCondition(int index, Hashtable params)
   {
      String       condition, param;
      char[]       src;
      StringBuffer dest = new StringBuffer();
      int          save = 0, dollar = 0, state = FINDDOLLAR, numParams;
      Object       paramValue;

      condition = (String)conditions.elementAt(index);
      src = condition.toCharArray();

      for (int i = 0; i < src.length; i++)
      {
         if (state == FINDDOLLAR)
         {
            switch (src[i])
            {
               case '$':
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

                  // Search for the end of the parameter name token and
                  // get the parameter value.

                  param = new String(src, dollar, i - dollar);
                  paramValue = params.get(param);

                  // If the parameter name does not match a parameter name
                  // passed by the application, ignore it.

                  if (paramValue == null)
                  {
                     state = FINDDOLLAR;
                     break;
                  }

                  // Otherwise, append the text up to (but not including) the dollar
                  // sign to the destination string.

                  dest.append(src, save, dollar - save);

                  // If the parameter is a Vector, replace it with Vector.size()
                  // parameter markers (?). Otherwise, replace it with a single
                  // parameter marker.

                  numParams = (paramValue instanceof Vector) ? ((Vector)paramValue).size() : 1;
                  for (int j = 0; j < numParams; j++)
                  {
                     if (j != 0) dest.append(',');
                     dest.append('?');
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

/*
??

1) need efficient reparsing.
a) first time, parse everything.
b) in same run, no reparsing.
c) next time, only reparse Vectors

2) need to return hashtable of parameter names/numbers
a) build during initial parse run
b) rebuild when reparsing vectors => need to keep info about start/stop param nums
*/
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
      reparse = false;
   }
}