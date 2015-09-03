package medicalActTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Nagesh Chauhan
 * 
 */
public class ReadCsv {
	public void readCsv() {
		String csvFileToRead = "pharmacy_script.csv";
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		int count = 0;
		try {

			br = new BufferedReader(new FileReader(csvFileToRead));
			while ((line = br.readLine()) != null) {

				String[] act = line.split(splitBy);
				String name = act[0];
				if (name.equals("")) {
					continue;
				}
				count=count+1;
				System.out.println(count+" "+name+ " "+act[1]+"  "+act[2]+"  "+act[3]+"  "+act[4]+"  "+act[5]+" "+act[6]+"  "+act[7]);

			

			}

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

		System.out.println("Done with reading CSV");
	}
}