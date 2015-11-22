package reader;

import java.util.HashMap;

public class ReaderDirectoryResult {
	public HashMap<String, ReaderFileResult> fileResults;
	
	public ReaderDirectoryResult(){
		fileResults = new HashMap<String, ReaderFileResult>();
	}
}
