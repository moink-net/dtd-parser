package de.tudarmstadt.ito.xmldbms.tools;

import java.util.Properties;
/**
 * Insert the type's description here.
 * Creation date: (18/06/01 15:00:29)
 * @author: Adam Flinton
 */
public class Proptest extends ProcessProperties {
/**
 * Proptest constructor comment.
 */
public Proptest() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (18/06/01 15:00:46)
 * @param args java.lang.String[]
 */
public static void main(String[] args) throws java.lang.Exception {
	
	Properties p1 = new Properties();
	Proptest pt = new Proptest();

	p1 = pt.getProperties(args, 0);

	System.out.println("Props p1 = " +p1);

	
	}
}
