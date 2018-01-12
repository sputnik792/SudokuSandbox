//TripleTup is a triple variation of Tuple
public class TripleTup<X, Y, Z> extends Tuple<X, Y> {

	public final Z third;
	
	public TripleTup(X x, Y y, Z z) {
		super(x, y);
		this.third = z;
	}

}
