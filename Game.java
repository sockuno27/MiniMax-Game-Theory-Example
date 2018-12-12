import java.util.Vector;
import java.util.concurrent.*;

/**
 * Game engine for CMSC474 project 1
 * It keeps track of the positions of pieces, and provides functions for game playing
 * Please change the arguments to the g.start function under main() to use different agents
 *
 * @author Kan-Leung Cheng
 * Modified by Soham De
 */
public class Game {
	/** Interface of all players.  Your player class also needs to implement this interface. */
	interface Player {
		/**
		 * @param g - the game to be played
		 * @param playerID - your player ID, 0 or 1
		 * @param timelimit - time limit in ms
		 * @return a chosen move
		 */
		Game.Move chooseMove(Game g, int playerID, int timelimit);
	}

	static class Move {
		public int _fromx, _fromy, _tox, _toy;
		public Move(int fromx, int fromy, int tox, int toy) {
			_fromx = fromx;
			_fromy = fromy;
			_tox = tox;
			_toy = toy;
		}
		public Move reverse() {
			return new Move(_tox, _toy, _fromx, _fromy);
		}
		@Override public String toString() {
			return _fromx+","+_fromy+"->"+_tox+","+_toy;
		}
	}

	private final int[][] _piecePos = new int[][]{{2,0,4,9,6,10,8,3}, {2,8,4,1,6,2,8,11}}; // pieces' positions (always sorted) for each player

	public static final char EMPTY = '.';	// symbol for empty space on board
	public static final char  WALL = ' ';	// symbol for wall on board
	private static final int[][] START_POS = new int[][]{{2,0,4,9,6,10,8,3}, {2,8,4,1,6,2,8,11}}; // starting positions

	public Game() {}

	/** Copy constructor */
	public Game(Game g) { this(g._piecePos); }

	/** Builds a Game object by filling in the _piecePos array */
	public Game(int[][] pos) {
		for (int i=0; i<_piecePos.length; i++) {
			for (int j=0; j<_piecePos[0].length; j++) {
				_piecePos[i][j] = pos[i][j];
			}
		}
	}
	
	/** Return true if and only if the player's pieces form a straight line, with none of the opponent's markers in between, and none of the player's pieces in any of the four starting positions. */
	public boolean win(int playerID) {
		for (int i=0; i<_piecePos[0].length; i+=2) {
			for (int j=0; j<_piecePos[0].length; j+=2) {
				if (_piecePos[playerID][i] == START_POS[playerID][j] && _piecePos[playerID][i+1] == START_POS[playerID][j+1]) {
					return false;
				}
			}
		}
		char[][] b = getBoard();
		return win(playerID, b, 1, 1) || win(playerID, b, 1, 0) || win(playerID, b, 0, 1);
	}
	/** Helper function for win(int playerID) */
	private boolean win(int playerID, char[][] b, int dx, int dy) {
		boolean foundSecond = false;
		boolean foundThird = false;
		for (int i=_piecePos[playerID][0]+dx, j=_piecePos[playerID][1]+dy; i<b.length && j<b[0].length; i+=dx, j+=dy) {
			if (b[i][j]==playerID+'1') {
				if (foundThird) return true;
				else if (foundSecond) foundThird = true;
				else foundSecond = true;
			}
			else if (b[i][j]!=EMPTY) {
				return false;
			}
		}
		return false;
	}

	/** Get the position of the specified piece */
	public int getPos(int playerID, int pieceIndex) {
		return _piecePos[playerID][pieceIndex];
	}
	
	/** Get all legal moves. The pieces can move in a straight line, any number of tiles, in any of the six possible directions. Pieces may not jump over other pieces and may not go off of the board, and only one piece may be at any given board position. */
	public Move[] getMoves(int playerID) {
		char[][] b = getBoard();
		Vector<Move> m = new Vector<Move>();
		for (int i=0; i<_piecePos[0].length; i+=2) {
			int fromx = getPos(playerID, i);
			int fromy = getPos(playerID, i+1);
			addMoves(m, b, fromx, fromy, 1, 1);
			addMoves(m, b, fromx, fromy, -1, -1);
			addMoves(m, b, fromx, fromy, 1, 0);
			addMoves(m, b, fromx, fromy, -1, 0);
			addMoves(m, b, fromx, fromy, 0, 1);
			addMoves(m, b, fromx, fromy, 0, -1);
		}
		return m.toArray(new Move[m.size()]);
	}
	/** Helper function for getMoves(int playerID) */
	private static void addMoves(Vector<Move> m, char[][] b, int fromx, int fromy, int dx, int dy) {
		for (int i=fromx+dx, j=fromy+dy; i<b.length && i>=0 && j<b[0].length && j>=0; i+=dx, j+=dy) {
			if (b[i][j]==EMPTY) m.add(new Move(fromx,fromy,i,j));
			else break;
			if (i==fromx+dx+dx && j==fromy+dy+dy) break;
		}
	}
	
	/** Performs a move with validity check.  Slower than the makeMove method. */
	public void makeMoveWithCheck(int playerID, Move m) {
		for (int i=0; i<_piecePos[0].length; i+=2) {
			if (_piecePos[playerID][i]==m._fromx && _piecePos[playerID][i+1]==m._fromy) {
				for (Move move:getMoves(playerID))
					if (move._fromx==m._fromx && move._fromy==m._fromy && move._tox==m._tox && move._toy==m._toy) {
						_piecePos[playerID][i] = m._tox;
						_piecePos[playerID][i+1] = m._toy;
						// java.util.Arrays.sort(_piecePos[playerID]);
						sort_tuples(_piecePos[playerID]);
						return;
					}
				throw new IllegalArgumentException("Invalid m._to");
			}
		}
		throw new IllegalArgumentException("Invalid m._from");
	}
	
	/** Performs a move without any validity check.  Faster than the makeMoveWithCheck method. */
	public void makeMove(int playerID, Move m) {
		for (int i=0; i<_piecePos[0].length; i+=2) {
			if (_piecePos[playerID][i]==m._fromx && _piecePos[playerID][i+1]==m._fromy) {
				_piecePos[playerID][i] = m._tox;
				_piecePos[playerID][i+1] = m._toy;
				sort_tuples(_piecePos[playerID]);
				//java.util.Arrays.sort(_piecePos[playerID]);
				return;
			}
		}
	}
	/** Helper function for makeMove and makeMoveWithCheck */
	private static void sort_tuples(int[] posArray) {
		int[] totalPos = new int[posArray.length/2];
		int temp;
		for (int i=0; i<posArray.length; i+=2) {
			totalPos[i/2] = posArray[i]*10 + posArray[i+1];
		}
		java.util.Arrays.sort(totalPos);
		for (int i=0; i<totalPos.length; i++) {
			for (int j=2*i; j<posArray.length; j+=2) {
				if (totalPos[i] == posArray[j]*10+posArray[j+1]) {
					temp = posArray[2*i]; posArray[2*i] = posArray[j]; posArray[j] = temp;
					temp = posArray[2*i+1]; posArray[2*i+1] = posArray[j+1]; posArray[j+1] = temp;
					break;
				}
			}
		}
	}
	
	/** Get the board in char array representation */
	public char[][] getBoard() {
		char[][] b = new char[][]{
			{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL, WALL, WALL, WALL},
			{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL, WALL, WALL},
			{EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL, WALL},
			{WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL, WALL},
			{WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL},
			{WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL, WALL},
			{WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL},
			{WALL, WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL, WALL},
			{WALL, WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL},
			{WALL, WALL, WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL},
			{WALL, WALL, WALL, WALL, WALL, EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY, EMPTY, WALL}};
		for (int i=0; i<_piecePos.length; i++) {
			for (int j=0; j<_piecePos[0].length; j+=2) {
				b[_piecePos[i][j]][_piecePos[i][j+1]] = (char)(i+'1');
			}
		}
		return b;
	}

	/** Provides the String of the board to be printed. */
	@Override
	public String toString() { return toString(getBoard()); }

	public String toString(char[][] b) {
		StringBuffer s = new StringBuffer();
		for (int i=0; i<b.length; i++) {
			s.append("           ".substring(i));
			for (int j=0; j<b[0].length; j++) s.append(" " + b[i][j]);
			s.append("\n");
		}
		return s.toString();
	}
	
	/** Helper function for start method */
	private boolean chooseAndMakeMove(final Player p, final int timelimit, final int playerID) {
		ExecutorService executor = Executors.newSingleThreadExecutor(); // ref: http://stackoverflow.com/questions/2275443
		Future<Move> future = executor.submit(new Callable<Move>() { public Move call() throws Exception { return p.chooseMove(new Game(_piecePos), playerID, timelimit); } });
		
		try {
			makeMoveWithCheck(playerID, future.get(timelimit, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Timeout! Player "+(playerID+1)+" loses.");
			return false;
		}
		finally {
			executor.shutdownNow();	// need to place it in finally block to shutdown properly even when timeout
		}
		return true;
	}
	
	/** Starts a game between 2 players with timelimits */
	public void start(Player p1, int timelimit1, Player p2, int timelimit2) {
		boolean p1win = false;
		boolean p2win = false;
		int player_turn = 1;
		while (!p1win && !p2win) {
			// if (!chooseAndMakeMove(p1, timelimit1, 0) || !chooseAndMakeMove(p2, timelimit2, 1)) break;
			if (player_turn == 1) {
				if (!chooseAndMakeMove(p1, timelimit1, 0)) break;	
				player_turn = 2;
			}
			else if (player_turn == 2) {
				if (!chooseAndMakeMove(p2, timelimit2, 1)) break;
				player_turn = 1;
			}
			p1win = win(0);
			p2win = win(1);
			if (p1win && p2win) System.out.println("Draw!");
			else if (p1win) System.out.println("Player 1 wins!");
			else if (p2win) System.out.println("Player 2 wins!");
		}
	}


	public static void main(String[] args) {
		Game g = new Game();
		//g.start(new HumanPlayer(), 50000, new HumanPlayer(), 50000);

		// Arguments are as follows: Player 1, Time limit for player 1, Player 2, time limit for player 2. Time limits are in milliseconds.
		g.start(new DummyAgent(), 500, new P1_Nguyen(), 500);
	}
}


