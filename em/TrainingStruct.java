import graph.GlobalGraph;
import graph.QuestionGraph;

public class TrainingStruct {
	private QuestionGraph qGraph;
	private String answer;
	private GlobalGraph gGraph;

	public TrainingStruct(QuestionGraph qGraph, String answer, GlobalGraph gGraph) {
		setQGraph(qGraph);
		setAnswer(answer);
		setGGraph(gGraph);
	}

	public QuestionGraph getQGraph() {
		return qGraph;
	}

	public void setQGraph(QuestionGraph qGraph) {
		this.qGraph = qGraph;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public GlobalGraph getGGraph() {
		return gGraph;
	}

	public void setGGraph(GlobalGraph gGraph) {
		this.gGraph = gGraph;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Question: %s\n", qGraph));
		sb.append(String.format("Answer: %s\n", answer));
		
		return sb.toString();
	}
}
