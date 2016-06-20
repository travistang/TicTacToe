package tictactoe.tictactoe;

public class AIPlayer extends Player {

	private Policy policy;

	AIPlayer(String name,char c) {
		super(name,c);
		policy = Policy.defaultPolicy;
	}
	
	AIPlayer(char c)
	{
		super(c);
		policy = Policy.defaultPolicy;
	}
	
	@Override
	public int[] decide(char[][] board) 
	{
		return policy.decide(board);
	}
	
	public void adoptMCTSPolicy()
	{
		policy = new MCTSPolicy(this.representation);
	}
	public final Policy getPolicy()
	{
		return policy;
	}
	
}
