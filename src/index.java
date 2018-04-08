import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class index {
	
	
	public static void main(String[] args) {
		
		String sourcefile = "";
		String stoplist = "";
		Boolean verbose = false; //Print the list?
		
		if (args.length < 1)
			 throw new IllegalArgumentException("No source file detected. Please provide an source file.");
		else if (args.length == 1) { //Only the source file was provided
			File file = new File(args[0]);
		    Boolean exists = file.exists();
			if (file.exists() && file.isFile())
		    {
		    	sourcefile = args[0];
		    } else {
		    	throw new IllegalArgumentException("Source file'"+args[0]+"' does not exist");
		    }
		}
		
		int stopfilePosition = -1;
		for (int i = 0; i < args.length; i++) {
			
			if (args[i].equals("-s") && stoplist.equals("")) {
				stoplist = args[i+1];
				stopfilePosition = i;
			} else if (args[i].equals("-p")) {
				verbose = true;
			} else if (i != stopfilePosition && sourcefile.equals("")) {
				File file = new File(args[i]);
			    Boolean exists = file.exists();
			    if (file.exists() && file.isFile())
			    {
			    	sourcefile = args[i];
			    } else {
			    	throw new IllegalArgumentException("Source file'"+args[i]+"' does not exist");
			    }
			}
	        
		}
		
		if(sourcefile.equals(""))
			throw new IllegalArgumentException("Could not determine the source file from the arguments");
		
		
		
		// create Document ArrayList
		ArrayList<Document> docList = new ArrayList<Document>();

		Map<String, LexiconNode> lexicon = new TreeMap<String, LexiconNode>();  //lexicons list
		
		List<LexiconNode> allLexiconValues = new ArrayList<LexiconNode>();

		//Need commend line args.
		
		// 1.1 Parsing
		mapDoc(sourcefile, docList);
		
		// 1.2 add stoplist
		if(!stoplist.equals(""))
			removeStopWords(stoplist, docList);
		
		if (verbose)
			printSortedList(docList);
		
		try {
			saveMap(docList, "map");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		//1.3 inverted list and lexicons
		for (Document doc: docList) {
			createLexicon(doc, lexicon); //Temp lexicon
		}
		
		allLexiconValues.addAll(lexicon.values());
		
		 try {
			 saveInvertedLists(allLexiconValues, "invlist"); //Save inverted list and also set pointers
			 saveLexicons(allLexiconValues, "lexicon");
		 }  catch (IOException e) {
			 e.printStackTrace();
		 }
		
	}
	
	/**  
	    * Saves document mapping data to file
	    * @param docList - the document list array with filters terms from the stop list etc
	    * @param file - file name to save the mapping data to
	    */
	public static String cleanString(String string) {
		return string.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase();
	}
	
	/**  
	    * Saves document mapping data to file
	    * @param docList - the document list array with filters terms from the stop list etc
	    * @param file - file name to save the mapping data to
	    */
	public static void saveMap(ArrayList<Document> docList, String file) 
			throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		for (Document doc: docList) {
			writer.write(doc.getDocID() + " " + doc.getDocNum());
			writer.newLine();
		}
		writer.close();
	}
	
	// print sorted list
	
	/**  
	    * Prints the sorted list to console
	    * @param docList - the document list array with filters terms from the stop list etc
	    */
	public static void printSortedList(ArrayList<Document> docList) {
		
		
		
		for (Document doc: docList) {	
			
		System.out.println(doc.getDocNum());
		List<String> list = new ArrayList<String>(doc.getMap().keySet());
		Collections.sort(list);
	
		for (String key: list) {
			int value = doc.getMap().get(key);
			System.out.println(key + " " + value);
		}
			System.out.println();
		
		}
	}
	
	
	/**  
	    * Maps the source file into memory
	    * @param sourcefile - Source file with data to be indexed
	    * @param docList - the document list to store the maping information
	    */
	public static void mapDoc(String sourcefile, ArrayList<Document> docList) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourcefile));
		
			// define Tag 
			boolean openHeadlineTag = false;
			String openHeadline = "<HEADLINE>";
			String closeHeadline = "</HEADLINE>";
			
			boolean openPTag = false;
			String openP = "<P>";
			String closeP = "</P>";
			
			boolean openTextTag = false;
			String openText = "<TEXT>";
			String closeText = "</TEXT>";
			
			while (true) {
				String line = br.readLine();
				
				if (line == null) {
					break;
				}
				
				// add Document to ArrayList
				Pattern pattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
				Matcher m = pattern.matcher(line);
				if(m.find()){
					Document currentDoc = new Document(m.group(1).replaceAll(" ", ""), docList.size());
					docList.add(currentDoc);
				}
				
				// check head tag
				if (line.equals(closeHeadline)) {
					openHeadlineTag = false;
				}
				
				if (openHeadlineTag) {
					if (!line.equals(openP) && !line.equals(closeP)) {
						
						String[] lineArray = line.split(" ");
						for (String word: lineArray) {

							String newString = cleanString(word);
							if (newString.equals("")) {
								continue;
							}
							docList.get(docList.size() -1).addWord(newString);
						}						
					}
				}
				
				if (line.equals(openHeadline)) {
					openHeadlineTag = true;
				}
				
				// check text tag
				if (line.equals(closeText)) {
					openTextTag = false;
				}
				
				if (openTextTag) {
					if (!line.equals(openP) && !line.equals(closeP)) {
						String[] lineArray = line.split(" ");
						for (String word: lineArray) {
							String newString = cleanString(word);
							if (newString.equals("")) {
								continue;
							}
							docList.get(docList.size() -1).addWord(newString);
						}
					}
				}
				if (line.equals(openText)) {
					openTextTag = true;
				}
			}
			
			//printSortedList(docList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**  
	    * Removes stop words from the list ( in memory)
	    * @param stoplistFilename - File name/location that contains the stop list
	    * @param docList - the document list with all terms currently loaded
	    * etc
	    */
	public static void removeStopWords(String stoplistFilename, ArrayList<Document> docList) {
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
			for (Document doc: docList){
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
	    * Ads documents' terms into their corresponding lexicons and the term frequency information etc
	    * @param doc - current document being processed
	    * @param lexicon - lexicon list
	    */
	   private static void createLexicon(Document doc, Map<String, LexiconNode> lexicon) {   

				List<String> list = new ArrayList<String>(doc.getMap().keySet());
				Collections.sort(list);
				
			
				for (String key: list) {

					if (!lexicon.containsKey(key)) {
						int frequency = doc.getMap().get(key);
						LexiconNode lexNode = new LexiconNode(key, doc.getDocID()); //instantly insert docID since we are going to do that anyway
						InvertedList docList = lexNode.invertedList.get(doc.getDocID());
						docList	.setCounter(frequency); // We can now set the frequency directly
						lexicon.put(key, lexNode);
					} else {
						LexiconNode lexNode = lexicon.get(key);
						lexNode.insert(doc.getDocID());
						int frequency = doc.getMap().get(key);
						InvertedList docList = lexNode.invertedList.get(doc.getDocID());
						docList.setCounter(frequency); // We can now set the frequency directly
					}
					
					
					
					

					
				}
		   }
	   
	   /**  
	    * Saves inverted list file
	    * @param lexicons - lexicon list
	    * @param file - file name/location to save the lexicon
	    */
	   public static void saveInvertedLists(List<LexiconNode> lexicons, String file)
			   throws IOException {
		   

		      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		      int pointerLocation = 0;
		      
		      
		      for (LexiconNode lexconNode : lexicons) {
		    	  Collection<InvertedList> lexiconCollection = lexconNode.getInvertedListValues();
		    	  lexconNode.setPointer(pointerLocation); //Setting pointer to the file offset position as stated in the requirements
		    	  writer.write(Integer.toBinaryString(lexconNode.invertedList.size())+" "); //How many documents it occurs in the collection
		    	  
		         for (InvertedList current : lexiconCollection) {
		        	 int documenId = current.getDocumentId();
		        	 int counter = current.getCounter();
		        	 writer.write(Integer.toBinaryString(documenId)+" "+Integer.toBinaryString(counter)+" ");
		         }
		         
		         
		         writer.newLine();
		         pointerLocation++; //Increasing pointer to next line
		      }
		      writer.close();

		   }
	   
	   /**  
	    * Saves lexicon file
	    * @param lexicons - lexicon list
	    * @param file - file name/location to save the lexicon
	    */
	   public static void saveLexicons(List<LexiconNode> lexiconsList, String fileName)
			   throws IOException {

		      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		      
		      
		      for (LexiconNode lexicon : lexiconsList) {
		            writer.write(lexicon.getTerm()+" "+lexicon.getPointer());
		            writer.newLine();
		         }
		         writer.close();

		   }
}
