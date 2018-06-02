public class Tuple<X, Y> { 
  
	public final X first; 
	public final Y second; 
  
	public Tuple(X x, Y y) { 
		this.first = x; 
		this.second = y; 
	} 

	public Tuple<Y, X> reverse() {
		Tuple<Y, X> newtup = new Tuple<Y, X>(this.second, this.first);
		return newtup;
	}

	public void print_tuple(){
		System.out.println("tuple: "+this.first+","+this.second);
	}
	
	public boolean equals(Tuple<X, Y> tup) { //check if two tuples contain the same values
		if ((this.first == tup.first)&&(this.second == tup.second)) {
			return true;
		}
		else {
			return false;
		}
	}
} 
