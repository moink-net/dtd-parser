package de.tudarmstadt.ito.xmldbms.gui;

// 3/23/2001, Ronald Bourret
// Removed variable xd. This isn't necessary because the property names
// are static strings, so we can address them as XMLDBMSProps.XXXX

import java.util.Properties;
import java.beans.*;

import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;

import java.util.StringTokenizer;public class BaseProp extends de.tudarmstadt.ito.xmldbms.tools.ProcessProperties {
	public static java.util.Properties prop = new java.util.Properties();
	
	public static int Change = 0;
	private java.beans.PropertyChangeSupport support = 
		  new PropertyChangeSupport(this);
/**
 * BaseProp constructor comment.
 */
public BaseProp() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (12/02/01 14:49:08)
 * @return int
 */
public int getChange() {
	return Change;
}
/**
 * Insert the method's description here.
 * Creation date: (08/02/01 15:36:28)
 * @return java.util.Properties
 */
public java.util.Properties getProp() {
	return prop;
}
/**
 * Insert the method's description here.
 * Creation date: (12/02/01 15:30:31)
 * @return java.beans.PropertyChangeSupport
 */
public java.beans.PropertyChangeSupport getSupport() {
	return support;
}
/**
 * Insert the method's description here.
 * Creation date: (08/02/01 16:37:37)
 * @return java.lang.String
 */
public String propToString() {
	String S;
	S = prop.toString();
	return S;
}
/**
 * Insert the method's description here.
 * Creation date: (08/02/01 16:29:56)
 * @param Key java.lang.String
 * @param value java.lang.String
 */
public void put(String key, String value) {
	prop.put(key,value);
	return;
	}
	
/**
 * Insert the method's description here.
 * Creation date: (08/02/01 15:36:28)
 * @param newProp java.util.Properties
 */
public void setProp(java.util.Properties newProp) {
	prop = newProp;
}
/**
 * Insert the method's description here.
 * Creation date: (12/02/01 15:30:31)
 * @param newSupport java.beans.PropertyChangeSupport
 */
public void setSupport(java.beans.PropertyChangeSupport newSupport) {
	support = newSupport;
}

		  public void addPropertyChangeListener(
			  PropertyChangeListener l) {
  support.addPropertyChangeListener(l);
}

public void removePropertyChangeListener(
			  PropertyChangeListener l) {
  support.removePropertyChangeListener(l);
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 14:49:08)
 * @param newChange int
 */
public void setChange() {
	int oldchange = Change;
	int newchange;
	if (Change == 1)
		Change = 0;
	else if (Change == 0)
		Change = 1;
	newchange = Change;
	//System.out.println("old  = "+oldchange);
	//System.out.println("new  = "+newchange);
	//System.out.println("Change  = "+Change);

// 3/23/2001, Ronald Bourret
// firePropertyChange(String, int, int) was introduced in version 2. The
// version 2 docs say it is just a wrapper around the the version 1 method,
// which uses Objects instead of ints. Therefore, change the following to
// use Integers instead of int and pray that it works...
//	support.firePropertyChange("Change", 
//	  oldchange, newchange);
	support.firePropertyChange("Change", 
	  new Integer(oldchange), new Integer(newchange));
  
		
	return;
	
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 16:18:23)
 * @return java.lang.String
 */
public String getChangeS() {
	Integer i = new Integer(Change);
	return i.toString();
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getPropFileOut() {
	return XMLDBMSProps.OUTPUTFILE;
}



/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */   
public String getURL() {
	
	return XMLDBMSProps.URL;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getDriver() {
	
	return XMLDBMSProps.DRIVER;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getUser() {
	
	return XMLDBMSProps.USER;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getPassword() {
	
	return XMLDBMSProps.PASSWORD;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getNameQualifierClass() {
	
	return XMLDBMSProps.NAMEQUALIFIERCLASS;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getDocumentFactoryClass() {
	
	return XMLDBMSProps.DOCUMENTFACTORYCLASS;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getParserUtilsClass() {
	
	return XMLDBMSProps.PARSERUTILSCLASS;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getAction() {
	
	return XMLDBMSProps.ACTION;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getMapFile() {
	
	return XMLDBMSProps.MAPFILE;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getXMLFile() {
	
	return XMLDBMSProps.XMLFILE;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getCommitMode() {
	
	return XMLDBMSProps.COMMITMODE;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getKeyGeneratorClass() {
	
	return XMLDBMSProps.KEYGENERATORCLASS;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getSelect() {
	
	return XMLDBMSProps.SELECT;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getTable() {
	
	return XMLDBMSProps.TABLE;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getKey() {
	
	return XMLDBMSProps.KEY;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getSchemaFile() {
	
	return XMLDBMSProps.SCHEMAFILE;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getOrderColumns() {
	
	return XMLDBMSProps.ORDERCOLUMNS;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getCatalog() {
	
	return XMLDBMSProps.CATALOG;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getSchema() {
	
	return XMLDBMSProps.SCHEMA;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getSQLSeparator() {
	
	return XMLDBMSProps.SQLSEPARATOR;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getPrefix() {
	
	return XMLDBMSProps.PREFIX;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getNameSpaceURI() {
	
	return XMLDBMSProps.NAMESPACEURI;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getBasename() {
	
	return XMLDBMSProps.BASENAME;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getFile() {
	return XMLDBMSProps.FILE;
}


/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getYes() {
	
	return XMLDBMSProps.YES;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
 
public String getNo() {
	
	return XMLDBMSProps.NO;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getAfterInsert() {
	
	return XMLDBMSProps.AFTERINSERT;
}

/**
 * Insert the method's description here.
 * Creation date: (12/02/01 17:03:16)
 * @return java.lang.String
 */
public String getAfterDocument() {
	
	return XMLDBMSProps.AFTERDOCUMENT;
}


//	private de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps xd = new de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps();
/**
 * Insert the method's description here.
 * Creation date: (13/02/01 15:42:03)
 * @return java.lang.String
 * @param key java.lang.String
 */
public String getPropVal(String key) {

	String S;
	S = prop.getProperty(key);
	return S;
}/**
 * Insert the method's description here.
 * Creation date: (08/11/00 19:21:35)
 */ 
public void addKeys(String str) {
try{
	String z1 = "";
	String x1 = "";
	StringTokenizer st1 = new StringTokenizer(str,"=");
	System.out.println("Number of tokens ="+ st1.countTokens());
	while (st1.hasMoreTokens()) {
	//	System.out.println("tokens = "+ st1.nextToken());
						z1 = st1.nextToken();
						x1 = st1.nextToken();
	//	System.out.println("Key="+z1);
	//	System.out.println("Value="+x1);
				prop.put(z1,x1);
	//		System.out.println("Value for " +z1  + "="+ prop.getProperty(z1));
	
	} 
					} catch (Throwable e) {
		System.out.println(e);
		}
		}/**
 * Insert the method's description here.
 * Creation date: (08/11/00 19:21:35)
 */ 
public void addMultipleVal(String Key,String Val)   {
try{
	String z1 = "";
	String x1 = "";
	int i = 1;
	StringTokenizer st1 = new StringTokenizer(Val,",");
//	System.out.println("Number of tokens ="+ st1.countTokens());
	while (st1.hasMoreTokens()) {
		z1 = Key+i;
						x1 = st1.nextToken();
//		System.out.println("Key="+z1);
//		System.out.println("Value="+x1);
				prop.put(z1,x1.trim());
//			System.out.println("Value for " +z1  + "="+ prop.getProperty(z1));
	i++;
	} 
					} catch (Throwable e) {
		System.out.println(e);
		}
		}/**
 * Insert the method's description here.
 * Creation date: (08/11/00 19:21:35)
 * @param str java.lang.String
 * @param sep java.lang.String
 * @param b1 boolean
 */ 
public void addMultipleVal(String Key,String Val, String Sep)   {
try{
	String z1 = "";
	String x1 = "";
	int i = 1;
	StringTokenizer st1 = new StringTokenizer(Val,Sep);
//	System.out.println("Number of tokens ="+ st1.countTokens());
	while (st1.hasMoreTokens()) {
		z1 = Key+i;
						x1 = st1.nextToken();
//		System.out.println("Key="+z1);
//		System.out.println("Value="+x1);
				prop.put(z1,x1.trim());
//			System.out.println("Value for " +z1  + "="+ prop.getProperty(z1));
	i++;
	} 
					} catch (Throwable e) {
		System.out.println(e);
		}
		}/**
 * This method was created in VisualAge.
 */ 
public String bool2String(boolean b) {
	if (b) { return getYes();}
	else { return getNo();}

}/**
 * Insert the method's description here.
 * Creation date: (18/02/01 19:49:54)
 */ 
public void emptyProp() {

	prop.remove(getPropFileOut());
	prop.remove(getFile());
	prop.remove(getAction());
	prop.remove(getURL());
	prop.remove(getDriver());
	prop.remove(getUser());
	prop.remove(getPassword());
	prop.remove(getNameQualifierClass());
	prop.remove(getDocumentFactoryClass());
	prop.remove(getParserUtilsClass());
	prop.remove(getMapFile());
	prop.remove(getXMLFile());
	prop.remove(getCommitMode());
	prop.remove(getKeyGeneratorClass());
	prop.remove(getSelect());
	prop.remove(getTable());
	prop.remove(getKey());
	prop.remove(getSchemaFile());
	prop.remove(getOrderColumns());
	prop.remove(getCatalog());
	prop.remove(getSchema());
	prop.remove(getSQLSeparator());
	prop.remove(getPrefix());
	prop.remove(getNameSpaceURI());
	prop.remove(getBasename());
	}/**
 * Insert the method's description here.
 * Creation date: (18/02/01 20:33:56)
 * @return int
 * @param field java.lang.String
 * @param sep java.lang.String
 */ 
public int getNumTokens(String field, String sep) {
	StringTokenizer sta = new StringTokenizer(field,sep);
	int i = sta.countTokens();
	//System.out.println("Number of tokens ="+i);
	return i;
}}