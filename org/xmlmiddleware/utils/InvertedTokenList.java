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

package org.xmlmiddleware.utils;

import java.util.Hashtable;

/**
 * An inverted token list is used to find the name associated with a token.
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 * @see TokenList
 */

public class InvertedTokenList
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Hashtable hash;
   private String    defaultName = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
    * Construct an InvertedTokenList without a default.
    *
    * @param tokens Token values.
    * @param names Token names.
    */
   public InvertedTokenList(int[] tokens, String[] names)
   {
      initTokenNames(tokens, names);
   }

   /**
    * Construct an InvertedTokenList with a default.
    *
    * @param tokens Token values.
    * @param names Token names.
    * @param defaultName Default value (returned when a token is not found).
    */
   public InvertedTokenList(int[] tokens, String[] names, String defaultName)
   {
      this.defaultName = defaultName;
      initTokenNames(tokens, names);
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Get the name for a particular token, overriding the default value
    * if necessary.
    *
    * @param token The token.
    * @param overrideDefault The temporary default value.
    * @return The token name or overrideDefault if the name is not found.
    */
   public String getTokenName(int token, String overrideDefault)
   {
      String name = (String)hash.get(new Integer(token));
      return (name == null) ? overrideDefault : name;
   }

   /**
    * Get the name for a particular token.
    *
    * @param token The token.
    * @return The token name or the list default if the token is not found.
    *  If no list default has been set, null is returned.
    */
   public String getTokenName(int token)
   {
      return getTokenName(token, defaultName);
   }

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private void initTokenNames(int[] tokens, String[] names)
   {
      hash = new Hashtable(tokens.length);
      
      for (int i = 0; i < tokens.length; i++)
      {
         hash.put(new Integer(tokens[i]), names[i]);
      }
   }
}

