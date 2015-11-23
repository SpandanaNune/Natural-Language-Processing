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
		List<String> paths = initPaths();
		List<List<String>> answerLists = initAnswerLists();
		Parameters params = new Parameters();
		int correct = 0, asked = 0;
		
		for(int i = 0; i < paths.size(); i++){
			String path = paths.get(i);
			Parser.readSetFromFile(path);
			QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
			GlobalGraph gGraph = Parser.getGlobalGraph();
			List<String> answers = answerLists.get(i);
			
			for(int j = 0; j < answers.size(); j++){
				String answer = answers.get(j);
				QuestionGraph qGraph = qGraphs[j];
				
				if(qGraph.getSentence().contains("Why") || qGraph.getSentence().contains("How")){
					continue;
				}
				
				Parameters tempParams = calculateParameters(answer, qGraph, gGraph, params);

				System.out.println(String.format("Question: %s", qGraph.getSentence()));
				System.out.println(String.format("Answer: %s", answer));
				
				if(tempParams == null){
					System.out.println("FAILED\n");
				}
				else{
					System.out.println(String.format("Success Params: %s\n", tempParams.toString()));

					if(!params.equals(tempParams) && j != 0){
						System.out.println("Changing Parameter Values & Restarting\n");
						params = tempParams;
						j = -1;
						i = -1;
						correct = 0;
						asked = 0;
					}
					
					correct++;
				}
				
				asked++;
			}
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

			for(String paramKey : Parameters.getKeys()){
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

	private static List<String> initPaths(){
		List<String> paths  = new ArrayList<String>();

		paths.add(basePath + "level2/rm2-1.ser");
		//paths.add(basePath + "level2/rm2-15.ser");
		//paths.add(basePath + "level2/rm2-3.ser");
		//paths.add(basePath + "level2/rm2-13.ser");

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
