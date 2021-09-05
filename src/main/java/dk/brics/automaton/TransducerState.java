package dk.brics.automaton;

import java.util.Arrays;

public class TransducerState extends State {
	private static final long serialVersionUID = 1L;

	@Override
	TransducerTransition[] getSortedTransitionArray(boolean to_first) {
		TransducerTransition[] e = transitions.toArray(new TransducerTransition[transitions.size()]);
		Arrays.sort(e, new TransitionComparator(to_first));
		return e;
	}

	/**
	 * Adds transitions to accept any input except <code>exclude</code>. The
	 * output is always epsilon.
	 * 
	 * @param exclude
	 *            The char that shouldn't be accepted.
	 * @param to
	 *            The state that this state should transition to.
	 */
	public void addEpsilonExcludeTransition(char exclude, TransducerState to) {
		char lmin = TransducerTransition.UNICODE_MIN;
		char lmax = (char) (exclude - 1);
		char rmin = (char) (exclude + 1);
		char rmax = TransducerTransition.UNICODE_MAX;
		addTransition(TransducerTransition.createEpsilonTransition(lmin, lmax, to));
		addTransition(TransducerTransition.createEpsilonTransition(rmin, rmax, to));
	}

	/**
	 * Adds transitions to accept any input except <code>exclude</code>. The
	 * output is identical to the input.
	 * 
	 * @param exclude
	 *            The char that shouldn't be accepted.
	 * @param to
	 *            The state that this state should transition to.
	 */
	public void addIdenticalExcludeTransition(char exclude, TransducerState to) {
		char lmin = TransducerTransition.UNICODE_MIN;
		char lmax = (char) (exclude - 1);
		char rmin = (char) (exclude + 1);
		char rmax = TransducerTransition.UNICODE_MAX;
		addTransition(new TransducerTransition(lmin, lmax, to));
		addTransition(new TransducerTransition(rmin, rmax, to));
	}

	/**
	 * Adds a single {@link TransducerTransition} that accepts anything. Output
	 * is always epsilon.
	 * 
	 * @param to
	 *            The destination state.
	 */
	public void addEpsilonAcceptAllTransition(State to) {
		addTransition(TransducerTransition.createEpsilonTransition(TransducerTransition.UNICODE_MIN,
				TransducerTransition.UNICODE_MAX, to));
	}

	/**
	 * Adds a single {@link TransducerTransition} that accepts anything. Output
	 * is identical to the input.
	 * 
	 * @param to
	 *            The destination state.
	 */
	public void addIdenticalAcceptAllTransition(State to) {
		addTransition(new TransducerTransition(TransducerTransition.UNICODE_MIN, TransducerTransition.UNICODE_MAX,
				TransducerTransition.UNICODE_NULL, TransducerTransition.UNICODE_NULL, to, true));
	}

}
