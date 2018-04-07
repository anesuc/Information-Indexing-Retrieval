import java.util.*;

public class LexiconNode {

	   private String term;
	   private SortedMap<String, InvertedList> invertedList;
	   private int pointer;
	
	   public LexiconNode(String term) {
		      this.term = term;
		      this.invertedList = new TreeMap<String, InvertedList>();
		   }
	   
		public String getTerm() {
		      return term;
		   }
		
		public void setPointer(int pointer) {
			this.pointer = pointer;
		   }
		
		public int getPointer() {
		      return pointer;
		   }		
		
		public Collection<InvertedList> getInvertedListValues() {
		      return invertedList.values();
		   }

	   public void insert(String documentID) {
		   
	      if (!invertedList.containsKey(documentID)) {
	    	  InvertedList list = new InvertedList(pointer);
	    	  invertedList.put(documentID, list);
	      } else {
	    	  InvertedList list = invertedList.get(documentID);
	    	  list.addToCounter();
	      }
	   }
	   
	   

}
