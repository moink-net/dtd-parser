package samples;
/**No copyright, no warranty; use as you will.
* Written by Adam Flinton and Ronald Bourret, 2001
* Version 1.1
* Changes from version 1.01: New in 1.1
* No copyright, no warranty; use as you will.
* Written by Adam Flinton and Ronald Bourret, 2001
* Version 1.1
* Changes from version 1.01: New in 1.1
*/
import java.util.Properties;
import de.tudarmstadt.ito.xmldbms.tools.*;
/**
 * A simple class to test the loading from Properties file code.
 * Creation date: (18/06/01 15:00:29)
 * @author: Adam Flinton
 */
public class Proptest extends ProcessProperties
{
    /**
     * Proptest constructor comment.
     */
    public Proptest()
    {
        super();
    }
    /**
     * Insert the method's description here.
     * Creation date: (18/06/01 15:00:46)
     * @param args java.lang.String[]
     */
    public static void main(String[] args) throws java.lang.Exception
    {
        Properties p1 = new Properties();
        Proptest pt = new Proptest();
        p1 = pt.getProperties(args, 0);
        System.out.println("Props p1 = " + p1);
    }
}