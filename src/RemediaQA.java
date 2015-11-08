import java.io.FileNotFoundException;

import graph.GlobalGraph;
import graph.QuestionGraph;

public class RemediaQA {
	public static void main(String[] args) throws FileNotFoundException{
		String testParagraph = "(ENGLAND, June, 1989) - Christopher Robin is alive and well.  He lives in England.  He is the same person that you read about in the book, Winnie the Pooh. "
				+ "As a boy, Chris lived in a pretty home called Cotchfield Farm.  When Chris was three years old, his father wrote a poem about him.  The poem was printed in a magazine for others to read."
				+ " Mr. Robin then wrote a book.  He made up a fairy tale land where Chris lived.  His friends were animals.  There was a bear called Winnie the Pooh.  There was also an owl and a young pig, called a piglet.  All the animals were stuffed toys that Chris owned.  Mr. Robin made them come to life with his words.  The places in the story were all near Cotchfield Farm. "
				+ "Winnie the Pooh was written in 1925.  Children still love to read about Christopher Robin and his animal friends.  Most people don't know he is a real person who is grown now.  He has written two books of his own.  They tell what it is like to be famous.";
		
		testParagraph = "(ABERDEEN, S.D., September 14, 1963) - Mr. and Mrs. Fischer became parents today.  They have a healthy, new baby boy ... and a boy ... and a boy ... and a boy ... and a girl.  They had five babies!  The babies are called quints, which means five."
				+ " Quints are quite rare.  They happen only once in every 85 million births.  The Fischer quints are the first ones born in the United States that have lived.  Those born in the past lived only hours or days.  Most quints are so tiny that their hearts and lungs are not big enough to keep them alive."
				+ " The Fischers are surprised to have so many babies.  They need some time to think about names for their children.  Many people are sending them clothes and toys."
				+ " There is another famous set of quints in Canada.  Five baby girls were born May 28, 1934, to the Dionne family.  Their names are Annette, Cecile, Emilie, Marie, and Yvonne."
				+ " When four babies are born at once, they are called quads.  Three makes triplets, and two are twins.";
		
		/*testParagraph = "(JOPLIN, MISSOURI, October 26, 1861) From now on, mail will be sent a new, faster way.  It is called the telegraph.  It uses wires to send messages.  Now there will be no need for the Pony Express.  Since April, 1860, mail has been sent this way."
				+ " The last Pony Express rider leaves town today.  His horse will carry sacks full of mail.  He will ride for 15 miles.  Then the rider will stop at a swing station.  There he will get a fresh horse.  The rider will also have a hot meal there."
				+ " Like most Pony Express riders, he will ride about 75 miles today.  He will make about five stops."
				+ " Last year, the Pony Express was a new way to deliver the mail.  Brave, young men were hired to ride the horses.  Along the way, they often met thieves.  Sometimes their horses got hurt."
				+ " Before the Pony Express, mail was sent by stagecoach.  The Pony Express was 12 to 14 days faster than the coach.  Now, there is another newer, faster way to send mail.";

		testParagraph = "When Chris was three years old, his father wrote a poem about him.";
		testParagraph = "Winnie the Pooh was written in 1925.";
		
		testParagraph = "(SALEM, MASSACHUSETTS, 1899) - The merry-go-round is 100 years old this year!  No other park ride has lasted so long."
				+ " The first merry-go-round in the United States was built in 1799.  It was built in a park in Salem. "
				+ " A merry-go-round has wooden animals on it.  The most favorite are the horses. They are attached to poles.  They can move up and down.  The animals are on a platform.  It turns in a circle.  The merry-go-round spins to the sound of music."
				+ " In time, the weather damages the animals.  They lose their bright colors."
				+ " Then, workers must fix the animals.  They sand away all the old paint.  Then they patch the broken parts.  The next step is to paint the animals white.  After this, bright colors of paint are added.  Then the animals are carefully put back in place."
				+ " Another name for a merry-go-round is \"carousel\" (CAR-uh-sel).  Call it what you like.  By any name, it's great fun!";
		*/
		String testQuestion = "When was Winnie the Pooh written?";
		testQuestion = "Who became parents of quints on September 14?";
		//testQuestion = "When did the Pony Express start?";
		/*
		testQuestion =  "What did Mr. Robin do when Chris was three years old?";
		testQuestion = "Who fixes the merry-go-round?";
		*/
		QuestionGraph questionGraph = Parser.parseQuestion(testQuestion);
		GlobalGraph paragraphGraph = Parser.parseText(testParagraph);
		
		System.out.println(String.format("Graph for Test Question:\n\n%s", questionGraph));
		System.out.println(String.format("Graph for Test Paragraph:\n\n%s", paragraphGraph));
		
		QA qa = new QA(questionGraph, paragraphGraph);
		System.out.println("RANKED SENTENCES: \n");
		qa.findAnswer();
	}
}
