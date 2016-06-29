package tictactoe.tictactoe;
import java.util.ArrayList;
import java.util.Random;
public interface Policy {
	public int[] decide(char[][] board);
	
	// randomly pick a coordinates
	public Policy defaultPolicy = new Policy()
	{
		public int[] decide(char[][] board)
		{
			Random r = new Random();
			ArrayList<int[]> amoves = availableMoves(board);
			
			if(amoves.size() == 0)
				throw new IllegalArgumentException("No moves available");
			
			int i = r.nextInt(amoves.size());
			return amoves.get(i);
		}
		
		private ArrayList<int[]>availableMoves(char[][] board)
		{
			ArrayList<int[]> moves = new ArrayList<int[]>();
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					if(board[i][j] == '_')
					{
						int[] c = new int[2];
						c[0] = i;
						c[1] = j;
						moves.add(c);
					}
				}
			}
			
			return moves;
		}
	};
	
	
}
