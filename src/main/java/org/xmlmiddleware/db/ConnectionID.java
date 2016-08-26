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

package org.xmlmiddleware.db;

/**
 * Internal ID class used by JDBC1DataSource and JDBC2DataSource.
 *
 * <p>ConnectionID encapsulates a user name and password.</p>
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConnectionID
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new ConnectionID with a null user name and password.
    */
   public ConnectionID()
   {
      username = null;
      password = null;
   }

   /**
    * Construct a new ConnectionID with a non-null user name and password.
    *
    * @param u The user name
    * @param p The password
    */
   public ConnectionID(String u, String p)
   {
      username = u;
      password = p;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Overrides Object.equals(Object)
    *
    * <p>Two ConnectionIDs with the same user name and password are
    * considered equal.</p>
    *
    * @return Whether the objects are equal.
    */
   public boolean equals(Object object)
   {
      if (!(object instanceof ConnectionID)) return false;

      ConnectionID id = (ConnectionID)object;

      if (id.username == null)
      {
         if (username != null) return false;
      }
      else if (!id.username.equals(username)) return false;

      if (id.password == null)
      {
         if (password != null) return false;
      }
      else if (!id.password.equals(password)) return false;

      return true;
   }

   /**
    * Overrides Object.hashCode().
    *
    * <p>Two ConnectionIDs with the same user name and password return
    * the same hash code.</p>
    *
    * @return The hash code.
    */
   public int hashCode()
   {
      String s, uHash, pHash;

      uHash = (username == null) ? NULL : Integer.toString(username.hashCode());
      pHash = (password == null) ? NULL : Integer.toString(password.hashCode());

      s = uHash + SEMICOLON + pHash;

      return s.hashCode();
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   /** User name. May be null. */
   public String username;

   /** Password. May be null. */
   public String password;

   private static String SEMICOLON = ";";
   private static String NULL = "null";
}
