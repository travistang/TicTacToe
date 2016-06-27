package tictactoe.tictactoe;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import tictactoe.tictactoe.MCTSPolicy.*;

public class MCTSPolicyTest {

	MCTSPolicy policy;
	char[][] emptyBoard = 
	{
			{'_','_','_'},
			{'_','_','_'},
			{'_','_','_'}
	};
	/**
	 * Place one tile on the given coordinate 
	 * @param board
	 * @param x
	 * @param y
	 * @return the same board instance in param
	 */
	public char[][] move(char[][] board,char tile,int x,int y)
	{
		Tree<Data> n = treeByBoard(board);
		if(tile != 'X' || tile != 'O')
			throw new IllegalArgumentException("The tile to be put should be either 'X' or 'O'");
		if(x < 0 || y < 0 || x > 2 || y > 2) 
			throw new IllegalArgumentException("The given Coordinates are invalid");
		if(MCTSPolicy.hasEnded(n))
			throw new IllegalArgumentException("No (illegal) moves can be made on the given board as it is full already");
		if(board[x][y] != '_')
			throw new IllegalArgumentException("The given coordinates are occpuied");
		board[x][y] = tile;
		return board;
	}
	/**
	 * Get a random board by putting tiles randomly on the board.
	 * @return a random board
	 */
	public char[][] randomBoard()
	{
		Random r = new Random();
		char[][] board = new char[3][3];
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j ++)
			{
				int n = r.nextInt();
				switch(n % 3)
				{
					case 0:
						board[i][j] = 'X';
						break;
					case 1:
						board[i][j] = 'O';
						break;
					default:
						board[i][j] = '_';
						break;
				}
			}
		}
		return board;
	}
	//some helper functions
	//get a tree with no parent, no child ( :( )
	//the representation of this tree is set to 'X'.
	
	public Tree<Data> emptyTree()
	{
		return new Tree<Data>(new Data(emptyBoard.clone(),'X'));
	}
	
	//some other helper functions
	// get a tree with no child attached.
	// except the game board associated to the tree is provided in the parameter
	
	public Tree<Data> treeByBoard(char[][] board)
	{
		Tree<Data> res = emptyTree();
		res.getData().board = board;
		return res;
	}
	
	@Before
	public void setUp() throws Exception {
		policy = new MCTSPolicy('X');
	}

	@Test
	public void testDecide() {
		char[][] xwin  = {{'_','X','X'},
						  {'O','O','X'},
						  {'O','O','X'},
						 },
				 
				 notEnd ={{'_','X','_'},
				 		  {'O','_','O'},
				 		  {'X','O','_'},
				 		 };
		Tree<Data> t = emptyTree();
		
	}

	@Test
	public void testUpdateRoot() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void expandTest()
	{
		policy.updateRoot(this.emptyBoard);
		policy.expandTest(policy.getRoot());
		assertEquals("expand test on empty board",9,policy.getRoot().getChildren().size());
	
	}
	public void uctTest()
	{
		char[][] board = randomBoard();
		System.out.println(board);
		Tree<Data> n = new Tree<Data>(null,new Data(board,'X'));
		policy.updateRoot(n);
		
	}
}
