import java.util.*;
import java.io.*;
import java.sql.*;
import java.math.*;
import java.text.*;
import org.xml.sax.*;

import org.xmlmiddleware.xmldbms.helpers.NumberFormatter;

public class NumberFormatTest
{
   public static void main (String[] argv)
   {
      try
      {
//         NumberFormat n = NumberFormat.getInstance();
         DecimalFormat n = new DecimalFormat("000.00");

         NumberFormatter nf = new NumberFormatter(n);
         Object o = nf.parse(argv[0]);

         if (o instanceof Long)
            System.out.println("Long object");
         else if (o instanceof Double)
            System.out.println("Double object");
         else
            System.out.println("Huh?");

         if (o instanceof Long)
            System.out.println("Number: " + nf.format((Long)o));
         else if (o instanceof Double)
            System.out.println("Number: " + nf.format((Double)o));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}


