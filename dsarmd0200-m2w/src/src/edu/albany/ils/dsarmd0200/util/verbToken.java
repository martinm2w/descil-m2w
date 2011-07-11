package edu.albany.ils.dsarmd0200.util;

import java.util.ArrayList;

public class verbToken 
{
		int prevChainIndex;
		String word;
		ArrayList<Integer> subMentions;
		int i = 1;
		
		public verbToken(String word, ArrayList<Integer>  subMentions)
		{
			this.word = word;
			this.subMentions = subMentions;
		}

		public String getWord() {
			return word;
		}

		public ArrayList<Integer> getSubMentions() {
			return subMentions;
		}
		
		public void display(int prevChainIndex)
		{
			this.prevChainIndex = prevChainIndex;
			System.out.print("chain "+ (prevChainIndex + i) + " starts: ");
			System.out.println(this.word);
			//System.out.println("Submentions:");
			for(int index = 0; index < subMentions.size(); index++)
			{
				System.out.println(subMentions.get(index));
			}
			System.out.println("chain " + (prevChainIndex + i) + " ends");
			i++;
		}

}
