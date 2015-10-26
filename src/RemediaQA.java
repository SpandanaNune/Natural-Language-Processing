import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import module.graph.SentenceToGraph;
import module.graph.helper.GraphPassingNode;

public class RemediaQA {
	public static void main(String[] args) throws FileNotFoundException{
		SentenceToGraph stg = new SentenceToGraph();
		String sent = "Why did David Koresh ask the FBI for a word processor?";
		
		GraphPassingNode gpn2 = stg.extractGraph(sent,false,true,false);
		HashMap<String, String> posMap = gpn2.getposMap();
		ArrayList<String> list = gpn2.getAspGraph();
		String[] elements;
		String parentName, childName, relationship, temp;
		int sentenceNum = 0;
		
		Graph graph = new Graph(posMap);

		for(int i = 0; i < list.size(); i++){
			temp = list.get(i);
			elements= temp.substring(4, temp.length()-2).split(",");
			parentName = elements[0];
			relationship = elements[1];
			childName = elements[2];

			graph.add(parentName, relationship, childName, sentenceNum);
		}
		
		System.out.println(graph);
	}
}
