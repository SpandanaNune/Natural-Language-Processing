package reader;

import java.util.ArrayList;

public class ReaderFileResult {
	private ArrayList<String> questions;
	private String text, filename;
	
	public ReaderFileResult(String filename){
		setQuestions(new ArrayList<String>());
		setText("");
		setFilename(filename);
	}
	
	public void addQuestion(String question){
		this.questions.add(question);
	}
	
	public void addToText(String more){
		this.text += more;
	}

	public ArrayList<String> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<String> questions) {
		this.questions = questions;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
