package it.unibo.ai.didattica.competition.tablut.domain;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class represents an action of a player
 * 
 * @author A.Piretti
 * 
 */
public class Action implements Serializable {

	private static final long serialVersionUID = 1L;

	private String from;
	private String to;

	private State.Turn turn;

	public Action(String from, String to, StateTablut.Turn t) throws IOException {
		if (from.length() != 2 || to.length() != 2) {
			throw new InvalidParameterException("the FROM and the TO string must have length=2");
		} else {
			this.from = from;
			this.to = to;
			this.turn = t;
		}
	}

	public String getFrom() {
		return this.from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public StateTablut.Turn getTurn() {
		return turn;
	}

	public void setTurn(StateTablut.Turn turn) {
		this.turn = turn;
	}

	public String toString() {
		return "Turn: " + this.turn + " " + "Pawn from " + from + " to " + to;
	}

	/**
	 * @return means the index of the column where the pawn is moved from
	 */
	public int getColumnFrom() {
		return Character.toLowerCase(this.from.charAt(0)) - 97;
	}

	/**
	 * @return means the index of the column where the pawn is moved to
	 */
	public int getColumnTo() {
		return Character.toLowerCase(this.to.charAt(0)) - 97;
	}

	/**
	 * @return means the index of the row where the pawn is moved from
	 */
	public int getRowFrom() {
		return Integer.parseInt(this.from.charAt(1) + "") - 1;
	}

	/**
	 * @return means the index of the row where the pawn is moved to
	 */
	public int getRowTo() {
		return Integer.parseInt(this.to.charAt(1) + "") - 1;
	}

	/**
	 * @return the list of all the symmetries of the action
	 */
	public List<String> generateSymmetries() {
		List<String> symmetries = new ArrayList<>();
		for (int i = 0; i < 4; i++) { // 0°, 90°, 180°, 270°
			String fromRot = rotate(this.from, i);
			String toRot = rotate(this.to, i);
			symmetries.add(fromRot + "->" + toRot);
			symmetries.add(flipHorizontal(fromRot) + "->" + flipHorizontal(toRot));
		}
		return symmetries;
	}

	/**
	 * @return the list of all the symmetries of the action
	 */
	private String rotate(String pos, int times) {
		int col = pos.charAt(0) - 'a';
		int row = pos.charAt(1) - '1';
		for (int i = 0; i < times; i++) {
			int temp = col;
			col = 8 - row;
			row = temp;
		}
		return "" + (char) ('a' + col) + (char) ('1' + row);
	}
	
	/**
	 * @return the list of all the symmetries of the action
	 */
	private String flipHorizontal(String pos) {
		int col = pos.charAt(0) - 'a';
		return "" + (char) ('a' + (8 - col)) + pos.charAt(1);
	}
}


