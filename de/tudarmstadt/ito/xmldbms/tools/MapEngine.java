// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
// Version 1.1
// Changes from version 1.01: New in 1.1
package de.tudarmstadt.ito.xmldbms.tools;
import de.tudarmstadt.ito.domutils.ParserUtils;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.mapfactories.MapFactory_DTD;
import de.tudarmstadt.ito.xmldbms.mapfactories.MapFactory_MapDocument;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Hashtable;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import de.tudarmstadt.ito.xmldbms.db.*;
import de.tudarmstadt.ito.xmldbms.tools.GetFileException;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
/**
 * High level map factory interface.
 *
 * <p>MapEngine provides a high-level interface to XML-DBMS map factories.
 * It is primarily designed to be used by tools that want to specify a
 * limited amount of information -- such as a DTD name and a few options --
 * and get a map file in return.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see GenerateMap
 */
public class MapEngine
{
    // ************************************************************************
    // Class variables
    // ************************************************************************
    private String url, user, password;
    private ParserUtils parserUtils;
    private DbConn dbConn;
    // ************************************************************************
    // Constants
    // ************************************************************************
    private static String PARSERUTILS = "ParserUtils";
    // ************************************************************************
    // Constructor
    // ************************************************************************
    /**
    	* Construct a MapEngine object.
    	*/
    public MapEngine()
    {
    }

    // ************************************************************************
    // Public methods
    // ************************************************************************
    /**
    	* Set the properties used to connect to the database.
    	*
    	* <p>This method must be called before generating a map. The following
      * properties are accepted for JDBC 1.0 drivers:</p>
    	*
    	* <ul>
    	* <li>Driver: Name of the JDBC driver class to use. Required.</li>
    	* <li>URL: URL of the database. Required.</li>
    	* <li>User: Database user name. Depends on database.</li>
    	* <li>Password: Database password. Depends on database.</li>
    	* </ul>
    	*
    	* <p>The following properties are accepted for JDBC 2.0 drivers:</p>
    	*
    	* <ul>
    	* <li>DBInitialContext: Name of the JNDI Context in which to create
      *     the JDBC 2.0 DataSource. Required.</li>
    	* <li>DataSource: Logical name of the database. Required.</li>
    	* <li>User: Database user name. Depends on database.</li>
    	* <li>Password: Database password. Depends on database.</li>
    	* </ul>
      *
      * <p>The JDBCLevel property is used to specify whether you are using
      * a JDBC 1.0 or JDBC 2.0 driver. If it is not present, JDBC 1.0 is assumed.</p>
      *
    	* @param props A Properties object containing the above properties.
    	*/
    public void setDatabaseProperties(Properties props)
        throws
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            java.lang.Exception
    {
        int i = 1;
        String JDBC = props.getProperty(DBProps.JDBCLEVEL);
        if (JDBC != null)
            {
            try
                {
                i = Integer.parseInt(JDBC.trim());
            }
            catch (NumberFormatException nfe)
                {
                throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);
            }
        }
        switch (i)
            {
            case 1 :
                dbConn = (DbConn) instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn1");
                break;
            case 2 :
                dbConn = (DbConn) instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn2");
                break;
            default: throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);

        }
        dbConn.setDB(props);
    }

    /**
    	* Set the user name and password.
    	*
    	* <p>This method may be used to set the user name and
    	* password. In some cases, this is easier than loading the user
    	* name and password into properties and calling setDatabaseProperties.
    	*
    	* @param user User name
    	* @param password Password
    	*/
    public void setUserInfo(String user, String password)
    {
        this.user = user;
        this.password = password;
    }

    /**
    	* Set the parser properties.
    	*
    	* <p>This method must be called before creating a map from a DDML file.
    	* The following properties are accepted:</p>
    	*
    	* <ul>
    	* <li>ParserUtilsClass. Name of a class that implements the ParserUtils interface for the parser. Required.</li>
    	* </ul>
    	*
    	* @param props A properties object containing the above properties.
    	*/
    public void setParserProperties(Properties props)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        parserUtils =
            (ParserUtils) instantiateClass(props
                .getProperty(XMLDBMSProps.PARSERUTILSCLASS));
    }
    /**
     * Convenience method that calls setDatabaseProperties and setParserProperties.
     *
     * @param props Initialization properties. See setDatabaseProperties and
     *              setParserProperties for a list of legal values.
     */
    public void init(Properties props)
        throws
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            java.lang.Exception
    {
        setDatabaseProperties(props);
        setParserProperties(props);
    }
    /**
    	* Create a map from a DDML file.
    	*
    	* <p>setParserProperties must be called before calling this method.</p>
    	*
    	* @param schemaFilename Name of the DDML file.
    	* @param catalog Name of the database catalog to use in
    	*                CREATE TABLE statements (not implemented).
    	* @param schema  Name of the database schema to use in
    	*                CREATE TABLE statements (not implemented).
    	* @param sqlSeparator Character to separate CREATE TABLE statements.
    	*                     If this is null, a colon (;) is used.
    	* @param orderColumns Whether to generate order columns.
    	*/
    public void createMap(
        String schemaFilename,
        String catalog,
        String schema,
        String sqlSeparator,
        boolean orderColumns)
        throws Exception, GetFileException
    {
        Map map;
        String basename, ext;
        InputSource src;
        MapFactory_DTD factory = new MapFactory_DTD();
        GetFileURL gfu = new GetFileURL();
        checkState(PARSERUTILS, parserUtils);
        // Create an InputSource over the schema file.
        src = new InputSource(gfu.fullqual(schemaFilename));
        // Get the basename of the file.
        basename = getBasename(schemaFilename);
        // Create and serialize the Map, then create the CREATE TABLE statements.
        ext = getExtension(schemaFilename);
        if (ext.equals("DDM"))
            {
            // DDML Document.
            map =
                factory.createMapFromSchema(
                    src,
                    MapFactory_DTD.SCHEMA_DDML,
                    orderColumns,
                    parserUtils.getSAXParser());
        }
        else
            {
            throw new IllegalArgumentException("Unsupported schema file type: ." + ext);
        }
        serializeMap(map, basename);
        createStatements(map, basename, catalog, schema, sqlSeparator);
    }
    /**
    	* Create a map from a DTD file.
    	*
    	* <p>The DTD can be an internal or external subset.</p>
    	*
    	* @param schemaFilename Name of the DTD file or the XML file
    	*                       containing the DTD.
    	* @param catalog Name of the database catalog to use in
    	*                CREATE TABLE statements (not implemented).
    	* @param schema  Name of the database schema to use in
    	*                CREATE TABLE statements (not implemented).
    	* @param sqlSeparator Character to separate CREATE TABLE statements.
    	*                     If this is null, a colon (;) is used.
    	* @param orderColumns Whether to generate order columns.
    	* @param prefixes An array of namespace prefixes used in the DTD.
    	* @param namespaceURIs An array of namespace URIs corresponding to
    	*                      the prefixes.
    	*/
    public void createMap(
        String schemaFilename,
        String catalog,
        String schema,
        String sqlSeparator,
        boolean orderColumns,
        String[] prefixes,
        String[] namespaceURIs)
        throws Exception, GetFileException
    {
        Map map;
        String basename, ext;
        InputSource src;
        MapFactory_DTD factory = new MapFactory_DTD();
        Hashtable uris = new Hashtable();
        GetFileURL gfu = new GetFileURL();
        // Create a Hashtable of namespace URIs
        if (((prefixes == null) && (namespaceURIs != null))
            || ((prefixes != null) && (namespaceURIs == null)))
            throw new IllegalArgumentException("Can't have list of namespace URIs without list of prefixes or vice versa.");
        if (namespaceURIs != null)
            {
            if (namespaceURIs.length != prefixes.length)
                throw new IllegalArgumentException("Number of prefixes must be same as number of namespace URIs.");
            uris = new Hashtable(prefixes.length);
            for (int i = 0; i < prefixes.length; i++)
                {
                uris.put(prefixes[i], namespaceURIs[i]);
            }
        }
        // Create an InputSource over the schema file.
        src = new InputSource(gfu.fullqual(schemaFilename));
        // Get the basename of the file.
        basename = getBasename(schemaFilename);
        //System.out.println("Basename = "+basename);
        // Create the Map
        ext = getExtension(schemaFilename);
        if (ext.equals("DTD"))
            {
            // External subset (DTD)
            map =
                factory.createMapFromDTD(src, MapFactory_DTD.DTD_EXTERNAL, orderColumns, uris);
        }
        else
            {
            // Assume we have an XML document containing a DTD.
            map =
                factory.createMapFromDTD(
                    src,
                    MapFactory_DTD.DTD_XMLDOCUMENT,
                    orderColumns,
                    uris);
        }
        // Serialize the Map, then create the CREATE TABLE statements.
        //System.out.println("Serialing Map = "+basename);
        serializeMap(map, basename);
        createStatements(map, basename, catalog, schema, sqlSeparator);
    }
    /**
    	* Create a map from a database table or tables. Not implemented.
    	*
    	* @param tables An array of root tables.
    	* @param basename Basename of the map file and .sql file.
    	* @param sqlSeparator Character to separate CREATE TABLE statements.
    	*                     If this is null, a colon (;) is used.
    	*/
    public void createMap(String[] tables, String basename, String sqlSeparator)
        throws Exception
    {
        throw new Exception("Generating map from tables not implemented.");
    }
    /**
    	* Create a map from a result set. Not implemented.
    	*
    	* @param select A SELECT statement to create the result set.
    	* @param basename Basename of the map file and .sql file.
    	* @param sqlSeparator Character to separate CREATE TABLE statements.
    	*                     If this is null, a colon (;) is used.
    	*/
    public void createMap(String select, String basename, String sqlSeparator)
        throws Exception
    {
        throw new Exception("Generating map from select statement not implemented.");
    }
    // ************************************************************************
    // Private methods -- map utilities
    // ************************************************************************
    private void serializeMap(Map map, String basename) throws IOException
    {
        FileOutputStream mapFile;
        // Serialize the map to a mapping document with the same base name.
        mapFile = new FileOutputStream(basename + ".xdb");
        map.serialize(mapFile, true, 3);
        mapFile.close();
    }
    private void createStatements(
        Map map,
        String basename,
        String catalog,
        String schema,
        String sqlSeparator)
        throws SQLException, IOException, InvalidMapException
    {
        // Catalog, schema, and sqlSeparator arguments are currently ignored.
        FileOutputStream sqlFile;
        String[] createStrings;
        Connection conn;
        // Connect to the database and pass the JDBC Connection to the
        // Map object. The Map object needs this to retrieve information
        // about how to construct the CREATE TABLE statements.
        conn = dbConn.getConn();
        map.setConnection(conn);
        // Create the output file.
        sqlFile = new FileOutputStream(basename + ".sql");
        // Get the CREATE TABLE strings from the Map object and write them to
        // the output file.
        byte[] RETURN = System.getProperty("line.separator").getBytes();
        createStrings = map.getCreateTableStrings();
        for (int i = 0; i < createStrings.length; i++)
            {
            sqlFile.write(createStrings[i].getBytes());
            sqlFile.write(sqlSeparator.getBytes());
            sqlFile.write(RETURN);
        }
        // Close the output file.
        sqlFile.close();
    }
    private String getBasename(String filename)
    {
        int period;
        // Get the basename of the file.
        period = filename.lastIndexOf('.', filename.length());
        if (period == -1)
            {
            return filename;
        }
        else
            {
            return filename.substring(0, period);
        }
    }
    private String getExtension(String filename)
    {
        int period;
        // Get the file extension.
        period = filename.lastIndexOf('.', filename.length());
        if (period == -1)
            {
            return "";
        }
        else
            {
            return filename.substring(period + 1, filename.length()).toUpperCase();
        }
    }
    private void checkState(String interfaceName, Object interfaceObject)
    {
        if (interfaceObject == null)
            throw new IllegalStateException(
                "Name of class that implements " + interfaceName + " not set.");
    }
    // ************************************************************************
    // Private methods -- general utilities
    // ************************************************************************
    private Object instantiateClass(String className)
        throws
            java.lang.ClassNotFoundException,
            java.lang.InstantiationException,
            java.lang.IllegalAccessException
    {
        if (className == null)
            return null;
        return Class.forName(className).newInstance();
    }
}