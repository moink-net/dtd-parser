package de.tudarmstadt.ito.xmldbms.tools;

public class GetFileException extends Exception
{
   private String detail;
   
   GetFileException(String s)
   {
	 detail = s;
   }// end of constructor               

  public String toString()
  {
	return "Encountered Exception"+detail;

  }// end of toString method          

} // end of class MyException