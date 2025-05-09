package it.unibo.ai.didattica.competition.tablut.domain;

public class TranspositionTableEntry {
	private long zobrist;
	private int depth;
	//private int flag;
	private double eval;
	//private boolean ancient;
	//private Action action;
	private State.Turn player;
	//private State state;
	
	public TranspositionTableEntry(long zobrist, int depth, double eval, State.Turn player/*, State state*/) {
		super();
		this.zobrist = zobrist;
		this.depth = depth;
		this.eval = eval;
		this.player = player;
		//this.state = state;
	}

	public long getZobrist() {
		return zobrist;
	}

	public void setZobrist(long zobrist) {
		this.zobrist = zobrist;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public double getEval() {
		return eval;
	}

	public void setEval(double eval) {
		this.eval = eval;
	}	
	
	public State.Turn getPlayer() {
		return player;
	}

	public void setPlayer(State.Turn player) {
		this.player = player;
	}
	
	/*
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}	
	*/
	
}
