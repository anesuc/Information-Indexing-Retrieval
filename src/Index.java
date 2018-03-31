import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Index {
	
	
	public static void main(String[] args) {
		
		// create Document ArrayList
		ArrayList<Document> docList = new ArrayList<Document>();

		//Need commend line args.
		
		// 1.1 Parsing
		mapDoc(docList);
		
		// 1.2 add stoplist
		removeStopWords(docList);
		
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
			printSortedList(docList);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
