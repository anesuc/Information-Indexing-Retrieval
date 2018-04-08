import java.util.*;

public class LexiconNode {

	   private String term;
	   SortedMap<Integer, InvertedList> invertedList;
	   private int pointer;
	
	   public LexiconNode(String term, int documentID) {
		      this.term = term;
		      this.invertedList = new TreeMap<Integer, InvertedList>();
		      if ( documentID != -1)
		    	  insert(documentID); //Insert initial documentID inverted List
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

	   public void insert(int documentID) {
		   
	      if (!invertedList.containsKey(documentID)) {
	    	  InvertedList list = new InvertedList(documentID); //Give document ID to the inverted list because we need it later on when saving to the lexicon file
	    	  invertedList.put(documentID, list);
	      } else {
	    	  InvertedList list = invertedList.get(documentID);
	    	  list.addToCounter();
	      }
	      
	   }
	   
	   

}
