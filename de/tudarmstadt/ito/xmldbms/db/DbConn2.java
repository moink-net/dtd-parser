package de.tudarmstadt.ito.xmldbms.db;

/**
 * Insert the type's description here.
 * Creation date: (08/04/01 20:46:48)
 * @author: Adam Flinton
 */

import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import DbConn;

import java.util.Hashtable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.*;
import javax.naming.*;
 
public class DbConn2 implements DbConn {
	private String user,password,InitContext,Data_Source;
	private DataSource ds;
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 22:35:42)
 * @return java.sql.Connection
 */
public java.sql.Connection getConn()  throws java.sql.SQLException {

	Connection conn = ds.getConnection(user,password);
	return conn;
}
/**
 * Insert the method's description here.
 * Creation date: (08/04/01 21:14:17)
 * @param prop java.util.Properties
 */
public void setDB(java.util.Properties props) throws javax.naming.NamingException {

	 InitContext = (String)props.getProperty(XMLDBMSProps.DBINITIALCONTEXT);
	 Data_Source = (String)props.getProperty(XMLDBMSProps.DATASOURCE);
	 user = (String)props.getProperty(XMLDBMSProps.USER);
	 password = (String)props.getProperty(XMLDBMSProps.PASSWORD);
		 
	if (InitContext == null) {System.out.println("Initial Context Not Set");}

	Hashtable env = new Hashtable();
	env.put(Context.INITIAL_CONTEXT_FACTORY, InitContext.trim());

	Context ctx = new InitialContext(env);
	ds = (DataSource)ctx.lookup(Data_Source);

	
	
	}
}
