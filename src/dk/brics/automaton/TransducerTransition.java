package dk.brics.automaton;

/**
 * A finite state transducer transition which belongs to a source state. Consists of a Unicode character interval and
 * output.
 * 
 * @author Rodney Rodriguez
 */
public class TransducerTransition extends Transition {
	private static final long serialVersionUID = 1L;

	// output when transition is made
	char outMin;
	char outMax;

	// true if input should also serve as the output
	boolean isTransparent;

	public TransducerTransition(char c, State to) {
		this(c, c, to);
	}

	public TransducerTransition(char min, char max, State to) {
		this(min, max, '\u0000', '\u0000', to, true);
	}

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
	 * 
	 * @param min
	 *            The input min value.
	 * @param max
	 *            The input max value.
	 * @return the output of this transition given the input.
	 */
	public Transition output(char min, char max, State to) {
		if (isTransparent) {
			return new Transition(min, max, to);
		}
		return new Transition(outMin, outMax, to);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransducerTransition) {
			TransducerTransition t = (TransducerTransition) obj;
			return t.getMin() == getMin() && t.getMax() == getMax() && t.getDest() == getDest() && t.outMin == outMin && t.outMax == outMax;
		} else
			return false;
	}

	@Override
	public TransducerTransition clone() {
		TransducerTransition clone = (TransducerTransition) super.clone();
		clone.isTransparent = isTransparent;
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
		if (isTransparent) {
			b.append('*');
		} else {
			b.append(outMin);
			if (outMin != outMax) {
				b.append("-");
				appendCharString(outMax, b);
			}
		}
		b.append("\"]\n");
	}

}
