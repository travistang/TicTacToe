package tictactoe.tictactoe;

public class MCTSPolicy implements Policy{

	/**
	 * The MCTS Tree
	 * Here it is assumed that the opponent will place their token on a valid coordinate
	 * randomly ( which is naive)... 
	 */
	private Tree<Data> tree;
	private final char rep;
	private class Data
	{
		char[][] board;
		// probability of the one who adopts this policy wins the game
		float prob;
		int visitedTimes;
		public Data(char[][] board,float prob)
		{
			this.board = board;
			this.prob = prob;
			this.visitedTimes = 0;
		}
		
		public void visit()
		{
			visitedTimes++;
		}
		
	}

	public MCTSPolicy(char rep)
	{
		this.rep = rep;
	}
	
	@Override
	public int[] decide(char[][] board) {
		// TODO this is the most interesting part
		// try standard MCTS Algorithm, then use Spark to solve it.
		// at the end check if it's actually faster (which is a trivial result..)
		// after this function there should be at least one child under the root.
		mcts();
		char nextBoard[][] = bestChildren(tree).getData().board;
		char curBoard[][] = tree.getData().board;
		/**
		 * figure out where the next move should be
		 */
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(curBoard[i][j] != nextBoard[i][j])
				{
					int[] res = new int[2];
					res[0] = i;
					res[1] = j;
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * Play-out
	 * @param board
	 * @param x
	 * @param y
	 * @param representation
	 * @return
	 */
	private char[][] play(char[][] board,int x, int y,char representation)
	{
		if(x < 0 || x > 2 || y < 0 || y > 2)
			throw new IllegalArgumentException("invalid index");
		if(representation != 'X' && representation != 'Y')
			throw new IllegalArgumentException("Invalid representation");
		if(board[x][y] != '_')
			throw new IllegalArgumentException("Coordinate specified is occupied");
		
		board[x][y] = representation;
		return board;
	}
	/**
	 * main algorithm
	 */
	private void mcts()
	{
		
	}
	// first stage
	private float select(Tree<Data> node, int lv)
	{
		if(lv < 0) 
			throw new IllegalArgumentException("The required number of level "
					+ "to be explored under given node must be a positive integer");
		float prob = node.getData().prob;
		
		return prob;
	}
	private void expand(Tree<Data> node,int lv)
	{
	
	}
	/*
	 * 
	 */
	private void updateProbability(Tree<Data> node)
	{
		Tree<Data> next;
		float curProb = 0;
		for(Tree<Data> child : node.getChildren())
		{
			float p;
			if((p = child.getData().prob) > curProb)
			{
				next = child;
				curProb = p;
			}
		}
		
	}
	// exploitation, also used to tell the decision from the move
	private Tree<Data> bestChildren(Tree<Data> n)
	{
		Tree<Data> res = null;
		float prob = 0;
		for(Tree<Data> child : n.getChildren())
		{
			if(child.getData().prob > prob)
			{
				prob = child.getData().prob;
				res = child;
			}
		}
		return res;
	}
	public float currentWinningProbability()
	{
		return tree.getData().prob;
	}
	/**
	 * This methods is used by the AIPlayer to update the new root after playing the game.
	 * @param curboard
	 */
	public void updateRoot(char[][] curboard)
	{
		tree = new Tree<Data>(
				new Data(curboard,currentWinningProbability()));
	}
	
	private boolean isTerminalNode(Tree<Data> n)
	{
		char[][] board = n.getData().board;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(board[i][j] == '_') return false;
		return true;
	}
}
