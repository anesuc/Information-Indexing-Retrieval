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
		int queryLabel = 0;
		int numResult = 0;
		String lexiconFile = "";
		String mapFile = "";
		String invlistFile = "";
		String stopList = "";
		String stoplistFilename = "";
		String latimesDocumentFile = "";
		int summaryType = 0;
		
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
			
			if (args.length < 16)
				throw new IllegalArgumentException("Not enough arguments provided");
			
			if (args[1].equals("-q"))
				queryLabel = Integer.parseInt(args[2]);// set queryLabel
			
			if (args[3].equals("-n"))
				numResult = Integer.parseInt(args[4]); // set number of result
			
			if (args[5].equals("-l") && fileExists(args[6]))
				lexiconFile = args[6]; //lexicons file exists
			
			if (args[7].equals("-i") && fileExists(args[8]))
				invlistFile = args[8]; //inverted invlists file exists
			
			if (args[9].equals("-m") && fileExists(args[10]))
				mapFile = args[10]; //Map file exists
				
			if (args[11].equals("-d") && fileExists(args[12]))
				latimesDocumentFile = args[12]; //Latimes file exists
			
			if (args[13].equals("-t"))
				summaryType = Integer.parseInt(args[14]);//Type of summary
				
			//check if stoplist apply
			if (args[15].equals("-s")){
				
				if (fileExists(args[16]))
					stoplistFilename = args[16];
				
				searchTerms = new String[args.length - 17];
				
				for (int i = 17; i < args.length; i++)
					searchTerms[i-17] = args[i].toLowerCase();
				
			}else {
				searchTerms = new String[args.length - 15];
				
				for (int i = 15; i < args.length; i++)
					searchTerms[i-15] = args[i].toLowerCase();
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
		
		search(searchTerms, lexiconMap, documents, invlistFile);
		
		if(args[15].equals("-s"))
			removeStopWords(stoplistFilename, documents);
		
		bm25(searchTerms, documents, replace);
		
		heapifyList(numResult, replace, sortedDocument);
		
		if(args[15].equals("-s")) {
			String[] noStopWordsSearchTerms = removeStopWordsFromSearchTerms(stoplistFilename, searchTerms);
			addDocumentSummary(noStopWordsSearchTerms, sortedDocument,latimesDocumentFile, summaryType);
		} else {
			addDocumentSummary(searchTerms, sortedDocument,latimesDocumentFile, summaryType);
		}
		
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
							documents.get(Integer.parseInt(invlistDataParts[j])).setTermFreq(terms[i], counter);
								
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
		int docPositionLocation; //stores location within a string that has document position information
		
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
				docPositionLocation = Integer.parseInt(parts[3]); // set document length
				
				Document document = new Document(docNumber, docId, docLength, docPositionLocation);
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
	 * @param numResult - the number of results for printing
	 * @param replace - store the documents which have valid score
	 * @param sortedDocument - store the sorted documents after heapify
	 */
	public static void heapifyList(int numResult, ArrayList<Document> replace, ArrayList<Document> sortedDocument) {
		
		ArrayList<Document> heapify = new ArrayList<Document>();// a new list used for heapify only
		
		//put needed number of documents to list
		//for example, if it need to print 100 results, firstly we put first 100 documents in this arraylist and heapify.
		if (numResult < replace.size()){ //checking the size
		for (int i = 0; i < numResult; i++){
			heapify.add(replace.get(i));
		}
		}else{
			for (int i = 0; i < replace.size(); i++){
				heapify.add(replace.get(i));
			}
		}
		//heapify before next step
		for (int j = (int) Math.floor(numResult/2); j >=1; j--){
		heapify(heapify, numResult);
		}

		//secondly, compare all other documents score to the first elements in the list, which always has the smallest value.
		for (int i = numResult ; i <replace.size() ; i++){
			
			int retval = Double.compare(replace.get(i).getScore(), heapify.get(0).getScore());//comparing two double values
			
			//if retval greater than 0, which means this document score is greater than the first elements in the list.
			//replace the first elements with this score, then heapify the list for the new smallest element(in position 0).
			if (retval > 0){
				heapify.set(0, replace.get(i));
				for (int j = (int) Math.floor(numResult / 2); j >= 1; j--){
					heapify(heapify, j);
					}
			
			//if retval is not greater than 0, the for loop continue for next larger value
			}else{
				continue;
			}
		}

		//in the end, put all value to sortedDocument list from small to large, then reverse
		for (int i = heapify.size(); i >= 1; i--){
			for (int j = (int) Math.floor(i / 2); j >= 1; j--){
				heapify(heapify, j);
				}
			sortedDocument.add(heapify.get(0));// put smallest value into sortedDocument
			heapify.set(0, heapify.get(heapify.size()-1));// set the last position child value to the first place
			heapify.remove(heapify.size()-1);//remove the last position child
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
					doc.removeTermFreq(stopWord);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**  
	    * Removes stop words from the search terms ( in memory)
	    * @param stoplistFilename - File name/location that contains the stop list
	    * @param searchTerms - A simple array of search terms from the user
	    * @return searchTerms - a simple array of search terms without stop words
	    */
	public static String[] removeStopWordsFromSearchTerms(String stoplistFilename, String[] searchTerms) {
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
			
			List<String> tempSearchTerms = new ArrayList<String>();;
			
			// compare search term and stoplist, don't add same word
			for (int i = 0; i < searchTerms.length; i++){
				Boolean found = false;
				for (String stopWord : stopList){
					if(stopWord.equals(searchTerms[i]))
						found = true;
				}
				if (!found)
					tempSearchTerms.add(searchTerms[i]);
			}
			
			String[] finalSearchTerms = new String[ tempSearchTerms.size() ];
			tempSearchTerms.toArray( finalSearchTerms );
			
			return finalSearchTerms;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return searchTerms;
	}
	

	
	
	public static void addDocumentSummary(String[] searchTerms, ArrayList<Document> sortedDocument,String sourcefile, int summaryType) {
		// Making variables mutable so that I can read them inside the forEach
		String[] summaryText = new String[1];
		String finalSummaryText = "";
		Boolean[] processThisDocument = new Boolean[1];
		Integer[] linesProcessed = new Integer[1];
		Integer[] documentPosition = new Integer[1];
		String[] documentNo = new String[1];
		
		//Boolean[] neverProcess = new Boolean[1];
		//String documentNo;
		//SortedMap<Integer, String> termLocations;
		List<Integer>  termLocations = new ArrayList<Integer>();
		//String line;
		//String term = searchTerms;
		
		if (summaryType == 1) { // Just get first few words
			
		}
		
		Integer[] setSummaryType =  {summaryType};
		//setSummaryType[0] = summaryType;
		
		if (sortedDocument.size() > 0){
				for (int i = 0; i <= sortedDocument.size() -1; i++) {
					summaryText[0] = "";
					processThisDocument[0] = false;
					linesProcessed[0] = 0;
					documentPosition[0] = sortedDocument.get(i).getDocumentPosition();
					documentNo[0] = sortedDocument.get(i).getDocNum();
					termLocations.clear();
					//System.out.println("documentPosition: "+documentPosition[0]);
					
					try (Stream<String> lines = Files.lines(Paths.get(sourcefile))) {
							boolean[] openHeadlineTag = {false};
							boolean[] openPTag = {false};
							boolean[] openTextTag = {false};
							Integer[] currentLine = {documentPosition[0]};
							
							boolean[] doNotProcess = {false};
							
						lines.skip(documentPosition[0]).forEach((line) -> {
						
						String openHeadline = "<HEADLINE>";
						String closeHeadline = "</HEADLINE>";
						
						
						String openP = "<P>";
						String closeP = "</P>";
						
						
						String openText = "<TEXT>";
						String closeText = "</TEXT>";
						
						String closeDoc = "</DOC>";
						
						/*if (currentLine[0] == documentPosition[0])
							System.out.println("First ever line: "+line);*/
						
						//while (true) {
							//String line = br.readLine();
							
							if (line == null) {
								//doNotProcess[0] = true;
								//return; //throw new Exception();
							}
							
							if (line.equals(closeDoc) && processThisDocument[0]) {
								doNotProcess[0] = true;
								//System.out.println("Close: "+line);
								//return;//throw new Exception();
							}
							
							// add Document to ArrayList
							Pattern pattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
							Matcher m = pattern.matcher(line);
							if(m.find()){
								if (m.group(1).replaceAll(" ", "").equals(documentNo[0]))
									processThisDocument[0] = true;
							}
							
							// check head tag
							if (line.equals(closeHeadline)) {
								openHeadlineTag[0] = false;
							}
							
							if (openHeadlineTag[0]) {
								if (!line.equals(openP) && !line.equals(closeP)) {
									
								
								}
							}
							
							if (line.equals(openHeadline)) {
								openHeadlineTag[0] = true;
							}
							
							// check text tag
							if (line.equals(closeText)) {
								openTextTag[0] = false;
							}
							
							if (openTextTag[0]) {
								if (!line.equals(openP) && !line.equals(closeP)) {
									if (setSummaryType[0] == 1 && !doNotProcess[0]) {
									if (processThisDocument[0]) {
										if (linesProcessed[0] < 5) {
											summaryText[0] += line+"\n";
											linesProcessed[0]++;
										} else {
											doNotProcess[0] = true;//break; //We already got the lines we need
										}
										
									}
									} else {
										if (!doNotProcess[0]) {
											//System.out.println("Gets here: "+line);
										for (int j = 0; j < searchTerms.length; j++) {
											int wordLocation = line.toLowerCase().indexOf(searchTerms[j]);
											
											// Also check the rest of the sentence incase it occurs more than once in the sentence
											/*while (wordLocation >= 0) {
												termLocations.add(linesProcessed[0]-1);
												wordLocation = summaryText[0].toLowerCase().indexOf(searchTerms[j], wordLocation+1);
											}*/
											
											if (wordLocation != -1)
												termLocations.add(linesProcessed[0]);
											
										}
										summaryText[0] += line+"\n";
										linesProcessed[0]++;
										}
									}
									
								}
							}
							if (line.equals(openText)) {
								openTextTag[0] = true;
							}
							
							currentLine[0]++;
							
							//line = lines.skip(currentLine).findFirst().get();
							
						//}
						});
					

					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (setSummaryType[0] == 1) {
						
						for (int m = 0; m < searchTerms.length; m++) {
							String term = searchTerms[m];
							int wordLocation = summaryText[0].toLowerCase().indexOf(term);
							//System.out.println("Index of term: "+wordLocation);
							while (wordLocation >= 0) {
								StringBuilder input = new StringBuilder(summaryText[0]);
								input.insert(wordLocation+term.length(), "\033[0;0m"); // Start with the end of the word;
								input.insert(wordLocation, "\033[0;1m"); // End with with start of the word
								//input.insert(wordLocation+term.length(), "hi");
								summaryText[0] = input.toString();
								int jumpTo = wordLocation+("\033[0;0m"+term+"\033[0;0m").length(); // Take into account the new chars
								wordLocation = summaryText[0].toLowerCase().indexOf(term, jumpTo+1);
							}
						}
						
						/*String term = searchTerms[0];
					int wordLocation = summaryText[0].toLowerCase().indexOf(term);
					//System.out.println("Index of term: "+summaryText[0].length());
					if (wordLocation != -1) {
						StringBuilder input = new StringBuilder(summaryText[0]);
						input.insert(wordLocation+term.length(), "\033[0;0m"); // Start with the end of the word;
						input.insert(wordLocation, "\033[0;1m"); // End with with start of the word
						//input.insert(wordLocation+term.length(), "hi");
						summaryText[0] = input.toString();
					}*/
					
					sortedDocument.get(i).addSummary(summaryText[0]);
					} else {
						
						
						Collections.sort(termLocations);
						
						int startingLine = 0;
						
						if (termLocations.size() > 0)
							startingLine = termLocations.get(0);
						else {
							System.out.println("\033[0;1m Summary based on context was not successful due to the word(s) of interest only occuring in the header or equivalent in document: : "+documentNo[0]+"\033[0;0m \n");
						}
						
						
						for (int k = 0; k < termLocations.size(); k++) {
							
							/* Checking the difference of 4 because we want a max of 5 sentences. And as seen
							 * later in the code we usually start from the line before the first occurance of a term
							 * so that we give the user enough space for context.
							 */
							if ((k+1) < termLocations.size())
								if ((termLocations.get(k+1) - termLocations.get(k)) < 4) {
									startingLine = termLocations.get(k);
									break;
									/* Breaking as early as possible because we want the earliest match.
									 * For example if 2 of the search terms appear in the first 2 lines of the
									 * document, then it's better to show those than later on in the document
									 */
								}
						}
						
						
					
						
						String[] allLines = summaryText[0].split("(?<=\r\n|\r|\n)"); // Split and Keep the delimiters for paragraphs
						
						summaryText[0] = "";
						
						//System.out.println("allLines 0: "+allLines[0]);
						
						//System.out.println("lines processed: "+linesProcessed[0]);
						//System.out.println("lines length: "+allLines.length);
						//System.out.println("lines 0: "+allLines.length);
						
						/* Check if the location we are starting the summary is near the end of the document.
						 * If so, then we want to go back 4 sentences so that the summary has a bit of context.
						 */
						//System.out.println(" allLines: "+ allLines.length);
						//System.out.println(" goBackTo: "+ goBackTo);
						int goBackTo = allLines.length - 4;
						int startingLineUpperPosition = startingLine-4;
						
						if (startingLine > goBackTo && goBackTo > 0) {
							for (int k = goBackTo; k < allLines.length; k++) {
								summaryText[0] += allLines[k];
								if ((k - startingLineUpperPosition) == 5)
									break; //line limit for summary
							}
						} else {
							for (int k = startingLine; k < allLines.length; k++) {
								
								if (k == startingLine && (k-1)>=0)
									summaryText[0] += allLines[k-1]; // First line, add a bit of context before it
								
								summaryText[0] += allLines[k];
								if ((k - startingLine) == 4)
									break; //line limit for summary
							}
						}
						
						
						//System.out.println("summaryText 0: "+summaryText[0]);
						
						
						for (int m = 0; m < searchTerms.length; m++) {
							String term = searchTerms[m];
							int wordLocation = summaryText[0].toLowerCase().indexOf(term);
							while (wordLocation >= 0) {
								StringBuilder input = new StringBuilder(summaryText[0]);
								input.insert(wordLocation+term.length(), "\033[0;0m"); // Start with the end of the word;
								input.insert(wordLocation, "\033[0;1m"); // End with with start of the word
								summaryText[0] = input.toString();
								int jumpTo = wordLocation+("\033[0;0m"+term+"\033[0;1m").length(); // Take into account the new chars
								wordLocation = summaryText[0].toLowerCase().indexOf(term, jumpTo+1);
							}
						}
						
							
						
						sortedDocument.get(i).addSummary("..."+summaryText[0]);
					}
					
					
				}
			
		}
		

		
		
	}
	
	
	
	/**
	 * 
	 * @param queryLabel - the label of the query terms
	 * @param numResult - the number of results for printing
	 * @param sortedDocument - store the sorted documents after heapify
	 */
	public static void printResult(int queryLabel, int numResult, ArrayList<Document> sortedDocument) {
		
		DecimalFormat df = new DecimalFormat("#.0000");
		
		if (sortedDocument.size() > 0){
			// check the scored document number is enough for the required number or not
			if (sortedDocument.size() >= (numResult - 1)){
				for (int i = 0; i <= numResult-1; i++) {
					System.out.println(queryLabel + " " + sortedDocument.get(i).getDocNum() + " " + (i + 1) + " " + df.format(sortedDocument.get(i).getScore()));
					System.out.println(sortedDocument.get(i).getSummary());
				}
			}
				
			else {
				for (int i = 0; i <= sortedDocument.size() -1; i++) {
					System.out.println(queryLabel + " " + sortedDocument.get(i).getDocNum() + " " + (i + 1) + " " + df.format(sortedDocument.get(i).getScore()));
					System.out.println(sortedDocument.get(i).getSummary());
				}	
			}
		}
		else {
			System.out.println("No result!");
		}
		
	}
}
