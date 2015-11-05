import java.io.FileNotFoundException;

public class RemediaQA {
	public static void main(String[] args) throws FileNotFoundException{
		String testParagraph = "(ENGLAND, June, 1989) - Christopher Robin is alive and well.  He lives in England.  He is the same person that you read about in the book, Winnie the Pooh. "
				+ "As a boy, Chris lived in a pretty home called Cotchfield Farm.  When Chris was three years old, his father wrote a poem about him.  The poem was printed in a magazine for others to read."
				+ " Mr. Robin then wrote a book.  He made up a fairy tale land where Chris lived.  His friends were animals.  There was a bear called Winnie the Pooh.  There was also an owl and a young pig, called a piglet.  All the animals were stuffed toys that Chris owned.  Mr. Robin made them come to life with his words.  The places in the story were all near Cotchfield Farm. "
				+ "Winnie the Pooh was written in 1925.  Children still love to read about Christopher Robin and his animal friends.  Most people don't know he is a real person who is grown now.  He has written two books of his own.  They tell what it is like to be famous.";
		
		String testQuestion = "When was Winnie the Pooh written?";
		
		QuestionGraph questionGraph = Parser.parseQuestion(testQuestion);
		GlobalGraph paragraphGraph = Parser.parseText(testParagraph);
		
		System.out.println(String.format("Graph for Test Question:\n\n%s", questionGraph));
		System.out.println(String.format("Graph for Test Paragraph:\n\n%s", paragraphGraph));
	}
}
