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
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmlutils.*;

import java.sql.*;
import java.util.*;

/**
 * Checks if names conform to the rules of the XML Name production.
 *
 * <p>XMLNameChecker checks that a name conforms to the rules of the XML Name
 * production. It also checks that new element type names do not collide with
 * existing element type names and that new attribute names do not collide with
 * existing attribute names. Names are modified as follows:</p>
 *
 * <ol>
 * <li>Any characters not supported by the Name production are discarded.</li>
 * <li>Names are checked against currently used names. If any collisions are
 *     found, then names are prepended until a unique name is constructed.
 *     (This is designed to be used with database names, where a column name is
 *     prepended with its table, schema, catalog, etc. name.) If the name is
 *     still not unique or if no names to prepend were passed, then a number
 *     (starting with 1) is appended until a non-colliding name is found.</li>
 * </ol>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class XMLNameChecker
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private Hashtable elementTypeNames = new Hashtable();
   private Hashtable attributeHashtables = new Hashtable();

   //**************************************************************************
   // Constants
   //**************************************************************************

   private static final Object obj = new Object();

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new XMLNameChecker.
    */
   public XMLNameChecker()
   {
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Start a new name-checking session.
    *
    * <p>This method removes all names from the lists of element type and attribute names
    * created during the previous session. Thus, names are checked for collisions
    * only against names created after this call and before the next call to
    * startNewSession().</p>
    */
   public void startNewSession()
   {
      elementTypeNames.clear();
      attributeHashtables.clear();
   }

   /**
    * Checks an element type name.
    *
    * <p>The prefixes argument is designed to be used when constructing element type
    * names from database names. When the element type name is constructed from a column
    * name, it should contain the table, schema, catalog, and database names. When the
    * element type name is constructed from a table name, it should contain the schema,
    * catalog, and database names. If the prefixes argument is null, collisions are
    * resolved by appending numbers starting with 1.</p>
    *
    * @param prefixes An array of prefixes to prepend to the name to resolve
    *     collisions. May be null.
    * @param namespaceURI The URI of the namespace in which the element type name resides. May be null.
    * @param localName The local part of the element type name to check.
    * @param namespacePrefix The prefix of the namespace in which the element type name
    *     resides. May be null, even if the namespaceURI argument is non-null.
    * @return A unique element type name.
    * @exception XMLMiddlewareException Thrown if a valid name cannot be constructed.
    */
   public XMLName checkElementTypeName(String[] prefixes, String namespaceURI, String localName, String namespacePrefix)
      throws XMLMiddlewareException
   {
      return checkName(prefixes, namespaceURI, localName, namespacePrefix, elementTypeNames);
   }

   /**
    * Checks an attribute name.
    *
    * <p>The prefixes argument is designed to be used when constructing attribute
    * names from database names. It should contain the table, schema, catalog, and
    * database names. If the prefixes argument is null, collisions are resolved
    * by appending numbers starting with 1.</p>
    *
    * @param prefixes An array of prefixes to prepend to the name to resolve
    *     collisions. May be null.
    * @param elementTypeName XMLName of the element type name to which the attribute belongs.
    * @param namespaceURI The URI of the namespace in which the attribute name resides.
    *    May be null.
    * @param localName The local part of the attribute name to check.
    * @param namespacePrefix The prefix of the namespace in which the attribute name
    *     resides. May be null, even if the namespaceURI argument is non-null.
    * @return A unique attribute name.
    * @exception XMLMiddlewareException Thrown if a valid name cannot be constructed.
    */
   public XMLName checkAttributeName(String[] prefixes, XMLName elementTypeName, String namespaceURI, String localName, String namespacePrefix)
      throws XMLMiddlewareException
   {
      Hashtable attributeNames;

      attributeNames = (Hashtable)attributeHashtables.get(elementTypeName);
      if (attributeNames == null)
      {
         attributeNames = new Hashtable();
         attributeHashtables.put(elementTypeName, attributeNames);
      }

      return checkName(prefixes, namespaceURI, localName, namespacePrefix, attributeNames);
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private XMLName checkName(String[] prefixes, String namespaceURI, String localName, String namespacePrefix, Hashtable existingNames)
      throws XMLMiddlewareException
   {
      XMLName newXMLName;
      String  newLocalName, prefix, baseName;
      int     length, suffix;

      // Check the characters in the local name and construct and XMLName.

      newLocalName = checkCharacters(localName);
      newXMLName = XMLName.create(namespaceURI, newLocalName, namespacePrefix);

      // If the XMLName is not unique, prepend the prefixes until the name
      // is unique or we run out of prefixes.

      if (existingNames.get(newXMLName) != null)
      {
         length = (prefixes == null) ? -1 : prefixes.length;
         for (int i = 0; i < length; i++)
         {
            prefix = checkCharacters(prefixes[i]);
            newLocalName = prefix + "." + newLocalName;
            newXMLName = XMLName.create(namespaceURI, newLocalName, namespacePrefix);
            if (existingNames.get(newXMLName) == null) break;
         }

         // If the XMLName is still not unique, append numbers until the name is
         // unique. Except in ridiculous situations, this will yield a unique name.

         baseName = newLocalName;
         suffix = 1;
         while (existingNames.get(newXMLName) != null)
         {
            newLocalName = baseName + String.valueOf(suffix);
            newXMLName = XMLName.create(namespaceURI, newLocalName, namespacePrefix);
            suffix++;
         }
      }

      // Store the XML name, then return it.

      existingNames.put(newXMLName, obj);
      return newXMLName;
   }

   private String checkCharacters(String name)
      throws XMLMiddlewareException
   {
      // Construct a valid XML Name from the input name.

      char[] oldName, newName;
      int    oldPos, newPos;

      // Set things up.

      oldName = name.toCharArray();
      newName = new char[oldName.length];
      newPos = 0;

      // Get the first legal name-start character.

      for (oldPos = 0; oldPos < oldName.length; oldPos++)
      {
         if (isStartChar(oldName[oldPos]))
         {
            newName[newPos] = oldName[oldPos];
            newPos++;
            break;
         }
      }

      // If there are no legal name-start characters, throw an exception.

      if (newPos == 0)
         throw new XMLMiddlewareException("Cannot construct an XML Name from the input name: " + name);

      // Read the remaining characters and discard any non-NameChar characters.

      for (oldPos = oldPos + 1; oldPos < oldName.length; oldPos++)
      {
         if (isNameChar(oldName[oldPos]))
         {
            newName[newPos] = oldName[oldPos];
            newPos++;
         }
      }

      // Return the new name.

      return new String(newName, 0, newPos);
   }

   private boolean isStartChar(char c)
   {
      return (isLetter(c) || (c != '_') || (c != ':'));
   }

   private boolean isNameChar(char c)
   {
      // Checks if the character is a valid NameChar. This is optimized by first
      // checking for A-Z, a-z, 0-9, and '.', '-'. '_', and ':'.

      if (isLatinLetter(c)) return true;
      if (isLatinDigit(c)) return true;
      if ((c == '.') || (c == '-') || (c == '_') || (c == ':')) return true;
      if (isLetter(c)) return true;
      if (isDigit(c)) return true;
      if (isCombiningChar(c)) return true;
      if (isExtender(c)) return true;
      return false;
   }

   private boolean isLatinLetter(char c)
   {
      return (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')));
   }

   private boolean isLatinDigit(char c)
   {
      return ((c >= '0') && (c <= '9'));
   }

   private boolean isLetter(char c)
   {
      // Checks for letters (BaseChar | Ideographic)

      switch(c >> 8)
      {
         case 0x00:
            if ((c >= 0x0041) && (c <= 0x005A)) return true;
            if ((c >= 0x0061) && (c <= 0x007A)) return true;
            if ((c >= 0x00C0) && (c <= 0x00D6)) return true;
            if ((c >= 0x00D8) && (c <= 0x00F6)) return true;
            if ((c >= 0x00F8) && (c <= 0x00FF)) return true;

            return false;

         case 0x01:
            if ((c >= 0x0100) && (c <= 0x0131)) return true;
            if ((c >= 0x0134) && (c <= 0x013E)) return true;
            if ((c >= 0x0141) && (c <= 0x0148)) return true;
            if ((c >= 0x014A) && (c <= 0x017E)) return true;
            if ((c >= 0x0180) && (c <= 0x01C3)) return true;
            if ((c >= 0x01CD) && (c <= 0x01F0)) return true;
            if ((c >= 0x01F4) && (c <= 0x01F5)) return true;
            if ((c >= 0x01FA) && (c <= 0x01FF)) return true;

            return false;

         case 0x02:
            if ((c >= 0x0200) && (c <= 0x0217)) return true;
            if ((c >= 0x0250) && (c <= 0x02A8)) return true;
            if ((c >= 0x02BB) && (c <= 0x02C1)) return true;

            return false;

         case 0x03:
            if ((c >= 0x0388) && (c <= 0x038A)) return true;
            if ((c >= 0x038E) && (c <= 0x03A1)) return true;
            if ((c >= 0x03A3) && (c <= 0x03CE)) return true;
            if ((c >= 0x03D0) && (c <= 0x03D6)) return true;
            if ((c >= 0x03E2) && (c <= 0x03F3)) return true;

            if ((c == 0x0386)  || (c == 0x038C)  || (c == 0x03DA)  ||
                (c == 0x03DC)  || (c == 0x03DE)  || (c == 0x03E0)) return true;

            return false;

         case 0x04:
            if ((c >= 0x0401) && (c <= 0x040C)) return true;
            if ((c >= 0x040E) && (c <= 0x044F)) return true;
            if ((c >= 0x0451) && (c <= 0x045C)) return true;
            if ((c >= 0x045E) && (c <= 0x0481)) return true;
            if ((c >= 0x0490) && (c <= 0x04C4)) return true;
            if ((c >= 0x04C7) && (c <= 0x04C8)) return true;
            if ((c >= 0x04CB) && (c <= 0x04CC)) return true;
            if ((c >= 0x04D0) && (c <= 0x04EB)) return true;
            if ((c >= 0x04EE) && (c <= 0x04F5)) return true;
            if ((c >= 0x04F8) && (c <= 0x04F9)) return true;

            return false;

         case 0x05:
            if ((c >= 0x0531) && (c <= 0x0556)) return true;
            if ((c >= 0x0561) && (c <= 0x0586)) return true;
            if ((c >= 0x05D0) && (c <= 0x05EA)) return true;
            if ((c >= 0x05F0) && (c <= 0x05F2)) return true;

            if (c == 0x0559) return true;

            return false;

         case 0x06:
            if ((c >= 0x0621) && (c <= 0x063A)) return true;
            if ((c >= 0x0641) && (c <= 0x064A)) return true;
            if ((c >= 0x0671) && (c <= 0x06B7)) return true;
            if ((c >= 0x06BA) && (c <= 0x06BE)) return true;
            if ((c >= 0x06C0) && (c <= 0x06CE)) return true;
            if ((c >= 0x06D0) && (c <= 0x06D3)) return true;
            if ((c >= 0x06E5) && (c <= 0x06E6)) return true;

            if (c == 0x06D5) return true;

            return false;

         case 0x09:
            if ((c >= 0x0905) && (c <= 0x0939)) return true;
            if ((c >= 0x0958) && (c <= 0x0961)) return true;
            if ((c >= 0x0985) && (c <= 0x098C)) return true;
            if ((c >= 0x098F) && (c <= 0x0990)) return true;
            if ((c >= 0x0993) && (c <= 0x09A8)) return true;
            if ((c >= 0x09AA) && (c <= 0x09B0)) return true;
            if ((c >= 0x09B6) && (c <= 0x09B9)) return true;
            if ((c >= 0x09DC) && (c <= 0x09DD)) return true;
            if ((c >= 0x09DF) && (c <= 0x09E1)) return true;
            if ((c >= 0x09F0) && (c <= 0x09F1)) return true;

            if ((c == 0x093D)  || (c == 0x09B2)) return true;

            return false;

         case 0x0A:
            if ((c >= 0x0A05) && (c <= 0x0A0A)) return true;
            if ((c >= 0x0A0F) && (c <= 0x0A10)) return true;
            if ((c >= 0x0A13) && (c <= 0x0A28)) return true;
            if ((c >= 0x0A2A) && (c <= 0x0A30)) return true;
            if ((c >= 0x0A32) && (c <= 0x0A33)) return true;
            if ((c >= 0x0A35) && (c <= 0x0A36)) return true;
            if ((c >= 0x0A38) && (c <= 0x0A39)) return true;
            if ((c >= 0x0A59) && (c <= 0x0A5C)) return true;
            if ((c >= 0x0A72) && (c <= 0x0A74)) return true;
            if ((c >= 0x0A85) && (c <= 0x0A8B)) return true;
            if ((c >= 0x0A8F) && (c <= 0x0A91)) return true;
            if ((c >= 0x0A93) && (c <= 0x0AA8)) return true;
            if ((c >= 0x0AAA) && (c <= 0x0AB0)) return true;
            if ((c >= 0x0AB2) && (c <= 0x0AB3)) return true;
            if ((c >= 0x0AB5) && (c <= 0x0AB9)) return true;

            if ((c == 0x0A5E)  || (c == 0x0A8D)  || (c == 0x0ABD)  ||
                (c == 0x0AE0)) return true;

            return false;

         case 0x0B:
            if ((c >= 0x0B05) && (c <= 0x0B0C)) return true;
            if ((c >= 0x0B0F) && (c <= 0x0B10)) return true;
            if ((c >= 0x0B13) && (c <= 0x0B28)) return true;
            if ((c >= 0x0B2A) && (c <= 0x0B30)) return true;
            if ((c >= 0x0B32) && (c <= 0x0B33)) return true;
            if ((c >= 0x0B36) && (c <= 0x0B39)) return true;
            if ((c >= 0x0B5C) && (c <= 0x0B5D)) return true;
            if ((c >= 0x0B5F) && (c <= 0x0B61)) return true;
            if ((c >= 0x0B85) && (c <= 0x0B8A)) return true;
            if ((c >= 0x0B8E) && (c <= 0x0B90)) return true;
            if ((c >= 0x0B92) && (c <= 0x0B95)) return true;
            if ((c >= 0x0B99) && (c <= 0x0B9A)) return true;
            if ((c >= 0x0B9E) && (c <= 0x0B9F)) return true;
            if ((c >= 0x0BA3) && (c <= 0x0BA4)) return true;
            if ((c >= 0x0BA8) && (c <= 0x0BAA)) return true;
            if ((c >= 0x0BAE) && (c <= 0x0BB5)) return true;
            if ((c >= 0x0BB7) && (c <= 0x0BB9)) return true;

            if ((c == 0x0B3D)  || (c == 0x0B9C)) return true;

            return false;

         case 0x0C:
            if ((c >= 0x0C05) && (c <= 0x0C0C)) return true;
            if ((c >= 0x0C0E) && (c <= 0x0C10)) return true;
            if ((c >= 0x0C12) && (c <= 0x0C28)) return true;
            if ((c >= 0x0C2A) && (c <= 0x0C33)) return true;
            if ((c >= 0x0C35) && (c <= 0x0C39)) return true;
            if ((c >= 0x0C60) && (c <= 0x0C61)) return true;
            if ((c >= 0x0C85) && (c <= 0x0C8C)) return true;
            if ((c >= 0x0C8E) && (c <= 0x0C90)) return true;
            if ((c >= 0x0C92) && (c <= 0x0CA8)) return true;
            if ((c >= 0x0CAA) && (c <= 0x0CB3)) return true;
            if ((c >= 0x0CB5) && (c <= 0x0CB9)) return true;
            if ((c >= 0x0CE0) && (c <= 0x0CE1)) return true;

            if (c == 0x0CDE) return true;

            return false;

         case 0x0D:
            if ((c >= 0x0D05) && (c <= 0x0D0C)) return true;
            if ((c >= 0x0D0E) && (c <= 0x0D10)) return true;
            if ((c >= 0x0D12) && (c <= 0x0D28)) return true;
            if ((c >= 0x0D2A) && (c <= 0x0D39)) return true;
            if ((c >= 0x0D60) && (c <= 0x0D61)) return true;

            return false;

         case 0x0E:
            if ((c >= 0x0E01) && (c <= 0x0E2E)) return true;
            if ((c >= 0x0E32) && (c <= 0x0E33)) return true;
            if ((c >= 0x0E40) && (c <= 0x0E45)) return true;
            if ((c >= 0x0E81) && (c <= 0x0E82)) return true;
            if ((c >= 0x0E87) && (c <= 0x0E88)) return true;
            if ((c >= 0x0E94) && (c <= 0x0E97)) return true;
            if ((c >= 0x0E99) && (c <= 0x0E9F)) return true;
            if ((c >= 0x0EA1) && (c <= 0x0EA3)) return true;
            if ((c >= 0x0EAA) && (c <= 0x0EAB)) return true;
            if ((c >= 0x0EAD) && (c <= 0x0EAE)) return true;
            if ((c >= 0x0EB2) && (c <= 0x0EB3)) return true;
            if ((c >= 0x0EC0) && (c <= 0x0EC4)) return true;

            if ((c == 0x0E30)  || (c == 0x0E84)  || (c == 0x0E8A)  ||
                (c == 0x0E8D)  || (c == 0x0EA5)  || (c == 0x0EA7)  ||
                (c == 0x0EB0)  || (c == 0x0EBD)) return true;

            return false;

         case 0x0F:
            if ((c >= 0x0F40) && (c <= 0x0F47)) return true;
            if ((c >= 0x0F49) && (c <= 0x0F69)) return true;

            return false;

         case 0x10:
            if ((c >= 0x10A0) && (c <= 0x10C5)) return true;
            if ((c >= 0x10D0) && (c <= 0x10F6)) return true;

            return false;

         case 0x11:
            if ((c >= 0x1102) && (c <= 0x1103)) return true;
            if ((c >= 0x1105) && (c <= 0x1107)) return true;
            if ((c >= 0x110B) && (c <= 0x110C)) return true;
            if ((c >= 0x110E) && (c <= 0x1112)) return true;
            if ((c >= 0x1154) && (c <= 0x1155)) return true;
            if ((c >= 0x115F) && (c <= 0x1161)) return true;
            if ((c >= 0x116D) && (c <= 0x116E)) return true;
            if ((c >= 0x1172) && (c <= 0x1173)) return true;
            if ((c >= 0x11AE) && (c <= 0x11AF)) return true;
            if ((c >= 0x11B7) && (c <= 0x11B8)) return true;
            if ((c >= 0x11BC) && (c <= 0x11C2)) return true;

            if ((c == 0x1100)  || (c == 0x1109)  || (c == 0x113C)  ||
                (c == 0x113E)  || (c == 0x1140)  || (c == 0x114C)  ||
                (c == 0x114E)  || (c == 0x1150)  || (c == 0x1159)  ||
                (c == 0x1163)  || (c == 0x1165)  || (c == 0x1167)  ||
                (c == 0x1169)  || (c == 0x1175)  || (c == 0x119E)  ||
                (c == 0x11A8)  || (c == 0x11AB)  || (c == 0x11BA)  ||
                (c == 0x11EB)  || (c == 0x11F0)  || (c == 0x11F9)) return true;

            return false;

         case 0x1E:
            if ((c >= 0x1E00) && (c <= 0x1E9B)) return true;
            if ((c >= 0x1EA0) && (c <= 0x1EF9)) return true;

            return false;

         case 0x1F:
            if ((c >= 0x1F00) && (c <= 0x1F15)) return true;
            if ((c >= 0x1F18) && (c <= 0x1F1D)) return true;
            if ((c >= 0x1F20) && (c <= 0x1F45)) return true;
            if ((c >= 0x1F48) && (c <= 0x1F4D)) return true;
            if ((c >= 0x1F50) && (c <= 0x1F57)) return true;
            if ((c >= 0x1F5F) && (c <= 0x1F7D)) return true;
            if ((c >= 0x1F80) && (c <= 0x1FB4)) return true;
            if ((c >= 0x1FB6) && (c <= 0x1FBC)) return true;
            if ((c >= 0x1FC2) && (c <= 0x1FC4)) return true;
            if ((c >= 0x1FC6) && (c <= 0x1FCC)) return true;
            if ((c >= 0x1FD0) && (c <= 0x1FD3)) return true;
            if ((c >= 0x1FD6) && (c <= 0x1FDB)) return true;
            if ((c >= 0x1FE0) && (c <= 0x1FEC)) return true;
            if ((c >= 0x1FF2) && (c <= 0x1FF4)) return true;
            if ((c >= 0x1FF6) && (c <= 0x1FFC)) return true;

            if ((c == 0x1F59)  || (c == 0x1F5B)  || (c == 0x1F5D)  ||
                (c == 0x1FBE)) return true;

            return false;

         case 0x21:
            if ((c >= 0x212A) && (c <= 0x212B)) return true;
            if ((c >= 0x2180) && (c <= 0x2182)) return true;

            if ((c == 0x2126)  || (c == 0x212E)) return true;

            return false;

         case 0x20:
            if ((c >= 0x3041) && (c <= 0x3094)) return true;
            if ((c >= 0x30A1) && (c <= 0x30FA)) return true;
            if ((c >= 0x3021) && (c <= 0x3029)) return true;

            if (c == 0x3007) return true;

            return false;

         case 0x31:
            if ((c >= 0x3105) && (c <= 0x312C)) return true;

            return false;

         default:
            if ((c >= 0xAC00) && (c <= 0xD7A3)) return true;
            if ((c >= 0x4E00) && (c <= 0x9FA5)) return true;

            return false;
      }
   }

   private boolean isDigit(char c)
   {
      // Checks for digits. Note that the Java Character.isDigit() function
      // includes the values 0xFF10 - 0xFF19, which are not considered digits
      // according to the XML spec. Therefore, we need to check if these are
      // the reason Character.isDigit() returned true.

      if (!Character.isDigit(c)) return false;
      return (c > 0xF29);
   }

   private boolean isCombiningChar(char c)
   {
      // Checks for combining characters.

      switch (c >> 8)
      {
         case 0x03:
            if ((c >= 0x0300) && (c <= 0x0345)) return true;
            if ((c >= 0x0360) && (c <= 0x0361)) return true;

            return false;

         case 0x04:
            if ((c >= 0x0483) && (c <= 0x0486)) return true;

            return false;

         case 0x05:
            if ((c >= 0x0591) && (c <= 0x05A1)) return true;
            if ((c >= 0x05A3) && (c <= 0x05B9)) return true;
            if ((c >= 0x05BB) && (c <= 0x05BD)) return true;
            if ((c >= 0x05C1) && (c <= 0x05C2)) return true;

            if ((c == 0x05BF) || (c == 0x05C4)) return true;

            return false;

         case 0x06:
            if ((c >= 0x064B) && (c <= 0x0652)) return true;
            if ((c >= 0x06D6) && (c <= 0x06DC)) return true;
            if ((c >= 0x06DD) && (c <= 0x06DF)) return true;
            if ((c >= 0x06E0) && (c <= 0x06E4)) return true;
            if ((c >= 0x06E7) && (c <= 0x06E8)) return true;
            if ((c >= 0x06EA) && (c <= 0x06ED)) return true;

            if (c == 0x0670) return true;

            return false;

         case 0x09:
            if ((c >= 0x0901) && (c <= 0x0903)) return true;
            if ((c >= 0x093E) && (c <= 0x094C)) return true;
            if ((c >= 0x0951) && (c <= 0x0954)) return true;
            if ((c >= 0x0962) && (c <= 0x0963)) return true;
            if ((c >= 0x0981) && (c <= 0x0983)) return true;
            if ((c >= 0x09C0) && (c <= 0x09C4)) return true;
            if ((c >= 0x09C7) && (c <= 0x09C8)) return true;
            if ((c >= 0x09CB) && (c <= 0x09CD)) return true;
            if ((c >= 0x09E2) && (c <= 0x09E3)) return true;

            if ((c == 0x093C) || (c == 0x094D) || (c == 0x09BC) ||
                (c == 0x09BE) || (c == 0x09BF) || (c == 0x09D7)) return true;

            return false;

         case 0x0A:
            if ((c >= 0x0A40) && (c <= 0x0A42)) return true;
            if ((c >= 0x0A47) && (c <= 0x0A48)) return true;
            if ((c >= 0x0A4B) && (c <= 0x0A4D)) return true;
            if ((c >= 0x0A70) && (c <= 0x0A71)) return true;
            if ((c >= 0x0A81) && (c <= 0x0A83)) return true;
            if ((c >= 0x0ABE) && (c <= 0x0AC5)) return true;
            if ((c >= 0x0AC7) && (c <= 0x0AC9)) return true;
            if ((c >= 0x0ACB) && (c <= 0x0ACD)) return true;

            if ((c == 0x0A02) || (c == 0x0A3C) || (c == 0x0A3E) ||
                (c == 0x0A3F) || (c == 0x0ABC)) return true;

            return false;

         case 0x0B:
            if ((c >= 0x0B01) && (c <= 0x0B03)) return true;
            if ((c >= 0x0B3E) && (c <= 0x0B43)) return true;
            if ((c >= 0x0B47) && (c <= 0x0B48)) return true;
            if ((c >= 0x0B4B) && (c <= 0x0B4D)) return true;
            if ((c >= 0x0B56) && (c <= 0x0B57)) return true;
            if ((c >= 0x0B82) && (c <= 0x0B83)) return true;
            if ((c >= 0x0BBE) && (c <= 0x0BC2)) return true;
            if ((c >= 0x0BC6) && (c <= 0x0BC8)) return true;
            if ((c >= 0x0BCA) && (c <= 0x0BCD)) return true;

            if ((c == 0x0B3C) || (c == 0x0BD7)) return true;

            return false;

         case 0x0C:
            if ((c >= 0x0C01) && (c <= 0x0C03)) return true;
            if ((c >= 0x0C3E) && (c <= 0x0C44)) return true;
            if ((c >= 0x0C46) && (c <= 0x0C48)) return true;
            if ((c >= 0x0C4A) && (c <= 0x0C4D)) return true;
            if ((c >= 0x0C55) && (c <= 0x0C56)) return true;
            if ((c >= 0x0C82) && (c <= 0x0C83)) return true;
            if ((c >= 0x0CBE) && (c <= 0x0CC4)) return true;
            if ((c >= 0x0CC6) && (c <= 0x0CC8)) return true;
            if ((c >= 0x0CCA) && (c <= 0x0CCD)) return true;
            if ((c >= 0x0CD5) && (c <= 0x0CD6)) return true;
            return false;

         case 0x0D:
            if ((c >= 0x0D02) && (c <= 0x0D03)) return true;
            if ((c >= 0x0D3E) && (c <= 0x0D43)) return true;
            if ((c >= 0x0D46) && (c <= 0x0D48)) return true;
            if ((c >= 0x0D4A) && (c <= 0x0D4D)) return true;

            if (c == 0x0D57) return true;

            return false;

         case 0x0E:
            if ((c >= 0x0E34) && (c <= 0x0E3A)) return true;
            if ((c >= 0x0E47) && (c <= 0x0E4E)) return true;
            if ((c >= 0x0EB4) && (c <= 0x0EB9)) return true;
            if ((c == 0x0EBB) && (c <= 0x0EBC)) return true;
            if ((c >= 0x0EC8) && (c <= 0x0ECD)) return true;

            if ((c == 0x0E31) || (c == 0x0EB1)) return true;

            return false;

         case 0x0F:
            if ((c >= 0x0F18) && (c <= 0x0F19)) return true;
            if ((c >= 0x0F71) && (c <= 0x0F84)) return true;
            if ((c >= 0x0F86) && (c <= 0x0F8B)) return true;
            if ((c >= 0x0F90) && (c <= 0x0F95)) return true;
            if ((c >= 0x0F99) && (c <= 0x0FAD)) return true;
            if ((c >= 0x0FB1) && (c <= 0x0FB7)) return true;

            if ((c == 0x0F35) || (c == 0x0F37) || (c == 0x0F39) ||
                (c == 0x0F3E) || (c == 0x0F3F) || (c == 0x0F97) ||
                (c == 0x0FB9)) return true;

            return false;

         case 0x20:
            if ((c >= 0x20D0) && (c <= 0x20DC)) return true;

            if (c == 0x20E1) return true;

            return false;

         case 0x30:
            if ((c >= 0x302A) && (c <= 0x302F)) return true;

            if ((c == 0x3099) || (c == 0x309A)) return true;

            return false;

         default:
            return false;
      }
   }

   private boolean isExtender(char c)
   {
      // Checks for extenders.

      switch (c)
      {
         case 0x00B7:
         case 0x02D0:
         case 0x02D1:
         case 0x0387:
         case 0x0640:
         case 0x0E46:
         case 0x0EC6:
         case 0x3005:
            return true;

         default:
            if ((c >= 0x3031) && (c <= 0x3035)) return true;
            if ((c >= 0x309D) && (c <= 0x309E)) return true;
            if ((c >= 0x30FC) && (c <= 0x30FE)) return true;
            return false;
      }
   }
}
