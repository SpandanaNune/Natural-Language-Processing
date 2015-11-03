import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import module.graph.SentenceToGraph;
import module.graph.helper.GraphPassingNode;

public abstract class Parser {
	private static Reader reader;
	private static DocumentPreprocessor dp;
	private static SentenceToGraph stg = new SentenceToGraph();
	private static GraphPassingNode gpn2;
	private static String parentName, childName, relationship, currSentence, currRelation;
	private static ArrayList<String> relationList;
	private static String[] elements;
	
	public static QuestionGraph parseQuestion(String question){
		init(question);

		gpn2 = stg.extractGraph(question,false,true,false);
		QuestionGraph qGraph = new QuestionGraph(gpn2.getposMap(), question);
		relationList = gpn2.getAspGraph();

		currRelation = relationList.get(0);
		elements = currRelation.substring(4, currRelation.length()-2).split(",");
		parentName = elements[0];
		relationship = elements[1];
		childName = elements[2];

		qGraph.add(parentName, relationship, childName);
		
		return qGraph; 
	}
	
	public static GlobalGraph parseText(String text){
		init(text);
		
		GlobalGraph gGraph = new GlobalGraph();
		int sentenceNum = 0;

		for (List<HasWord> sentenceWordList : dp) {
			currSentence = Sentence.listToString(sentenceWordList);
			gpn2 = stg.extractGraph(currSentence,false,true,false);
			SentenceGraph sGraph = new SentenceGraph(gpn2.getposMap(), currSentence);
			relationList = gpn2.getAspGraph();
	
			for(int i = 0; i < relationList.size(); i++){
				currRelation = relationList.get(i);
				elements = currRelation.substring(4, currRelation.length()-2).split(",");
				parentName = elements[0];
				relationship = elements[1];
				childName = elements[2];
	
				sGraph.add(parentName, relationship, childName, sentenceNum);
			}
			
			sentenceNum++;
			gGraph.add(sGraph);
		}
		
		return gGraph;
	}
	
	private static void init(String input){
		reader = new StringReader(input);
		dp = new DocumentPreprocessor(reader);
	}
}
