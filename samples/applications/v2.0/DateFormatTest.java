import java.util.*;
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import org.xml.sax.*;

import org.xmlmiddleware.xmldbms.helpers.DateFormatter;

public class DateFormatTest
{
   public static void main (String[] argv)
   {
      try
      {
//         DateFormat d = DateFormat.getDateInstance();
         SimpleDateFormat d = new SimpleDateFormat("MM-yy-dd");

         DateFormatter df = new DateFormatter(d);
         Date date = (Date)df.parse(argv[0]);

         System.out.println("To string " + date.toString());

         System.out.println("Date: " + df.format(date));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}


