package de.tudarmstadt.ito.xmldbms.db;

/**
 * Insert the type's description here.
 * Creation date: (08/04/01 22:43:38)
 * @author: Adam Flinton
 */
import java.util.Properties;
 
public interface DbConn {
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 22:44:19)
 * @return java.sql.Connection
 */
java.sql.Connection getConn() throws java.sql.SQLException;
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 22:45:02)
 * @param prop java.util.Properties
 */
void setDB(java.util.Properties prop) throws java.lang.Exception;
}
