/**
 * Human player code
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HumanPlayer implements Game.Player {
	static BufferedReader _br = new BufferedReader(new InputStreamReader(System.in));	// do not use Scanner, as it's difficult to interrupt its blocking methods http://stackoverflow.com/questions/12803151

	@Override
	public Game.Move chooseMove(Game g, int playerID, int timelimit) {
		char[][] b = g.getBoard();

		for (int i=0; i<4; i++) {
			b[g.getPos(playerID, 2*i)][g.getPos(playerID, 2*i+1)] = (char)(i+'a');
			b[g.getPos((playerID+1)%2, 2*i)][g.getPos((playerID+1)%2, 2*i+1)] = '*';
		}
		System.out.println("\n" + g.toString(b) + "Time limit = " + timelimit + "ms");
		System.out.print("Player "+(playerID+1)+": Which piece (a, b, c or d)? ");
		
		try {
			int input = getInput();
			int fromx = g.getPos(playerID, input*2);
			int fromy = g.getPos(playerID, input*2+1);
			Game.Move[] m = g.getMoves(playerID);
			int baseIndex = -1;
			for (int i=0; i<4; i++) {
				b[g.getPos(0, 2*i)][g.getPos(0, 2*i+1)] = '1';
				b[g.getPos(1, 2*i)][g.getPos(1, 2*i+1)] = '2';
			}
			for (int i=0; i<m.length; i++) {
				if (m[i]._fromx==fromx && m[i]._fromy==fromy) {
					if (baseIndex<0) baseIndex = i;		// assume Moves are grouped by pieces
					b[m[i]._tox][m[i]._toy] = (char)(i-baseIndex+'a');
				}
			}
			System.out.print(g.toString(b) + "Player "+(playerID+1)+": Which move (a to "+(char)('a'+m.length-1)+")? ");
			return m[getInput()+baseIndex];
		}
		catch (Exception e) {
			return null;
		}
	}
	
	int getInput() throws Exception {
		while (!_br.ready()) {	// ref: http://stackoverflow.com/questions/4983065
			Thread.sleep(100);
			if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
		}
		char c = _br.readLine().charAt(0);
		return c-'a';
	}
}


