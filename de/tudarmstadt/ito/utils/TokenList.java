// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.utils;

// import de.tudarmstadt.ito.domutils.NameQualifier;
import java.util.Hashtable;

/**
 * Manages a list of tokens.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class TokenList
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Hashtable hash;
   private int       listDefault = 0;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
	* Construct a TokenList without a default.
	*
	* @param names Token names.
	* @param tokens Token values.
	*/
   public TokenList(String[] names, int[] tokens)
   {
	  initTokens(names, tokens);
   }   

   /**
	* Construct a TokenList with a default.
	*
	* @param names Token names.
	* @param tokens Token values.
	* @param listDefault Default value (returned when a name is not found).
	*/
   public TokenList(String[] names, int[] tokens, int listDefault)
   {
	  this.listDefault = listDefault;
	  initTokens(names, tokens);
   }   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
	* Get the token value for a particular name, overriding the default value
	* if necessary.
	*
	* @param name Token name.
	* @param overrideDefault The temporary default value.
	* @return The token value or overrideDefault if the name is not found.
	*/
   public int getToken(String name, int overrideDefault)
   {
	  Integer i = (Integer) hash.get(name);
	  return (i == null) ? overrideDefault : i.intValue();
   }   

   /**
	* Get the token value for a particular name.
	*
	* @param name Token name.
	* @return The token value or the list default if the name is not found.
	*  If no list default has been set, 0 is returned.
	*/
   public int getToken(String name)
   {
	  Integer i = (Integer) hash.get(name);
	  return (i == null) ? listDefault : i.intValue();
   }   

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private void initTokens(String[] names, int[] tokens)
   {
	  hash = new Hashtable(names.length);
	  
	  for (int i = 0; i < names.length; i++)
	  {
		 hash.put(names[i], new Integer(tokens[i]));
	  }
   }   
   
/*
   private void initTokens(String uri, String[] names, int[] tokens)
   {
	  Hashtable hash = new Hashtable(names.length);
	  
	  for (int i = 0; i < names.length; i++)
	  {
		 hash.put(uri + NameQualifier.SEPARATOR + names[i], new Integer(tokens[i]));
	  }
   }
*/
   
}