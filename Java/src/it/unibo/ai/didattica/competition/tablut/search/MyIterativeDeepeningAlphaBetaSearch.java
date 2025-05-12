package it.unibo.ai.didattica.competition.tablut.search;

import java.util.ArrayList;
import java.util.List;

import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.framework.Metrics;
import it.unibo.ai.didattica.competition.tablut.PytorchIntegration.ModelEvaluator;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.TranspositionTableEntry;

/*
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
	
	// ORDER ACTIONS
		// si possono ordinare le azioni in ordine decrescente dalla più favorevole alla meno favorevole.

}
*/

public class MyIterativeDeepeningAlphaBetaSearch implements AdversarialSearch<State, Action> {

	public final static String METRICS_NODES_EXPANDED = "nodesExpanded";
	public final static String METRICS_MAX_DEPTH = "maxDepth";
	public final static int MAX_TRANSPOSITION_SIZE = 1024;
	public final static int ASSOCIATIVE_WAYS = 2;

	protected Game<State, Action, State.Turn> game;
	protected double utilMax;
	protected double utilMin;
	protected int currDepthLimit;
	private boolean heuristicEvaluationUsed; // indicates that non-terminal nodes have been evaluated.
	private Timer timer;
	private boolean logEnabled;

	private long[] zobrist;
	private TranspositionTableEntry[] transpositionTable;

	private ModelEvaluator evaluator;
	String dirPath = "src/it/unibo/ai/didattica/competition/tablut/PytorchIntegration/models";
	String fileName = "global_epoch5000_cnn.pt";

	private Metrics metrics = new Metrics();

	/**
	 * Creates a new search object for a given game.
	 *
	 * @param game    The game.
	 * @param utilMin Utility value of worst state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param utilMax Utility value of best state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param time    Maximal computation time in seconds.
	 */
	public static <STATE, ACTION, PLAYER> IterativeDeepeningAlphaBetaSearch<STATE, ACTION, PLAYER> createFor(
			Game<STATE, ACTION, PLAYER> game, double utilMin, double utilMax, int time) {
		return new IterativeDeepeningAlphaBetaSearch<>(game, utilMin, utilMax, time);
	}

	/**
	 * Creates a new search object for a given game.
	 *
	 * @param game    The game.
	 * @param utilMin Utility value of worst state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param utilMax Utility value of best state for this player. Supports
	 *                evaluation of non-terminal states and early termination in
	 *                situations with a safe winner.
	 * @param time    Maximal computation time in seconds.
	 */
	public MyIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax,
			int time, long[] zobrist) {
		this.game = game;
		this.utilMin = utilMin;
		this.utilMax = utilMax;
		this.timer = new Timer(time);

		this.zobrist = zobrist;
		// this.transpositionTable = new
		// TranspositionTableEntry[MAX_TRANSPOSITION_SIZE];
	}

	public void setLogEnabled(boolean b) {
		logEnabled = b;
	}

	/**
	 * Template method controlling the search. It is based on iterative deepening
	 * and tries to make to a good decision in limited time. Credit goes to Behi
	 * Monsio who had the idea of ordering actions by utility in subsequent
	 * depth-limited search runs.
	 */
	@Override
	public Action makeDecision(State state) {
		metrics = new Metrics();
		this.transpositionTable = new TranspositionTableEntry[MAX_TRANSPOSITION_SIZE];

		try {
			this.evaluator = new ModelEvaluator(dirPath + "/" + fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// StringBuffer logText = null;
		State.Turn player = game.getPlayer(state);
		// List<A> results = orderActions(state, game.getActions(state), player, 0);
		List<Action> results = game.getActions(state);
		timer.start();
		currDepthLimit = 0;
		do {
			incrementDepthLimit();
			// if (logEnabled)
			// logText = new StringBuffer("depth " + currDepthLimit + ": ");
			heuristicEvaluationUsed = false;
			ActionStore<Action> newResults = new ActionStore<>();
			for (Action action : results) {
				double value = minValue(game.getResult(state, action), player, Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY, 1);
				if (timer.timeOutOccurred())
					break; // exit from action loop
				newResults.add(action, value);
				// if (logEnabled)
				// logText.append(action).append("->").append(value).append(" ");
			}
			// if (logEnabled)
			// System.out.println(logText);
			if (newResults.size() > 0) {
				results = newResults.actions;

				if (!timer.timeOutOccurred()) {
					if (hasSafeWinner(newResults.utilValues.get(0)))
						break; // exit from iterative deepening loop
					// else if (newResults.size() > 1
					// && isSignificantlyBetter(newResults.utilValues.get(0),
					// newResults.utilValues.get(1)))
					// break; // exit from iterative deepening loop
				}

			}
		} while (!timer.timeOutOccurred() && heuristicEvaluationUsed);

		System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED)
				+ " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH));
		this.evaluator.close();
		return results.get(0);
	}

	// returns an utility value
	public double maxValue(State state, State.Turn player, double alpha, double beta, int depth) {
		updateMetrics(depth);

		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			/*
			 * long zobrist = hashState(state); double value; int hashIndex = (int) (zobrist
			 * % MAX_TRANSPOSITION_SIZE); TranspositionTableEntry entry =
			 * transpositionTable[hashIndex];
			 * 
			 * // if (checkTranspositionTable(zobrist))
			 * 
			 * if (entry == null) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() ==
			 * zobrist && entry.getDepth() >= depth) { return
			 * state.getTurn().equals(entry.getPlayer()) ? entry.getEval() :
			 * -entry.getEval(); } else if (entry.getZobrist() == zobrist &&
			 * entry.getDepth() < depth) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() !=
			 * zobrist && entry.getDepth() < depth) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() !=
			 * zobrist && entry.getDepth() >= depth) { return eval(state, player); } else {
			 * return eval(state, player); }
			 */
			return eval(state, player);
		} else {
			double value = Double.NEGATIVE_INFINITY;
			// for (A action : orderActions(state, game.getActions(state), player, depth)) {
			for (Action action : game.getActions(state)) {
				value = Math.max(value, minValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value >= beta)
					return value;
				alpha = Math.max(alpha, value);
			}
			return value;
		}
	}

	// returns an utility value
	public double minValue(State state, State.Turn player, double alpha, double beta, int depth) {
		updateMetrics(depth);

		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
			/*
			 * long zobrist = hashState(state); //System.out.println("zobrist is " +
			 * zobrist); double value; int hashIndex = (int) (zobrist %
			 * MAX_TRANSPOSITION_SIZE); TranspositionTableEntry entry =
			 * transpositionTable[hashIndex];
			 * 
			 * // if (checkTranspositionTable(zobrist))
			 * 
			 * if (entry == null) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() ==
			 * zobrist && entry.getDepth() >= depth) {
			 * //System.out.println("something worked " + zobrist);
			 * //System.out.println("state curr:" + state + "\nstate entry: " +
			 * entry.getState()); return state.getTurn().equals(entry.getPlayer()) ?
			 * entry.getEval() : -entry.getEval(); } else if (entry.getZobrist() == zobrist
			 * && entry.getDepth() < depth) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() !=
			 * zobrist && entry.getDepth() < depth) { value = eval(state, player);
			 * transpositionTable[hashIndex] = new TranspositionTableEntry(zobrist, depth,
			 * value, state.getTurn()); return value; } else if (entry.getZobrist() !=
			 * zobrist && entry.getDepth() >= depth) { return eval(state, player); } else {
			 * return eval(state, player); }
			 */
			return eval(state, player);
		} else {
			double value = Double.POSITIVE_INFINITY;
			// for (A action : orderActions(state, game.getActions(state), player, depth)) {
			for (Action action : game.getActions(state)) {
				value = Math.min(value, maxValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value <= alpha)
					return value;
				beta = Math.min(beta, value);
			}
			return value;
		}
	}

	private void updateMetrics(int depth) {
		metrics.incrementInt(METRICS_NODES_EXPANDED);
		metrics.set(METRICS_MAX_DEPTH, Math.max(metrics.getInt(METRICS_MAX_DEPTH), depth));
	}

	/*
	 * private long hashState(State state) { int i, j; long hash = 0;
	 * 
	 * for (i = 0; i < 9; i++) { for (j = 0; j < 9; j++) { State.Pawn pawn =
	 * state.getPawn(i, j); if (!pawn.equals(State.Pawn.EMPTY) ||
	 * !pawn.equals(State.Pawn.THRONE)) { switch (pawn) { case State.Pawn.WHITE:
	 * hash = hash ^ this.zobrist[(j + i * 9) * 3]; case State.Pawn.BLACK: hash =
	 * hash ^ this.zobrist[(j + i * 9) * 3 + 1]; case State.Pawn.KING: hash = hash ^
	 * this.zobrist[(j + i * 9) * 3 + 2]; default: break; } } } }
	 * 
	 * return hash; }
	 */
	/**
	 * Returns some statistic data from the last search.
	 */
	@Override
	public Metrics getMetrics() {
		return metrics;
	}

	/**
	 * Primitive operation which is called at the beginning of one depth limited
	 * search step. This implementation increments the current depth limit by one.
	 */
	protected void incrementDepthLimit() {
		currDepthLimit++;
	}

	/**
	 * Primitive operation which is used to stop iterative deepening search in
	 * situations where a clear best action exists. This implementation returns
	 * always false.
	 */
	protected boolean isSignificantlyBetter(double newUtility, double utility) {
		return false;
	}

	/**
	 * Primitive operation which is used to stop iterative deepening search in
	 * situations where a safe winner has been identified. This implementation
	 * returns true if the given value (for the currently preferred action result)
	 * is the highest or lowest utility value possible.
	 */
	protected boolean hasSafeWinner(double resultUtility) {
		return resultUtility <= utilMin || resultUtility >= utilMax;
	}

	/**
	 * Primitive operation, which estimates the value for (not necessarily terminal)
	 * states. This implementation returns the utility value for terminal states and
	 * <code>(utilMin + utilMax) / 2</code> for non-terminal states. When
	 * overriding, first call the super implementation!
	 */
	protected double eval(State state, State.Turn player) {
		// ENRICO's HEURISTICS -> DON'T MODIFY
		if (game.isTerminal(state)) {
			// return game.getUtility(state, player);
		} else {
			heuristicEvaluationUsed = true;
			// return game.getUtility(state, player);
		}

		// YOU CAN MODIFY THIS **** BELOW

		float score = 0f;
		float[] encodedState = MyIterativeDeepeningAlphaBetaSearch.encodeState(state);

		try {
			score = this.evaluator.evaluate(encodedState);
			// evaluator.close();
			// System.out.println("Valutazione stato: " + score);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// CLASSIC HEURISTIC
		// return super.game.getUtility(state, player);

		return score;
	}

	/**
	 * Primitive operation for action ordering. This implementation preserves the
	 * original order (provided by the game).
	 */
	public List<Action> orderActions(State state, List<Action> actions, State.Turn player, int depth) {
		return actions;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// nested helper classes

	private static class Timer {
		private long duration;
		private long startTime;

		Timer(int maxSeconds) {
			this.duration = 1000 * maxSeconds;
		}

		void start() {
			startTime = System.currentTimeMillis();
		}

		boolean timeOutOccurred() {
			return System.currentTimeMillis() > startTime + duration;
		}
	}

	/**
	 * Orders actions by utility.
	 */
	private static class ActionStore<A> {
		private List<A> actions = new ArrayList<>();
		private List<Double> utilValues = new ArrayList<>();

		void add(A action, double utilValue) {
			int idx = 0;
			while (idx < actions.size() && utilValue <= utilValues.get(idx))
				idx++;
			actions.add(idx, action);
			utilValues.add(idx, utilValue);
		}

		int size() {
			return actions.size();
		}
	}

	/*
	 * INTEGRATION WITH PYTORCH
	 */

	public static float[] encodeState(State state) {
		State.Pawn[][] board = state.getBoard();

		// "V*X*Y" V is the array of possible values in a cell (B, W, T, ...) X and Y
		// are the dimensions of the board

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
}
