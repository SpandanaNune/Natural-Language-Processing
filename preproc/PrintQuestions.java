import graph.GlobalGraph;
import graph.QuestionGraph;
import graph.SentenceGraph;

public class PrintQuestions {
	private static final int LEVEL = 2,
			SET = 24;
	public static void main(String[] args) {
		Parser.readSetFromFile(String.format("all-remedia-processed/level%d/rm%d-%d.ser", LEVEL, LEVEL, SET));
		
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		GlobalGraph gGraph = Parser.getGlobalGraph();
	
		for(SentenceGraph sGraph : gGraph.getSentences()){
			System.out.println(sGraph);
		}

		System.out.println("\nQuestions:\n");
		
		for(QuestionGraph qGraph : qGraphs){
			System.out.println(qGraph);
		}
	}

}
