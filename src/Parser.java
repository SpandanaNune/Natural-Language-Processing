import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;
import module.graph.SentenceToGraph;
import module.graph.helper.GraphPassingNode;

public abstract class Parser {
	private static Reader reader;
	private static DocumentPreprocessor dp;
	private static SentenceToGraph stg = new SentenceToGraph();
	private static GraphPassingNode gpn2;
	
	public static QuestionGraph parseQuestion(String question){
		init(question);

		gpn2 = stg.extractGraph(question,false,true,false);
		QuestionGraph qGraph = new QuestionGraph(gpn2.getposMap(), question);
		ArrayList<String> relationList = gpn2.getAspGraph();

		Parser.parse(qGraph, relationList, -1);
		
		return qGraph; 
	}
	
	public static GlobalGraph parseText(String text){
		init(text);
		
		GlobalGraph gGraph = new GlobalGraph();
		int sentenceNum = 0;
		String currSentence;

		for (List<HasWord> sentenceWordList : dp) {
			currSentence = Sentence.listToString(sentenceWordList);
			gpn2 = stg.extractGraph(currSentence,false,true,false);
			SentenceGraph sGraph = new SentenceGraph(gpn2.getposMap(), currSentence);
			ArrayList<String> relationList = gpn2.getAspGraph();
	
			Parser.parse(sGraph, relationList, sentenceNum);
			
			sentenceNum++;
			gGraph.add(sGraph);
		}
		
		
		return gGraph;
	}
	
	private static void parse(SentenceGraph graph, ArrayList<String> relationList, int sentenceNum){
		String currRelation, parentName, childName, relationship;
		String[] elements;

		for(int i = 0; i < relationList.size(); i++){
			currRelation = relationList.get(i);
			elements = currRelation.substring(4, currRelation.length()-2).split(",");
			parentName = elements[0];
			relationship = elements[1];
			childName = elements[2];

			graph.add(parentName, relationship, childName, sentenceNum);
		}
	}
	
	private static void init(String input){
		reader = new StringReader(input);
		dp = new DocumentPreprocessor(reader);
	}
}
