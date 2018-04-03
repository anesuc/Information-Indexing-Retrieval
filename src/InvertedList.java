
public class InvertedList {
	
	   private String documentId;
	   private int counter = 0;
	
	public InvertedList(String docId) {
	      this.documentId = docId;
	   }
	
	public void addToCounter() {
	      this.counter++;
	   }
	
	public int getCounter() {
	      return counter;
	   }
	
	public String getDocumentId() {
	      return documentId;
	   }

}
