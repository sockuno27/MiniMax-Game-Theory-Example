import java.util.concurrent.TimeoutException;
/**
 * #### README for basic instructions. ####
 * 
 * This is the stub for a new player. Please rename the class *and* file to include your lastname,
 * then implement your player here. When complete, please email this file to 
 * sohamde AT cs.umd.edu. Make sure to include "cmsc474 p1 submission"
 * in the subject line.
 */


public class P1_Nguyen implements Game.Player {
	// Constants for the game
	public static final int TIME_BUFFER = 20;
	public static final int MAX = 1;
	public static final int MIN = 0;
	public static final int MAX_DEPTH = 10;
	public static final int MAX_NUM_POS_MOVES = 48;
	public static final int NUM_INDEX = 8;
	public static final int MAX_SCORE = 1000;
	public static final int WIN_SCORE = 100;
	public static final int START_POS_PENALTY = 1 ;
	public static final int CENTER_POINT = 1;
	public static final int BLOCKING_POINT = 1 ;
	public static final int INLINE_POINT = 1;
	public static final int[][] START_POS = new int[][]{{2,0,4,9,6,10,8,3}, {2,8,4,1,6,2,8,11}};
	public static final int[][] PAIRS = new int[][] {{0,2}, {0, 4}, {0, 6}, {2, 4}, {2, 6}, {4, 6}};
	public static final int[][] TRIPLES = new int[][] {{0,2,4},{0,2,6},{0,4,6},{2,4,6}};
	
	
/**
 * default constructor
 */
	public P1_Nguyen() {	
	}
	
	/**
	 * This method provide the AI logic for the game. Using minimax search on the game tree
	 * with alpha-beta pruning and node ordering. To keep the search time below the timelimit,
	 *  this method using iterative deepening search to progressively search the game tree
	 *  starting from node depth of 1 up to node depth of MAX_DEPTH or until the timelimit is
	 *  up. Return the last successful search best move result.
	 *  
	 *  To minimize the number of memory allocations and java garbage collections, therefore 
	 *  increase performance, this method uses preallocated arrays to store the available moves
	 *  at every call to alphaBeta. At each node depth d in the game tree, the algorithm will 
	 *  uses the array moves[d-1] to store and rank the moves. The evaluate method also uses
	 *  a preallocated array to hold the players' positions.
	 *  
	 *   The evaluate method estimates value for the current node as followed:
	 * initialize score = 0.
	 * score -START_POS_PENALTY for each MAX pieces in the start positions.
	 * score +START_POS_PENALTY for each MIN pieces in the start positions.
	 * score +CENTER_POINT for each MAX piece in center board (3<x<7, 3<y<7)
	 * score -CENTER_POINT for each MIN piece in center board (3<x<7, 3<y<7)
	 * score +INLINE_POINT for each pair or triple of MAX pieces lined up.(double counting)
	 * score -INLINE_POINT for each pair or triple of MIN pieces lined up.(double counting)
	 * score +BLOCKING_POINT for every two MIN pieces a MAX piece stand between.
	 * score -BLOCKING_POINT for every two MAX pieces a MIN piece stand between.
	 *  
	 *  /* I pledge on my honor that I have not given or received any unauthorized assistance 
	 *  on this project. (Trung Nguyen, trungnguyensy@gmail.com) 
	 */
	public Game.Move chooseMove(Game g, int playerID, int timelimit) {
		Node result = new Node();
		Game game = new Game(g); // make a copy of the game to run simulation on it.
		int[][] playerPos = initializePlayerPosArray(); // preallocated positions array 
		Node[][] moves = initializeMovesArray(); // preallocated moves array
		// calculate the system time we need our search to end at
		long time = System.currentTimeMillis() + timelimit - TIME_BUFFER; 
		try {
			for (int d = 1; d <= MAX_DEPTH; d ++) //run alpha-beta with increasing search depth
				result = alphaBeta(game, playerID, d, - MAX_SCORE, MAX_SCORE, playerPos, moves, time);
		} catch (TimeoutException e) { // timeout, do nothing.		
		}
		// return the best move calculate by the last successful search
		return result.getMove();
	}
	
	/**
	 * Helper method to allocate and initialize a 2 x NUM_INDEX array of integer.
	 * @return the allocated array
	 */
	private int[][] initializePlayerPosArray() {
		int[][] player = new int[2][];
		player[0] = new int[NUM_INDEX];
		player[1] = new int[NUM_INDEX];
		return player;
	}
	
	/**
	 * Helper method to allocate and initialize a 2 dimensional array to stores
	 * the moves used in the alphaBeta method.
	 * @return the allocated array.
	 */
	private Node[][] initializeMovesArray() {
		Node[][] moves = new Node[MAX_DEPTH][];
		for (int i = 0; i < MAX_DEPTH; i++) {
			Node[] v = new Node[MAX_NUM_POS_MOVES];
			for (int j = 0; j < MAX_NUM_POS_MOVES; j++)
				v[j] = new Node();
			moves[i] = v;
		}
		return moves;
	}
	
	/**
	 * This method perform alpha-beta pruning minimax search on the game tree using node ordering.
	 * @param game : a reference to a copy of the game.
	 * @param playerID : the player MIN or MAX.
	 * @param d : current depth in the game tree.
	 * @param alpha : lower bound alpha of the cut-off.
	 * @param beta : upper bound beta of the cut-off.
	 * @param playerPos : the preallocated array used in the evaluation method.
	 * @param moves : the preallocated array to be used to store the available moves.
	 * @param time : the system time if exceeded the method should throw a TimeoutException.
	 * @return a Node with contain a bestMove and the minimax return value.
	 * @throws TimeoutException when the method detected it has exceed the time specified.
	 */
	private Node alphaBeta(Game game, int playerID, int d, int alpha, int beta,
							int[][] playerPos, Node[][] moves, long time) throws TimeoutException {
		if (System.currentTimeMillis() > time) // ran out of time
			throw new TimeoutException();
		int v = 0; // score
		Game.Move bestMove = null;
		Node[] moveList = null; // list to hold all possible moves
		if (playerID == MAX && game.win(MAX)) // if Max won, return positive win_score
			return new Node(null, WIN_SCORE);
		else if (playerID == MIN && game.win(MIN)) // if Min won. return negative win_score
			return new Node(null, - WIN_SCORE);
		if (d == 0) // reach lowest depth, return evaluated score
			return new Node(null, evaluate(playerPos, game));
		else { // depth >= 1
			int numMoves = 0; //initialize number of available moves
			for (Game.Move move : game.getMoves(playerID))  { // for each available move
				game.makeMove(playerID, move); // simulate making the move
				// save the moves and evaluated scores in the nodes at moves array at position d - 1
				moves[d - 1][numMoves++].setMoveAndValue(move, evaluate(playerPos, game));
				game.makeMove(playerID, move.reverse()); // reverse the move
			}
			moveList = moves[d - 1];
			if (playerID == MAX) { //Max move
				v = - MAX_SCORE; // sort available moves by descending order of evaluated values
				selectionSort(moveList, numMoves, false);
				for (int i = 0; i < numMoves; i++) { //make a move for each available move
					Node move = moveList[i];
					game.makeMove(playerID, move.getMove()); // make the move
					Node score = alphaBeta(game, MIN, d - 1, alpha, beta, playerPos, moves, time);
					game.makeMove(playerID, move.getMove().reverse()); // reverse the move
					if (v < score.getValue()) { // best move so far
						v = score.getValue();
						bestMove = move.getMove(); //save this move
					}
					if (v >= beta) // beta pruning
						return new Node(bestMove, v);
					alpha = Math.max(alpha, v);
						
				}
			}else if (playerID == MIN) { // Min move
				v = MAX_SCORE; // sort moves by ascending order
				selectionSort(moveList, numMoves, true);
				for (int i = 0; i < numMoves; i++) {
					Node move = moveList[i];
					game.makeMove(playerID, move.getMove()); // make the move
					Node score = alphaBeta(game, MAX, d - 1, alpha, beta, playerPos, moves, time);
					game.makeMove(playerID, move.getMove().reverse()); // reverse the move
					if (v > score.getValue()) { // best move so far
						v = score.getValue();
						bestMove = move.getMove(); // save this move
					}
					if (v <= alpha) // alpha pruning
						return new Node(bestMove, v);
					beta = Math.min(beta, v);
				}
			}
		return new Node(bestMove, v);
		}
	}
	/**
	 * This method sort the array in ascending or descending order.
	 * @param v : the array to be sorted.
	 * @param size : the number of items to be sorted, starting from the first.
	 * @param increasing : true if ascending order, false for descending order.
	 */
	private void selectionSort(Node[] v, int size, boolean increasing) {
		int index;
		Node swap = new Node();
		for (int i = 0; i < size - 1; i ++) {
			index = i;
			for (int j = i + 1; j < size; j ++) {
				if((increasing && v[index].compareTo(v[j]) > 0) ||
						(!increasing && v[index].compareTo(v[j]) < 0))
						index = j;
			}
			if (i != index) {
				swap.copy(v[i]);
				v[i].copy(v[index]);
				v[index].copy(swap);
			}
		}
	}
	/**
	 * This method evaluate an estimated value for the current node as followed:
	 * score -START_POS_PENALTY for each MAX pieces in the start positions.
	 * score +START_POS_PENALTY for each MIN pieces in the start positions.
	 * score +CENTER_POINT for each MAX piece in center board (3<x<7, 3<y<7)
	 * score -CENTER_POINT for each MIN piece in center board (3<x<7, 3<y<7)
	 * score +INLINE_POINT for each pair or triple of MAX pieces lined up.
	 * score -INLINE_POINT for each pair or triple of MIN pieces lined up.
	 * score +BLOCKING_POINT for each MAX piece stand between two MIN pieces.
	 * score -BLOCKING_POINT for each MIN piece stand between two MAX pieces.
	 * @param player : the pre-allocated array to store players' positions.
	 * @param game : the reference to the game.
	 * @return a integer score for the current node.
	 */
	private int evaluate(int[][] player, Game game) {
		int score = 0;
		
		for (int i = 0; i < NUM_INDEX; i ++) {
			player[MAX][i] = game.getPos(MAX, i);
			player[MIN][i] = game.getPos(MIN, i);
		}
		score -= startPositions(player[MAX], MAX);
		score += startPositions(player[MIN], MIN);
		score += centerPositions(player[MAX]);
		score -= centerPositions(player[MIN]);
		score += inlinePositions(player[MAX]);
		score -= inlinePositions(player[MIN]);
		score += blockingPositions(player[MAX], player[MIN]);
		score -= blockingPositions(player[MIN], player[MAX]);
		
		return score;
		
	}
	/**
	 * helper method to score how many pieces in the center board(3<x<7, 3<y<7)
	 * @param p : a player pieces' positions
	 * @return an integer score.
	 */
	private int centerPositions(int p[]) {
		int score = 0;
		for (int i = 0; i < NUM_INDEX; i += 2) {
			if (p[i] >= 4 && p[i] <= 6 && p[i+1] >= 4 && p[i+1] <= 6)
				score += CENTER_POINT;
		}
		return score;
	}
	
	/**
	 * Helper method to score how many pieces currently in the start positions.
	 * @param positions : player pieces' positions.
	 * @param playerID : the player MAX or MIN.
	 * @return an integer score.
	 */
	private int startPositions(int[] positions, int playerID) {
		int score = 0;
		for (int i=0; i<NUM_INDEX - 1; i+=2) {
			for (int j=0; j<NUM_INDEX - 1; j+=2) {
				if (START_POS[playerID][i] == positions[j] && 
					START_POS[playerID][i+1] == positions[j+1]) {
					score += START_POS_PENALTY;
				}
			}
		}
		return score;
	}
	
	/**
	 * Helper method to score how many pieces are lined up in a straight line.
	 * @param pos : the pieces' positions.
	 * @return an integer score.
	 */
	private int inlinePositions(int [] pos) {
		int score = 0;
		for (int[] pair : PAIRS) {
			if (pos[pair[0]] == pos[pair[1]] ||    //horizontal pair
					pos[pair[0] + 1] == pos[pair[1] + 1] || //NE-SW pair
					pos[pair[0]] - pos[pair[1]] == pos[pair[0] + 1] - pos[pair[1] + 1]) // NW_SE pair
				score += INLINE_POINT;
		}
		for(int[] triple : TRIPLES) {
			if (pos[triple[0]] == pos[triple[1]] && pos[triple[0]] == pos[triple[2]] ||    //horizontal triples
					pos[triple[0] + 1] == pos[triple[1] + 1] && pos[triple[0]+1] == pos[triple[2]+1] || //NE-SW triple
					pos[triple[0]] - pos[triple[1]] == pos[triple[0] + 1] - pos[triple[1] + 1] &&
					pos[triple[0]] - pos[triple[2]] == pos[triple[0] + 1] - pos[triple[2] + 1]) // NW_SE triple
				score += INLINE_POINT;
		}
		return score;
	}
	
	/**
	 * Helper method to score the number of pieces in between opponent's pieces.
	 * @param player1 : player1 pieces positions.
	 * @param player2 : player2 pieces positions.
	 * @return an integer score.
	 */
	private int blockingPositions(int[] player1, int [] player2) {
		int score = 0;
		for (int[] pair : PAIRS) {
			if (player2[pair[0]] == player2[pair[1]]) { // player2 horizontal pair
				for (int i = 0; i < NUM_INDEX; i += 2)
					if (player1[i] == player2[pair[0]] && 
						(player1[i+1] - player2[pair[0]+1]) * (player1[i+1] - player2[pair[1] + 1]) < 0) {
						score += BLOCKING_POINT;
						break;
					}
			} else if (player2[pair[0]+1] == player2[pair[1]+1]) { //player2 NE_SW pair
				for(int i = 0; i < NUM_INDEX; i += 2) 
					if (player1[i+1] == player2[pair[0]+1] &&
						(player1[i] - player2[pair[0]]) * (player1[i] - player2[pair[1]]) < 0) {
						score += BLOCKING_POINT;
						break;
					}
				//player2 NW-SE pair
			} else if (player2[pair[0]] - player2[pair[1]] == player2[pair[0]+1] - player2[pair[1]+1]) {
				for (int i = 0; i < NUM_INDEX; i += 2)
					if (player1[i] - player2[pair[0]] == player1[i+1] - player2[pair[0]+1] &&
							(player1[i] - player2[pair[0]]) * (player1[i] - player2[pair[1]]) < 0 &&
							(player1[i+1] - player2[pair[0]+1]) * (player1[i+1] - player2[pair[1] + 1]) < 0) {
						score += BLOCKING_POINT;
						break;
					}
			}
		}
		return score;
	}
	
	/**
	 * 
	 * This is an inner class to hold a move and its score value. This will be used in the move array
	 * in the alphaBeta method.
	 *
	 */
	private class Node implements Comparable<Node> {
		private Game.Move move;
		private int value;
		
		public Node() {
			move = null;
			value = 0;
		}
		
		public Node(Game.Move move, int val) {
			this.move = move;
			this.value = val;
		}
		
		/**
		 * @return the move
		 */
		public Game.Move getMove() {
			return move;
		}

		public int getValue() {
			return value;
		}
		
		public void setMoveAndValue(Game.Move move, int val) {
			this.move = move;
			this.value = val;
		}
		
		public void copy(Node node) {
			this.value = node.value;
			this.move = node.move;
		}

		@Override
		public int compareTo(Node other) {
				return this.value - other.value;
		}	
	}
	
}


