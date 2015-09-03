package medicalActTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SaveCsv {
	public static void  insertCsvTarrif(){
		
		String csvFileToRead = "pharmacy_script.csv";
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		int count = 0;
//		String sql="insert into moh_bill_facility_service_price (name,category,full_price,start_date,location_id,created_date,retired, creator) values " ;
		String sql="insert into moh_bill_beneficiary(beneficiary_id,policy_id_number,created_date,retired,patient_id,insurance_policy_id,creator) values " ;
		System.out.println("==============the biginning of insert query===============");
		System.out.println("");
		System.out.println(sql);
		 String valuesToInsert="";
		
		try {

			br = new BufferedReader(new FileReader(csvFileToRead));
			while ((line = br.readLine()) != null) {
			

				String[] actLine = line.split(splitBy);
				String name = actLine[0];
				if (name.equals("")) {
					continue;
				}
				valuesToInsert =new String();
//				valuesToInsert="('"+actLine[0]+"','"+actLine[1]+"',"+actLine[2]+",'"+actLine[3]+"',"+actLine[4]+",'"+actLine[5]+"',"+actLine[6]+","+actLine[7]+"),";
				
				valuesToInsert="('"+actLine[0]+"','"+actLine[1]+"',"+actLine[2]+",'"+actLine[3]+"',"+actLine[4]+",'"+actLine[5]+"',"+actLine[6]+"),";
					
				//	+" values('"+actLine[0]+"','"+actLine[1]+"','"+actLine[2]+"','"+actLine[3]+"','"+actLine[4]+"','"+actLine[5]+"','"+actLine[6]+"','"+actLine[7]+"') ";
				count=count+1;
				//System.out.println(count);
				
        System.out.println(valuesToInsert);
			

			}
			
			System.out.println("=======================end query======================");
			

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
}
