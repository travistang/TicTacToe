package tictactoe.tictactoe;

public class Data implements Cloneable
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
		public Data(char[][] board)
		{
			this(board,0.5f);
		}
		/**
		 * Note: Do not call this function unless you are in selection state..
		 * ( This affects the UCT score of the particular node owning this data )
		 */
		public void visit()
		{
			visitedTimes++;
		}
		@Override
		public String toString()
		{
			return "prob:" + prob + " times:" + visitedTimes;
		}
		@Override
		public Object clone() throws CloneNotSupportedException
		{
			Data data = new Data(new char[3][3],this.prob);
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					data.board[i][j] = this.board[i][j];
				}
			}
			data.visitedTimes = this.visitedTimes;
			return data;
		}
}
