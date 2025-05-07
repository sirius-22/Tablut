package it.unibo.ai.didattica.competition.tablut.search;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MyIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

	public MyIterativeDeepeningAlphaBetaSearch(Game game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
	}

	public static float[] encodeState(State state) {
		State.Pawn[][] board = state.getBoard();

		/*
		 * "V*X*Y" V is the array of possible values in a cell (B, W, T, ...) X and Y
		 * are the dimensions of the board
		 */
		float[] input = new float[4 * 9 * 9];

		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 9; x++) {
				char piece = board[y][x].toString().charAt(0);
				int idx = y * 9 + x;

				switch (piece) {
				case 'B':
					input[idx] = 1f; // Channel 0
					break;
				case 'W':
					input[81 + idx] = 1f; // Channel 1
					break;
				case 'K':
					input[162 + idx] = 1f; // Channel 2
					break;
				case 'O':
				case 'T':
					input[243 + idx] = 1f; // Channel 3
					break;
				default:
					// niente: lascia 0 (casella vuota implicita)
					break;
				}
			}
		}

		return input;
	}

	@Override
	protected double eval(State state, State.Turn player) {
		// super.eval(state, player);

		float[] encodedState = MyIterativeDeepeningAlphaBetaSearch.encodeState(state);
		// ModelEvaluator evaluator = new ModelEvaluator("models/deep_cnn_scripted.pt");
		// float score = evaluator.evaluate(state);
		// System.out.println("Valutazione stato: " + score);

		// evaluator.close();

		return super.game.getUtility(state, player);
	}

}
