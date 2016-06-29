package tictactoe.tictactoe;

import java.util.Random;

public class App 
{
	private Player pa,pb;

	public static final char X = 'X';
	public static final char O = 'O';
	public static final char _ = '_';
     
	char data[][] = new char[3][3];
    App()
    {
    	data = new char[3][3];
    	for(int i = 0; i < 3; i++)
    	{
    		for(int j = 0; j < 3; j++)
    		{
    			data[i][j] = _;
    		}    		
    	}
    	pa = pb = null;
    }
	public static void main( String[] args )
    {
		
        App game = new App();
        AIPlayer a = new AIPlayer("A",'X');
//        a.adoptMCTSPolicy();
        game.addPlayer(a);
        
        AIPlayer ai = new AIPlayer("B",'O');
        ai.adoptMCTSPolicy();
        MCTSPolicy p = (MCTSPolicy)(ai.getPolicy());
        p.setUCTConstant(0f);
        p.setParallel(true);
        game.addPlayer(ai);
        int aWins = 0, bWins = 0,total = 20;
        for(int i = 0; i < total; i++)
        {
        	game.reset();
        	char win = game.run();
        	if(win == 'X') aWins++;
        	if(win == 'O') bWins++;
        }
        System.out.println("Statistics:");
        System.out.println("A(random) wins: " + (float)aWins/(float)total * 100 + "% aka " + aWins + " times");
        System.out.println("B(MCTS) wins: " + (float)bWins/(float)total * 100 + "% aka " + bWins + " times");
        System.out.println("Draw: " + (float)(total - aWins - bWins)/(float)total * 100 + "% aka " + (total - aWins - bWins) + " times");
    }
	public char run()
	{
		int[] coord;
		while(!this.isGameEnded())
		{
			// A's turn
			show();
			coord = getCoord(pa);
			data[coord[0]][coord[1]] = pa.representation;
			if(this.isGameEnded())
			{
				if(this.checkWin())
				{
					System.out.println("Player " + pa.name + " wins");
					show();
					return pa.representation;
				}
				else
				{
					System.out.println("Draw!");
					show();
					return '_';
				}
				
			}
			// B's turn
			show();
			coord = getCoord(pb);
			data[coord[0]][coord[1]] = pb.representation;
		}
		if(checkWin()) // B wins at the end
		{
			System.out.println("Player " + pb.name + "wins");
			show();
			return pb.representation;
		}else // draw
		{
			System.out.println("Draw!");
			show();
			return '_';
				
		}
		
	}
	/**
	 * Make sure this is used only when the board is not filled. Otherwise this results in an infinite loop...
	 * @param p
	 * @return int[] with player's input inside
	 */
	public int[] getCoord(Player p)
	{
		int[] coord;
		do
		{
			coord = p.decide(data);
		}while(data[coord[0]][coord[1]] != '_');
		return coord;
	}
	
	public void addPlayer(Player p)
	{
		if(pa == null) 
		{
			pa = p;
			return;
		}
		if(pb == null) pb = p;
	}
	
    public void show()
    {
    	for(int i = 0; i < 3; i++)
    	{
    		for(int j = 0; j < 3; j++)
    		{
    			System.out.print(data[i][j]);
    			System.out.print('\t');
    		}
    		System.out.println();
    	}
    }
    
    public boolean checkWin()
    {
    	for(int i = 0; i < 3; i++)
    	{
    		if(data[i][0] == data[i][1] && data[i][1] == data[i][2] && data[i][2] != '_')
    		{
    			return true;
    		}
    		if(data[0][i] == data[1][i] && data[1][i] == data[2][i] && data[2][i] != '_')
    		{
    			return true;
    		}
    	}
    	if(data[0][0] == data[1][1] && data[1][1] == data[2][2] && data[1][1] != '_') return true;
    	if(data[0][2] == data[1][1] && data[1][1] == data[2][0] && data[1][1] != '_') return true;
    	return false;
    }
    public boolean isFull()
    {
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(data[i][j] == '_') return false;
			}
		}
		return true;
    }
    public boolean isGameEnded()
    {
    	return checkWin() || isFull();
    }
    public void reset()
    {
    	for(int i = 0; i < 3; i++)
    	{
    		for(int j = 0; j < 3; j++)
    			data[i][j] = '_';
    	}
    }
}
