package tictactoe.tictactoe;

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
        game.addPlayer(new HumanPlayer("A",'X'));
        AIPlayer ai = new AIPlayer("B",'O');
        ai.adoptMCTSPolicy();
        game.addPlayer(ai);
        game.run();
    }
	public void run()
	{
		int[] coord;
		while(!this.isGameEnded())
		{
			// A's turn
			show();
			coord = getCoord(pa);
			data[coord[0]][coord[1]] = pa.representation;
			if(this.checkWin())
			{
				System.out.println("Player " + pa.name + " wins");
				show();
				return;
			}
			// B's turn
			show();
			coord = getCoord(pb);
			data[coord[0]][coord[1]] = pb.representation;
		}
		if(checkWin()) // B wins at the end
		{
			System.out.println("Player " + pb.name + "wins");
			
		}else // draw
		{
			System.out.println("Draw!");
			show();
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
}
