import java.util.*;

public class LexiconNode {

	   private String term;
	   private SortedMap<String, InvertedList> invertedList;
	   private int pointer;
	
	   public LexiconNode(String term, int pointer) {
		      this.term = term;
		      this.pointer = pointer;
		      this.invertedList = new TreeMap<String, InvertedList>();
		   }
	   
		public String getTerm() {
		      return term;
		   }
		
		public int getPointer() {
		      return pointer;
		   }
		
		public Collection<InvertedList> getInvertedListValues() {
		      return invertedList.values();
		   }

	   public void insert(String documentID) {
		   
	      if (!invertedList.containsKey(documentID)) {
	    	  InvertedList list = new InvertedList(documentID, pointer);
	    	  invertedList.put(documentID, list);
	      } else {
	    	  InvertedList list = invertedList.get(documentID);
	    	  list.addToCounter();
	      }
	   }
	   
	   

}
