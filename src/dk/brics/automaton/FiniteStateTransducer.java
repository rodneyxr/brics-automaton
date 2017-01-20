package dk.brics.automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class FiniteStateTransducer extends Automaton {
	private static final long serialVersionUID = 1L;

	/**
	 * Copies and converts all automaton states and transitions to a new
	 * FiniteStateTransducer. All output is set to epsilon as there is no way to
	 * set the output for each state. You will need to traverse each transition
	 * and set the output yourself.
	 * 
	 * @param a
	 *            The automaton to be converted.
	 * @return a {@link FiniteStateTransducer} version of <code>a</code>.
	 */
	public static FiniteStateTransducer AutomatonToTransducer(Automaton aut) {
		FiniteStateTransducer fst = new FiniteStateTransducer();
		if (!aut.isSingleton()) {
			HashMap<State, TransducerState> m = new HashMap<State, TransducerState>();
			Set<State> states = aut.getStates();
			for (State s : states)
				m.put(s, new TransducerState());
			for (State s : states) {
				TransducerState p = m.get(s);
				p.accept = s.accept;
				if (s == aut.initial)
					fst.initial = p;
				for (Transition t : s.transitions)
					p.transitions.add(TransducerTransition.createEpsilonTransition(t.min, t.max, m.get(t.to)));
			}
		}
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
		a.expandSingleton(); // some automatons fail without this
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
