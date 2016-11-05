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

import java.util.*;

/**
 * Describes a key in a table; <a href="../readme.htm#NotForUse">not for
 * general use</a>.
 *
 * <p>The Key class contains the information describing a key in a table:
 * its name, an ordered list (vector) of columns, its type (primary, unique,
 * or foreign), and, if the key is a primary or unique key, whether to generate it.
 * Key names are arbitrary strings used to identify keys in map documents. They
 * must be unique within a key type in a table. Key names should be unique within
 * a database, but this matters only when the map is used to generate CREATE TABLE
 * statements and is not enforced.</p>
 *
 * <p>Keys are stored in Table and LinkInfo objects.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class Key extends MapBase
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Key or generation type is unknown. */
   public static final int UNKNOWN = 0;

   /** Primary key. */
   public static final int PRIMARY_KEY = 1;

   /** Unique key (not primary key). */
   public static final int UNIQUE_KEY = 2;

   /** Foreign key. */
   public static final int FOREIGN_KEY = 3;

   /** Key stored in document, not generated. */
   public static final int DOCUMENT = 1;

   /** Key generated by a KeyGenerator. */
   public static final int KEYGENERATOR = 2;

   /** Key generated by the database. */
   public static final int DATABASE = 3;

   private static final String PRIMARYKEY = "PrimaryKey";

   // ********************************************************************
   // Private variables
   // ********************************************************************

   private String name = null;
   private int    type = UNKNOWN;
   private int    generate = UNKNOWN;
   private String generatorName = null;
   private Vector columns = null;
   private Table  pointsToTable = null; // Points to primary/unique key table
   private Key    pointsToKey = null;   // Points to primary/unique key

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private Key(String name, int type)
   {
      this.name = name;
      this.type = type;
      if (type == PRIMARY_KEY)
      {
         generate = DOCUMENT;
      }
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new primary key.
    *
    * @param keyName Name of the key. Used in map documents and CREATE TABLE
    *    statements. If this is null, "PrimaryKey" is used.
    *
    * @return The new Key.
    */
   public static Key createPrimaryKey(String keyName)
   {
      if (keyName == null)
      {
         keyName = PRIMARYKEY;
      }
      return new Key(keyName, PRIMARY_KEY);
   }

   /**
    * Create a new unique key.
    *
    * @param keyName Name of the key. Used in map documents and CREATE TABLE
    *    statements. Must be non-null.
    * @return The new Key.
    */
   public static Key createUniqueKey(String keyName)
   {
      checkArgNull(keyName, ARG_KEYNAME);
      if (keyName.equals(PRIMARYKEY))
         throw new IllegalArgumentException("Only the primary key can be named 'PrimaryKey'.");
      return new Key(keyName, UNIQUE_KEY);
   }

   /**
    * Create a new foreign key.
    *
    * @param keyName Name of the key. Used in map documents and CREATE TABLE
    *    statements. Must be non-null.
    * @return The new Key.
    */
   public static Key createForeignKey(String keyName)
   {
      checkArgNull(keyName, ARG_KEYNAME);
      if (keyName.equals(PRIMARYKEY))
         throw new IllegalArgumentException("Only the primary key can be named 'PrimaryKey'.");
      return new Key(keyName, FOREIGN_KEY);
   }

   // ********************************************************************
   // Accessor and mutator methods
   // ********************************************************************

   // ********************************************************************
   // Name
   // ********************************************************************

   /**
    * Get the key's name.
    *
    * @return The key's name.
    */
   public final String getName()
   {
      return name;
   }

   // ********************************************************************
   // Type
   // ********************************************************************

   /**
    * Get the key's type.
    *
    * @return The key's type. Key.PRIMARY_KEY, Key.UNIQUE_KEY, or Key.FOREIGN_KEY.
    */
   public final int getType()
   {
      return type;
   }

   // ********************************************************************
   // Generation
   // ********************************************************************

   /**
    * How the key is generated.
    *
    * <p>Keys have three sources. Key.DOCUMENT means that elements, attributes,
    * or PCDATA from the XML document are mapped to the key column(s). Key.KEYGENERATOR
    * means that XML-DBMS generates the key, using a KeyGenerator supplied by
    * application at run time. Key.DATABASE means that the database generates the key.</p>
    *
    * <p>This method should be called only for primary and unique keys -- that is,
    * when getType() returns Key.PRIMARY_KEY or Key.UNIQUE_KEY. If getType() returns
    * Key.FOREIGN_KEY, the value returned by this method is undefined.</p>
    *
    * @return How the key is generated.
    */
   public final int getKeyGeneration()
   {
      return generate;
   }

   /**
    * Get the logical name of the key generator, if any.
    *
    * <p>This method should be called only for primary and unique keys that XML-DBMS
    * generates -- that is, when getType() returns Key.PRIMARY_KEY or Key.FOREIGN_KEY
    * and getKeyGeneration() returns Key.KEYGENERATOR. If either method returns
    * another value, the value returned by this method is undefined.</p>
    *
    * @return Logical key generator name.
    */
   public final String getKeyGeneratorName()
   {
      return generatorName;
   }

   /**
    * Set the method by which the key is generated.
    *
    * <p>Keys have three sources. Key.DOCUMENT means that elements, attributes,
    * or PCDATA from the XML document are mapped to the key column(s). Key.KEYGENERATOR
    * means that XML-DBMS generates the key, using a KeyGenerator supplied by
    * application at run time. Key.DATABASE means that the database generates the key.</p>
    *
    * <p>This method may be called only for primary and unique keys. Call getType() to
    * determine if the key is a primary or unique key.</p>
    *
    * @param generate How the key is generated.
    * @param generatorName Logical name of the key generator. Must be non-null if
    *    generate is Key.KEYGENERATOR and null otherwise.
    */
   public void setKeyGeneration(int generate, String generatorName)
   {
      if (type == FOREIGN_KEY)
         throw new IllegalStateException("This method cannot be called for foreign keys. Call getType() first to determine the key type.");
      if (((generate == KEYGENERATOR) && (generatorName == null)) ||
          ((generate != KEYGENERATOR) && (generatorName != null)))
         throw new IllegalArgumentException("The generatorName argument must be non-null if generate is Key.KEYGENERATOR and null otherwise.");
      this.generate = generate;
      this.generatorName = generatorName;
   }

   // ********************************************************************
   // Columns
   // ********************************************************************

   /**
    * Get the Vector of columns in the key.
    *
    * @return A Vector of the columns in the key.
    */
   public final Vector getColumns()
   {
      return columns;
   }

   /**
    * Set the Vector of columns in the key.
    *
    * @param columns The Vector of columns in the key.
    */
   public void setColumns(Vector columns)
   {
      if (columns != null)
      {
         if (columns.size() == 0)
            throw new IllegalArgumentException("Columns vector must have non-zero length.");
      }
      this.columns = columns;
   }

   // ********************************************************************
   // Pointers to tables and keys
   // ********************************************************************

   /**
    * Get the Table to which a foreign key points.
    *
    * <p>This method may be called only for foreign keys.</p>
    *
    * @return The Table
    */
   public final Table getRemoteTable()
   {
      if (type != FOREIGN_KEY)
         throw new IllegalStateException("This method may be called only for foreign keys.");
      return pointsToTable;
   }

   /**
    * Get the Key to which a foreign key points.
    *
    * <p>This method may be called only for foreign keys.</p>
    *
    * @return The Key
    */
   public final Key getRemoteKey()
   {
      if (type != FOREIGN_KEY)
         throw new IllegalStateException("This method may be called only for foreign keys.");
      return pointsToKey;
   }

   /**
    * Set the Table and Key to which a foreign key points.
    *
    * <p>This method may be called only for foreign keys.</p>
    *
    * @param remoteTable The Table
    * @param remoteKey The Key. Must be a primary or unique key.
    */
   public final void setRemoteKey(Table remoteTable, Key remoteKey)
   {
      int    remoteKeyType;
      String remoteKeyName;
      Key    actualRemoteKey;

      if (type != FOREIGN_KEY)
         throw new IllegalStateException("This method may be called only for foreign keys.");

      // Check that the remote key is actually in the remote table.

      remoteKeyType = remoteKey.getType();
      if (remoteKeyType == PRIMARY_KEY)
      {
         actualRemoteKey = remoteTable.getPrimaryKey();
      }
      else if (remoteKeyType == UNIQUE_KEY)
      {
         remoteKeyName = remoteKey.getName();
         actualRemoteKey = remoteTable.getUniqueKey(remoteKeyName);
      }
      else // if (remoteKeyType == FOREIGN_KEY)
         throw new IllegalArgumentException("The remote key must be a primary or unique key.");

      if (actualRemoteKey != null)
      {
         if (actualRemoteKey.equals(remoteKey))
         {
            pointsToKey = remoteKey;
            pointsToTable = remoteTable;
            return;
         }
      }
      throw new IllegalArgumentException("The remote key is not a primary or unique key in the remote table.");
   }
}