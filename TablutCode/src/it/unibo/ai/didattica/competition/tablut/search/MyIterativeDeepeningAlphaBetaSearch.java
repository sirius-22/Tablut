package it.unibo.ai.didattica.competition.tablut.search;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;


public class MyIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

	public MyIterativeDeepeningAlphaBetaSearch(Game game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected double eval(State state, State.Turn player) {
		super.eval(state, player);
		
		return super.game.getUtility(state, player);
	}

}
