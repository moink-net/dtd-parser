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

package org.xmlmiddleware.db.jdbc;

import java.sql.*;

/**
 * Wrapper around a JDBC-ODBC Bridge DatabaseMetaData.
 *
 * <p>This class calls the underlying DatabaseMetaData object for all methods
 * except getImportedKeys, getExportedKeys and getPrimaryKeys. These methods are
 * implemented directly on top of the Microsoft Access system tables.</p>
 *
 * <p>Note that catalog and schema names are not supported and wild card
 * characters in table names are treated as literals.</p> 
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class MSAccessDBMetaData implements DatabaseMetaData
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private Connection conn;
   private DatabaseMetaData meta;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   MSAccessDBMetaData(Connection conn, DatabaseMetaData meta)
   {
      this.conn = conn;
      this.meta = meta;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   public String getDatabaseProductName() throws SQLException
   {
      return meta.getDatabaseProductName();
   }

   public String getDatabaseProductVersion() throws SQLException
   {
      return meta.getDatabaseProductVersion();
   }

   public String getDriverName() throws SQLException
   {
      return meta.getDriverName();
   }

   public String getDriverVersion() throws SQLException
   {
      return meta.getDriverVersion();
   }

   public int getDriverMajorVersion()
   {
      return meta.getDriverMajorVersion();
   }

   public int getDriverMinorVersion()
   {
      return meta.getDriverMinorVersion();
   }

   public String getURL() throws SQLException
   {
      return meta.getURL();
   }

   public Connection getConnection() throws SQLException
   {
      return conn;
   }

   public String getUserName() throws SQLException
   {
      return meta.getUserName();
   }

   public boolean isReadOnly() throws SQLException
   {
      return meta.isReadOnly();
   }

   public boolean usesLocalFiles() throws SQLException
   {
      return meta.usesLocalFiles();
   }

   public boolean usesLocalFilePerTable() throws SQLException
   {
      return meta.usesLocalFilePerTable();
   }

   public boolean supportsMinimumSQLGrammar() throws SQLException
   {
      return meta.supportsMinimumSQLGrammar();
   }

   public boolean supportsCoreSQLGrammar() throws SQLException
   {
      return meta.supportsCoreSQLGrammar();
   }

   public boolean supportsExtendedSQLGrammar() throws SQLException
   {
      return meta.supportsExtendedSQLGrammar();
   }

   public boolean supportsANSI92EntryLevelSQL() throws SQLException
   {
      return meta.supportsANSI92EntryLevelSQL();
   }

   public boolean supportsANSI92IntermediateSQL() throws SQLException
   {
      return meta.supportsANSI92IntermediateSQL();
   }

   public boolean supportsANSI92FullSQL() throws SQLException
   {
      return meta.supportsANSI92FullSQL();
   }

   public boolean supportsColumnAliasing() throws SQLException
   {
      return meta.supportsColumnAliasing();
   }

   public boolean supportsGroupBy() throws SQLException
   {
      return meta.supportsGroupBy();
   }

   public boolean supportsUnion() throws SQLException
   {
      return meta.supportsUnion();
   }

   public boolean supportsUnionAll() throws SQLException
   {
      return meta.supportsUnionAll();
   }

   public boolean supportsTableCorrelationNames() throws SQLException
   {
      return meta.supportsTableCorrelationNames();
   }

   public boolean supportsDifferentTableCorrelationNames() throws SQLException
   {
      return meta.supportsDifferentTableCorrelationNames();
   }

   public boolean supportsExpressionsInOrderBy() throws SQLException
   {
      return meta.supportsExpressionsInOrderBy();
   }

   public boolean supportsOrderByUnrelated() throws SQLException
   {
      return meta.supportsOrderByUnrelated();
   }

   public boolean supportsGroupByUnrelated() throws SQLException
   {
      return meta.supportsGroupByUnrelated();
   }

   public boolean supportsGroupByBeyondSelect() throws SQLException
   {
      return meta.supportsGroupByBeyondSelect();
   }

   public boolean supportsLikeEscapeClause() throws SQLException
   {
      return meta.supportsLikeEscapeClause();
   }

   public boolean supportsOuterJoins() throws SQLException
   {
      return meta.supportsOuterJoins();
   }

   public boolean supportsFullOuterJoins() throws SQLException
   {
      return meta.supportsFullOuterJoins();
   }

   public boolean supportsLimitedOuterJoins() throws SQLException
   {
      return meta.supportsLimitedOuterJoins();
   }

   public boolean supportsIntegrityEnhancementFacility() throws SQLException
   {
      return meta.supportsIntegrityEnhancementFacility();
   }

   public boolean supportsSubqueriesInComparisons() throws SQLException
   {
      return meta.supportsSubqueriesInComparisons();
   }

   public boolean supportsSubqueriesInExists() throws SQLException
   {
      return meta.supportsSubqueriesInExists();
   }

   public boolean supportsSubqueriesInIns() throws SQLException
   {
      return meta.supportsSubqueriesInIns();
   }

   public boolean supportsSubqueriesInQuantifieds() throws SQLException
   {
      return meta.supportsSubqueriesInQuantifieds();
   }

   public boolean supportsCorrelatedSubqueries() throws SQLException
   {
      return meta.supportsCorrelatedSubqueries();
   }

   public String getCatalogTerm() throws SQLException
   {
      return meta.getCatalogTerm();
   }

   public boolean isCatalogAtStart() throws SQLException
   {
      return meta.isCatalogAtStart();
   }

   public String getCatalogSeparator() throws SQLException
   {
      return meta.getCatalogSeparator();
   }

   public int getMaxCatalogNameLength() throws SQLException
   {
      return meta.getMaxCatalogNameLength();
   }

   public boolean supportsCatalogsInDataManipulation() throws SQLException
   {
      return meta.supportsCatalogsInDataManipulation();
   }

   public boolean supportsCatalogsInProcedureCalls() throws SQLException
   {
      return meta.supportsCatalogsInProcedureCalls();
   }

   public boolean supportsCatalogsInTableDefinitions() throws SQLException
   {
      return meta.supportsCatalogsInTableDefinitions();
   }

   public boolean supportsCatalogsInIndexDefinitions() throws SQLException
   {
      return meta.supportsCatalogsInIndexDefinitions();
   }

   public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
   {
      return meta.supportsCatalogsInPrivilegeDefinitions();
   }

   public ResultSet getCatalogs() throws SQLException
   {
      return meta.getCatalogs();
   }

   public String getSchemaTerm() throws SQLException
   {
      return meta.getSchemaTerm();
   }

   public int getMaxSchemaNameLength() throws SQLException
   {
      return meta.getMaxSchemaNameLength();
   }

   public boolean supportsSchemasInDataManipulation() throws SQLException
   {
      return meta.supportsSchemasInDataManipulation();
   }

   public boolean supportsSchemasInProcedureCalls() throws SQLException
   {
      return meta.supportsSchemasInProcedureCalls();
   }

   public boolean supportsSchemasInTableDefinitions() throws SQLException
   {
      return meta.supportsSchemasInTableDefinitions();
   }

   public boolean supportsSchemasInIndexDefinitions() throws SQLException
   {
      return meta.supportsSchemasInIndexDefinitions();
   }

   public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
   {
      return meta.supportsSchemasInPrivilegeDefinitions();
   }

   public ResultSet getSchemas() throws SQLException
   {
      return meta.getSchemas();
   }

   public ResultSet getTypeInfo() throws SQLException
   {
      return meta.getTypeInfo();
   }

   public boolean allTablesAreSelectable() throws SQLException
   {
      return meta.allTablesAreSelectable();
   }

   public boolean supportsAlterTableWithAddColumn() throws SQLException
   {
      return meta.supportsAlterTableWithAddColumn();
   }

   public boolean supportsAlterTableWithDropColumn() throws SQLException
   {
      return meta.supportsAlterTableWithDropColumn();
   }

   public ResultSet getTableTypes() throws SQLException
   {
      return meta.getTableTypes();
   }

   public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
   {
      return meta.getTables(catalog, schemaPattern, tableNamePattern, types);
   }

   public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
   {
      return meta.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
   }

   public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
   {
      return meta.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
   }

   public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
   {
      return meta.getColumnPrivileges(catalog, schema, table, columnNamePattern);
   }

   /**
    * Implements DatabaseMetaData.getPrimaryKeys
    *
    * <p>This method requires there to be a table named USysPrimaryKeys with three
    * columns -- TABLE_NAME, COLUMN_NAME, and KEY_SEQ -- which contains primary key
    * information for all tables. KEY_SEQ is 1-based.</p>
    *
    * @param catalog Must be null or empty.
    * @param schema Must be null or empty.
    * @param table The table.
    * @return See the JDBC spec.
    */
   public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
   {
      Statement stmt;

      if (catalog != null)
         if (catalog.length() > 0) throw new IllegalArgumentException("Catalog arguments not supported.");
      if (schema != null)
         if (schema.length() > 0) throw new IllegalArgumentException("Schema arguments not supported.");

      stmt = conn.createStatement();
      return stmt.executeQuery("SELECT NULL AS TABLE_CAT, " +
                               "NULL AS TABLE_SCHEM, " +
                               "TABLE_NAME, " +
                               "COLUMN_NAME, " +
                               "KEY_SEQ, " +
                               "NULL AS PK_NAME " +
                               "FROM USysPrimaryKeys " +
                               "WHERE TABLE_NAME = '" + table + "'");
   }

   public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
   {
      return meta.getBestRowIdentifier(catalog, schema, table, scope, nullable);
   }

   public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
   {
      return meta.getIndexInfo(catalog, schema, table, unique, approximate);
   }

   /**
    * Implements DatabaseMetaData.getImportedKeys
    *
    * <p>This method uses the MSysRelationships system table. It returns NULL for
    * catalog and schema names, as well as primary key names. It always returns
    * importedKeyNoAction for UPDATE_RULE, importedKeyNoAction for DELETE_RULE, and
    * importedKeyNotDeferrable for DEFERRABILITY. These columns are therefore useless.</p>
    *
    * @param catalog Must be null or empty.
    * @param schema Must be null or empty.
    * @param table The table.
    * @return See the JDBC spec.
    */
   public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
   {
      Statement stmt;

      if (catalog != null)
         if (catalog.length() > 0) throw new IllegalArgumentException("Catalog arguments not supported.");
      if (schema != null)
         if (schema.length() > 0) throw new IllegalArgumentException("Schema arguments not supported.");

      stmt = conn.createStatement();
      return stmt.executeQuery("SELECT NULL AS PKTABLE_CAT, " +
                               "NULL AS PKTABLE_SCHEM, " +
                               "szReferencedObject AS PKTABLE_NAME, " +
                               "szReferencedColumn AS PKCOLUMN_NAME, " +
                               "NULL AS FKTABLE_CAT, " +
                               "NULL AS FKTABLE_SCHEM, " +
                               "szObject AS FKTABLE_NAME, " +
                               "szColumn AS FKCOLUMN_NAME, " +
                               "icolumn + 1 AS KEY_SEQ, " +
                               DatabaseMetaData.importedKeyNoAction + " AS UPDATE_RULE, " +
                               DatabaseMetaData.importedKeyNoAction + " AS DELETE_RULE, " +
                               "szRelationship AS FK_NAME, " +
                               "NULL AS PK_NAME, " +
                               DatabaseMetaData.importedKeyNotDeferrable + " AS DEFERRABILITY " +
                               "FROM MSysRelationships " +
                               "WHERE szObject = '" + table + "'");
   }

   /**
    * Implements DatabaseMetaData.getExportedKeys
    *
    * <p>This method uses the MSysRelationships system table. It returns NULL for
    * catalog and schema names, as well as primary key names. It always returns
    * importedKeyNoAction for UPDATE_RULE, importedKeyNoAction for DELETE_RULE, and
    * importedKeyNotDeferrable for DEFERRABILITY. These columns are therefore useless.</p>
    *
    * @param catalog Must be null or empty.
    * @param schema Must be null or empty.
    * @param table The table.
    * @return See the JDBC spec.
    */
   public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
   {
      Statement stmt;

      if (catalog != null)
         if (catalog.length() > 0) throw new IllegalArgumentException("Catalog arguments not supported.");
      if (schema != null)
         if (schema.length() > 0) throw new IllegalArgumentException("Schema arguments not supported.");

      stmt = conn.createStatement();
      return stmt.executeQuery("SELECT NULL AS PKTABLE_CAT, " +
                               "NULL AS PKTABLE_SCHEM, " +
                               "szReferencedObject AS PKTABLE_NAME, " +
                               "szReferencedColumn AS PKCOLUMN_NAME, " +
                               "NULL AS FKTABLE_CAT, " +
                               "NULL AS FKTABLE_SCHEM, " +
                               "szObject AS FKTABLE_NAME, " +
                               "szColumn AS FKCOLUMN_NAME, " +
                               "icolumn + 1 AS KEY_SEQ, " +
                               DatabaseMetaData.importedKeyNoAction + " AS UPDATE_RULE, " +
                               DatabaseMetaData.importedKeyNoAction + " AS DELETE_RULE, " +
                               "szRelationship AS FK_NAME, " +
                               "NULL AS PK_NAME, " +
                               DatabaseMetaData.importedKeyNotDeferrable + " AS DEFERRABILITY " +
                               "FROM MSysRelationships " +
                               "WHERE szReferencedObject = '" + table + "'");
   }

   public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
   {
      return meta.getCrossReference(primaryCatalog, primarySchema, primaryTable, foreignCatalog, foreignSchema, foreignTable);
   }

   public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
   {
      return meta.getVersionColumns(catalog, schema, table);
   }

   public String getProcedureTerm() throws SQLException
   {
      return meta.getProcedureTerm();
   }

   public boolean allProceduresAreCallable() throws SQLException
   {
      return meta.allProceduresAreCallable();
   }

   public boolean supportsStoredProcedures() throws SQLException
   {
      return meta.supportsStoredProcedures();
   }

   public int getMaxProcedureNameLength() throws SQLException
   {
      return meta.getMaxProcedureNameLength();
   }

   public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
   {
      return meta.getProcedures(catalog, schemaPattern, procedureNamePattern);
   }

   public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
   {
      return meta.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
   }

   public boolean supportsMultipleResultSets() throws SQLException
   {
      return meta.supportsMultipleResultSets();
   }

   public boolean supportsPositionedDelete() throws SQLException
   {
      return meta.supportsPositionedDelete();
   }

   public boolean supportsPositionedUpdate() throws SQLException
   {
      return meta.supportsPositionedUpdate();
   }

   public boolean supportsSelectForUpdate() throws SQLException
   {
      return meta.supportsSelectForUpdate();
   }

   public boolean supportsTransactions() throws SQLException
   {
      return meta.supportsTransactions();
   }

   public boolean supportsMultipleTransactions() throws SQLException
   {
      return meta.supportsMultipleTransactions();
   }

   public int getDefaultTransactionIsolation() throws SQLException
   {
      return meta.getDefaultTransactionIsolation();
   }

   public boolean supportsTransactionIsolationLevel(int level) throws SQLException
   {
      return meta.supportsTransactionIsolationLevel(level);
   }

   public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
   {
      return meta.supportsDataDefinitionAndDataManipulationTransactions();
   }

   public boolean supportsDataManipulationTransactionsOnly() throws SQLException
   {
      return meta.supportsDataManipulationTransactionsOnly();
   }

   public boolean dataDefinitionCausesTransactionCommit() throws SQLException
   {
      return meta.dataDefinitionCausesTransactionCommit();
   }

   public boolean dataDefinitionIgnoredInTransactions() throws SQLException
   {
      return meta.dataDefinitionIgnoredInTransactions();
   }

   public boolean nullsAreSortedHigh() throws SQLException
   {
      return meta.nullsAreSortedHigh();
   }

   public boolean nullsAreSortedLow() throws SQLException
   {
      return meta.nullsAreSortedLow();
   }

   public boolean nullsAreSortedAtStart() throws SQLException
   {
      return meta.nullsAreSortedAtStart();
   }

   public boolean nullsAreSortedAtEnd() throws SQLException
   {
      return meta.nullsAreSortedAtEnd();
   }

   public boolean supportsMixedCaseIdentifiers() throws SQLException
   {
      return meta.supportsMixedCaseIdentifiers();
   }

   public boolean storesUpperCaseIdentifiers() throws SQLException
   {
      return meta.storesUpperCaseIdentifiers();
   }

   public boolean storesLowerCaseIdentifiers() throws SQLException
   {
      return meta.storesLowerCaseIdentifiers();
   }

   public boolean storesMixedCaseIdentifiers() throws SQLException
   {
      return meta.storesMixedCaseIdentifiers();
   }

   public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
   {
      return meta.supportsMixedCaseQuotedIdentifiers();
   }

   public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
   {
      return meta.storesUpperCaseQuotedIdentifiers();
   }

   public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
   {
      return meta.storesLowerCaseQuotedIdentifiers();
   }

   public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
   {
      return meta.storesMixedCaseQuotedIdentifiers();
   }

   public String getIdentifierQuoteString() throws SQLException
   {
      return meta.getIdentifierQuoteString();
   }

   public String getSQLKeywords() throws SQLException
   {
      return meta.getSQLKeywords();
   }

   public String getNumericFunctions() throws SQLException
   {
      return meta.getNumericFunctions();
   }

   public String getStringFunctions() throws SQLException
   {
      return meta.getStringFunctions();
   }

   public String getSystemFunctions() throws SQLException
   {
      return meta.getSystemFunctions();
   }

   public String getTimeDateFunctions() throws SQLException
   {
      return meta.getTimeDateFunctions();
   }

   public String getSearchStringEscape() throws SQLException
   {
      return meta.getSearchStringEscape();
   }

   public String getExtraNameCharacters() throws SQLException
   {
      return meta.getExtraNameCharacters();
   }


   public boolean nullPlusNonNullIsNull() throws SQLException
   {
      return meta.nullPlusNonNullIsNull();
   }

   public boolean supportsConvert() throws SQLException
   {
      return meta.supportsConvert();
   }

   public boolean supportsConvert(int fromType, int toType) throws SQLException
   {
      return meta.supportsConvert(fromType, toType);
   }

   public boolean supportsNonNullableColumns() throws SQLException
   {
      return meta.supportsNonNullableColumns();
   }

   public boolean supportsOpenCursorsAcrossCommit() throws SQLException
   {
      return meta.supportsOpenCursorsAcrossCommit();
   }

   public boolean supportsOpenCursorsAcrossRollback() throws SQLException
   {
      return meta.supportsOpenCursorsAcrossRollback();
   }

   public boolean supportsOpenStatementsAcrossCommit() throws SQLException
   {
      return meta.supportsOpenStatementsAcrossCommit();
   }

   public boolean supportsOpenStatementsAcrossRollback() throws SQLException
   {
      return meta.supportsOpenStatementsAcrossRollback();
   }

   public int getMaxBinaryLiteralLength() throws SQLException
   {
      return meta.getMaxBinaryLiteralLength();
   }

   public int getMaxCharLiteralLength() throws SQLException
   {
      return meta.getMaxCharLiteralLength();
   }

   public int getMaxColumnNameLength() throws SQLException
   {
      return meta.getMaxColumnNameLength();
   }

   public int getMaxColumnsInGroupBy() throws SQLException
   {
      return meta.getMaxColumnsInGroupBy();
   }

   public int getMaxColumnsInIndex() throws SQLException
   {
      return meta.getMaxColumnsInIndex();
   }

   public int getMaxColumnsInOrderBy() throws SQLException
   {
      return meta.getMaxColumnsInOrderBy();
   }

   public int getMaxColumnsInSelect() throws SQLException
   {
      return meta.getMaxColumnsInSelect();
   }

   public int getMaxColumnsInTable() throws SQLException
   {
      return meta.getMaxColumnsInTable();
   }

   public int getMaxConnections() throws SQLException
   {
      return meta.getMaxConnections();
   }

   public int getMaxCursorNameLength() throws SQLException
   {
      return meta.getMaxCursorNameLength();
   }

   public int getMaxIndexLength() throws SQLException
   {
      return meta.getMaxIndexLength();
   }

   public int getMaxRowSize() throws SQLException
   {
      return meta.getMaxRowSize();
   }

   public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
   {
      return meta.doesMaxRowSizeIncludeBlobs();
   }

   public int getMaxStatementLength() throws SQLException
   {
      return meta.getMaxStatementLength();
   }

   public int getMaxStatements() throws SQLException
   {
      return meta.getMaxStatements();
   }

   public int getMaxTableNameLength() throws SQLException
   {
      return meta.getMaxTableNameLength();
   }

   public int getMaxTablesInSelect() throws SQLException
   {
      return meta.getMaxTablesInSelect();
   }

   public int getMaxUserNameLength() throws SQLException
   {
      return meta.getMaxUserNameLength();
   }
}
