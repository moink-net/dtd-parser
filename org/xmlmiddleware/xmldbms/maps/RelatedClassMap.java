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
// Changes from version 1.0: None
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

/**
 * Maps a reference to an element type-as-class to a property in another class;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>RelatedClassMaps are similar to PropertyMaps and InlineClassMaps in that
 * all map references to child element types. A RelatedClassMap contains a pointer
 * to the ClassMap of the child element type, as well as the information needed
 * to join the tables of each class and order the child class in the parent.</p>
 *
 * <p>RelatedClassMap inherits from RelatedMapBase. See RelatedMapBase for methods
 * to get and set the element type and the link and order information.</p>
 *
 * <p>The element type name in a RelatedClassMap can be different from the element
 * type name in the ClassMap it points to. This allows references to a given
 * element type to be mapped differently, depending on their parent element type.</p>
 *
 * <p>RelatedClassMaps are stored in ClassMaps and InlineClassMaps.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 * @see RelatedMapBase
 */

public class RelatedClassMap extends RelatedMapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   // The following variables are inherited from RelatedMapBase

//   private XMLName   elementTypeName = null;
//   private LinkInfo  linkInfo = null;
//   private OrderInfo orderInfo = null;

   // The following variables are new to RelatedClassMap

   private ClassMap  classMap = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private RelatedClassMap(XMLName elementTypeName)
   {
      setElementTypeName(elementTypeName);
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a RelatedClassMap.
    *
    * @param uri Namespace URI of the related element type. May be null.
    * @param localName Local name of the related element type.
    *
    * @return The RelatedClassMap.
    */
   public static RelatedClassMap create(String uri, String localName)
   {
      return new RelatedClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create a RelatedClassMap.
    *
    * @param elementTypeName Name of the related element type.
    *
    * @return The RelatedClassMap.
    */
   public static RelatedClassMap create(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      return new RelatedClassMap(elementTypeName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   /**
    * Get the class map of the related element type.
    *
    * @return The ClassMap. Null if the ClassMap has not yet been set.
    */
   public final ClassMap getClassMap()
   {
      return classMap;
   }

   /**
    * Set the class map to use for the related element type.
    *
    * <p>Generally, this will be the ClassMap of the element type used to create the
    * RelatedClassMap. However, it can be the ClassMap of a different element type. This
    * effectively "casts" the reference to the first element type as a reference to the
    * second element type. For more information, see the description of the &lt;UseClassMap>
    * element type in the XML-DBMS mapping language.</p>
    *
    * @param classMap The ClassMap.
    */
   public void setClassMap(ClassMap classMap)
   {
      checkArgNull(classMap, ARG_CLASSMAP);
      this.classMap = classMap;
   }
}