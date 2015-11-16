import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import graph.GlobalGraph;
import graph.QuestionGraph;

public class QAPreProc {
	private static final int MAX_LEVEL = 5;
	private static final String OUT_DIR = "all-remedia-processed-pronoun-replace",
			IN_DIR = "all-remedia";
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		String path;
		File dir;
		File[] dirListing;
		for(int i = 2; i < MAX_LEVEL + 1; i++){
			path = String.format("%s/level%d/org/", IN_DIR, i);

			dir = new File(path);
			dirListing = dir.listFiles();
			
			for(int j = 0; j < dirListing.length; j++){
				File file = dirListing[j],
						outFile = new File(String.format("%s/level%d/%s", OUT_DIR, i, file.getName().replace(".txt", ".ser")));
				if(getExtension(file.getName()).equals("txt") && !outFile.exists()){
					System.out.println(String.format("\n\nCURRENT LEVEL: %d\nCURRENT FILE INDEX: %d\nCURRENT FILE: %s\n\n", i, (j-2), file.getAbsolutePath()));
					
					try{
						process(file, i);
					}
					catch(StackOverflowError e){
						System.out.println(String.format("\n\nERROR PROCESSING FILE %s\n\n", file.getAbsolutePath()));
						Path p = FileSystems.getDefault().getPath(outFile.getAbsolutePath());
						Files.delete(p);
					}
				}

			}
		}
	}
	
	private static void process(File file, int level) throws IOException, ClassNotFoundException{
		BufferedReader in = new BufferedReader(new FileReader(file.getAbsolutePath()));
		Iterator<String> lines = in.lines().iterator();
		boolean questions = false;
		ArrayList<String> questionList = new ArrayList<String>();
		String line, text = "";
		char currChar;
		int lineIdx, lineNum = 0;

		while(lines.hasNext()){
			if(lineNum < 2){
				lineNum++;
				lines.next();
				continue;
			}

			lineIdx = 0;
			line = lines.next();
			line = line.replace("\t", "");
			
			if(line.length() > 2 && line.charAt(0) == '1' && line.charAt(1) == '.'){
				questions = true;
			}
			
			if(questions && line.length() > 2){
				currChar = line.charAt(0);
				
				while(currChar != '.'){
					lineIdx++;
					currChar = line.charAt(lineIdx);
				}
				
				questionList.add(line.substring(lineIdx + 1).trim());
			}
			else{
				if(lineNum == 4){
					String[] parts = line.split("\\)", 2);
					String part2;
					
					text += " " + parts[0].trim() + "). ";
					part2 = parts[1].trim();
					
					if(part2.substring(0, 2).equals("- ")){
						part2 = part2.substring(2);
					}
					
					text += part2;

				}
				else{
					text += line;
				}
				
				lineNum++;
			}
		}
		
		in.close();
		
		parseAndWrite(text, questionList, file.getName(), level);
	}
	
	private static void test(File path, GlobalGraph properGGraph, QuestionGraph[] properQGraphs) throws IOException, ClassNotFoundException{
		Parser.readSetFromFile(path);
		boolean match = true;
		
		GlobalGraph gGraph = Parser.getGlobalGraph();
		QuestionGraph[] qGraphs = Parser.getQuestionGraphs();
		
		System.out.println("\ngGraph deserialize: " + (gGraph.toString().equals(properGGraph.toString())));
		
		for(int i = 0; i < properQGraphs.length; i++){
			if(!properQGraphs[i].toString().equals(qGraphs[i].toString())){
				match = false;
				break;
			}
		}

		System.out.println("qGraphs deserialize: " + match);
	}
	
	private static void parseAndWrite(String text, ArrayList<String> questionList, String filename, int level) throws IOException, ClassNotFoundException{
		filename = filename.replace(".txt", ".ser");
		String path = String.format("%s/level%d/%s", OUT_DIR, level, filename);
		
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		GlobalGraph gGraph = Parser.parseText(text);
		QuestionGraph[] qGraphs = new QuestionGraph[questionList.size()];
		
		oos.writeObject(gGraph);
		
		for(int i = 0; i < questionList.size(); i++){
			qGraphs[i] = Parser.parseQuestion(questionList.get(i));
		}
		
		oos.writeObject(qGraphs);
			
		oos.close();
		
		test(new File(path), gGraph, qGraphs);
	}
	
	private static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
 
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
}
