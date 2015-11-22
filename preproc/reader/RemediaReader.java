package reader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

public abstract class RemediaReader {
	private static final char[] SENTENCE_DELIMS = {'.', '!', '?'};

	public static ReaderFileResult readFile(String path) throws IOException{
		File inputFile = new File(path);
		return readFile(inputFile);
	}
	
	public static ReaderFileResult readFile(File inputFile) throws IOException{
		checkValidFile(inputFile);

		System.out.println("Reading " + inputFile.getAbsolutePath());
		ReaderFileResult result = new ReaderFileResult(inputFile.getName());
		FileReader fReader = new FileReader(inputFile);
		BufferedReader bReader = new BufferedReader(fReader);
		String line;
		boolean questions = false;
		
		if(inputFile.getAbsolutePath().contains("rm4-30")){
			System.out.println("HERE");
		}
		
		while((line = bReader.readLine()) != null){
			line = line.replace("\n", "");

			if(line.equals("QUESTION_SECTION:")){
				questions = true;
				continue;
			}
			
			if(questions){
				String question = line.substring(2).trim();
				result.addQuestion(question);
			}
			else{
				char last = line.charAt(line.length() - 1);
				
				if(last != ' '){
					if(!isSentenceDelim(last)){
						line += '.';
					}

					line += ' ';
				}

				result.setText(result.getText() + line);
			}
		}
		
		
		bReader.close();
		
		return result;
	}
	
	public static ReaderDirectoryResult readFilesInDirectory(String path) throws IOException{
		File directory = new File(path);
		return readFilesInDirectory(directory);
	}
	
	public static ReaderDirectoryResult readFilesInDirectory(File directory) throws IOException{
		checkValidDirectory(directory);
		
		ReaderDirectoryResult result = new ReaderDirectoryResult();

		for(String filename : directory.list(new TextFileFilter())){
			String path = String.format("%s/%s", directory.getAbsolutePath(), filename);
			
			if(!result.fileResults.containsKey(path)){
				result.fileResults.put(path, readFile(path));
			}
			else{
				throw new ArrayStoreException("Entry in fileResults already present for key '" + path + "'.");
			}
		}
		
		return result;
	}
	
	protected static void checkValidDirectory(File inputDirectory) throws FileNotFoundException{
		checkExists(inputDirectory);

		if(!inputDirectory.isDirectory()){
			throw new IllegalArgumentException("inputFile must be a directory.");
		}
	}
	
	protected static void checkValidFile(File inputFile) throws FileNotFoundException{
		checkExists(inputFile);

		if(inputFile.isDirectory()){
			throw new IllegalArgumentException("inputFile cannot be a directory.");
		}
		
		String extension = FilenameUtils.getExtension(inputFile.getAbsolutePath());
		if(!extension.equals("txt")){
			throw new IllegalArgumentException("inputFile path must be '.txt.'");
		}
	}
	
	private static boolean isSentenceDelim(char c){
		for(char o : SENTENCE_DELIMS){
			if(c == o){
				return true;
			}
		}
		
		return false;
	}
	
	private static void checkExists(File input) throws FileNotFoundException{
		if(!input.exists()){
			throw new FileNotFoundException(input.getAbsolutePath());
		}
	}
	
	protected static class TextFileFilter implements FilenameFilter{
		public TextFileFilter() { }

		@Override
		public boolean accept(File dir, String name) {
			String ext = FilenameUtils.getExtension(name);
			return ext.equals("txt");
		}
		
	}
}
