import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graph.GlobalGraph;
import graph.QuestionGraph;
import params.Parameters;
import ranker.RankResult;
import ranker.Ranker;

public class EM{
	private static String basePath = "all-remedia-processed/";
	private static final int MAX_ALLOWED_RANK = 5;
	private static int numCorrectAnswers, numQuestions;

	public static void main(String[] args) throws FileNotFoundException{
		Parameters params = new Parameters();
		List<List<String>> answerLists = initAnswerLists();
		List<String> paths = initPaths(),
				foundQuestions = new ArrayList<String>();

		numQuestions = 0;

		for(int j = 0; j < paths.size(); j++){
			if(j == 0){
				numCorrectAnswers = 0;
			}

			String path = paths.get(j);
			List<String> answers = answerLists.get(j);

			Parser.readSetFromFile(new File(path));
			QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
			GlobalGraph gGraph = Parser.getGlobalGraph();


			/*
			System.out.println("Text: ");

			for(SentenceGraph sGraph : gGraph.sentences){
				System.out.println("\t" + sGraph.sentence);
			}
			 */

			int prevNumCorrect = numCorrectAnswers;

			for(int i = 0; i < qGraphs.length; i++){
				if(i == 0){
					numCorrectAnswers = prevNumCorrect;
				}

				String answer = answers.get(i);
				QuestionGraph qGraph = qGraphs[i];

				if(qGraph.getSentence().contains("Who")){
					qGraph.setAnswerType("person");
				}
				else if(qGraph.getSentence().contains("How") || qGraph.getSentence().contains("Why")){
					continue;
				}

				if(!foundQuestions.contains(qGraph.getSentence())){
					numQuestions++;
					foundQuestions.add(qGraph.getSentence());
				}

				System.out.println("\n\nQuestion: " + qGraph.getSentence());
				System.out.println("Answer: " + answer);

				if(calculateParameters(answer, qGraph, gGraph, params)){
					i = -1;
					j = -1;
				}
			}
		}

		System.out.println("\nWith max allowed rank = " + MAX_ALLOWED_RANK);
		System.out.println("Num Questions: " + numQuestions);
		System.out.println("Num correct answers: " + numCorrectAnswers);
	}

	public static boolean improveRank(int currRank, int lastRank){
		return lastRank > currRank;
	}

	public static boolean compareMaps(HashMap<String, Double> m1, HashMap<String, Double> m2){
		if(m1.keySet().size() != m2.keySet().size()){
			return false;
		}

		Double m1Val, m2Val;

		for(String key : m1.keySet()){
			if(!m2.containsKey(key)){
				return false;
			}

			m1Val = m1.get(key);
			m2Val = m2.get(key);

			if(m1Val.doubleValue() != m2Val.doubleValue()){
				return false;
			}
		}

		return true;
	}

	public static boolean calculateParameters(String answer, QuestionGraph qGraph, GlobalGraph gGraph, Parameters params){
		double currParamValue, originalParamValue;
		int currRank = MAX_ALLOWED_RANK + 1, lastRank, originalRank = 0;
		Ranker ranker = new Ranker(params);

		for(int i = 0; i < Parameters.getKeys().size(); i++){
			String currParamName = Parameters.getKeys().get(i);
			currParamValue = params.get(currParamName);
			originalParamValue = currParamValue;

			RankResult rankedSentences = ranker.rankSentences(qGraph, gGraph);

			currRank = Ranker.getRank(answer, rankedSentences);
			lastRank = currRank;
			originalRank = lastRank;

			if(currRank > MAX_ALLOWED_RANK){
				do{
					currParamValue += .1;
					params.replace(currParamName, currParamValue);

					rankedSentences = ranker.rankSentences(qGraph, gGraph);

					lastRank = currRank;
					currRank = Ranker.getRank(answer, rankedSentences);
				}
				while(currRank < lastRank || (currRank < lastRank && currRank > MAX_ALLOWED_RANK));

				if(currRank < originalRank){
					params.replace(currParamName, currParamValue - .1);

					if(currRank <= MAX_ALLOWED_RANK){
						break;
					}
					else{
						i = - 1;
					}
				}
				else{
					params.replace(currParamName, originalParamValue);
				}
			}
		}

		ranker.rankSentences(qGraph, gGraph);

		if(currRank < MAX_ALLOWED_RANK){
			Parameters parameters = params.clone();
			System.out.println("Achieved Rank: " + currRank + " with parameters:");
			System.out.println(parameters);


			if(currRank < originalRank){
				return true;
			}
			else{
				numCorrectAnswers++;
				return false;
			}
		}
		else{
			System.out.println("FAILED");

			return false;
		}
	}

	//Initialization

	private static List<String> initPaths(){
		List<String> paths  = new ArrayList<String>();

		paths.add(basePath + "level2/rm2-1.ser");
		paths.add(basePath + "level2/rm2-15.ser");
		paths.add(basePath + "level2/rm2-3.ser");
		paths.add(basePath + "level2/rm2-13.ser");

		return paths;
	}

	private static List<List<String>> initAnswerLists(){
		List<List<String>> answerLists = new ArrayList<List<String>>();

		List<String> rm21Answers = new ArrayList<String>();
		rm21Answers.add("He is the same person that you read about in the book , Winnie the Pooh .");
		rm21Answers.add("When Chris was three years old , his father wrote a poem about him .");
		rm21Answers.add("Winnie the Pooh was written in 1925 .");
		rm21Answers.add("As a boy , Chris lived in a pretty home called Cotchfield Farm .");
		rm21Answers.add("They tell what it is like to be famous .");

		List<String> rm215Answers = new ArrayList<String>();
		rm215Answers.add("Baby Shamu 's mother is named Kandu .");
		rm215Answers.add("Her name is Baby Shamu .");
		rm215Answers.add("-LRB- ORLANDO , FLORIDA , September , 1985 -RRB- - A six-foot-long baby was born this month .");
		rm215Answers.add("She was born in a sea animal park called Sea World .");
		rm215Answers.add("Baby Shamu will be the first killer whale to grow up with people .");

		List<String> rm23Answers = new ArrayList<String>();
		rm23Answers.add("The pledge was written by Frances Bellamy .");
		rm23Answers.add("We are named after this brave sailor .");
		rm23Answers.add("(COLUMBUS, OHIO, October 12, 1892) Four hundred years ago today, Christopher Columbus first saw our country. ");
		rm23Answers.add("So , today , schools all over our land will display the flag .");
		rm23Answers.add("He wrote it so young people could feel proud of their land .");

		List<String> rm213Answers = new ArrayList<String>();
		rm213Answers.add("-LRB- RUSSIA , July , 1987 -RRB- - Today , a girl named Lynne Cox swam from the United States to Russia !");
		rm213Answers.add("To get ready for this swim , Lynne swam miles each day in ice-cold water .");
		rm213Answers.add("-LRB- RUSSIA , July , 1987 -RRB- - Today , a girl named Lynne Cox swam from the United States to Russia !");
		rm213Answers.add("-LRB- RUSSIA , July , 1987 -RRB- - Today , a girl named Lynne Cox swam from the United States to Russia !");
		rm213Answers.add("They were ready to help if she needed it .");

		answerLists.add(rm21Answers);
		answerLists.add(rm215Answers);
		answerLists.add(rm23Answers);
		answerLists.add(rm213Answers);

		return answerLists;
	}
}
