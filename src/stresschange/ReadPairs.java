/*
-- Reads tab-delimited text file of stress pairs
 */

package stresschange;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;

/**
 * @author James
 */
public class ReadPairs {
       
    private String path;
    
    public ReadPairs(String file_path) {
        path = file_path;
    }
    
    public HashMap<String, double[]> OpenFile() throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader textReader = new BufferedReader(fr);
        HashMap<String, double[]> textData = new HashMap<>();
        
        int numberOfLines = readLines();
        
        for (int i = 0; i < numberOfLines; i++){
            String[] line = textReader.readLine().split("\t");
            double[] probs = {Double.parseDouble(line[1]), Double.parseDouble(line[2])};
            textData.put(line[0], probs);
        }
        
        /* 
        // for testing constraint (Models 2 and 3)
        double[] oneZero = {0.75,0.25};
        textData.put("a-test", oneZero);
        // */
        
        textReader.close();
             
        return textData;
    }
    
    int readLines() throws IOException {
        FileReader file_to_read = new FileReader(path);
        BufferedReader bf = new BufferedReader(file_to_read);
        
        String aLine;
        int numberOfLines = 0;
        
        while ((aLine = bf.readLine()) != null) {
            numberOfLines++;
        }
        bf.close();
        
        return numberOfLines;
    }

}
