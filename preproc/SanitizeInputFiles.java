import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import reader.SanitizingReader;

public class SanitizeInputFiles {
	private static final String ALL_REMEDIA_PATH = FilenameUtils.separatorsToSystem("/Volumes/Files/gregorymoon/Google Drive/School/2015-2016/Fall 2015/CSE576 - Natural Language Processing/Non-Shared Project/workspace/Remedia-QA/all-remedia/");
	private static final String[] REMEDIA_LEVELS = {"level2/org/", "level3/org/", "level4/org/", "level5/org/"};

	public static void main(String[] args) {
		try {
			for(String level : REMEDIA_LEVELS){
				System.out.println(level);
				SanitizingReader.readFilesInDirectory(ALL_REMEDIA_PATH + level, "all-remedia", "all-remedia-sanitized");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
