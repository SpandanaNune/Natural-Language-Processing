import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import reader.RemediaReader;

public abstract class SanitizingReader extends RemediaReader{
	public static void readFile(String path, String inDir, String outDir) throws IOException{
		File inputFile = new File(path);
		readFile(inputFile, inDir, outDir);
	}

	public static void readFile(File inputFile, String inDir, String outDir) throws IOException{
		checkValidFile(inputFile);

		String line, outPath = inputFile.getAbsolutePath().replace(inDir, outDir);
		createOutPath(outPath);
		
		FileReader fReader = new FileReader(inputFile);
		BufferedReader bReader = new BufferedReader(fReader);
		
		File outFile = new File(outPath);
		
		if(outFile.exists()){
			outFile.delete();
		}

		outFile.createNewFile();
		PrintStream writer = new PrintStream(outFile);
		boolean headerFound = false, questionsFound = false;
		
		bReader.readLine();
		while((line = bReader.readLine()) != null){
			line = line.trim().replaceAll(" +", " ").replaceAll("\\.([A-Za-z0-9])", ". $1");
			
			if(line.isEmpty()){
				continue;
			}

			if(!headerFound){
				int startIdx = line.indexOf("("),
						stopIdx = line.indexOf(")") + 1;
				
				
				if(startIdx > -1 && stopIdx > -1){
					String leadInfo = line.substring(startIdx, stopIdx);
					writer.println(leadInfo);
					line = line.substring(stopIdx).replaceFirst("-", "").trim();
					headerFound = true;
				}
			}
			
			if(!questionsFound){
				if(line.startsWith("1.")){
					questionsFound = true;
					writer.println("QUESTION_SECTION:");
				}
			}

			writer.println(line);
		}
		
		writer.close();
		bReader.close();
	}
	
	public static void readFilesInDirectory(String path, String inDir, String outDir) throws IOException{
		File directory = new File(path);
		readFilesInDirectory(directory, inDir, outDir);
	}
	
	public static void readFilesInDirectory(File directory, String inDir, String outDir) throws IOException{
		checkValidDirectory(directory);

		for(String filename : directory.list(new TextFileFilter())){
			String path = String.format("%s/%s", directory.getAbsolutePath(), filename);
			readFile(path, inDir, outDir);
		}
	}
	
	private static void createOutPath(String path){
		File dir = new File(path);
		
		if(!dir.exists()){
			dir.mkdirs();
		}
	}
}
