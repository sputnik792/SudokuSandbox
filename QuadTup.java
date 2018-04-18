//QuadTup is a quadruple variation of Tuple
public class QuadTup<X, Y, Z, W> extends Tuple<X, Y> {

	public final Z third;
	public final W last;
	
	public QuadTup(X x, Y y, Z z, W w) {
		super(x, y);
		this.third = z;
		this.last = w;
	}

}
