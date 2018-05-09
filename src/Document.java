import java.util.HashMap;
import java.util.Map;

public class Document{
	private String docNum;
	private Map<String, Integer> dict;
	private int docID;
	private int length;
	private HashMap<String, Integer> termFrequency;
	private double score;
	
	public Document (String docNum, int docID) {
		this.docNum = docNum;
		this.dict = new HashMap<String, Integer>();
		this.docID = docID;
		this.length = 0;
	}
	
	public Document (String docNum, int docID, int length) {
		this.docNum = docNum;
		this.docID = docID;
		this.length = length;
		this.termFrequency = new HashMap<String, Integer>();
		this.score = 0;
		this.dict = new HashMap<String, Integer>();
	}
	
	public String getDocNum () {
		return docNum;
	}
	
	public int getDocID () {
		return docID;
	}
	
	/**  
	    * Save within-document frequency
	    * @param dict - the map that contains the word and within-document frequency
	    * @param word - the item collected during processing
	    */
	public void addWord(String word) {
		if (dict.get(word) == null) {
			dict.put(word, 1);
		} else {
			dict.put(word, dict.get(word) + 1);
		}
		this.length += word.length();
	}
	
	public Map<String, Integer> getMap() {
		return dict;
	}
	
	/**
	 * 
	 * @param key - stopword
	 */
	public void removeKey(String key){
		if (dict.get(key) != null) {
			this.length -= key.length() * dict.get(key);
		}
		dict.remove(key);
	}
	
	
	public int getLength() {
		return this.length;
	}
	
	/**
	 * 
	 * @param term - indexed word
	 * @param freq - frequency of word
	 */
	public void setTermFreq(String term, int freq) {
		this.termFrequency.put(term, freq);
	}
	
	/**
	 * 
	 * @param term - indexed word
	 * @return 
	 */
	public double getTermFreq (String term) {
		if (termFrequency.get(term) != null) {
			return this.termFrequency.get(term);
		}
		return -1;
	}
	
	public double getScore () {
		return this.score;
	}
	
	/**
	 * 
	 * @param score - score of BM25 calculation
	 */
	public void setScore (double score) {
		this.score += score;
	}
}
