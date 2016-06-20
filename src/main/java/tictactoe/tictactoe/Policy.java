package tictactoe.tictactoe;
import java.util.Random;
public interface Policy {
	public int[] decide(char[][] board);
	
	// randomly pick a coordinates
	public static Policy defaultPolicy = new Policy()
	{
		public int[] decide(char[][] board)
		{
			int[] res = new int[2];
			do
			{
				Random r = new Random();
				res[0] = r.nextInt(3);
				res[1] = r.nextInt(2);
			}while(board[res[0]][res[1]] != '_');
			
			return res;
		}
	};
	
}
