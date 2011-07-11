/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.util;

import edu.albany.ils.dsarmd0200.lu.LocalTopic;
import edu.albany.ils.dsarmd0200.lu.LocalTopics;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * M2W: this class is for evaluations. 
 * @Author ruobo
 * @Date May 2, 2011
 */
public class Evaluation {

//	====================================Attributes=============================================
    private static ArrayList<String> evalLtsList = new ArrayList<String>();
//	====================================main method=============================================

//	===================================util methods============================================
    /**
     * m2w: this method is for evaluation over local topics. prints out 3 precisions now.
     * @param autolts automated local topics
     * @param annolts annotated local topics
     * @param currentFileName
     * @last 5/3/11 2:12 PM
     */
    public static void evalLocalTopics(LocalTopics autolts, LocalTopics annolts, String currentFileName, String EvalFileDir, boolean isLast){

            //m2w: 1st step, topic hit eval. 5/3/11 12:11 PM
            int topicHit = 0;
            double topicRecall = 0.0;
            double topicPrec = 0.0;
            double mentionRecallTotal = 0.0;
            double mentionPrecTotal = 0.0;
            double averageMentionRecall = 0.0;
            double averageMentionPrec = 0.0;
            double totalMentionRecall = 0.0;
            double totalMentionPrec = 0.0;
            int totalMentionsHit = 0;
            int totalAnnoTpcLength = 0;
            int totalAutoTpcLength = 0;
            ArrayList<String> annoTopicList = new ArrayList<String>();//4
            ArrayList<String> topicHitList = new ArrayList<String>();//4
            ArrayList<String> topicMissList = new ArrayList<String>();//4
            ArrayList<String> autoTopicIsMention = new ArrayList<String>();//5.5/8/11 10:27 AM
            //
    
            for(int i = 0; i < annolts.size(); i++){
                LocalTopic annoTopic = (LocalTopic)annolts.get(i);
                String annoTpcName = annoTopic.getContent().getWord();
                ArrayList<NounToken> annoMentions = annoTopic.getMentions(); 
                totalAnnoTpcLength = totalAnnoTpcLength + annoMentions.size(); // for 3rd step
                // add total topiclist here
                String annoTurnNo = String.valueOf(annoTopic.getContent().getTurnNo());
                annoTopicList.add(annoTpcName + ":" + annoTurnNo);//4
                                
                for(int j = 0; j < autolts.size(); j++){
                    LocalTopic autoTopic = (LocalTopic)autolts.get(j);
                    String autoTpcName = autoTopic.getContent().getWord();
                    String autoTpcTurnNo = String.valueOf(autoTopic.getContent().getTurnNo());//5
                    //m2w: if topic name is the same.
                    if(annoTpcName.equals(autoTpcName)){
                        topicHit++;
                        //topic hit list here, substract and get the misslist after this.
                        topicHitList.add(annoTpcName + ":" + annoTurnNo);//4

                        //m2w: 2nd step, submention hit eval. 5/3/11 12:12 PM
                        ArrayList<NounToken> autoMentions = autoTopic.getMentions();
                        int mentionsHit = 0; // initialized each loop.
                        double tempMentionRecall = 0.0;
                        double tempMentionPrec = 0.0;


                        for(int x = 0; x < annoMentions.size(); x++){
                            for(int y = 0; y < autoMentions.size(); y++){
                                int annoTurn = annoMentions.get(x).getTurnNo();
                                int autoTurn = autoMentions.get(y).getTurnNo();
                                if(annoTurn == autoTurn){
                                    mentionsHit++;
                                    totalMentionsHit++; // for 3.
                                }//ends if turn number is the same
                            }//ends automentions loop
                        }//ends annomentions loop

                        //m2w: calculating mention precision. 5/3/11 12:41 PM
                        if(mentionsHit == 0 || annoMentions.isEmpty()){
                                        tempMentionRecall = 0.0;
                                    }
                        tempMentionRecall = (double)mentionsHit / (double)annoMentions.size();
                        tempMentionPrec = (double)mentionsHit / (double)autoMentions.size();
                        mentionRecallTotal = mentionRecallTotal + tempMentionRecall;//add to total
                        mentionPrecTotal = mentionPrecTotal + tempMentionPrec;

                    } else if(!annoTpcName.equals(autoTpcName)){//if topic name didn't match. 5/6/11 11:44 PM
                        //m2w: for step 5 . 5/8/11 10:16 AM. do auto topic is submention check.
                        //m2w: 5.1 if topic equals submen,
                        for(int k = 0; k < annoMentions.size(); k++){
                            String annoSubMentionWord = annoMentions.get(k).getWord();                        
                            if(autoTpcName.equals(annoSubMentionWord)){//if auto tpc name is anno sub mention
                               //list add topic is submen 
                               String statement = "auto topic: " + autoTpcName + ":" + autoTpcTurnNo + ", in anno topic: " + annoTpcName + ":" + annoTurnNo;
                               if(!autoTopicIsMention.contains(statement)){
                                   autoTopicIsMention.add(statement);
//                                   System.out.println(statement);
                               }
                               
                            }//if auto tpc name is anno sub mention
                        }
                    }//ends if topic name is the same
                }//ends autolts loop
            }//ends annolts loop

            //m2w: 1. calculating topic precision and recall. 5/3/11 12:41 PM
            if(topicHit == 0 || annolts.isEmpty() || autolts.isEmpty()){
                topicRecall = 0.0;
                topicPrec = 0.0;
            }
//            System.out.println("topicHIt: " + topicHit);
            topicRecall = (double)topicHit / (double)annolts.size();
            topicPrec = (double)topicHit / (double)autolts.size();
//            System.out.println("1. topicHit: " + topicHit);
//            System.out.println("1. annosize: " + annolts.size());
//            System.out.println("1. autosize: " + autolts.size());
//            System.out.println("1. topic recall: " + topicRecall);
//            System.out.println("1. topic prec: " + topicPrec);
            
            

            //m2w: 2. calculating average precision of mentions. 5/3/11 12:45 PM
            if(mentionRecallTotal == 0.0 || topicHit == 0 || mentionPrecTotal == 0.0){
                averageMentionRecall = 0.0;
            }
            averageMentionRecall = mentionRecallTotal / (double)topicHit;
            averageMentionPrec = mentionPrecTotal / (double)topicHit;
//            System.out.println("2. mention recall total: " + mentionRecallTotal);
//            System.out.println("2. mention prec total: " + mentionPrecTotal);
//            System.out.println("2. topic hit: " + topicHit);
//            System.out.println("2. ave mention recall: " + averageMentionRecall);
//            System.out.println("2. ave mention prec: " + averageMentionPrec);
            

            //m2w: 3. calcualting total precision of mentions. 5/3/11 1:58 PM
            if(totalMentionsHit == 0 || totalAnnoTpcLength == 0){
                totalMentionRecall = 0.0;
            }
            
            for(int i = 0; i < autolts.size(); i++){
                LocalTopic tempAutoTpc = (LocalTopic)autolts.get(i);
                totalAutoTpcLength = totalAutoTpcLength + tempAutoTpc.getMentions().size();
            }
            totalMentionRecall = (double)totalMentionsHit / (double)totalAnnoTpcLength;
            totalMentionPrec = (double)totalMentionsHit / (double)totalAutoTpcLength;
            
//                System.out.println("3. total mentions hit: " + totalMentionsHit);
//                System.out.println("3. total anno tpc length: " + totalAnnoTpcLength);
//                System.out.println("3. total auto tpc length: " + totalAutoTpcLength);
//                System.out.println("3. total mention recall: " + totalMentionRecall);
//                System.out.println("3. total mention prec: " + totalMentionPrec);
            
            //m2w: 4. miss topic list. 5/7/11 12:28 PM
            topicMissList.addAll(annoTopicList);
//            System.out.println(topicMissList);
//            System.out.println(topicHitList);
            if(!topicMissList.isEmpty()){
                for(int i = 0; i < topicMissList.size(); i++){
                    for(int j = 0; j < topicHitList.size(); j++){
                        String tempMiss = topicMissList.get(i);
                        String tempHit = topicHitList.get(j);
                        if(tempMiss.equals(tempHit)){
                            topicMissList.remove(i);
                            i--;
                            break;
                        }

                    }
                }
            }
            
            //m2w: 5. miss calculated auto topics, which should be calculated as sub - mentions. 5/7/11 3:26 PM
//            
//            
//            for(int i = 0; i < autolts.size(); i++){
//                LocalTopic autoTopic = (LocalTopic)autolts.get(i);
//                String autoTpcTurnNo = String.valueOf(autoTopic.getContent().getTurnNo());
//                String autoTpcName = autoTopic.getContent().getWord();
//                ArrayList<NounToken> autoMentions = autoTopic.getMentions();
//                for(int j = 0; j < annolts.size(); j++){
//                    LocalTopic annoTopic = (LocalTopic)annolts.get(j);
//                    String annoTpcName = annoTopic.getContent().getWord();
//                    ArrayList<NounToken> annoMentions = annoTopic.getMentions();
//                    //if auto topic missed, check if this auto topic is a sub mention in any anno topic
//                    if(!autoTpcName.equals(annoTpcName)){
//                        for(int k = 0; k < annoMentions.size(); k ++){
//                            String annoSubMention = annoMentions.get(k).getWord();
//                            String annoTurnNo = String.valueOf(annoMentions.get(k).getTurnNo());
//                            //if auto topic name equals anno sub mention, compare its sub mentions see if there are more matches.
//                            if(autoTpcName.equals(annoSubMention)){
//                                //see if there are any auto sub mention in this auto topic is in the anno sub mentions too.
//                                ArrayList<String> autoMentionIsMention = new ArrayList<String>(); // for sub mentions
//                                for(int l = 0; l < autoMentions.size(); l++){
//                                    String autoSubMention = autoMentions.get(l).getWord();
//                                    String autoSubMentionTurnNo = String.valueOf(autoMentions.get(l).getTurnNo());
//                                    for(int m = k; m < annoMentions.size(); m++){//starts from k
//                                        String tempAnnoMention = annoMentions.get(m).getWord();
//                                        if(autoSubMention.equals(tempAnnoMention)){
//                                            autoMentionIsMention.add(autoSubMention + ":" + autoSubMentionTurnNo);
//                                        }//ends if mention matches 
//                                    }//ends subsequent anno mention loop
//                                }//ends auto mention loop
//                                autoTopicIsMention.add("auto topic: " + autoTpcName + ":" + autoTpcTurnNo + " is sub-mention in anno topic: " + annoTpcName + ":" + annoTurnNo);
//                                System.out.println("auto topic: " + autoTpcName + ":" + autoTpcTurnNo + " is sub-mention in anno topic: " + annoTpcName + ":" + annoTurnNo);
////                                autoTopicIsMention.add("auto topic: " + autoTpcName + ":" + autoTpcTurnNo + " is sub-mention in anno topic: " + annoTpcName + ":" + annoTurnNo + " and sub-mentions matches: " + autoMentionIsMention.toString()); // adds anno topic name
//                            }//ends if an auto topic is a mention in anno topic 
//                        }//ends anno mentions loop
//                    }//ends if auto topic name do not match anno topic name                    
//                }//ends annolts loop
//            }//ends autolts loop. 5/8/11 9:34 AM
            
//            System.out.println(topicMissList);
                
            
//                System.out.println("--------------------------------------------------------");
//                System.out.println("Evaluating Local Topic on file...   " + currentFileName);
//                System.out.println("Topic Recall: " + topicRecall);
//                System.out.println("Average Mention Recall: " + averageMentionRecall);
//                System.out.println("Total Mention Recall: " + totalMentionRecall);
//                System.out.println("Topic Precision: " + topicPrec);
//                System.out.println("Average Mention Precision: " + averageMentionPrec);
//                System.out.println("Total Mention Precision: " + totalMentionPrec);
//                System.out.println("--------------------statistics--------------------------");
//                System.out.println("Missed Topic List size: " + topicMissList.size());
//                System.out.println("Missed Topic List: " + topicMissList);
                
                
                

                evalLtsList.add("=======================================================");
                evalLtsList.add("----------------------results--------------------------");
                evalLtsList.add("Evaluating Local Topic on file...   " + currentFileName);
                evalLtsList.add("1.Topic Hits Recall: " + topicRecall);
                evalLtsList.add("1.Topic Hits Precision: " + topicPrec);
                evalLtsList.add("2.Sub-Mention Hits Recall (average): " + averageMentionRecall);
                evalLtsList.add("2.Sub-Mention Hits Precision (average): " + averageMentionPrec);
                evalLtsList.add("3.Sub-Mention Hits Recall (Total): " + totalMentionRecall);
                evalLtsList.add("3.Sub-Mention Hits Precision (total): " + totalMentionPrec);
                evalLtsList.add("----------------------details---------------------------");
                evalLtsList.add("1. topicHit of this file: " + topicHit);
                evalLtsList.add("1. anno local topic list size: " + annolts.size());
                evalLtsList.add("1. auto local topic list size: " + autolts.size());
                evalLtsList.add("2. mention recall total sum: " + mentionRecallTotal);
                evalLtsList.add("2. mention prec total sum: " + mentionPrecTotal);
                evalLtsList.add("2. topic hits in this file: " + topicHit);
                evalLtsList.add("3. total mentions hit: " + totalMentionsHit);
                evalLtsList.add("3. total anno tpc length: " + totalAnnoTpcLength);
                evalLtsList.add("3. total auto tpc length: " + totalAutoTpcLength);
                evalLtsList.add("--------------------statistics--------------------------");
                evalLtsList.add("4.Missed topic list size: " + topicMissList.size());
                evalLtsList.add("4.Missed topic list (topic name:turn): " + topicMissList.toString());
                evalLtsList.add("5.Auto Topic is sub-mention count: " + autoTopicIsMention.size());
                evalLtsList.add("5.Auto Topic is sub-mention List...");
                for(int i = 0; i < autoTopicIsMention.size(); i ++){
                    evalLtsList.add(autoTopicIsMention.get(i));
                }
                

                topicRecall = 0.0;
                
                if(isLast){
                    try {
                        PrintWriter pr = new PrintWriter(EvalFileDir);
                        for (int i = 0; i < evalLtsList.size(); i++){
                            pr.println(evalLtsList.get(i));
                        }
                        pr.close();
                            } catch (FileNotFoundException ex) {
                        Logger.getLogger(Evaluation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                topicMissList.clear();
                topicHitList.clear();
                annoTopicList.clear();
                autoTopicIsMention.clear();
//                autoMentionIsMention.clear();
            


    }//ends method
//      =================================setters & getters=========================================


}

