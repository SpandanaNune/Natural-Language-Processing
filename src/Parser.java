import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	private static GlobalGraph gGraph;
	private static QuestionGraph[] qGraphs;
	
	public static QuestionGraph parseQuestion(String question){
		init(question);

		gpn2 = stg.extractGraph(question,false,true,true);
		QuestionGraph qGraph = new QuestionGraph(gpn2.getposMap(), question);
		ArrayList<String> relationList = sortRelations(gpn2.getAspGraph());

		Parser.parse(qGraph, relationList);
		
		return qGraph; 
	}
	
	public static GlobalGraph getGlobalGraph(){
		if(gGraph == null){
			throw new IllegalArgumentException("You must call 'readSetFromFile' before calling this method.");
		}
		
		GlobalGraph retVal = gGraph;
		gGraph = null;
		
		return retVal;
	}
	
	public static QuestionGraph[] getQuestionGraphs(){
		if(qGraphs == null){
			throw new IllegalArgumentException("You must call 'readSetFromFile' before calling this method.");
		}
		
		QuestionGraph[] retVal = qGraphs;
		qGraphs = null;

		return retVal;
	}
	
	public static void readSetFromFile(File file){
		if(!file.exists()){
			throw new IllegalArgumentException(String.format("File at '%s' does not exist.", file.getAbsolutePath()));
		}
		else if(!getExtension(file.getName()).equals("ser")){
			throw new IllegalArgumentException("File to read from must have .ser extension.");
		}
		
		
		
		try {
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			gGraph = (GlobalGraph) ois.readObject();
			qGraphs= (QuestionGraph[]) ois.readObject();
			
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static GlobalGraph parseText(String text){
		init(text);
		
		GlobalGraph gGraph = new GlobalGraph();
		int sentenceNum = 0;
		String currSentence;

		for (List<HasWord> sentenceWordList : dp) {
			currSentence = Sentence.listToString(sentenceWordList);
			gpn2 = stg.extractGraph(currSentence,false,true,true);
			SentenceGraph sGraph = new SentenceGraph(gpn2.getposMap(), currSentence, sentenceNum);
			ArrayList<String> relationList = sortRelations(gpn2.getAspGraph());
	
			Parser.parse(sGraph, relationList);
			
			sentenceNum++;
			gGraph.add(sGraph);
		}
		
		
		return gGraph;
	}
	
	private static void parse(SentenceGraph graph, ArrayList<String> relationList){
		String currRelation, parentName, childName, relationship;
		String[] elements;

		for(int i = 0; i < relationList.size(); i++){
			currRelation = relationList.get(i);
			elements = currRelation.substring(4, currRelation.length()-2).split(",");
			parentName = elements[0];
			relationship = elements[1];
			childName = elements[2];

			if(graph.getClass() == QuestionGraph.class){
				QuestionGraph qGraph = (QuestionGraph)graph;
				qGraph.add(parentName, relationship, childName);
			}
			else{
				graph.add(parentName, relationship, childName);
			}
		}
	}
	
	private static void init(String input){
		reader = new StringReader(input);
		dp = new DocumentPreprocessor(reader);
	}
	
	private static ArrayList<String> sortRelations(ArrayList<String> relations){
		ArrayList<String> sortedRelations = new ArrayList<String>(),
				instanceRelations = new ArrayList<String>(),
				subclassRelations = new ArrayList<String>(),
				coreferences = new ArrayList<String>();
		
		for(String relation : relations){
			if(relation.contains("instance_of")){
				if(!instanceRelations.contains(relation)){
					instanceRelations.add(relation);
				}
			}
			else if(relation.contains("is_subclass_of")){
				if(!subclassRelations.contains(relation)){
					subclassRelations.add(relation);
				}
			}
			else if(relation.contains("has_coreferent")){
				if(!coreferences.contains(relation)){
					coreferences.add(relation);
				}
			}
			else{
				if(!sortedRelations.contains(relation)){
					sortedRelations.add(relation);
				}
			}
		}
		
		sortedRelations.addAll(instanceRelations);
		sortedRelations.addAll(subclassRelations);
		sortedRelations.addAll(coreferences);
		
		return sortedRelations;
	}

	private static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
 
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
}
