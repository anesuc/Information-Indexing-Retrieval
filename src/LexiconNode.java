import java.util.SortedMap;
import java.util.TreeMap;

public class LexiconNode {

	   private String name;
	   SortedMap<String, InvertedList> invlist;
	
	   public LexiconNode(String name) {
		      this.name = name;
		      this.invlist = new TreeMap<String, InvertedList>();
		   }
	   
		public String getName() {
		      return name;
		   }

	   public void insert(String documentID) {
		   InvertedList list = new InvertedList(documentID);
		   
	      if (!invlist.containsKey(documentID)) {
	    	  invlist.put(documentID, list);
	      }
	      list.addToCounter();
	   }
	   
	   

}
