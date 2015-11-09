package graph;
import java.util.HashMap;

public class QuestionGraph extends SentenceGraph{
	private static final long serialVersionUID = -2135547892436405158L;
	public String answerType;
	
	public QuestionGraph(HashMap<String, String> posMap, String question) {
		super(posMap, question, -1);
		answerType = null;
	}
	
	public void add(String parentName, String relationship, String childName){
		super.add(parentName, relationship, childName);
		
		if(parentName.equals("?") && relationship.equals("is_subclass_of")){
			answerType = childName;
		}
	}
}
