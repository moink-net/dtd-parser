package de.tudarmstadt.ito.xmldbms.db;

/**
 * Insert the type's description here.
 * Creation date: (08/04/01 20:46:48)
 * @author: Adam Flinton
 */
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public class DbConn1 implements DbConn{
	private String user,password,url,Driver;
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 22:35:42)
 * @return java.sql.Connection
 */
public java.sql.Connection getConn() throws java.sql.SQLException {

	Connection conn = null;

	conn = DriverManager.getConnection(url,user,password);
		
	return conn;
}
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 21:14:17)
 * @param prop java.util.Properties
 */
public void setDB(java.util.Properties props) throws java.lang.ClassNotFoundException {

	 Driver = props.getProperty(DBProps.DRIVER);
	 url = props.getProperty(DBProps.URL);
	 user = props.getProperty(DBProps.USER);
	 password = props.getProperty(DBProps.PASSWORD);
		 
	if (Driver == null) {System.out.println("Driver Not Set");}
	else {
		 // Load the driver.
	 Class.forName(Driver);
	}
		
	}
}