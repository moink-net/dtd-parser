package de.tudarmstadt.ito.xmldbms.mapfactories;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBNameChecker
{
   // Class variables
   private Connection       conn;
   private boolean          initialized = false;
   private DatabaseMetaData meta;
   private String           extraChars;
   private int              maxColumnNameLen,
							maxColumnsInTable,
							maxTableNameLen;
   private boolean          mixedCase, lowerCase, upperCase;


   // Constructor

   public DBNameChecker(Connection conn)
   {
	  this.conn = conn;
   }   

   // Public methods

   public String getTableName(String catalogName, String schemaName,
String inputTableName, String[] newTableNames)
	  throws SQLException
   {
	  // Constructs a name from the tableName argument that is legal
	  // and doesn't conflict with any names in the specified catalog
	  // or schema or with any of the names listed in newTableNames. The
	  // latter names are those currently being constructed by the map
	  // factory.

	  String tableName = inputTableName;

	  if (!initialized) initialize();

	  tableName = fixCharacters(tableName);
	  tableName = fixLength(tableName, maxTableNameLen);
	  tableName = fixCase(tableName);
	  tableName = fixTableNameCollisions(catalogName, schemaName, tableName, newTableNames);
	  return tableName;
   }   

   public String getColumnName(String inputColumnName, String[]
newColumnNames)
	  throws SQLException
   {
	  // Constructs a name from the columnName argument that is legal and
	  // doesn't conflict with any of the names listed in newColumnNames.
	  // newColumnNames lists the names of columns that already exist in
	  // the target table.

	  String columnName = inputColumnName;

	  if (!initialized) initialize();

	  columnName = fixCharacters(columnName);
	  columnName = fixLength(columnName, maxColumnNameLen);
	  columnName = fixCase(columnName);
	  columnName = fixColumnNameCollisions(columnName, newColumnNames);
	  return columnName;
   }   

   // Private methods

   private void initialize() throws SQLException
   {
	  meta = conn.getMetaData();
	  extraChars = meta.getExtraNameCharacters();
	  maxColumnNameLen = meta.getMaxColumnNameLength();
	  maxColumnsInTable = meta.getMaxColumnsInTable();
	  maxTableNameLen = meta.getMaxTableNameLength();
	  mixedCase = meta.supportsMixedCaseQuotedIdentifiers() ||
				  meta.storesMixedCaseQuotedIdentifiers();
	  lowerCase = meta.storesLowerCaseQuotedIdentifiers();
	  upperCase = meta.storesUpperCaseQuotedIdentifiers();
	  initialized = true;
   }   

   private String fixCharacters(String name)
   {
	  char[] newName, oldName;
	  int    position = 0;
	  char   character;

	  newName = new char[name.length()];
	  oldName = name.toCharArray();

	  for (int i = 0; i < oldName.length; i++)
	  {
		 // If a character in the name is in the range 0-9, a-z, or A-Z,
		 // if the character is '_', or if the character is a legal "extra"
		 // character, transfer it to the new name. Otherwise, remove the
		 // character from the name.

		 character = oldName[i];

		 if (((character >= '0') && (character <= '9')) ||
			 ((character >= 'a') && (character <= 'z')) ||
			 ((character >= 'A') && (character >= 'Z')) ||
			 (character == '_'))
		 {
			newName[position++] = character;
		 }
		 else if (extraChars.indexOf((int)character) != -1)
		 {
			newName[position++] = character;
		 }
	  }
	  return new String(newName, 0, position);
   }   

   private String fixLength(String name, int maxLength)
   {
	  // Truncate the string to maxLength characters.

	  if (name.length() <= maxLength)
	  {
		 return name;
	  }
	  else
	  {
		 return name.substring(0, maxLength);
	  }
   }   

   private String fixCase(String name)
   {
	  // Change the case to the case used by the database

	  if (mixedCase)
	  {
		 return name;
	  }
	  else if (lowerCase)
	  {
		 return name.toLowerCase();
	  }
	  else // if (upperCase)
	  {
		 return name.toUpperCase();
	  }
   }   

   private String fixTableNameCollisions(String catalogName, String schemaName, String inputTableName, String[] newTableNames)
	  throws SQLException
   {
	  String tableName, listTableName, dbTableName;
	  int    suffixNum = 1;

	  // Initialize the variables:
	  //  tableName: name we are currently checking
	  //  listTableName: name that has been checked against the list
	  //  dbTableName: name that has been checked against the database

	  tableName = inputTableName;
	  listTableName = null;
	  dbTableName = null;

	  while (!tableName.equals(listTableName))
	  {
		 // Get a table name that doesn't collide with any names
		 // in the list of new table names. Save this so the loop
		 // can check if this gets changed while checking the table
		 // name against the database.

		 while (nameInList(tableName, newTableNames))
		 {
			tableName = getSuffixedName(inputTableName, suffixNum, maxTableNameLen);
			suffixNum++;
		 }
		 listTableName = tableName;

		 // Get a table name that doesn't collide with any names in
		 // the database. Note that we don't do this check if the
		 // name has already been checked against the database. This
		 // happens if we change the name on the first time through
		 // the while loop and the name is not in the list.

		 if (!tableName.equals(dbTableName))
		 {
			while (tableNameInDB(catalogName, schemaName, tableName))
			{
			   tableName = getSuffixedName(inputTableName, suffixNum, maxTableNameLen);
			   suffixNum++;
			}
			dbTableName = tableName;
		 }
	  }

	  return tableName;
   }   

   private String fixColumnNameCollisions(String inputColumnName, String[] newColumnNames)
	  throws SQLException
   {
	  // Check if the column name is in the list of new column names.
	  // If it is, append a number and check again.

	  String columnName = inputColumnName;
	  int    suffixNum = 1;

	  while (nameInList(columnName, newColumnNames))
	  {
		 columnName = getSuffixedName(inputColumnName, suffixNum, maxColumnNameLen);
		 suffixNum++;
	  }
	  return columnName;
   }   

   private boolean nameInList(String name, String[] nameList)
   {
	  // Check if a name is in a list of names

	  for (int i = 0; i < nameList.length; i++)
	  {
		 if (nameList[i].equals(name)) return true;
	  }
	  return false;
   }   

   private boolean tableNameInDB(String catalogName, String schemaName, String tableName)
	  throws SQLException
   {
	  // Check if a table name is in the database

	  ResultSet rs;
	  boolean   tableFound;

	  rs = meta.getTables(catalogName, schemaName, tableName, null);

	  // If the result set contains any rows, then the table is
	  // already in the database.

	  tableFound = rs.next();
	  rs.close();
	  return tableFound;
   }   

   private String getSuffixedName(String inputName, int suffixNum, int maxLength)
	  throws SQLException
   {
	  String baseName, suffix;
	  int    newLength;

	  // Get a new suffix

	  baseName = inputName;
	  suffix = String.valueOf(suffixNum);

	  // If the suffix is too long, truncate the base name. If the
	  // new base name is zero length or less, throw an exception.

	  if ((baseName.length() + suffix.length()) > maxLength)
	  {
		 newLength = maxLength - (baseName.length() + suffix.length() - maxLength);
		 if (newLength <= 0)
			throw new SQLException("Cannot construct a unique name from " + baseName);
		 baseName = baseName.substring(0, newLength);
	  }
	  return baseName + suffix;
   }   
}