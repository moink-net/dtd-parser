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

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

/**
 * Base class for RelatedClassMap and RelatedClassTableMap;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>RelatedMapBase provides accessors and mutators for element type
 * name, link information, and order information. It is the base class for
 * RelatedClassMap and RelatedClassTableMap.</p>
 *
 * <p>RelatedMapBase is never used directly. It is only used through
 * RelatedClassMap and RelatedClassTableMap.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see RelatedClassMap
 * @see RelatedClassTableMap
 */

public class RelatedMapBase extends MapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private LinkInfo  linkInfo = null;
   private OrderInfo orderInfo = null;
   private XMLName   elementTypeName = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
    * Construct a RelatedMapBase.
    */
   RelatedMapBase()
   {
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Element type
   // ********************************************************************

   /**
    * Get the name of the related element type.
    *
    * @return The name of the related element type.
    */
   public final XMLName getElementTypeName()
   {
      return elementTypeName;
   }

   // PACKAGE LEVEL! This is called by the constructor of RelatedClassMap and
   // by setElementTypeName() in RelatedClassTableMap.

   void setElementTypeName(XMLName elementTypeName)
   {
      this.elementTypeName = elementTypeName;
   }

   // ********************************************************************
   // Link information
   // ********************************************************************

   /**
    * Get the information used to link the tables of the parent class and
    * the related class.
    *
    * @return The LinkInfo.
    */
   public final LinkInfo getLinkInfo()
   {
      return linkInfo;
   }

   /**
    * Set the information used to link the tables of the parent class and
    * the related class.
    *
    * @param linkInfo The LinkInfo.
    */
   public void setLinkInfo(LinkInfo linkInfo)
   {
      this.linkInfo = linkInfo;
   }

   // ********************************************************************
   // Order information
   // ********************************************************************

   /**
    * Get the information used to order the child element in its parent.
    *
    * @return The order information. Null if the child element is not ordered.
    */
   public final OrderInfo getOrderInfo()
   {
      return orderInfo;
   }

   /**
    * Set the information used to order the child element in its parent.
    *
    * @param orderInfo The order information. Null if the child element is not ordered.
    */
   public void setOrderInfo(OrderInfo orderInfo)
   {
      this.orderInfo = orderInfo;
   }
}
