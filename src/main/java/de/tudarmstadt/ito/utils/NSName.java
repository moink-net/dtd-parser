// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.utils;

import java.util.Hashtable;

/**
 * Contains an element type or attribute name in local, prefixed, and
 * namespace-URI qualified forms.
 *
 * <P>The local form of an element type or attribute name is the unprefixed
 * name. The prefixed form is the prefixed (with colon) form. The qualified
 * form is the namespace URI plus a caret (^) plus the local name. If the
 * element type or attribute does not belong to a namespace, then all three
 * forms are the same. For example:
 *
 * <PRE>
 *    <foo:element1 xmlns="http://foo">
 *    Local name: "element1"
 *    Prefixed name: "foo:element1"
 *    Qualified name: "http://foo^element1"<br />
 *
 *    <element2>
 *    Local name: "element2"
 *    Prefixed name: "element2"
 *    Qualified name: "element2"
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class NSName
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   /**
	* The character used to separate the URI from the local name. Following the
	* convention of John Cowan's SAX namespace filter, a caret (^) is used
	* because it is neither a valid URI character nor a valid XML name
	* character.
	*/
   public static String SEPARATOR = "^";

   private static String COLON = ":",
						 XML = "xml",
						 XMLNS = "xmlns",
						 W3CNAMESPACE = "http://www.w3.org/XML/1998/namespace";

   //***********************************************************************
   // Variables
   //***********************************************************************

   /** Local name of the element type or attribute. */
   public String local = null;

   /** Prefixed name of the element type or attribute. */
   public String prefixed = null;

   /** Qualified name of the element type or attribute. */
   public String qualified = null;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   /** Construct an empty NSName. */
   public NSName()
   {
   }   

   /** Construct as NSName from a local name, prefix, and namespace URI. */
   public NSName(String local, String prefix, String uri)
   {
	  this.local = local;
	  this.prefixed = getPrefixedName(local, prefix);
	  this.qualified = getQualifiedName(local, uri);
   }   

   //***********************************************************************
   // Methods
   //***********************************************************************

   /**
	* Construct a qualified name. Returns the local name if the URI is
	* null or zero-length.
	*
	* @param local The local name.
	* @param uri The namespace URI.
	*/
   public static String getQualifiedName(String local, String uri)
   {
	  if (uri == null) return local;
	  if (uri.length() == 0) return local;
	  return uri + SEPARATOR + local;
   }   

   /**
	* Construct a prefixed name. Returns the local name if the URI is
	* null or zero-length.
	*
	* @param local The local name.
	* @param uri The namespace prefix.
	*/
   public static String getPrefixedName(String local, String prefix)
   {
	  if (prefix == null) return local;
	  if (prefix.length() == 0) return local;
	  return prefix + COLON + local;
   }   

   /**
	* Given a prefixed name and a Hashtable relating prefixes to
	* namespace URIs, create a new NSName.
	*
	* <p>If the name is not prefixed, or if the Hashtable parameter is null,
	* then the local, prefixed, and qualified names in the returned NSName
	* are all set to the prefixedName parameter.</p>
	*
	* @param prefixedName The prefixed name. Not required to contain a prefix.
	* @param namespaceURIs The Hashtable containing prefixes as keys and
	*   namespace URIs as values. May be null.
	* @return The new NSName.
	* @exception IllegalArgumentException Thrown if the prefixed name contains
	*   more than one colon or the Hashtable does not contain the prefix as a
	*   key.
	* 
	*/
   public static NSName getNSName(String prefixedName, Hashtable namespaceURIs)
   {
	  // This method takes a (possibly) prefixed name and a (possibly null)
	  // Hashtable relating namespace prefixes to URIs and returns an NSName.
	  // If the Hashtable is null, all three values in the NSName are the
	  // prefixed name. If the Hashtable is non-null, then the prefix is
	  // resolved and the normal NSName is created.

	  NSName name;
	  String local = prefixedName, prefix = null, uri = null;
	  int    colon;

	  // If namespaces are being used, search the prefixed name for a
	  // colon and get the prefix, namespace URI, and local name.

	  if (namespaceURIs != null)
	  {
		 colon = prefixedName.indexOf(':');
		 if (colon == -1)
		 {
			// Check for a default namespace.
			uri = (String)namespaceURIs.get("");
		 }
		 else
		 {
			// Check that the name is constructed legally -- that is that
			// it doesn't have more than one colon or end in a colon.

			if (prefixedName.indexOf(':', colon + 1) != -1)
			   throw new IllegalArgumentException("More than one colon in element type or attribute name: " + prefixedName);
			if (colon == (prefixedName.length() - 1))
			   throw new IllegalArgumentException("Invalid prefixed name: " + prefixedName);

			// Get the local name, prefix, and namespace URI.
			
			prefix = prefixedName.substring(0, colon);
			if (prefix.equals("xmlns"))
			{
			   // If the prefix is xmlns, by definition, there is no namespace
			   // and (I suppose) the right thing to do is treat the attribute
			   // as a name not containing a colon.

			   prefix = null;
			}
			else if (prefix.equals(XML))
			{
			   // By definition, xml prefixes have a namespace of
			   // http://www.w3.org/XML/1998/namespace.

			   local = prefixedName.substring(colon + 1);
			   uri = W3CNAMESPACE;
			}
			else
			{
			   local = prefixedName.substring(colon + 1);
			   uri = (String)namespaceURIs.get(prefix);
			   if (uri == null)
				  throw new IllegalArgumentException("No namespace declaration for prefix: " + prefix);
			}
		 }
	  }

	  // Return a new NSName.

	  return new NSName(local, prefix, uri);
   }   

   /**
	* Get the prefix from a prefixed name.
	*
	* @param prefixedName The prefixed name.
	* @return The prefix or null if there is no prefix. Note that null is
	*  also returned if the prefix is xmlns, which we don't treat as a prefix
	*  since (by definition) it is not associated with any namespace.
	*/
   public static String getPrefix(String prefixedName)
   {
	  int    colon;
	  String prefix;

	  // Return the prefix. Note that we special-case the prefix "xmlns",
	  // which isn't really a prefix... (See the Namespaces spec for details.)

	  colon = prefixedName.indexOf(COLON);
	  if (colon == -1) return null;
	  prefix = prefixedName.substring(0, colon);
	  if (prefix.equals(XMLNS)) return null;
	  return prefix;
   }   

   /**
	* Get the URI from a qualified name.
	*
	* @param prefixedName The qualified name.
	* @return The URI or null if there is no URI.
	*/
   public static String getURI(String qualifiedName)
   {
	  int colon;

	  colon = qualifiedName.indexOf(SEPARATOR);
	  if (colon == -1) return null;
	  return qualifiedName.substring(0, colon);
   }   
}