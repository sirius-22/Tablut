package it.unibo.ai.didattica.competition.tablut.client;

import java.io.IOException;
import java.net.UnknownHostException;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateBrandub;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

/**
 * 
 * @author A. Piretti, Andrea Galassi
 *
 */
public class EnricoClient extends TablutClient {

	private int game;
	private int timeout;

	public EnricoClient(String player, String name, int gameChosen, int timeout, String ipAddress)
			throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		game = gameChosen;
		this.timeout = timeout;
	}

	public EnricoClient(String player, String name, int timeout, String ipAddress)
			throws UnknownHostException, IOException {
		this(player, name, 4, timeout, ipAddress);
	}

	public EnricoClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, "random", 4, timeout, ipAddress);
	}

	public EnricoClient(String player) throws UnknownHostException, IOException {
		this(player, "random", 4, 60, "localhost");
	}

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		int gametype = 4;
		String role = "";
		String name = "random";
		String ipAddress = "localhost";
		int timeout = 60;
		// TODO: change the behavior?
		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		} else {
			System.out.println(args[0]);
			role = (args[0]);
		}
		if (args.length == 2) {
			System.out.println(args[1]);
			timeout = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			ipAddress = args[2];
		}
		System.out.println("Selected client: " + args[0]);

		EnricoClient client = new EnricoClient(role, name, gametype, timeout, ipAddress);
		client.run();
	}

	@Override
	public void run() {

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		State state;

		Game rules = null;
		switch (this.game) {
		case 1:
			state = new StateTablut();
			rules = new GameTablut();
			break;
		case 2:
			state = new StateTablut();
			rules = new GameModernTablut();
			break;
		case 3:
			state = new StateBrandub();
			rules = new GameTablut();
			break;
		case 4:
			state = new StateTablut();
			state.setTurn(State.Turn.WHITE);
			rules = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
			System.out.println("Ashton Tablut game");
			break;
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}

		System.out.println("You are player " + this.getPlayer().toString() + "!");

		// create iterative deepening slave
		IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> oracoloEnrico = new IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(
				rules, -1, 1, this.timeout);

		// init gaming sequence
		while (true) {
			try {
				this.read();
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			}
			System.out.println("Current state:");
			state = this.getCurrentState();
			System.out.println(state.toString());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if (this.getPlayer().equals(Turn.WHITE)) { // WHITE
				if (state.getTurn().equals(StateTablut.Turn.WHITE)) {

					// Mio turno
					try {
						this.write(oracoloEnrico.makeDecision(state));
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
					// Turno dell'avversario
					System.out.println("Waiting for your opponent move... ");
				}
				// ho vinto
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				// ho perso
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				// pareggio
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} else { // BLACK PLAYER

				// Mio turno
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
					try {
						this.write(oracoloEnrico.makeDecision(state));
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
					// Turno dell'avversario
					System.out.println("Waiting for your opponent move... ");
				} else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			}
		}

	}
}
