
public class InvertedList {
	
	   private String documentId;
	   private int counter = 0;
	   private int pointer  = 0; //FIXME
	
	public InvertedList(int pointer) {
		this.pointer = pointer;
	      
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
	
	public String getDocumentId() {
	      return documentId;
	   }

}
