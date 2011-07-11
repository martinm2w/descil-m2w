package edu.albany.ils.dsarmd0200.lu;

import java.util.*;

/**
 * m2w: This class is for calculating certain group's degree of focusing on tasks.
 *
 * @author Ruobo.m2w
 * @date 2011-2-4
 * @version 1.0
 * 
 */
public class TaskFocus {

//	====================================Attributes=============================================

    private ArrayList utts_ = null;
    private ArrayList<ArrayList<String>> meso_topics_ = new ArrayList<ArrayList<String>>();
    private double MSM;
    private double MGM;
    private double taskFocus;
    

//	====================================cal method=============================================
    
        /**
         * m2w : the method calls from the Assertions class. This method calculates the MGM and MSM and their mean(TaskFocus).
         * @param mt : mesoTopic ArrayList<ArrayList<String>>
         * @param utts_ : the utts_ ArrayList
         */
        public void calTaskFocus(MesoTopic mt, ArrayList utts_){

                this.utts_ = utts_;
                this.meso_topics_ = mt.getMeso_topics_();
                
                this.setMSM(this.calMSM());
                this.setMGM(this.calMGM());
                this.setTaskFocus((this.getMGM() + this.getMSM()) / 2);

        }

//	===================================util methods============================================

        
        /**
         * m2w : calculate the msm of each mesoTopic and return their mean, more to add later
         * @return double type MSM
         * @lastedited 3/14/11 3:05 PM added preprocess for the input list, merge, remove, and top10
         */
	private double calMSM(){

            double msm = 0;

            ArrayList<ArrayList<String>> temp_List = (ArrayList<ArrayList<String>>)meso_topics_.clone();
            if(temp_List.isEmpty()){
                msm = 0.0;
            }else{
                temp_List = MesoTopic.mergeSameTopic(temp_List); // merge same topics

                for (int i = 0; i < temp_List.size(); i++){
//                    System.out.println("merged: " + temp_List.get(i));
                }
                temp_List = MesoTopic.rmTurnsGapGrTen(temp_List); // remove turns with gaps gr8ter than 10

                for (int i = 0; i < temp_List.size(); i++){
//                    System.out.println("removed10: " + temp_List.get(i));
                }
                temp_List = MesoTopic.sortTopicList(temp_List);
                temp_List = MesoTopic.removeShortLists(temp_List);
                temp_List = MesoTopic.getTopTenlist(temp_List);// get the top 10 list

                for (int i = 0; i < temp_List.size(); i++){
//                    System.out.println("top10: " + temp_List.get(i));
                }

                for (int i = 0; i < temp_List.size(); i ++ ){

                    //getting variables (if there is any changes, edit this part)
                    int end = Integer.parseInt(temp_List.get(i).get(temp_List.get(i).size() - 1));//getting the last turn number, sub list's size - 1
                    int start = Integer.parseInt(temp_List.get(i).get(1));//getting the first turn number start at 1, 0 is the topic
                    int mesoLength = end - start;
                    int uttsLength = this.utts_.size();

                    //calculating
                    double tempMsm = ((double)mesoLength / (double)uttsLength);
                    msm = msm + tempMsm;

                    //testing:
//                    System.out.println("-----start calculating MSM------");
//                    System.out.println("----- on topic : "+ temp_List.get(i).get(0) + "------");
//                    System.out.println(" ends: " + end);
//                    System.out.println("starts: " + start);
//                    System.out.println("mesoLength: " + mesoLength);
//                    System.out.println("uttsLength: " + uttsLength);
//                    System.out.println("tempMsm: " + tempMsm);
//                    System.out.println("-----end------");

                }

            
                msm = msm / (double)temp_List.size();
            }
            
            return msm;
	}

        /**
         * m2w: This method calculates the MGM of each topic and returns their mean.
         * @return double type MGM
         */
        private double calMGM(){
            
            double mgm = 0;
            ArrayList<ArrayList<String>> temp_List = (ArrayList<ArrayList<String>>)meso_topics_.clone();
            if(temp_List.isEmpty()){
                mgm = 0.0;
            }else{
                //pre calculation processing
                temp_List = MesoTopic.mergeSameTopic(temp_List); // merge same topics 4/21/11 1:36 PM
                temp_List = MesoTopic.rmTurnsGapGrTen(temp_List); // remove turns with gaps gr8ter than 10 4/21/11 1:36 PM
                temp_List = MesoTopic.sortTopicList(temp_List); //sort decreasing 4/21/11 1:36 PM
                temp_List = MesoTopic.removeShortLists(temp_List);

                for (int i = 0; i < temp_List.size(); i++){
//                            System.out.println("tempList: " + temp_List.get(i));
                        }
                
                // non-gapped length / non-gaped topic length. 4/21/11 1:38 PM
                for (int i = 0; i < temp_List.size() ; i ++ ){
                    //getting variables (if there is any changes, edit this part)
                    int nonGapLength = temp_List.get(i).size() - 1;//0 is the topic.
                    int end = Integer.parseInt(temp_List.get(i).get(temp_List.get(i).size() - 1));//getting the last turn number, sub list's size - 1
                    int start = Integer.parseInt(temp_List.get(i).get(1));//getting the first turn number start at 1, 0 is the topic
                    int mesoLength = end - start;

                    //calculating
                    double tempMgm = ((double)nonGapLength / (double)mesoLength);
                    mgm = mgm + tempMgm;


                    //testing
//                    System.out.println("-----start calculating MGM------");
//                    System.out.println("----- on topic : "+ temp_List.get(i).get(0) + "------");
//                    System.out.println(" ends: " + end);
//                    System.out.println("starts: " + start);
//                    System.out.println("nonGapLength: " + nonGapLength);
//                    System.out.println("mesoLength: " + mesoLength);
//                    System.out.println("tempMgm: " + tempMgm);
//                    System.out.println("-----end------");
                }
                mgm = mgm / (double)temp_List.size();
            }
           
            return mgm;

        }

        /**
         * This method initialize the MGM and the MSM, for iteration preparation of different input documents.
         */
        public void clear(){
            this.setMGM(0.0);
            this.setMSM(0.0);
            this.setTaskFocus(0);
        }
	
//      =================================setters & getters==========================================
        
     /**
     * @return the MSM
     */
    public double getMSM() {
        return MSM;
    }

    /**
     * @return the MGM
     */
    public double getMGM() {
        return MGM;
    }

    /**
     * @param MSM the MSM to set
     */
    public void setMSM(double MSM) {
        this.MSM = MSM;
    }

    /**
     * @param MGM the MGM to set
     */
    public void setMGM(double MGM) {
        this.MGM = MGM;
    }

    /**
     * @return the taskFocus
     */
    public double getTaskFocus() {
        return taskFocus;
    }

    /**
     * @param taskFocus the taskFocus to set
     */
    public void setTaskFocus(double taskFocus) {
        this.taskFocus = taskFocus;
    }

	
}
