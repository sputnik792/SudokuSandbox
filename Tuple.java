public class Tuple<X, Y> { 
  
	public final X first; 
	public final Y second; 
  
	public Tuple(X x, Y y) { 
		this.first = x; 
		this.second = y; 
	} 
 
	public void print_tuple(Tuple<X, Y> tup){
		System.out.println("tuple: "+tup.first+","+tup.second);
	}
	
} 