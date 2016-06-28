package tictactoe.tictactoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;

public class MCTSPolicy implements Policy{

	/**
	 * The MCTS Tree
	 * Here it is assumed that the opponent will place their token on a valid coordinate
	 * randomly ( which is naive)... 
	 */
	private Tree<Data> tree;
	private final char rep;
	private float uctConstant;
	private int simulationTimes = 10000;
	private int nodesConsider = 500;
	
	private static final char emptyBoard[][] = 
	{
		{'_','_','_'},
		{'_','_','_'},
		{'_','_','_'}
	};

	public void setNodesToConsider(int i)
	{
		nodesConsider = i;
	}
	
	public void setSimulationTimes(int i)
	{
		simulationTimes = i;
	}
	public MCTSPolicy(char rep)
	{
		this(rep,1);
	}
	
	public MCTSPolicy(char rep,float uctConstant)
	{
		this.rep = rep;
		this.uctConstant = uctConstant;
		this.tree = new Tree<Data>(new Data(emptyBoard));
	}
	
	public void setUCTConstant(float c)
	{
		uctConstant = c;
	}
	
	public float getUCTConstant()
	{
		return uctConstant;
	}
	
	@Override
	public int[] decide(char[][] board) {
		// TODO try standard MCTS Algorithm, then use Spark to solve it.
		// TODO at the end check if it's actually faster (which is a trivial result..)
		
		// update the root first
		// this will empty the tree so that a new search tree is constructed for the next move..
		this.updateRoot(board);
		// after this function there should be at least one child under the root.
		// perform the mcts algorithm and 
		//(hopefully it will) return the coordinates for the next move
		return mcts(board);
	}
	
	/**
	 * get the coordinate on which the two tiles on the corresponding are different.
	 * this assumes that there one different tile between the board or they are the same
	 * If there are more than one differences on the board the first one found will be returned. 
	 * @param ba the first board
	 * @param bb the second board
	 * @return the coordinates of which the difference lies on. This can be null
	 */
	private static int[] getBoardDifference(char[][] ba, char[][] bb)
	{
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(ba[i][j] == bb[i][j])
				{
					int res[] = new int[2];
					res[0] = i;
					res[1] = j;
					return res;
				}
			}
		}
		return null;
	}
	/**
	 * main algorithm
	 * TODO: there's probably something wrong with the visit counts.
	 * TODO: there may also be something wrong with the rule of selecting nodes
	 */
	private int[] mcts(char[][] board)
	{

		for(int i = 0; i < this.nodesConsider; i++)
		{
			//1. selection
			Tree<Data> selectedStage = tree;
			tree.print();
			while(!selectedStage.getChildren().isEmpty())
			{
				selectedStage = select(selectedStage);
			}
			
			//2. expansion
			// this add all possible stages to the selected Stage, which should not exceed 9...
			this.expand(selectedStage);
			
			//3. Simulation
			for(Tree<Data> child : selectedStage.getChildren())
			{
				float prob = this.simulate(child, this.simulationTimes);
				child.getData().prob = prob;
				child.getData().visit();
			}
			//4. back propagation
			// back-up to the front
			backPropagate(selectedStage);
		}
		// final decision
		Tree<Data> des = bestChildren(tree);
		int[] res = getBoardDifference(des.getData().board,tree.getData().board);
		if(res == null)
			throw new RuntimeErrorException(null, "the MCTS algorithm is unable to give a decision");
		return res;
		
	}
	// first stage
	private Tree<Data> select(Tree<Data> node)
	{
		// according to https://www.youtube.com/watch?v=Yf8vKTIQzHs
		if(node.getData().visitedTimes == 0)
		{
			node.getData().visit();
			return node;
		}
	
		float score = 0;
		// prevent an exception from being thrown
		// when the given node is actually a leaf of the tree
		// ( this should not happen though )
		if(node.getChildren().isEmpty())
			return node;
		// select the best children if there is any..
		Tree<Data> select = node.getChildren().get(0);
	
		for(Tree<Data> n : node.getChildren())
		{
			if(n.getData().visitedTimes == 0)
			{
				n.getData().visit();
				return n;
			}
			float s = uct(n);
			if(s > score)
			{
				score = s;
				select = n;
			}
		}
		// mark the selected state as visited. This helps in uct calculation
		select.getData().visit();
		return select;
	}
	
	/**
	 * Evaluate the UCT of a node.
	 * Reference: https://www.youtube.com/watch?v=Yf8vKTIQzHs
	 * @param node
	 * @return UCT of a node
	 */
	private float uct(Tree<Data> node)
	{
		int np = node.getData().visitedTimes;
		int ni = node.getParent().getData().visitedTimes;
		return (float) (node.getData().prob + uctConstant * Math.sqrt(Math.log(np)/ni));
	}
	
	// explore the given node by one level
	// use it for expansion ONCE, then move forward to the simulation step
	// all of the children should then be simulated in order to obtain a proper winning probability
	private void expand(Tree<Data> node)
	{
		//1. get a list of all legal moves
		char[][] board = node.getData().board;
		ArrayList<char[][]> legalMoves = new ArrayList<char[][]>();
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(board[i][j] == '_')
				{
					char[][] move = board.clone();
					move[i][j] = rep;
					legalMoves.add(move);
				}
			}
		}
		node.setChildren(
				legalMoves.stream()
				.map(b -> new Tree<Data>(node,new Data(b,0)))
				.collect(Collectors.toList())
				);
	}

	// exploitation, also used for decision making 
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
	// randomly visit states starting from given state n until it comes to an end.
	private float simulate(Tree<Data> n, int times)
	{
		if( times <= 0 ) 
			throw new IllegalArgumentException(
					"The number of times to simulate should be a positive integer");
		int winCount = 0;

		char curRep = this.rep;
		for(int i = 0; i < times; i++)
		{
			try
			{
				Tree<Data> state = treeByTree(n);
				while(!hasEnded(state))
				{
					state = randomNextState(state,curRep);
					curRep = (curRep == 'O')?'X':'O';
				}
				if(opponentWins(state)) winCount--;
				else if(iWin(state)) winCount++;
				//else do nothing
			}catch(CloneNotSupportedException e)
			{
				throw new RuntimeErrorException(null, "Failed to clone tree");
			}
		}
		return ((float)winCount / (float)times + 1)/2f;
	}
	private boolean iWin(Tree<Data> n)
	{
		char c = getWinner(n);
		return c == this.rep;
	}
	private boolean opponentWins(Tree<Data> n)
	{
		char c = getWinner(n);
		return c == ((this.rep == 'X')?'O':'X');
	}
	/**
	 * Some truths for the following boolean functions:
	 * 	1. hasEnded = getWinner || isFull
	 * 	2. !hasEnded = !getWinner && !isFull
	 * 	3. getWinner == hasEnded
	 * 	4. getWinner != isFull
	 * 	5. !getWinner != !isFull
	 */
	
	/**
	 * Check if the board associated to the given node has ended.
	 * If a game has ended, either there is a player who wins the game(hasWinner) or the board is full(isFull).
	 * @param n
	 * @return
	 */
	public static boolean hasEnded(Tree<Data> n)
	{
		return getWinner(n) != '_' || isFull(n);
	}
	
	/**
	 * Check if the board associated to the given node is full
	 * Note that a full board does not necessary mean there is a winner.
	 * It can be a draw...
	 * @param n
	 * @return
	 */
	public static boolean isFull(Tree<Data> n)
	{
		char[][] board = n.getData().board;
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(board[i][j] == '_') return false;
			}
		}
		return true;
	}
	// get a representation that wins the game
	/**
	 * The function returns non-'_' only if one of the player wins
	 * If '_' is returned, it could be:
	 * 	1. the board is full (can be properly checked by isFull)
	 * 	2. the game is on-going( means isFull returns false and this function returns '_')
	 * @param n
	 * @return
	 */
	private static char getWinner(Tree<Data> n)
	{
		char[][] data = n.getData().board;
    	for(int i = 0; i < 3; i++)
    	{
    		if(data[i][0] == data[i][1] && data[i][1] == data[i][2] && data[i][2] != '_')
    		{
    			return data[i][1];
    		}
    		if(data[0][i] == data[1][i] && data[1][i] == data[2][i] && data[2][i] != '_')
    		{
    			return data[1][i];
    		}
    	}
    	if(data[0][0] == data[1][1] && data[1][1] == data[2][2] && data[1][1] != '_') return data[1][1];
    	if(data[0][2] == data[1][1] && data[1][1] == data[2][0] && data[1][1] != '_') return data[1][1];
		return '_';
	}
	// evaluate the probability of winning given node.
	// Then return the parent of the node
	private Tree<Data> updateWinningProb(Tree<Data> n)
	{			
		int totalTimes = n.getData().visitedTimes;
		float total = n.getData().prob * totalTimes;
		for(Tree<Data> child : n.getChildren())
		{
			int times = child.getData().visitedTimes;
			totalTimes += times;
			total += child.getData().prob * times;
		}
		n.getData().prob = total/totalTimes;
		return n.getParent();
	}
	// backpropagation for the mcts algorithm
	private void backPropagate(Tree<Data> n)
	{
		while(n != null)
		{
			n = updateWinningProb(n);
		}
	}
	/**
	 * Randomly play out the given state n with 'rep' as 'representation' 
	 * @param state
	 * @return
	 */
	private Tree<Data> randomNextState(Tree<Data> state,char rep)
	{
		if(rep != 'O' && rep != 'X') 
			throw new IllegalArgumentException("Invalid representation");
		if(hasEnded(state)) 
			throw new IllegalArgumentException("A terminated state has no next state");
		
		Random r = new Random();
		
		int x = 0,y = 0;
		do
		{
			x = r.nextInt(3);
			y = r.nextInt(3);
		}while(state.getData().board[x][y] != '_');
		
		state.getData().board[x][y] = rep;
		return state;	
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
		char[][] board = new char[3][3];
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				board[i][j] = curboard[i][j];
			}
		}
		tree = new Tree<Data>(
				new Data(board,0));
	}
	public void updateRoot(Tree<Data> n) throws CloneNotSupportedException
	{
		tree = treeByTree(n);
	}
	public Tree<Data> treeByTree(Tree<Data> n) throws CloneNotSupportedException
	{
		char[][] board = new char[3][3];
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				board[i][j] = n.getData().board[i][j];
			}
		}
		Tree<Data> res = new Tree<Data>(new Data(board,0));
		res.getData().prob = n.getData().prob;
		res.getData().visitedTimes = n.getData().visitedTimes;
		return res;
	}
	/****************************************************
	 *
	 * 
	 * 
	 * 
	 * The method below are used for unit-testing the private methods
	 * TODO remove the methods below after the testing
	 *
	 *
	 *
	 *
	 *
	 ****************************************************/
	public void expandTest(Tree<Data> node)
	{
		expand(node);
		
	}
	public char getRep()
	{
		return rep;
	}
	public Tree<Data> randomNextStateTest(Tree<Data> state,char rep)
	{
		return randomNextState(state,rep);
	}
	public static char getWinnerTest(Tree<Data> board)
	{
		return getWinner(board);
	}
	public Tree<Data> getRoot()
	{
		return tree;
	}
	public Tree<Data> bestChildrenTest(Tree<Data> n)
	{
		return bestChildren(n);
	}
	public boolean opponentWinsTest(Tree<Data> n)
	{
		return opponentWins(n);
	}
	public float simulateTest(Tree<Data> n,int times)
	{
		return simulate(n,times);
	}
	public float uctTest(Tree<Data> n)
	{
		return uct(n);
	}
	public Tree<Data> selectTest(Tree<Data> n)
	{
		return select(n);
	}
	
}
