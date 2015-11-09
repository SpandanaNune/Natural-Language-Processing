import java.io.File;
import java.io.FileNotFoundException;

import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;

public class RemediaQA {
	private static String basePath = "all-remedia-processed/";

	public static void main(String[] args) throws FileNotFoundException{
		Parser.readSetFromFile(new File(basePath + "level2/rm2-13.ser"));
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		GlobalGraph gGraph = Parser.getGlobalGraph();
		
		System.out.println("Text:");
		
		for(int i = 0; i < gGraph.sentences.size(); i++){
			SentenceGraph sGraph = gGraph.sentences.get(i);
			System.out.println(String.format("%d: %s", (i+1), sGraph.sentence));
		}
		
		System.out.println();
		
		for(QuestionGraph qGraph : qGraphs){
			System.out.println("\nQuestion: " + qGraph.sentence + "\n");
			QA qa = new QA(qGraph, gGraph);
			System.out.println("RANKED SENTENCES: \n");
			qa.findAnswer();
		}
	}
}
