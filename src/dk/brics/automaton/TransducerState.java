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
	
}
