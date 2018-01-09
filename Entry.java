import java.util.ArrayList;

public class Entry{
	private ArrayList<Integer> entries = new ArrayList<Integer>();

	public Entry(){
		for (int i = 1; i <= 9; i++){
			entries.add(i);
		}
	}
	
	public void setEntries(ArrayList<Integer> arr){
		this.entries = arr;
	}
	
	public ArrayList<Integer> getEntries(){
		return this.entries;
	}
	
	public Integer get(int e){
		return entries.get(e);
	}
	
	//removes object e
	public void deleteObj(Integer e){
		entries.remove(e);
	}
	
	//removes the object at index e
	public void deleteIndex(int e){
		entries.remove(e);
	}
	
	public void remove_all(){
		while (!(this.entries.isEmpty())){
			entries.remove(0);
		}
	}
	
	public boolean isIn(Integer e){
		return entries.contains(e);
	}
	
	public int get_size(){
		return entries.size();
	}
	
	public void resetEntries(){
		if (entries.isEmpty()){
			for (int i = 1; i <= 9; i++){
				entries.add(i);
			}
		}
	}
}
