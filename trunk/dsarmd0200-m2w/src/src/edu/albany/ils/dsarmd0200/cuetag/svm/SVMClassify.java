/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.svm;

import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gjiao
 */
public class SVMClassify {

    private String trainingDatPath = "";
    private String trainingModelPath = "";
    private String testingDatPath = "";
    private String predictionsPath = "";
    private String predictionsResultPath = "";

    private String scriptPath_ = Settings.getValue("svmScriptsPath");
    private String testFileName_ = Settings.getValue("TestFileName");

    /**
     * Constructor
     * @param trainingDatPath SVM input for training set, use in ./svm_learn
     * @param trainingModelPath SVM model for training set, used in ./svm_classify
     * @param testingDatPath SVM input for testing set, used in ./svm_classify
     * @param predictionsPath SVM output for predictions
     * @param predictionsResultPath SVM output of total recall
     */
    public SVMClassify(String trainingDatPath,
            String trainingModelPath,
            String testingDatPath,
            String predictionsPath,
            String predictionsResultPath){

        this.trainingDatPath = trainingDatPath;
        this.trainingModelPath = trainingModelPath;
        this.testingDatPath = testingDatPath;
        this.predictionsPath = predictionsPath;
        this.predictionsResultPath = predictionsResultPath;
    }

    /*
     * Run svm classify
     */
    public void classify(){
        svmRun(trainingDatPath, trainingModelPath, testingDatPath, predictionsPath, predictionsResultPath);
    }

    private void svmRun(String trainingDat,
            String trainingModel,
            String testingDat,
            String predictions,
            String predictionsResult){
        try {
            Runtime rt = Runtime.getRuntime();
            String command = scriptPath_ + "svm_run " + testFileName_ + " " +
                    trainingDat + " " + trainingModel + " " +
                    testingDat + " " +
                    predictions + " " + predictionsResult;
            System.err.println("Laura debug: svm_run command = " + command + "\n");
            Process process = rt.exec(command);
            process.waitFor();
            String s = "";
            String errOutput = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((s = br.readLine()) != null) {
                s += s + "\n";
            }
            BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while (br2.ready() && (s = br2.readLine()) != null) {
                errOutput += s;
            }
            if (!errOutput.equals("") && errOutput != null) {
                System.err.println("System err stream = " + errOutput);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SVMClassify.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SVMClassify.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        
    }

}
