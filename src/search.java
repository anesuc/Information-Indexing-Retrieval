import java.io.BufferedReader;
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
		// TODO Auto-generated method stub
		
		Map<String, LexiconNode> lexiconMap = new Hashtable<String, LexiconNode>();
		
		Map<Integer, String> map = new Hashtable<Integer, String>();
		
		loadLexicons(lexiconMap, "lexicon.txt");
		loadMap(map, "map.txt");
		
		String[] searchTerms = {"west","1.1","one"};
		
		search(searchTerms, lexiconMap, map,"invlist.txt");

	}
	
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
					System.out.println(invlistDataParts[0]); //Document Frequency
					
					for (int j = 1; j < invlistDataParts.length; j++) {
						if ( (j & 1) != 0 ) { //if number is Odd then thats our document Id
							String documentId = map.get(Integer.parseInt(invlistDataParts[j]));
							String counter = invlistDataParts[j+1];
						System.out.println(documentId+" "+counter);
						}
					}
				}
				
				
			
		
		}  catch (IOException e) {
			 //FIXME handle errors here
		 }
		}
		
	}
	
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
				
				//System.out.println("term: "+term+" pointer: "+pointer);
			
			if (!lexicons.containsKey(term)) {
				LexiconNode lexNode = new LexiconNode(term, -1);
				lexNode.setPointer(pointer);
				lexicons.put(term, lexNode);
			} else {
				//This should not happen
				System.out.println("Debug: ERROR");
			}
			}
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
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
				
				//System.out.println("docId: "+docId+" pointer: "+docNumber);
				
				map.put(docId, docNumber);
			}
				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
