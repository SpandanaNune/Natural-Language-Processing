import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.GlobalGraph;
import graph.QuestionGraph;
import params.Parameters;
import ranker.RankResult;
import ranker.Ranker;

public class QuestionTypeEM{
	private static String basePath = "all-remedia-processed/";
	private static final int MAX_ALLOWED_RANK = 1;

	public static void main(String[] args) throws FileNotFoundException{
		List<String> paths = initPaths();
		List<List<String>> answerLists = initAnswerLists();
		Map<String, Parameters> questionTypeParams = new HashMap<String, Parameters>();

		boolean reset = true;
		int correct = 0, asked = 0;

		//TRAINING LOOP

		for(int i = 0; i < paths.size(); i++){
			if(reset){
				reset = false;
				asked = 0;
				correct = 0;
			}

			String path = paths.get(i);
			List<String> answers = answerLists.get(i);
			Parser.readSetFromFile(path);
			QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
			GlobalGraph gGraph = Parser.getGlobalGraph();

			for(int j = 0; j < answers.size(); j++){
				String answer = answers.get(j);
				QuestionGraph qGraph = qGraphs[j];

				if(qGraph.getSentence().contains("Why") || qGraph.getSentence().contains("How")){
					continue;
				}
				else if(qGraph.getAnswerType() == null || qGraph.getAnswerType().isEmpty()){
					System.out.println(String.format("Answer type for '%s' is empty.", qGraph));

					if(qGraph.getSentence().contains("Who")){
						qGraph.setAnswerType("person");
					}
					
					if(!questionTypeParams.containsKey(qGraph.getAnswerType())){
						questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
					}
				}

				if(!questionTypeParams.containsKey(qGraph.getAnswerType())){
					questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
				}

				Parameters currParams = questionTypeParams.get(qGraph.getAnswerType());
				Parameters tempParams = calculateParameters(answer, qGraph, gGraph, currParams);

				/*
					System.out.println(String.format("Question: %s", qGraph.getSentence()));
					System.out.println(String.format("Answer: %s", answer));
				 */

				if(tempParams == null){
					//	System.out.println("FAILED\n");
				}
				else{
					//	System.out.println(String.format("Success Params: %s\n", tempParams.toString()));

					if(!currParams.equals(tempParams)){
						//		System.out.println("Changing Parameter Values & Restarting\n");
						currParams = tempParams;
						questionTypeParams.replace(qGraph.getAnswerType(), currParams);
						j = -1;
						i = -1;
						reset = true;
						continue;
					}

					correct++;
				}

				asked++;
			}
		}

		//VALIDATION LOOP
		String validationPath = String.format("%slevel3/rm3-1.ser", basePath);

		List<String> answers = new ArrayList<String>();
		answers.add("At noon , two small children cut a ribbon .");
		answers.add("It is called the Empire State Building .");
		answers.add("-LRB- NEW YORK : May 1 , 1931 -RRB- .");
		answers.add("-LRB- NEW YORK : May 1 , 1931 -RRB- .");
		answers.add("They can see at least 50 miles away .");

		Parser.readSetFromFile(validationPath);
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		GlobalGraph gGraph = Parser.getGlobalGraph();
		int finalCorrect = 0, finalAsked = 0;

		for(int i = 0; i < answers.size(); i++){
			QuestionGraph qGraph = qGraphs[i];

			if(qGraph.getSentence().contains("Why") || qGraph.getSentence().contains("How")){
				continue;
			}
			else if(qGraph.getAnswerType() == null || qGraph.getAnswerType().isEmpty()){
				System.out.println(String.format("Answer type for '%s' is empty.", qGraph));

				if(qGraph.getSentence().contains("Who")){
					qGraph.setAnswerType("person");
				}
				
				if(!questionTypeParams.containsKey(qGraph.getAnswerType())){
					questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
				}
			}

			if(!questionTypeParams.containsKey(qGraph.getAnswerType())){
				questionTypeParams.put(qGraph.getAnswerType(), new Parameters());
			}

			Parameters params = questionTypeParams.get(qGraph.getAnswerType());
			Ranker ranker = new Ranker(params);
			String answer = answers.get(i);
			RankResult result = ranker.rankSentences(qGraph, gGraph);

			int rank = result.getRank(answer);

			if(rank <= MAX_ALLOWED_RANK){
				finalCorrect++;
			}

			finalAsked++;
		}

		System.out.println("Validation");
		System.out.println("\tFile: " + validationPath);
		System.out.println("\tAsked: " + finalAsked);
		System.out.println("\tCorrect: " + finalCorrect);
		System.out.println(String.format("\t%s Correct: %.2f%s\n", "%", (double) finalCorrect/(double) finalAsked * 100, "%"));

		for(String key : questionTypeParams.keySet()){
			System.out.println(String.format("Parameters for %s: %s", key, questionTypeParams.get(key)));
		}

	}

	private static Parameters calculateParameters(String answer, QuestionGraph qGraph, GlobalGraph gGraph, Parameters params){
		if(params == null){
			params = new Parameters();
		}

		Ranker ranker = new Ranker(params);

		RankResult initResult = ranker.rankSentences(qGraph, gGraph);
		int originalRank = initResult.getRank(answer);

		if(originalRank > MAX_ALLOWED_RANK){
			int lastRank = originalRank,
					currRank = originalRank;
			Parameters tempParams = params.clone();
			boolean allTried = false;
			List<Integer> usedIndices = new ArrayList<Integer>();

			while(!allTried){
				int idx = selectIndex(0, Parameters.getKeys().size(), usedIndices);
				String paramKey = Parameters.getKeys().get(idx);

				if(usedIndices.size() == Parameters.getKeys().size()){
					allTried = true;
				}

				do{
					tempParams.setParameter(paramKey, tempParams.get(paramKey) + .1);
					ranker.setParameters(tempParams);
					RankResult result = ranker.rankSentences(qGraph, gGraph);
					lastRank = currRank;
					currRank = result.getRank(answer); 
				}while(currRank < lastRank);

				tempParams.setParameter(paramKey, tempParams.get(paramKey) - .1);
			}

			if(lastRank <= MAX_ALLOWED_RANK){
				params = tempParams;
			}
			else{
				return null;
			}
		}

		return params;
	}

	private static int selectIndex(int min, int max, List<Integer> usedIndices){
		int retVal;

		do{
			retVal = min + (int)(Math.random()*max); 
		}while(usedIndices.contains(retVal));

		usedIndices.add(retVal);

		return retVal;
	}

	private static List<String> initPaths(){
		List<String> paths  = new ArrayList<String>();

		paths.add(basePath + "level2/rm2-1.ser");
		paths.add(basePath + "level2/rm2-2.ser");
		paths.add(basePath + "level2/rm2-3.ser");
		paths.add(basePath + "level2/rm2-13.ser");
		paths.add(basePath + "level2/rm2-15.ser");

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

		List<String> rm22Answers = new ArrayList<String>();
		rm22Answers.add("The last Pony Express rider leaves town today .");
		rm22Answers.add("Last year , the Pony Express was a new way to deliver the mail .");
		rm22Answers.add("Since April , 1860 , mail has been sent this way .");
		rm22Answers.add("Then the rider will stop at a swing station .");
		rm22Answers.add("It is called the telegraph .");

		List<String> rm23Answers = new ArrayList<String>();
		rm23Answers.add("The pledge was written by Frances Bellamy .");
		rm23Answers.add("We are named after this brave sailor .");
		rm23Answers.add("(COLUMBUS, OHIO, October 12, 1892) Four hundred years ago today, Christopher Columbus first saw our country. ");
		rm23Answers.add("So , today , schools all over our land will display the flag .");
		rm23Answers.add("He wrote it so young people could feel proud of their land .");

		List<String> rm213Answers = new ArrayList<String>();
		rm213Answers.add("Today , a girl named Lynne Cox swam from the United States to Russia !");
		rm213Answers.add("To get ready for this swim , Lynne swam miles each day in ice-cold water .");
		rm213Answers.add("-LRB- RUSSIA , July , 1987 -RRB-");
		rm213Answers.add("She left from an island in Alaska .");
		rm213Answers.add("They were ready to help if she needed it .");

		List<String> rm215Answers = new ArrayList<String>();
		rm215Answers.add("Baby Shamu 's mother is named Kandu .");
		rm215Answers.add("Her name is Baby Shamu .");
		rm215Answers.add("-LRB- ORLANDO , FLORIDA , September , 1985 -RRB-");
		rm215Answers.add("She was born in a sea animal park called Sea World .");
		rm215Answers.add("Baby Shamu will be the first killer whale to grow up with people .");

		answerLists.add(rm21Answers);
		answerLists.add(rm22Answers);
		answerLists.add(rm23Answers);
		answerLists.add(rm213Answers);
		answerLists.add(rm215Answers);

		return answerLists;
	}
}
