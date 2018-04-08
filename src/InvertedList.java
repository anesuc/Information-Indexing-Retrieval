
public class InvertedList {
	
	   private int documentId;
	   private int counter = 1; //initial addition would be  = 1
	
	public InvertedList(int documentId) {
	      this.documentId = documentId;
	   }
	
	public void setCounter(int counter) {
	      this.counter = counter;
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
