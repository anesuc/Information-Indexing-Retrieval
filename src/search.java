import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class search {

	public static void main(String[] args) {
		
		// Get system runtime
		long startTime = System.nanoTime();
		
		// Variable
		String queryLabel = "";
		int numResult = 0;
		String lexiconFile = "";
		String mapFile = "";
		String invlistFile = "";
		String stopList = "";
		String stoplistFilename = "";
		
		// Map & ArrayList 
		String[] searchTerms = null;
		
		Map<String, LexiconNode> lexiconMap = new Hashtable<String, LexiconNode>();
		
		Map<Integer, String> map = new Hashtable<Integer, String>();
		
		Map<Integer, Integer> length = new Hashtable<Integer, Integer>();
		
		ArrayList<Document> documents = new ArrayList<Document>();
		
		ArrayList<Document> replace = new ArrayList<Document>();
		
		ArrayList<Document> sortedDocument = new ArrayList<Document>();

		// Argument
		if (args[0].equals("-BM25")) {
			
			if (args.length < 12)
				throw new IllegalArgumentException("Not enough arguments provided");
			
			if (args[1].equals("-q"))
				queryLabel = args[2];// set queryLabel
			
			if (args[3].equals("-n"))
				numResult = Integer.parseInt(args[4]); // set number of result
			
			if (args[5].equals("-l") && fileExists(args[6]))
				lexiconFile = args[6]; //lexicons file exists
			
			if (args[7].equals("-i") && fileExists(args[8]))
				invlistFile = args[8]; //inverted invlists file exists
			
			if (args[9].equals("-m") && fileExists(args[10]))
				mapFile = args[10]; //Map file exists
				
			if (args[11].equals("-s")){
				if (fileExists(args[12]))
					stoplistFilename = args[12];
				
				searchTerms = new String[args.length - 13];
				
				for (int i = 13; i < args.length; i++)
					searchTerms[i-13] = args[i].toLowerCase();
				
			}else {
				searchTerms = new String[args.length - 11];
				
				for (int i = 11; i < args.length; i++)
					searchTerms[i-11] = args[i].toLowerCase();
			}
			
		}
		//normal search, may delete later
		else {
		if (args.length < 4)
			 throw new IllegalArgumentException("Not enough arguments provided");
			
		if (fileExists(args[0]))
			lexiconFile = args[0]; //lexicons file exists
		
		if (fileExists(args[1]))
			invlistFile = args[1]; //inverted invlists file exists
		
		if (fileExists(args[2]))
			mapFile = args[2]; //Map file exists
		
		
		searchTerms = new String[args.length - 3];
		for (int i = 3; i < args.length; i++)
			searchTerms[i-3] = args[i].toLowerCase();
		}

		loadLexicons(lexiconMap, lexiconFile);

		loadMap(documents, mapFile);
		
		if(args[11].equals("-s"))
		removeStopWords(stoplistFilename, documents);

		search(searchTerms, lexiconMap, documents, invlistFile);
		
		bm25(searchTerms, documents, replace);
		
		heapifyList(replace, sortedDocument);
		
		printResult(queryLabel, numResult, sortedDocument);
		
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime; // runtime calculation
		System.out.println("Running time is: " + totalTime/1000000 + " ms.");
	}
	
	/**  
	    * Performs the search
	    * @param map - contains the mapping information
	    * @param lexicons - contains the lexicons information
	    * @param fileLocation - file location that has the lexicon data
	    */
	public static void search(String[] terms, Map<String, LexiconNode> lexicons, ArrayList<Document> documents,
			String invlistFileLocation) {
		
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
					
					for (int j = 1; j < invlistDataParts.length; j++) {
						if ( (j & 1) != 0 ) { //if number is Odd then thats our document Id
							int counter = Integer.parseInt(invlistDataParts[j+1]);
							documents.get(j).setTermFreq(terms[i], counter);
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
				
				if (line == null) 
					break;
				
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
	    * @param documents - stores the mapping information
	    * @param fileLocation - file location that has the mapping data
	    */
	public static void loadMap(ArrayList<Document> documents, String fileLocation) {
		
		String docNumber; //Lexicon term
		int docId; //Lexicon pointer to the inverted list
		int docLength; //Document length
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileLocation));
			
			while (true) {
				
				String line = br.readLine();
				
				if (line == null) 
					break;
				
				//Using this method to split by the first space occurance to avoid additional potential spaces from affecting the processing
				String[] parts = line.split(" ");
	
				docId = Integer.parseInt(parts[0]);//set document ID
				docNumber = parts[1]; // set document name
				docLength = Integer.parseInt(parts[2]); // set document length
				
				Document document = new Document(docNumber, docId, docLength);
				documents.add(document); 
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
	    if (file.exists() && file.isFile()){
	    	return true; //Yes the file exists!
	    } else {
	    	throw new IllegalArgumentException("File'"+filename+"' does not exist"); //May as well handle the errors here
	    }
	}

	/**  
	    * BM25 function
	    * @param searchTerms - the string contain the query terms
	    * @param documents - store the documents information, such as the document ID, Name, termFrequency, score and etc.
	    * @param replace - store the documents which have valid score
	    */
	public static void bm25(String[] searchTerms, ArrayList<Document> documents, ArrayList<Document> replace){
		
		final double K1 = 1.2;
		final double B = 0.75;
		
		double N = documents.size();//number of documents in the collection, N
		if (N <= 0) {
			return;
		}
		double L = 0; // Length of all of the documents, L
		for (Document document : documents) {
			L += document.getLength();
		}
		double AL = L / N;//average document length, AL
		
		for (String searchTerm : searchTerms) {
			
			ArrayList<Document> occurList = new ArrayList<Document>();
			
			// put all documents which contain the search terms
			for (Document document: documents) {
				if (document.getTermFreq(searchTerm) <= 0) {
					continue;
				}
				occurList.add(document);
			}
			
			double Ft = occurList.size();//number of documents containing term t, Ft
			
			for (Document occurDocument : occurList) {
				double Fdt = occurDocument.getTermFreq(searchTerm);//number of occurrences of t in d, Fdt			
				double LD = occurDocument.getLength();//document length, LD
				
				// BM25 calculation
				double K = K1 * ( (1 - B) + (B * LD)/AL);
				double weight = (N - Ft + 0.5) / (Ft + 0.5);
				double score = (java.lang.Math.log(weight)) *( (K1 + 1) * Fdt / (K + Fdt) );
				score = Math.round (score * 10000d) /10000d; 
				occurDocument.setScore(score);
			}
		}
		
		// put scored documents to replace after scoring 
		for (Document document : documents) {
			if (document.getScore() > 0) {
				replace.add(document);
			}
		}
	}
	
	/**
	 * 
	 * @param documents - the documents which need to heapify
	 * @param i - parent position 
	 */
	
	public static void heapify(ArrayList<Document> documents, int i){

		// Since the min-heap sort is always start from 1, but in ArrayList is start from 0
		// manually set i&childPos offset to satify min-heap sort need
		int childPos = 2 * i - 1;
		i--;
		int size = documents.size() - 1;
		
		if (size != 0){
		while (childPos <= size) {
			if (childPos < size)  {
				double leftScore = documents.get(childPos).getScore();
				double rightScore = documents.get(childPos + 1).getScore();
				if (leftScore > rightScore) {
					childPos++;
				}
			}
			
			double parentScore = documents.get(i).getScore();
			double childScore = documents.get(childPos).getScore();

			if (parentScore <= childScore) {
				break;
			} else {
				Document temp = documents.get(i);
				documents.set(i, documents.get(childPos));
				documents.set(childPos, temp);
				i = childPos;
				childPos = 2 * i;
			}
		}
		}
	}
	
	/**
	 * 
	 * @param replace - store the documents which have valid score
	 * @param sortedDocument - store the sorted documents after heapify
	 */
	public static void heapifyList(ArrayList<Document> replace, ArrayList<Document> sortedDocument) {
		
		for (int i = replace.size() ; i >= 1 ; i--) {
			for (int j = (int) Math.floor(i / 2); j >= 1; j--){
			heapify(replace, j);
			}
			sortedDocument.add(replace.get(0));// put smallest value into sortedDocument
			replace.set(0, replace.get(replace.size()-1));// set the last position child value to the first place
			replace.remove(replace.size()-1); //remove the last position child
		}
		
		Collections.reverse(sortedDocument);// reverse the list and make it listed from large to small
		
	}
	
	/**  
	    * Removes stop words from the list ( in memory)
	    * @param stoplistFilename - File name/location that contains the stop list
	    * @param docList - the document list with all terms currently loaded
	    * etc
	    */
	public static void removeStopWords(String stoplistFilename, ArrayList<Document> documents) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(stoplistFilename));
			ArrayList<String> stopList = new ArrayList<String>();
			
			while(true) {
				String line = br.readLine();
				
				if (line == null){
					break;
				}
				stopList.add(line);
			}
			
			// compare index and stoplist, delete same word
			for (Document doc: documents){
				for (String stopWord : stopList){
					doc.removeKey(stopWord);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param queryLabel - the label of the query terms
	 * @param numResult - the number of results for printing
	 * @param sortedDocument - store the sorted documents after heapify
	 */
	public static void printResult(String queryLabel, int numResult, ArrayList<Document> sortedDocument) {
		
		if (sortedDocument.size() > 0){
			// check the scored document number is enough for the required number or not
			if (sortedDocument.size() >= (numResult - 1)){
				for (int i = 0; i <= numResult-1; i++)
					System.out.println(queryLabel + " " + sortedDocument.get(i).getDocNum() + " " + (i + 1) + " " + sortedDocument.get(i).getScore());
			}
			else {
				for (int i = 0; i <= sortedDocument.size() -1; i++)
					System.out.println(queryLabel + " " + sortedDocument.get(i).getDocNum() + " " + (i + 1) + " " + sortedDocument.get(i).getScore());
			}
		}
		else {
			System.out.println("No result!");
		}
		
	}
}
