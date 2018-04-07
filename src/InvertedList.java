
public class InvertedList {
	
	   private int documentId;
	   private int counter = 0;
	
	public InvertedList(int documentId) {
	      this.documentId = documentId;
	   }
	
	public void addToCounter() {
	      this.counter++;
	   }
	
	public int getCounter() {
	      return counter;
	   }
	
	public int getPointer() {
	      return counter;
	   }
	
	public int getDocumentId() {
	      return documentId;
	   }

}
