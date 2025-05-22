package it.unibo.ai.didattica.competition.tablut.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unibo.ai.didattica.competition.tablut.PytorchIntegration.ModelEvaluator;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateBrandub;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.search.MyIterativeDeepeningAlphaBetaSearch;

/**
 * 
 * @author A. Piretti, Andrea Galassi
 *
 */
public class SAMEClient extends TablutClient {

	private int game;
	private int timeout;
	private boolean enableNN;
	private ModelEvaluator evaluator;

	private long[] zobrist;

	public SAMEClient(String player, String name, int gameChosen, int timeout, String ipAddress, boolean enableNN)
			throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		this.game = gameChosen;
		this.timeout = timeout - 3;
		this.enableNN = enableNN;

		this.zobrist = new long[243];
		try {
			this.evaluator = new ModelEvaluator();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SAMEClient(String player, String name, int timeout, String ipAddress)
			throws UnknownHostException, IOException {
		this(player, name, 4, timeout, ipAddress, false);
	}

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		int gametype = 4;
		String role = "";
		String name = "SAME";
		String ipAddress = "localhost";
		String enableNNString = "-NN";
		boolean enableNN = false;
		int timeout = 60;
		
		//System.out.println("length is " + args.length);

		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <enable-NN>");
			System.exit(-1);
		} else { 
			System.out.println(args[0]);
			role = (args[0]);
		}
		if (args.length >= 2) {
			System.out.println(args[1]);
			try {
				timeout = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Timeout must be an integer representing seconds");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <enable-NN>");
				System.exit(-1);
			}
		}
		if (args.length >= 3) {
			try {
				timeout = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Timeout must be an integer representing seconds");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <enable-NN>");
				System.exit(-1);
			}
			ipAddress = args[2];
		}
		if (args.length == 4) {
			System.out.println(args[3]);
			if (enableNNString.equals(args[3])) {
				enableNN = true;
			} else {
				System.out.println("To enable the Neural Network you have to write -NN");
				System.out.println("USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip> <enable-NN>");
				System.exit(-1);
			}
		}
		
		System.out.println("Selected client: " + args[0]);

		SAMEClient client = new SAMEClient(role, name, gametype, timeout, ipAddress, enableNN);
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

		// attributes depends to parameters passed to main
		System.out.println("Player: " + (this.getPlayer().equals(State.Turn.BLACK) ? "BLACK" : "WHITE"));
		System.out.println("Timeout: " + this.timeout + " s");
		System.out.println("Server: " + this.serverIp);

		System.out.println("You are player " + this.getPlayer().toString() + "!");

		// INIT ZOBRIST
		this.initZobrist();

		// create iterative deepening slave
		// MyIterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> oracoloEnrico
		// =
		// new MyIterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(rules, -1,
		// 1, this.timeout, this.zobrist);

		MyIterativeDeepeningAlphaBetaSearch oracoloEnrico = new MyIterativeDeepeningAlphaBetaSearch(rules, -1, 1,
				this.timeout, this.zobrist, this.evaluator, this.enableNN);

		Future<Action> future = null;

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

			// Same for both white and black player
			ExecutorService executor = Executors.newSingleThreadExecutor();
			oracoloEnrico.setState(state);

			if (this.getPlayer().equals(Turn.WHITE)) { // WHITE
				if (state.getTurn().equals(StateTablut.Turn.WHITE)) {

					// Mio turno
					try {
						// Action a = oracoloEnrico.makeDecision(state);
						// this.write(oracoloEnrico.makeDecision(state));

						future = executor.submit(oracoloEnrico);
						Action a = future.get(timeout, TimeUnit.SECONDS);
						try {
							this.write(a);
						} catch (ClassNotFoundException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (InterruptedException | TimeoutException | ExecutionException e) {

						future.cancel(true);
						// Fallback to the best move found so far
						try {
							this.write(oracoloEnrico.results.get(0));
						} catch (ClassNotFoundException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
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
						// Action a = oracoloEnrico.makeDecision(state);
						// this.write(oracoloEnrico.makeDecision(state));

						future = executor.submit(oracoloEnrico);
						Action a = future.get(timeout, TimeUnit.SECONDS);
						try {
							this.write(a);
						} catch (ClassNotFoundException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (InterruptedException | TimeoutException | ExecutionException e) {

						future.cancel(true);
						// Fallback to the best move found so far
						try {
							this.write(oracoloEnrico.results.get(0));
						} catch (ClassNotFoundException | IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
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
			executor.shutdown();
		}

	}

	private void initZobrist() {
		// 3 pieces * 81 board positions
		int i, j;
		Random rand = new Random();

		for (i = 0; i < 9; i++) {
			for (j = 0; j < 9; j++) {
				this.zobrist[(j + i * 9) * 3] = Math.abs(rand.nextLong());
				this.zobrist[(j + i * 9) * 3 + 1] = Math.abs(rand.nextLong());
				this.zobrist[(j + i * 9) * 3 + 2] = Math.abs(rand.nextLong());
			}
		}

	}
}
