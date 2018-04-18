public class Tuple<X, Y> { 
  
	public final X first; 
	public final Y second; 
  
	public Tuple(X x, Y y) { 
		this.first = x; 
		this.second = y; 
	} 

	public Tuple<Y, X> reverse(Tuple<X, Y> tup) {
		Tuple<Y, X> newtup = new Tuple<Y, X>(tup.second, tup.first);
		return newtup;
	}

	public void print_tuple(Tuple<X, Y> tup){
		System.out.println("tuple: "+tup.first+","+tup.second);
	}
} 