package tictactoe.tictactoe;

public abstract class Player {
	public final char representation;
	public final String name;
	Player(String name, char c)
	{
		this.name = name;
		representation = c;
	}
	Player(char c)
	{
		this("Untitled",c);
	}
	
	public abstract int[] decide(char[][] board);
}

