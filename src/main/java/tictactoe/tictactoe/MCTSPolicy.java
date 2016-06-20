package tictactoe.tictactoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
		/**
		 * Note: Do not call this function unless you are in selection state..
		 * ( This affects the UCT score of the particular node owning this data )
		 */
		public void visit()
		{
			visitedTimes++;
		}
	}

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
	 * main algorithm
	 */
	private void mcts()
	{
		for(int i = 0; i < this.nodesConsider; i++)
		{
			//1. selection
			Tree<Data> selectedStage = tree;
			while(!selectedStage.getChildren().isEmpty())
			{
				selectedStage = select(selectedStage);
			}
			
			//2. expansion
			// this add all possible stages to the selected Stage, which should not exceed 9...
			this.expand(selectedStage);
			
			//3. Simulation
			float prob = this.simulate(selectedStage, this.simulationTimes);
			//4. back propagation
			selectedStage.getData().prob = prob;
			// back-up to the front
			while(selectedStage != tree)
			{
				prob = selectedStage.getData().prob;
				selectedStage = selectedStage.getParent(); // this traverse back to the tree
				float oldProb = selectedStage.getData().prob;
				int times = selectedStage.getData().visitedTimes;
				selectedStage.getData().prob = (oldProb * times + prob) / (oldProb + 1);
			}
		}
	}
	// first stage
	private Tree<Data> select(Tree<Data> node)
	{
		// according to https://www.youtube.com/watch?v=Yf8vKTIQzHs
		if(node.getData().visitedTimes == 0) return node;
	
		float score = 0;
		// select the first children by default
		Tree<Data> select = node.getChildren().get(0);
	
		for(Tree<Data> n : node.getChildren())
		{
			if(n.getData().visitedTimes == 0) return n;
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
	// use it for expansion ONCE, then just randomly pick a 
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

	// randomly visit states starting from given state n until it comes to an end.
	private float simulate(Tree<Data> n, int times)
	{
		if( times <= 0 ) 
			throw new IllegalArgumentException(
					"The number of times to simulate should be a positive integer");
		int winCount = 0;
		Tree<Data> state = n;
		char curRep = this.rep;
		for(int i = 0; i < times; i++)
		{
			while(!hasEnded(state))
			{
				state = randomNextState(state,curRep);
				curRep = (curRep == 'O')?'X':'O';
			}
			if(opponentWins(state)) winCount--;
			else winCount++;
		}
		return winCount / times;
	}
	
	private boolean opponentWins(Tree<Data> n)
	{
		char c = getWinner(n);
		if(c == '_') throw new IllegalArgumentException("Game needs to be ended in order to tell who the winner is");
		return c != this.rep;
	}
	
	private static boolean hasEnded(Tree<Data> n)
	{
		return getWinner(n) != '_';
	}
	// get a representation that wins the game
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
		tree = new Tree<Data>(
				new Data(curboard,currentWinningProbability()));
	}
	/**
	 * The method below are used for unit-testing the private methods
	 * TODO remove the methods below after the testing
	 */
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
}
