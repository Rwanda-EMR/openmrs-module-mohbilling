package medicalActTest;  
  
import java.io.BufferedReader;  
import java.io.FileNotFoundException;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.ArrayList;  
import java.util.List;  
  
  
/** 
 * @author Nagesh Chauhan 
 *  
 */  
public class CsvToJavaObject {  
  
 public void convertCsvToJava() {  
  String csvFileToRead = "pharmacy_script.csv";  
  BufferedReader br = null;  
  String line = "";  
  String splitBy = ",";  
  List<MedicalAct> actsList = new ArrayList<MedicalAct>();  
  
  try {  
  
   br = new BufferedReader(new FileReader(csvFileToRead));  
   while ((line = br.readLine()) != null) {  
  
    // split on comma(',')  
    String[] act = line.split(splitBy);  
  
    // create car object to store values  
    MedicalAct medAct = new MedicalAct();  
  
    // add values from csv to car object  
    medAct.setName(act[0]);
   // medAct.setCategory(act[1]);   
    /*medAct.setFullPrice(act[2]);
    
    medAct.setLocation(act[3]);
    medAct.setStartDate(act[4]);
    medAct.setLocation(act[5]);
    medAct.setCreatedDate(act[6]);
    medAct.setRetired(act[7]);*/
   
    
  
  
    // adding car objects to a list  
    actsList.add(medAct);  
  
   }  
   // print values stored in carList  
   printActsList(actsList);  
  
  } catch (FileNotFoundException e) {  
   e.printStackTrace();  
  } catch (IOException e) {  
   e.printStackTrace();  
  } finally {  
   if (br != null) {  
    try {  
     br.close();  
    } catch (IOException e) {  
     e.printStackTrace();  
    }  
   }  
  }  
 }  
  
 public void printActsList(List<MedicalAct> actsListToPrint) {	 
	 
  for (int i = 0; i < actsListToPrint.size(); i++) {	  
	  
   System.out.println("Act [Name= " + actsListToPrint.get(i).getName().toString())  ;
    /* + " , make=" + actsListToPrint.get(i).getMake()  
     + " , model=" + carListToPrint.get(i).getModel()  
     + " , description="  
     + carListToPrint.get(i).getDescription() + " , price="  
     + carListToPrint.get(i).getPrice() + "]"); */ 
    
  }  
 }  
}  