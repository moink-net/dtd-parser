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
// Changes from version 1.0: New in version 2.0

package org.xmlmiddleware.conversions.helpers;

import org.xmlmiddleware.conversions.StringFormatter;
import org.xmlmiddleware.conversions.ConversionException;

import java.sql.Types;

/**
 * Implements the StringFormatter interface for boolean values.
 *
 * <p>The accepted string forms of true are:</p>
 *
 * <pre>
 *    TRUE
 *    YES
 *    1
 * </pre>
 *
 * <p>The accepted string forms of false are:</p>
 *
 * <pre>
 *    FALSE
 *    NO
 *    0
 * </pre>
 *
 * <p>All are case insensitive.</p>
 *
 * <p>This class can easily be sub-classed to recognize other words as true and false.
 * To do this, simply override the value of the trueValues and falseValues
 * arrays and provide the string values to be recognized as true and false.
 * The first value in each array is used as the output of the format method,
 * and the caseSensitive variable states whether values are case sensitive
 * or not.</p>
 *
 * <p>For example, the following code creates a case-sensitive German version of
 * Boolean formatter:</p>
 *
 * <pre>
 *    public class GermanBooleanFormatter
 *       extends BooleanFormatter
 *       implements StringFormatter
 *    {
 *       public GermanBooleanFormatter() {super();}
 *       public String[] trueValues = {"wahr", "ja"};
 *       public String[] falseValues = {"falsch", "nein"};
 *       public boolean caseSensitive = true;
 *    }
 * </pre>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class BooleanFormatter implements StringFormatter
{
   // ********************************************************************
   // Class variables
   // ********************************************************************

   /**
    * An array of strings to be recognized as true.
    *
    * <p>The first string is used as the output value of format().</p>
    */
   public String[] trueValues = {"true", "yes", "1"};

   /**
    * An array of strings to be recognized as false.
    *
    * <p>The first string is used as the output value of format().</p>
    */
   public String[] falseValues = {"false", "no", "0"};

   /**
    * Whether the strings in trueValues and falseValues are case sensitive.
    */
   public boolean caseSensitive = false;

   private static final Boolean TRUE = new Boolean(true);
   private static final Boolean FALSE = new Boolean(false);
   private boolean firstTime = true;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public BooleanFormatter()
   {
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Parse a boolean string.
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return. This
    *    must be Types.BIT.
    * @return A Boolean.
    * @exception ConversionException Thrown if the string can't be parsed or the type isn't BIT.
    */
   public Object parse(String s, int jdbcType) throws ConversionException
   {
      int    i;
      String value;

      changeArrayCase();

      if (jdbcType == Types.BIT)
      {
         value = (caseSensitive) ? s.toUpperCase() : s;
         for (i = 0; i < trueValues.length; i++)
         {
            if (value.equals(trueValues[i])) return TRUE;
         }

         for (i = 0; i < falseValues.length; i++)
         {
            if (value.equals(falseValues[i])) return FALSE;
         }

         throw new ConversionException("Value can't be parsed as true or false: " + s);
      }
      else
         throw new ConversionException("Conversion to specified JDBC type not supported.");
   }

   /**
    * Convert a Boolean to a string.
    *
    * @param The Boolean
    * @return The string
    * @exception ConversionException Thrown if the object is not a Boolean.
    */
   public String format(Object o) throws ConversionException
   {
      changeArrayCase();

      if (o instanceof Boolean)
      {
         return ((Boolean)o).booleanValue() ? trueValues[0] : falseValues[0];
      }
      else
         throw new ConversionException("Object must be a Boolean.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * <p>This method returns true for Types.BIT.
    * It returns false for all other types.</p>
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Whether the type is supported
    */
   public boolean canConvert(int type)
   {
      return (type == Types.BIT);
   }

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private void changeArrayCase()
   {
      // If the formatter is not case sensitive, change the values in the
      // trueValues and falseValues arrays to upper case. This method is
      // primarily useful for subclasses.

      int i;

      if (!firstTime) return;
      firstTime = false;

      if (caseSensitive) return;

      for (i = 0; i < trueValues.length; i++)
      {
         trueValues[i] = trueValues[i].toUpperCase();
      }

      for (i = 0; i < falseValues.length; i++)
      {
         falseValues[i] = falseValues[i].toUpperCase();
      }
   }
}