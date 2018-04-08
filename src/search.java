import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class search {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String, LexiconNode> lexiconMap = new Hashtable<String, LexiconNode>();
		
		Map<Integer, String> map = new Hashtable<Integer, String>();
		
		loadLexicons(lexiconMap, "lexicon.txt");
		loadMap(map, "map.txt");

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
