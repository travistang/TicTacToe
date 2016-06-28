package tictactoe.tictactoe;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import tictactoe.tictactoe.MCTSPolicy.*;

public class MCTSPolicyTest {

	MCTSPolicy policy;
	final char[][] emptyBoard = 
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
		if(tile != 'X' && tile != 'O')
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
	public int countTile(char[][] board,char tile)
	{
		int c = 0;
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
				if(board[i][j] == tile) c++;
		}
		return c;
	}
	/**
	 * Get a random board by putting tiles randomly on the board.
	 * @return a random board
	 */
	public char[][] randomBoard()
	{
		Random r = new Random();
		char[][] board = emptyBoard.clone();
		int numTiles = r.nextInt(4);
		for(int i = 0; i < numTiles; i++)
		{
			int x,y;
			do
			{
					x = r.nextInt(3);
					y = r.nextInt(3);
			}while(board[x][y] != '_');
			board[x][y] = (i % 2 == 0) ? 'X':'O';
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
	public void testUpdateRoot()
	{
		char[][] board = emptyBoard.clone();
		policy.updateRoot(board);
		move(policy.getRoot().getData().board,'X',0,0);
		assertEquals("the update root should get a clone of board instead of the reference of board",
				9,countTile(policy.getRoot().getData().board,'_'));
	}
	@Test
	public void testSimulate()
	{
		int times = 10000;
		char[][] board = emptyBoard.clone();
		move(board, 'X',0,0);
		move(board,'O',0,1);
		move(board,'X',1,0);
		for(int i = 0 ; i < 10; i++)
		{
			float prob = policy.simulateTest(treeByBoard(board), times);
			assertTrue(0 < prob && prob < 1);
		}
		assertTrue("simulation does not alter the original board"
				,this.countTile(board, '_') == 9);
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
	public void testBackPropagate()
	{
		char board[][] = this.emptyBoard.clone();
		char x = 'X',o = 'O';
		move(board,x,0,0);
		move(board,o,1,0);
		move(board,x,1,1);
		policy.updateRoot(board);
		float oldProb = policy.getRoot().getData().prob;
		
	}

	@Test
	public void testExpand()
	{
		policy.updateRoot(this.emptyBoard);
		policy.expandTest(policy.getRoot());
		assertEquals("expand test on empty board",9,policy.getRoot().getChildren().size());
		char[][] board = this.randomBoard();
		policy.updateRoot(board);
		assertEquals("expand test on random board",
				9 - this.countTile(board, '_'),
				policy.getRoot().getChildren().size());
	}
	
	@Test
	public void testSelect()
	{
		policy.updateRoot(this.emptyBoard);
		Tree<Data> select = policy.selectTest(policy.getRoot());
		assertEquals("selection on a tree with one node",
				select,policy.getRoot());
		
		policy.expandTest(policy.getRoot());
		select = policy.selectTest(policy.getRoot());
		assertTrue("selection on a tree with level of 2",
				policy.getRoot().getChildren().contains(select));
		
		policy.simulateTest(select, 1000);
		Tree<Data> other = policy.selectTest(select);
		assertEquals("selection on a leaf",other,select);
	}
	public void uctTest() throws Exception
	{
		char[][] board = randomBoard();
		System.out.println(board);
		Tree<Data> n = new Tree<Data>(null,new Data(board,'X'));
		try
		{
			policy.updateRoot(n);
		}catch(Exception e)
		{
			throw e;
		}
		
	}
}
