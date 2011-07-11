package edu.albany.ils.dsarmd0200.util;
import java.util.ArrayList;

// Author: Ken Stahl
public class CorefEvaluation {
	ListCompare lc;

	public CorefEvaluation(ListCompare param){
		lc = param;
	}

	public void Calc(){
		ArrayList <String> auto;
		ArrayList <String> anno;
		int maxturn = lc.maxTurnNo();
		for (int i = 0; i < maxturn; i++){
			anno = lc.getAnnotatedAt(i);
			auto = lc.getAutomatedAt(i);
			System.out.println("Annotated for turn #" + i);
			for (int j = 0; j < anno.size(); j++){
				System.out.println(anno.get(j));
			}
			System.out.println("Automated for turn #" + i);
			for (int j = 0; j < auto.size(); j++){
				System.out.println(auto.get(j));
			}
			System.out.println();
		}
	}

}
