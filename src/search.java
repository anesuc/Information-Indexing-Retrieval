import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class search {

	public static void main(String[] args) {
		
		String lexiconFile = "";
		String mapFile = "";
		String invlistFile = "";
		
		Map<String, LexiconNode> lexiconMap = new Hashtable<String, LexiconNode>();
		
		Map<Integer, String> map = new Hashtable<Integer, String>();
		
		if (args.length < 4)
			 throw new IllegalArgumentException("Not enough arguments provided");
			
		if (fileExists(args[0]))
			lexiconFile = args[0]; //lexicons file exists
		
		if (fileExists(args[1]))
			invlistFile = args[1]; //inverted invlists file exists
		
		if (fileExists(args[2]))
			mapFile = args[2]; //Map file exists
			
		
		
		loadLexicons(lexiconMap, lexiconFile);
		loadMap(map, mapFile);
		
		String[] searchTerms = new String[args.length - 3];
		for (int i = 3; i < args.length; i++)
			searchTerms[i-3] = args[i];
		
		
		search(searchTerms, lexiconMap, map, invlistFile);

	}
	
	/**  
	    * Performs the search
	    * @param map - contains the mapping information
	    * @param lexicons - contains the lexicons information
	    * @param fileLocation - file location that has the lexicon data
	    */
	public static void search(String[] terms, Map<String, LexiconNode>  lexicons, Map<Integer, String>  map, String invlistFileLocation) {
		
		LexiconNode currentLexicon;
		String listData;
		String[] invlistDataParts;
		
		for (int i = 0; i < terms.length; i++) {
		try (Stream<String> lines = Files.lines(Paths.get(invlistFileLocation))) {
				currentLexicon = lexicons.get(terms[i]);
				
				if (currentLexicon !=  null) { //Make sure we found he search term
					listData = lines.skip(currentLexicon.getPointer()).findFirst().get();
					
					invlistDataParts = listData.split(" ");
					
					/*Print our results for this term*/
					
					System.out.println(terms[i]); //term
					System.out.println(Integer.parseInt(invlistDataParts[0], 2)); //Document Frequency
					
					for (int j = 1; j < invlistDataParts.length; j++) {
						if ( (j & 1) != 0 ) { //if number is Odd then thats our document Id
							String documentId = map.get(Integer.parseInt(invlistDataParts[j],2));
							int counter = Integer.parseInt(invlistDataParts[j+1],2);
						System.out.println(documentId+" "+counter);
						}
					}
				}
				
				
			
		
		}  catch (IOException e) {
			e.printStackTrace();
		 }
		}
		
	}
	
	/**  
	    * Load lexicons from file into memory
	    * @param lexicons - stores the lexicon information
	    * @param fileLocation - file location that has the lexicon data
	    */
	public static void loadLexicons(Map<String, LexiconNode>  lexicons, String fileLocation) {
		
		String term; //Lexicon term
		int pointer; //Lexicon pointer to the inverted list
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileLocation));
			
			while (true) {
				
				String line = br.readLine();
				
				if (line == null) {
					break;
				}
				
				String[] lexiconParts = line.split(" "); //Split lexicon term and inverted list pointer
				
				term = lexiconParts[0];
				pointer = Integer.parseInt(lexiconParts[1]);
			
			
				LexiconNode lexNode = new LexiconNode(term, -1);
				lexNode.setPointer(pointer);
				lexicons.put(term, lexNode);
		
			}
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**  
	    * Load map file into memory
	    * @param map - stores the mapping information
	    * @param fileLocation - file location that has the mapping data
	    */
	public static void loadMap(Map<Integer, String>  map, String fileLocation) {
		
		String docNumber; //Lexicon term
		int docId; //Lexicon pointer to the inverted list
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileLocation));
			
			while (true) {
				
				String line = br.readLine();
				
				if (line == null) {
					break;
				}
				
				//Using this method to split by the first space occurance to avoid additional potential spaces from affecting the processing
				docId = Integer.parseInt(line.substring(0, line.indexOf(' ')));
				docNumber = line.substring(line.indexOf(' ') + 1);
				
				map.put(docId, docNumber);
			}
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**  
	    * Checks if file exists
	    * @param filename - file name/location to check
	    */
	public static Boolean fileExists(String filename) {
		File file = new File(filename);
	    Boolean exists = file.exists();
	    if (file.exists() && file.isFile())
	    {
	    	return true; //Yes the file exists!
	    } else {
	    	throw new IllegalArgumentException("File'"+filename+"' does not exist"); //May as well handle the errors here
	    }
	}

}
