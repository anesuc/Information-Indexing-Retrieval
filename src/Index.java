import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class Index {
	
	
	public static void main(String[] args) {
		
		// create Document ArrayList
		ArrayList<Document> docList = new ArrayList<Document>();

		Map<String, LexiconNode> lexicon = new TreeMap<String, LexiconNode>(); //fixme
		
		List<LexiconNode> allLexiconValues = new ArrayList<LexiconNode>();

		//Need commend line args.
		
		// 1.1 Parsing
		mapDoc(docList);
		
		// 1.2 add stoplist
		removeStopWords(docList);
		
		//1.3 (A) Lexicon
		for (Document doc: docList) {
			createLexicon(doc, lexicon); //Temp lexicon
		}
		
		allLexiconValues.addAll(lexicon.values());
		
		 try {
		saveLexicons(allLexiconValues, "lexicon");
		 }  catch (IOException e) {
			 //FIXME hanle errors here
		 }
		
	}
	
	//clean up the word and transfer to lowercase
	public static String cleanString(String string) {
		return string.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase();
	}
	
	// print sorted list
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
	
	//Parsing method
	public static void mapDoc(ArrayList<Document> docList) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/latimes-100"));
		
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
					Document currentDoc = new Document(m.group(1).replaceAll(" ", ""));
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
			
//			printSortedList(docList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeStopWords(ArrayList<Document> docList) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/stoplist"));
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
			//printSortedList(docList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	   private static void createLexicon(Document doc,
		         Map<String, LexiconNode> lexicon) {   

				List<String> list = new ArrayList<String>(doc.getMap().keySet());
				Collections.sort(list);
				
				int lexiconPointer = 0;
			
				for (String key: list) {
					LexiconNode lexiconTemp;
					

					if (!lexicon.containsKey(key)) {
						lexicon.put(key, new LexiconNode(key, lexiconPointer));
						//lexiconPointer++; This was wrong implementation HELP needed lol
					}
					
					//System.out.println("key 1: "+key);

					lexiconTemp = lexicon.get(key);
					lexiconTemp.insert(doc.getDocNum());
				}
		   }
	   
	   public static void saveInvertedLists(List<LexiconNode> lexicons, String file)
			   throws IOException {
		   

		      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		      
		      
		      for (LexiconNode lexconNode : lexicons) {
		    	  Collection<InvertedList> lexiconCollection = lexconNode.invlist.values();
		    	  
		         for (InvertedList current : lexiconCollection) {
		        	 String documenId = current.getDocumentId();
		        	 int counter = current.getCounter();
		        	 writer.write(documenId+" "+counter);
		         }

		         writer.newLine();
		      }
		      writer.close();

		   }
	   
	   public static void saveLexicons(List<LexiconNode> lexiconsList, String fileName)
			   throws IOException {

		      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		      
		      
		      for (LexiconNode lexicon : lexiconsList) {
		            writer.write(lexicon.getTerm()+" : "+lexicon.getPointer()); //FIXME 
		            writer.newLine();
		         }
		         writer.close();

		   }
	
	public static void indexWords(ArrayList<Document> docList) {
		//System.out.println(docList.get(0).getMap.get("a"));
		
	}
}
