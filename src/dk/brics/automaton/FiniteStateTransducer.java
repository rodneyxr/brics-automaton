package dk.brics.automaton;

import java.util.HashMap;
import java.util.HashSet;
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
		TransducerState s3 = new TransducerState();
		TransducerState s4 = new TransducerState();

		// s1 -> s1: accept anything
		s1.addIdenticalAcceptAllTransition(s1);

		// s1 -> s2: '/' => epsilon
		s1.addTransition(new TransducerTransition('/', '/', s2));

		// s2 -> s3: accept anything minus '/' => epsilon
		s2.addEpsilonExcludeTransition('/', s3);

		// s3 -> s4: accept anything minus '/' => epsilon
		s3.addEpsilonExcludeTransition('/', s3);

		// s3 -> s4: '/' => epsilon
		s3.addTransition(TransducerTransition.createEpsilonTransition('/', '/', s4));

		s3.setAccept(true);
		s4.setAccept(true);
		fst.setInitialState(s1);
		return fst;
	}

	// S0 -> S0: input=All-'/', output=identical
	// S0 -> S1: input='/', output = identical
	// S1 -> S2: input='/', output = empty
	// S1 -> S0: input = All-'/', output = identical
	// S2 -> S0: input = All-'/', output = identical
	public static FiniteStateTransducer removeDoubleSeparator() {
		FiniteStateTransducer fst = new FiniteStateTransducer();
		TransducerState s0 = new TransducerState();
		TransducerState s1 = new TransducerState();
		TransducerState s2 = new TransducerState();

		// s0 -> s0: accept anything minus '/' => identical output
		s0.addIdenticalExcludeTransition('/', s0);

		// s0 -> s1: only accept '/' => identical output
		s0.addTransition(new TransducerTransition('/', s1));

		// s1 -> s2: only accept '/' => epsilon
		s1.addTransition(TransducerTransition.createEpsilonTransition('/', '/', s2));

		// s1 -> s0: accept anything minus '/' => identical output
		s1.addIdenticalExcludeTransition('/', s0);

		// s2 -> s0: accept anything minus '/' => identical output
		s2.addIdenticalExcludeTransition('/', s0);

		s0.setAccept(true);
		s1.setAccept(true);
		s2.setAccept(true);
		fst.setInitialState(s0);
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
		HashMap<StatePair, HashSet<StatePair>> epsilonTranses = new HashMap<StatePair, HashSet<StatePair>>();
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
							q.s.accept = q.s1.accept && q.s2.accept;
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						char min = t1[n1].min > t2[n2].min ? t1[n1].min : t2[n2].min;
						char max = t1[n1].max < t2[n2].max ? t1[n1].max : t2[n2].max;

						if (!t1[n1].isEpsilon()) {
							p.s.transitions.add(t1[n1].output(min, max, r.s));
						} else {
							HashSet<StatePair> dests = epsilonTranses.get(p);
							if (dests == null) {
								dests = new HashSet<StatePair>();
								epsilonTranses.put(p, dests);
							}
							dests.add(r);
						}
					}
			}
		}
		addEpsilonEdges(epsilonTranses);
		c.deterministic = a1.deterministic && a2.deterministic;
		c.removeDeadTransitions();
		c.checkMinimizeAlways();
		return c;
	}

	private void addEpsilonEdges(HashMap<StatePair, HashSet<StatePair>> epsilonTranses) {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (StatePair src : epsilonTranses.keySet()) {
				HashSet<StatePair> dests = epsilonTranses.get(src);
				for (StatePair dest : dests) {
					int transes = src.s.getTransitions().size();
					boolean accepted = src.s.accept;
					src.s.addEpsilon(dest.s);
					if ((src.s.accept && !accepted) || src.s.transitions.size() > transes) {
						changed = true;
					}
				}

			}
		}
	}
}
