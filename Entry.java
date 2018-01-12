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
	
	/**return element at index*/
	public Integer get(int e){
		return entries.get(e);
	}
	
	/**removes object e*/
	public void deleteObj(Integer e){
		entries.remove(e);
	}
	
	/**removes the object at index e*/
	public void deleteIndex(int e){
		entries.remove(e);
	}
	
	public void remove_all(){
		while (!(this.entries.isEmpty())){
			entries.remove(0);
		}
	}
	
	/** check if the element is in the entry list*/
	public boolean isIn(Integer e){
		return entries.contains(e);
	}
	
	public int get_size(){
		return entries.size();
	}
	
	public boolean contains_tup(Tuple<Integer, Integer> tup){
		if (entries.contains(tup.first)&&(entries.contains(tup.second))){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**check to see if the entry list contains the whole triplet or a subset of the triplet (of size 2)*/
	public boolean contains_trip(TripleTup<Integer, Integer, Integer> trip){
		if (entries.contains(trip.first)&&entries.contains(trip.second)&&entries.contains(trip.third)){
			return true;
		}
		else if (entries.contains(trip.first)&&entries.contains(trip.second)){
			return true;
		}
		else if (entries.contains(trip.first)&&entries.contains(trip.third)){
			return true;
		}
		else if (entries.contains(trip.second)&&entries.contains(trip.third)){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**check to see if it contains at least one value of the triple */
	public boolean contains_anyTrip(TripleTup<Integer, Integer, Integer> trip){
		if ((entries.contains(trip.first))||(entries.contains(trip.second))||(entries.contains(trip.third))){
			return true;
		}
		else {
			return false;
		}
	}
	
	public void resetEntries(){
		if (entries.isEmpty()){
			for (int i = 1; i <= 9; i++){
				entries.add(i);
			}
		}
	}
}