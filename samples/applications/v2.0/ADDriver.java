import java.sql.*;
import java.util.*;

public class ADDriver implements Driver
{
   public ADDriver()
   {
   }

   public Connection connect(String url, Properties info) throws SQLException
   {
      return new ADConnection(DriverManager.getConnection(url, info));
   }

   public boolean acceptsURL(String url) throws SQLException
   {
      return false;
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 1;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public boolean jdbcCompliant()
   {
      return true;
   }
}