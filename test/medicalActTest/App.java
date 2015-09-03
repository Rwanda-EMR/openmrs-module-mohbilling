package medicalActTest;


/**
 * @author Nagesh Chauhan
 * 
 */
public class App {
 public static void main(String[] args) {

 /*// reading data from a csv file
  System.out.println("Reading data from csv :");
  ReadCsv readCsv = new ReadCsv();
  readCsv.readCsv();*/

  // reading data from a csv file and convert to appCSVFile object
/*  System.out.println("Reading data from csv and convert to appCSVFile object:");
  CsvToJavaObject csvToJavaObject = new CsvToJavaObject();
  csvToJavaObject.convertCsvToJava();*/
  
  //reading sql script
  SaveCsv saveCsv =new SaveCsv();
saveCsv.insertCsvTarrif();
 }
}

