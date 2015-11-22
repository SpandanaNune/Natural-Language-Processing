import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	private static SentenceToGraph stg; //= new SentenceToGraph();
	private static GraphPassingNode gpn2; //= stg.extractGraph("Initializing Parser", false, true, true);
	private static GlobalGraph gGraph;
	private static QuestionGraph[] qGraphs;
	private static PrintStream stdout;
	private static HashMap<String, String> nnpMap, nnpsMap;
	private static HashMap<String, Integer> nnpCount, nnpsCount;
	private static String[] fullTitles = {"Mister", "Missus", "Miss", "Doctor"};
	private static ArrayList<String> fullTitlesList = new ArrayList<String>(Arrays.asList(fullTitles));
	
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
		
		int sentenceNum = 0;
		GlobalGraph gGraph = new GlobalGraph();
		String currSentence, modifiedSentence;
		ArrayList<String> relationList;
		SentenceGraph sGraph;
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		stdout = System.out;
		System.setOut(new PrintStream(baos));

		nnpMap = new HashMap<String, String>();
		nnpsMap = new HashMap<String, String>();
		
		nnpCount = new HashMap<String, Integer>();
		nnpsCount = new HashMap<String, Integer>();

		for (List<HasWord> sentenceWordList : dp) {
			for(String key : nnpCount.keySet()){
				nnpCount.replace(key, nnpCount.get(key) + 1);
			}

			for(String key : nnpsCount.keySet()){
				nnpsCount.replace(key, nnpsCount.get(key) + 1);
			}
			
			currSentence = sanitizeSentence(Sentence.listToString(sentenceWordList));
			gpn2 = stg.extractGraph(currSentence,false,true,true);
			relationList = gpn2.getAspGraph();
			
			modifiedSentence = baos.toString().split("\n")[0].replace("Modified:", "").trim();
			/*
			sGraph = new SentenceGraph(gpn2.getposMap(), currSentence, sentenceNum);
			relationList = sortRelations(gpn2.getAspGraph());
			*/

			stdout.println("Sentence from kparser: " + modifiedSentence);
			modifiedSentence = processSentence(modifiedSentence, gpn2.getposMap(), gpn2.getWordSenseMap(), relationList);
			stdout.println("Modified Sentence: " + modifiedSentence + "\n");

			//parse(sGraph, relationList);
			
			sentenceNum++;
			//gGraph.add(sGraph);
			baos.reset();
		}
		
		System.setOut(stdout);
		
		return gGraph;
	}
	
	private static String sanitizeSentence(String sentence){
		String[] titles = {"Mr.", "Mrs.", "Ms.", "Dr."};

		for(String title : titles){
			if(sentence.contains(title)){
				if(title.equals("Mr.")){
					sentence = sentence.replace(title, "Mister");
				}
				else if(title.equals("Mrs.")){
					sentence = sentence.replace(title, "Missus");
				}
				else if(title.equals("Ms.")){
					sentence = sentence.replace(title, "Miss");
				}
				else{
					sentence = sentence.replace(title, "Doctor");
				}
			}
		}
		
		return sentence;
	}
	
	private static String processSentence(String sentence, HashMap<String, String> posMap, HashMap<String, ArrayList<String>> wsMap,
			ArrayList<String> relationList){
		StringBuilder output = new StringBuilder();
		String[] words = sentence.split(" ");
		String word, pos, ws;
		boolean replace = true;

		for(int i = 0; i < words.length; i++){
			word = words[i].trim();
			ws = null;

			if(word.toLowerCase().equals("you") || word.toLowerCase().equals("your")){
				output.append(word + " ");
				continue;
			}
			
			if((pos = getPos(word, posMap)) != null){
				if(fullTitlesList.contains(word)){
					word = word + "_" + words[i+1];
					ws = "noun.person";
					pos = "NNP";
					i++;
				}
				
				if(i == 0 && (pos.equals("DT") || pos.equals("WRB")) && !word.toLowerCase().equals("the")){
					pos = "PRP";
				}

				if(ws == null){
					ws = findWS(word, pos, relationList);
				}

				if(ws == null){
					ws = getWS(word, wsMap);
				}
				
				if(ws == null){
					ws = isGroup(word, relationList) ? "noun.group" : null;
				}

				if(pos.equals("NN")){
					if(ws != null && nnpMap.containsKey(ws) && nnpCount.get(ws) < 2){
						if(replace){
							word = nnpMap.get(ws);
							nnpCount.replace(ws, 0);
						}

						replace = !replace;
					}
				}
				else if(pos.equals("NNS")){
					if(ws != null && nnpsMap.containsKey(ws) && nnpsCount.get(ws) < 2){
						if(replace){
							word = nnpsMap.get(ws);
							nnpsCount.replace(ws, 0);
						}
						
						replace = !replace;
					}
				}
				else if(pos.equals("NNP")){
					if(ws != null){
						if(nnpMap.containsKey(ws)){
							nnpMap.replace(ws, word);
						}
						else{
							nnpMap.put(ws, word);
						}

						nnpCount.put(ws, 0);
					}
				}
				else if(pos.equals("NNPS")){
					if(ws != null){
						if(nnpsMap.containsKey(ws)){
							nnpsMap.replace(ws, word);
						}else{
							nnpsMap.put(ws, word);
						}
						nnpsCount.put(ws, 0);
					}
				}
				else if(pos.equals("PRP")){
					if(ws != null && nnpMap.containsKey(ws) && nnpCount.get(ws) < 2){
						if(replace){
							word = nnpMap.get(ws);
							nnpCount.replace(ws, 0);
						}

						replace = !replace;
					}
				}
				else if(pos.equals("PRP$")){
					if(ws != null && nnpMap.containsKey(ws) && nnpCount.get(ws) < 2){
						if(replace){
							word = nnpMap.get(ws) + "'s";
							nnpCount.replace(ws, 0);
						}

						replace = !replace;
					}
				}
			}
			
			if(word.contains("_")){
				output.append(word.replace("_", " ") + " ");
			}
			else{
				output.append(word + " ");
			}
		}

		return output.toString().substring(0, output.length() - 1);
	}
	
	private static String findWS(String str, String pos, ArrayList<String> relationList){
		String[] commonPRP = {"he", "her", "we"},
				commonPRP$ = {"his", "hers"};
		str = str.toLowerCase().trim();
		
		if(pos.equals("PRP")){
			for(String s : commonPRP){
				if(str.equals(s)){
					return "noun.person";
				}
			}
		}
		else if(pos.equals("PRP$")){
			for(String s : commonPRP$){
				if(str.equals(s)){
					return "noun.person";
				}
			}
		}
		
		if(isPerson(str, relationList)){
			return "noun.person";
		}
		
		return null;
	}

	private static boolean isGroup(String str, ArrayList<String> relationList){
		String[] elements;
		String currRelation, parentName, relationship, childName;

		for(int i = 0; i < relationList.size(); i++){
			currRelation = relationList.get(i).toLowerCase();
			elements = currRelation.substring(4, currRelation.length()-2).split(",");
			parentName = elements[0];
			relationship = elements[1];
			childName = elements[2];
			
			if(parentName.equals(str) && relationship.equals("is_subclass_of") && childName.equals("group")){
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isPerson(String str, ArrayList<String> relationList){
		String[] elements;
		String currRelation, parentName, relationship, childName;

		for(int i = 0; i < relationList.size(); i++){
			currRelation = relationList.get(i).toLowerCase();
			elements = currRelation.substring(4, currRelation.length()-2).split(",");
			parentName = elements[0];
			relationship = elements[1];
			childName = elements[2];
			
			if(parentName.equals(str) && relationship.equals("is_subclass_of") && childName.equals("person")){
				return true;
			}
		}
		
		return false;
	}

	private static String getWS(String str, HashMap<String, ArrayList<String>> wsMap){
		String tempKey;
		str = str.toLowerCase().trim();

		for(String key : wsMap.keySet()){
			tempKey = key.toLowerCase().trim();
			
			if((double) str.length()/(double) tempKey.length() >= .25 && (tempKey.contains(str) || str.contains(tempKey))){
				return wsMap.get(key).get(1);
			}
		}
		
		return null;
	}

	private static String getPos(String str, HashMap<String, String> posMap){
		String tempKey;
		str = str.toLowerCase().trim();
		
		for(String key : posMap.keySet()){
			tempKey = key.toLowerCase().trim();
			
			if((double) str.length()/(double) tempKey.length() >= .25 && (tempKey.contains(str) || str.contains(tempKey))){
				return posMap.get(key);
			}
		}
		
		return null;
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
