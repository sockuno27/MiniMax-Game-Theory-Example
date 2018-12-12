/**
 * #### README for basic instructions. ####
 * 
 * This is an example of a dummy program which makes random moves at each iteration of the game.
 * There are some comments to give you pointers about which functions to use to get the player's positions and the board
 */

import java.util.Random;
public class DummyAgent implements Game.Player {
	/**
	 * Build your decision-making logic around this method 
	 */
	public Game.Move chooseMove(Game g, int playerID, int timelimit) {
		
		// to get the board
		// char[][] b = g.getBoard();
		// to use a very basic function to view the board, just print out g.toString(b)

		// gives you all the legal moves for the current configuration of the board
		Game.Move[] m = g.getMoves(playerID);

		// to get exact coordinates of each player
		// g.getPos(playerID, i); -> for i = 0 to 7. gives you index i of playerID's position array

		// returning a random move
		Random rand = new Random();
		int randomNum = rand.nextInt(m.length);
		return m[randomNum];

	}
}


