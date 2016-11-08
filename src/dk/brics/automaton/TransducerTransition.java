package dk.brics.automaton;

/**
 * A finite state transducer transition which belongs to a source state. This
 * class consists of Unicode character intervals to represent output.
 * 
 * @author Rodney Rodriguez
 */
public class TransducerTransition extends Transition {
	private static final long serialVersionUID = 1L;

	// output variables that are mapped to min and max
	char outMin;
	char outMax;

	// true if input should also serve as the output
	boolean isTransparent;
	// tells whether this is an epsilon transition
	boolean isEpsilon;

	/**
	 * Constructs a new {@link TransducerTransition} with min and max set to
	 * {@code c}. {@code isTransparent} is set to true since no output is
	 * specified.
	 * 
	 * @param c
	 *            The input value.
	 * @param to
	 *            The destination state.
	 */
	public TransducerTransition(char c, State to) {
		this(c, c, to);
	}

	/**
	 * Constructs a new {@link TransducerTransition} with {@code min} and
	 * {@code max} set to the parameters provided. {@code isTransparent} is set
	 * to true since no output is specified.
	 * 
	 * @param min
	 *            The min input value.
	 * @param max
	 *            The max input value.
	 * @param to
	 *            The destinition state.
	 */
	public TransducerTransition(char min, char max, State to) {
		this(min, max, '\u0000', '\u0000', to, true);
	}

	/**
	 * Constructs a new {@link TransducerTransition} with {@code min} and
	 * {@code max} set to the parameters provided. {@code isTransparent} is set
	 * to true since no output is specified.
	 * 
	 * @param min
	 *            The min input value.
	 * @param max
	 *            The max input value.
	 * @param outMin
	 *            The min output value.
	 * @param outMax
	 *            The max output value.
	 * @param to
	 *            The destination state.
	 * @param isTransparent
	 *            Denotes whether output is the input.
	 */
	public TransducerTransition(char min, char max, char outMin, char outMax, State to, boolean isTransparent) {
		super(min, max, to);
		if (outMax < outMin) {
			char t = outMax;
			outMax = outMin;
			outMin = t;
		}
		this.outMin = outMin;
		this.outMax = outMax;
		this.isTransparent = isTransparent;
	}

	/**
	 * Constructs an epsilon {@link TransducerTransition}.
	 * 
	 * @param min
	 *            The min input value.
	 * @param max
	 *            The max input value.
	 * @param to
	 *            The destination state.
	 * @return a new epsilon {@link TransducerTransition}
	 */
	public static TransducerTransition createEpsilonTransition(char min, char max, State to) {
		TransducerTransition t = new TransducerTransition(min, max, '\0', '\0', to, false);
		t.isEpsilon = true;
		return t;
	}

	/**
	 * Constructs a new {@link Transition} to the {@link State} {@code to} given
	 * the input values {@code min} and {@code max}. If {@code isTransparent} is
	 * true, the output values for the new {@link Transition} will be the input
	 * values; else the output values will be {@code outMin} and {@code outMax}.
	 * 
	 * @param min
	 *            The input min value.
	 * @param max
	 *            The input max value.
	 * @return the output of this transition given the input values.
	 */
	public Transition output(char min, char max, State to) {
		if (isTransparent) {
			return new Transition(min, max, to);
		}
		return new Transition(outMin, outMax, to);
	}

	/**
	 * Tells whether this transition is an epsilon transition.
	 * 
	 * @return true if this transition is an epsilon transition.
	 */
	public boolean isEpsilon() {
		return isEpsilon;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransducerTransition) {
			TransducerTransition t = (TransducerTransition) obj;
			return t.getMin() == getMin() && t.getMax() == getMax() && t.getDest() == getDest() && t.outMin == outMin
					&& t.outMax == outMax && t.isEpsilon == isEpsilon;
		} else
			return false;
	}

	@Override
	public TransducerTransition clone() {
		TransducerTransition clone = (TransducerTransition) super.clone();
		clone.isTransparent = isTransparent;
		clone.isEpsilon = isEpsilon;
		clone.outMin = outMin;
		clone.outMax = outMax;
		return clone;
	}

	@Override
	void appendDot(StringBuilder b) {
		b.append(" -> ").append(to.number).append(" [label=\"");
		appendCharString(min, b);
		if (min != max) {
			b.append("-");
			appendCharString(max, b);
		}
		b.append(" => ");
		if (isEpsilon) {
			b.append("eps");
		} else if (isTransparent) {
			b.append('*');
		} else {
			appendCharString(outMin, b);
			if (outMin != outMax) {
				b.append("-");
				appendCharString(outMax, b);
			}
		}
		b.append("\"]\n");
	}

}
