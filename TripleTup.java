//TripleTup is a triple variation of Tuple
public class TripleTup<X, Y, Z> extends Tuple<X, Y> {

	public final Z third;
	
	public TripleTup(X x, Y y, Z z) {
		super(x, y);
		this.third = z;
	}

	public boolean equals(TripleTup<X, Y, Z> trip) {
		if ((this.first == trip.first)&&(this.second == trip.second)&&(this.third == trip.third)) {
			return true;
		}
		else {
			return false;
		}
	}
}
