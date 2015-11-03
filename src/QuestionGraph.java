import java.util.HashMap;

public class QuestionGraph extends SentenceGraph{
	public String answerType;
	
	public QuestionGraph(HashMap<String, String> posMap, String question) {
		super(posMap, question);
		answerType = null;
	}
	
	public void add(String parentName, String relationship, String childName){
		super.add(parentName, relationship, childName, -1);
		
		if(parentName.equals("?") && relationship.equals("is_subclass_of")){
			answerType = childName;
		}
	}
}
