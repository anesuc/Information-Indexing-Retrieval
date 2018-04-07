import java.util.HashMap;
import java.util.Map;

public class Document {
	private String docNum;
	private Map<String, Integer> dict;
	private int docID;
	
	public Document (String docNum, int docID) {
		this.docNum = docNum;
		this.dict = new HashMap<String, Integer>();
		this.docID = docID;
	}
	
	public String getDocNum () {
		return docNum;
	}
	
	public int getDocID () {
		return docID;
	}
	
	public void addWord(String word) {
		if (dict.get(word) == null) {
			dict.put(word, 1);
		} else {
			dict.put(word, dict.get(word) + 1);
		}
	}
	
	public Map<String, Integer> getMap() {
		return dict;
	}
	
	public void removeKey(String key){
		dict.remove(key);
	}
}
