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

package org.xmlmiddleware.utils;

import java.util.*;

/**
 * Implements a simple object pool.
 *
 * <p>This is an abstract class that other classes extend to get pooling
 * functionality. Subclasses implement createObject and closeObject
 * to create and close objects that use the pool.</p>
 *
 * <p>The pool has explicit check-out and check-in methods. However,
 * the calling application never knows if new objects are allocated
 * during the check-out procedure.</p>
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public abstract class Pool
{
   //**************************************************************************
   // Constructors and finalizers
   //**************************************************************************

   /** 
    * Construct a new Pool object.
    */
   public Pool()
   {
      m_objects = new Hashtable();
      m_checked = new Hashtable();
   }

   /** 
    * Closes all objects under control of the pool, regardless of whether
    * they are checked in or out.
    */
   public void finalize()
   {
      clear();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Check an Object with the specified ID out of the pool.
    *
    * <p>The object is not available to other processes until it has been checked
    * back in. Note that the pool can contain more than one object that has the
    * same ID. For example, when used to pool connections, this allows the pool to
    * contain more than one connection to the same databases.</p>
    *
    * @return The object.
    * @exception XMLMiddlewareException Thrown if the pool does not recognize the ID or cannot
    *                          return the specified object for any reason, such as lack
    *                          of resources.
    */
   public Object checkOut(Object id)
      throws XMLMiddlewareException
   {
      Object obj = null;

      // Check if we have that id...
      if(m_objects.containsKey(id))
      {
         Stack stack = (Stack)m_objects.get(id);

         // .. and if object available get it
         if(!stack.empty())
            obj = stack.pop();
      }

      // Otherwise create
      if(obj == null)
         obj = createObject(id);
      
      // Put it in checked map. This map is indexed by object with the value
      // as the id for checking in again later.
      m_checked.put(obj, id);

      return obj;
   }

   /**
    * Check an Object back into the pool.
    *
    * @param object The object.
    * @exception XMLMiddlewareException Thrown if the object does not belong to this pool.
    */
   public void checkIn(Object object)
      throws XMLMiddlewareException
   {
      // Make sure this is our object
      if(!m_checked.containsKey(object)) 
         throw new XMLMiddlewareException("Object does not belong to this pool.");

      // Take it out of the checked map
      Object id = m_checked.remove(object);

      // And put it in the pool
      if(!m_objects.containsKey(id))
         m_objects.put(id, new Stack());

      Stack stack = (Stack)m_objects.get(id);
      stack.push(object);
   }

   //**************************************************************************
   // Abstract methods
   //**************************************************************************

   /**
    * Abstract method to create an object for this pool.
    *
    * @param id Identifier for creating this object.
    * @return The object created.
    * @exception XMLMiddlewareException Thrown if object cannot be created or id is invalid.
    */
   protected abstract Object createObject(Object id)
      throws XMLMiddlewareException;

   /**
    * Abstract method to close an object for this pool.
    *
    * @param obj The object to close.
    * @exception XMLMiddlewareException Thrown if object is not valid.
    */
   protected abstract void closeObject(Object obj)
      throws XMLMiddlewareException;

   //**************************************************************************
   // Protected methods
   //**************************************************************************

   /**
    * Remove all objects from the pool.
    *
    * <p>This method closes all objects in the pool, regardless of whether they
    * are checked in or out. It ignores any errors encountered while closing
    * objects.</p>
    */
   protected void clear()
   {
      // Close all objects under control of the pool
      closeCheckedInObjects();
      closeCheckedOutObjects();

      // Clear the hashtables
      m_objects.clear();
      m_checked.clear();
   }

   /**
    * Remove an object from the pool, such as in case of an error.
    *
    * <p>This method closes the object. It ignores any errors encountered while
    * closing the object.</p>
    *
    * @param object The object.
    * @exception XMLMiddlewareException Thrown if the object is not checked out.
    */
   protected void remove(Object object)
      throws XMLMiddlewareException
   {
      if (!m_checked.containsKey(object))
         throw new XMLMiddlewareException("Object not in pool");

      // Close the object. Ignore any errors, since the object may no
      // longer be in a valid state.

      try
      {
         closeObject(object);
      }
      catch (XMLMiddlewareException e)
      {
      }

      // Remove it from the checked queue. Note that we do not need to remove
      // the object from m_objects, since it is guaranteed to have been checked out.

      m_checked.remove(object);
   }

   /**
    * Close all objects checked in to the pool.
    */
   protected void closeCheckedInObjects()
   {
      for(Enumeration e = m_objects.elements(); e.hasMoreElements(); )
      {
         Stack stack = (Stack)e.nextElement();
         while(!stack.empty())
         {
            try
            {
               closeObject(stack.pop());
            }
            catch (XMLMiddlewareException p)
            {
            }
         }
      }
   }

   /**
    * Close all objects checked out of the pool.
    */
   protected void closeCheckedOutObjects()
   {
      for(Enumeration e = m_checked.keys(); e.hasMoreElements(); )
      {
         try
         {
            closeObject(e.nextElement());
         }
         catch (XMLMiddlewareException p)
         {
         }
      }
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   // Checked in objects
   // A Hashtable of Stacks of created objects indexed by id 
   protected Hashtable m_objects;

   // Checked out objects
   // A Hashtable of id's indexed by object (for checking back in)
   protected Hashtable m_checked;
}
