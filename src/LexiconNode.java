import java.util.SortedMap;
import java.util.TreeMap;

public class LexiconNode {

	   private String term;
	   private int pointer;
	   SortedMap<String, InvertedList> invlist;
	
	   public LexiconNode(String term, int pointer) {
		      this.term = term;
		      this.pointer = pointer;
		      this.invlist = new TreeMap<String, InvertedList>();
		   }
	   
		public String getTerm() {
		      return term;
		   }
		
		public int getPointer() {
		      return pointer;
		   }

	   public void insert(String documentID) {
		   InvertedList list = new InvertedList(documentID);
		   
	      if (!invlist.containsKey(documentID)) {
	    	  invlist.put(documentID, list);
	      }
	      list.addToCounter();
	   }
	   
	   

}
