/*
 * This class is used to run the weka batch filtering script, and the weka classify script
 * On the training.arff and testing.arff
 */

package edu.albany.ils.dsarmd0200.cuetag.weka;

import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Laura G.H. Jiao
 */
public class WekaClassify {

    private String trainingArff_ = "";
    private String testingArff_ = "";
    private String trainingArffBatchFiltered_ = "";
    private String testingArffBatchFiltered_ = "";
    private String resultFile_ = "";
    private String classifierName_ = "";

    private String scriptPath_ = Settings.getValue("wekaScriptsPath");
    private String testFileName_ = Settings.getValue("TestFileName");
    
    /**
     * Constructor
     * @param trainingArff Training file in weka's input file format
     * @param testingArff Testing file in weka's input file format
     * @param trainingArffBatchFiltered Training file after batch filtering
     * @param testingArffBatchFiltered Testing file after batch filtering
     * @param resultFile The output detailed result after weka's classification
     * @param classifierName weka's classifier
     */
    public WekaClassify(String trainingArff,
            String testingArff,
            String trainingArffBatchFiltered,
            String testingArffBatchFiltered,
            String resultFile, 
            String classifierName){
        
        this.trainingArff_ = trainingArff;
        this.testingArff_ = testingArff;
        this.trainingArffBatchFiltered_ = trainingArffBatchFiltered;
        this.testingArffBatchFiltered_ = testingArffBatchFiltered;
        this.resultFile_ = resultFile;
        this.classifierName_ = classifierName;
    }


    /*
     * Run weka's classifier on the training and testing data
     * Save the detailed results and the predictions
     */
    public void classify(){
        
        wekaBatchFiltering(testFileName_, trainingArff_, trainingArffBatchFiltered_, testingArff_, testingArffBatchFiltered_);
        wekaClassifiers(classifierName_, trainingArffBatchFiltered_, testingArffBatchFiltered_, resultFile_);
    }


    /*
     * Run weka_batch_filtering script
     */
    private void wekaBatchFiltering(String testFileName,
            String trainingArff,
            String trainingArffBatchFiltered,
            String testingArff,
            String testingArffBatchFiltered){
        
        try {
            
            Runtime rt = Runtime.getRuntime();
            String command = scriptPath_ + "weka_batch_filtering " + testFileName + " " +
                    trainingArff + " " + trainingArffBatchFiltered + " " +
                    testingArff + " " + testingArffBatchFiltered;
            //System.err.println("Laura debug: batch filter command = \n\t" + command + "\n");
            Process process = rt.exec(command);
            process.waitFor();
            String s = "";
            String errOutput = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((s = br.readLine()) != null)
            {
               s += s + "\n";
            }
            BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while (br2.ready() && (s = br2.readLine()) != null){
              errOutput += s;
            }
            if(!errOutput.equals("") && errOutput != null)
                System.err.println("System err stream = " + errOutput);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(WekaClassify.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WekaClassify.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }

    /*
     * Run weka_classifers script
     */
    private void wekaClassifiers(String ClassifierName,
            String trainingBatchFilteredArff,
            String testingBatchFilteredArff,
            String resultFile){
        try {

            Runtime rt = Runtime.getRuntime();
            String command = scriptPath_ + "weka_classifiers " + ClassifierName + " " +
                    trainingBatchFilteredArff + " " + testingBatchFilteredArff + " " + resultFile;
            //System.err.println("Laura debug: classify command = \n\t" + command + "\n");
            Process process = rt.exec(command);
            process.waitFor();
            String s = "";
            String errOutput = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((s = br.readLine()) != null)
            {
               s += s + "\n";
            }
            BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while (br2.ready() && (s = br2.readLine()) != null){
              errOutput += s;
            }
            if(!errOutput.equals("") && errOutput != null)
                System.err.println("System err stream = " + errOutput);

        } catch (InterruptedException ex) {
            Logger.getLogger(WekaClassify.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WekaClassify.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

