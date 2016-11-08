package dk.brics.automaton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class FiniteStateTransducer extends Automaton {
	private static final long serialVersionUID = 1L;

	public static FiniteStateTransducer parentDir() {
		// Forward Slash - '/' => '\u002f'
		// Back Slash '\' => '\u005c'
		FiniteStateTransducer fst = new FiniteStateTransducer();
		TransducerState s1 = new TransducerState();
		TransducerState s2 = new TransducerState();

		// s1 -> s1: accept anything
		s1.addTransition(new TransducerTransition('\u0001', '\uffff', s1));

		// s1 -> s2: '/' => epsilon
		s1.addTransition(TransducerTransition.createEpsilonTransition('/', '/', s2));

		// s2 -> s2: accept anything minus '/' (split into two transitions)
		s2.addTransition(new TransducerTransition('\u0001', '\u002e', s2));
		s2.addTransition(new TransducerTransition('\u0030', '\uffff', s2));

		s2.setAccept(true);
		fst.setInitialState(s1);
		return fst;
	}

	/**
	 * Returns a sorted array of transitions for each state (and sets state
	 * numbers).
	 */
	static TransducerTransition[][] getSortedTransitions(Set<State> states) {
		setStateNumbers(states);
		TransducerTransition[][] transitions = new TransducerTransition[states.size()][];
		for (State s : states)
			transitions[s.number] = ((TransducerState) s).getSortedTransitionArray(false);
		return transitions;
	}

	@Override
	public Automaton intersection(Automaton a) {
		FiniteStateTransducer a1 = this;
		Automaton a2 = a;
		if (a1.isSingleton()) {
			if (a2.run(a1.singleton))
				return a1.cloneIfRequired();
			else
				return BasicAutomata.makeEmpty();
		}
		if (a2.isSingleton()) {
			if (a1.run(a2.singleton))
				return a2.cloneIfRequired();
			else
				return BasicAutomata.makeEmpty();
		}
		if (a1 == a2)
			return a1.cloneIfRequired();
		TransducerTransition[][] transitions1 = FiniteStateTransducer.getSortedTransitions(a1.getStates());
		Transition[][] transitions2 = Automaton.getSortedTransitions(a2.getStates());
		Automaton c = new Automaton();
		LinkedList<StatePair> worklist = new LinkedList<StatePair>();
		HashMap<StatePair, StatePair> newstates = new HashMap<StatePair, StatePair>();
		StatePair p = new StatePair(c.initial, a1.initial, a2.initial);
		worklist.add(p);
		newstates.put(p, p);
		while (worklist.size() > 0) {
			p = worklist.removeFirst();
			p.s.accept = p.s1.accept && p.s2.accept;
			TransducerTransition[] t1 = transitions1[p.s1.number];
			Transition[] t2 = transitions2[p.s2.number];
			for (int n1 = 0, b2 = 0; n1 < t1.length; n1++) {
				while (b2 < t2.length && t2[b2].max < t1[n1].min)
					b2++;
				for (int n2 = b2; n2 < t2.length && t1[n1].max >= t2[n2].min; n2++)
					if (t2[n2].max >= t1[n1].min) {
						StatePair q = new StatePair(t1[n1].to, t2[n2].to);
						StatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						char min = t1[n1].min > t2[n2].min ? t1[n1].min : t2[n2].min;
						char max = t1[n1].max < t2[n2].max ? t1[n1].max : t2[n2].max;
						// p.s.transitions.add(new Transition(min, max, r.s));
						// TODO: implement this
						if (!t1[n1].isEpsilon()) {
							p.s.transitions.add(t1[n1].output(min, max, r.s));
						} else {
							 p.s.addEpsilon(r.s);
							// Transition epsilon = new Transition('\0', r.s);
							// p.s.transitions.add(epsilon);
						}
					}
			}
		}
		c.deterministic = a1.deterministic && a2.deterministic;
		c.removeDeadTransitions();
		c.checkMinimizeAlways();
		return c;
	}
}
