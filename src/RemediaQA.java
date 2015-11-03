import java.io.FileNotFoundException;
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

public class RemediaQA {
	public static void main(String[] args) throws FileNotFoundException{
		String testParagraph = "(ENGLAND, June, 1989) - Christopher Robin is alive and well.  He lives in England.  He is the same person that you read about in the book, Winnie the Pooh. "
				+ "As a boy, Chris lived in a pretty home called Cotchfield Farm.  When Chris was three years old, his father wrote a poem about him.  The poem was printed in a magazine for others to read."
				+ " Mr. Robin then wrote a book.  He made up a fairy tale land where Chris lived.  His friends were animals.  There was a bear called Winnie the Pooh.  There was also an owl and a young pig, called a piglet.  All the animals were stuffed toys that Chris owned.  Mr. Robin made them come to life with his words.  The places in the story were all near Cotchfield Farm. "
				+ "Winnie the Pooh was written in 1925.  Children still love to read about Christopher Robin and his animal friends.  Most people don't know he is a real person who is grown now.  He has written two books of his own.  They tell what it is like to be famous.";
		
		//testParagraph = "As a boy, Chris lived in a pretty home called Cotchfield Farm.";
		//testParagraph = "What did Chris's father do when he was three years old?";
		String testQuestion = "When was Winnie the Pooh written?";
		
		GlobalGraph questionGraph = parseText(testQuestion);
		GlobalGraph paragraphGraph = parseText(testParagraph);
		
		System.out.println(String.format("Graph for Test Question:\n\n%s", questionGraph));
		System.out.println(String.format("Graph for Test Paragraph:\n\n%s", paragraphGraph));
		/*
		Reader reader = new StringReader(testParagraph);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		SentenceToGraph stg = new SentenceToGraph();
		String sentence;
		GraphPassingNode gpn2;
		GlobalGraph gGraph = new GlobalGraph();
		
		int sentenceNum = 0;

		for (List<HasWord> sentenceWordList : dp) {
			sentence = Sentence.listToString(sentenceWordList);

			gpn2 = stg.extractGraph(sentence,false,true,false);
			HashMap<String, String> posMap = gpn2.getposMap();
			ArrayList<String> list = gpn2.getAspGraph();
			String[] elements;
			String parentName, childName, relationship, temp;
		
			SentenceGraph graph = new SentenceGraph(posMap);
	
			for(int i = 0; i < list.size(); i++){
				temp = list.get(i);
				elements= temp.substring(4, temp.length()-2).split(",");
				parentName = elements[0];
				relationship = elements[1];
				childName = elements[2];
	
				graph.add(parentName, relationship, childName, sentenceNum, sentence);
			}
			
			sentenceNum++;
			gGraph.add(graph);
		}
		
		System.out.println(gGraph);
		*/
	}
	
	public static GlobalGraph parseText(String text){
		Reader reader = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		SentenceToGraph stg = new SentenceToGraph();
		String sentence;
		GraphPassingNode gpn2;
		GlobalGraph gGraph = new GlobalGraph();
		
		int sentenceNum = 0;

		for (List<HasWord> sentenceWordList : dp) {
			sentence = Sentence.listToString(sentenceWordList);

			gpn2 = stg.extractGraph(sentence,false,true,false);
			HashMap<String, String> posMap = gpn2.getposMap();
			ArrayList<String> list = gpn2.getAspGraph();
			String[] elements;
			String parentName, childName, relationship, temp;
		
			SentenceGraph graph = new SentenceGraph(posMap);
	
			for(int i = 0; i < list.size(); i++){
				temp = list.get(i);
				elements= temp.substring(4, temp.length()-2).split(",");
				parentName = elements[0];
				relationship = elements[1];
				childName = elements[2];
	
				graph.add(parentName, relationship, childName, sentenceNum, sentence);
			}
			
			sentenceNum++;
			gGraph.add(graph);
		}
		
		return gGraph;
	}
}
