package tictactoe.tictactoe;
import java.util.Scanner;
public class HumanPlayer extends Player {

	public static Scanner scanner = new Scanner(System.in);
	public HumanPlayer(char c)
	{
		super(c);
	}
	
	public HumanPlayer()
	{
		super(getRepresentation());	
	}
	public HumanPlayer(String input)
	{
		super(input,getRepresentation());
	}
	public HumanPlayer(String input,char c)
	{
		super(input,c);
	}
	
	@Override
	public int[] decide(char[][] board) {
		System.out.println("Input the coordinates where you want to putf your tokens to");
		System.out.println("For example, if you want to put your token at (0,2) then input '0 2'");
		String input = scanner.nextLine();
		while(!checkInput(input))
		{
			System.out.println("Invalid input, try again");
			input = scanner.nextLine();
		};
		
		int[] res = new int[2];
		res[0] = Character.getNumericValue(input.charAt(0));
		res[1] = Character.getNumericValue(input.charAt(2));
		
		return res;
	}

	private static char getRepresentation()
	{
		Scanner scanner = new Scanner(System.in);
		String input = "";
		System.out.println("Choose a representation for this player");
		while(true)
		{
			input = scanner.nextLine();
			if(input.length() == 1 && (input.charAt(0) == 'O' || input.charAt(0) == 'X'))
			{
				break;
			}
			System.out.println("representation of the player must be either 'X' or 'O'");
		}
		scanner.close();
		return input.charAt(0);
	}
	
	private static boolean checkInput(String input)
	{
		// check if the length of the input is exactly 3
		if(input.replaceAll(" +", " ").length() != 3) return false;
		int x, y;
		// check if x coordinate is a digit
		if((x = Character.getNumericValue(input.charAt(0)) )== -1) return false;
		// check if y coordinate is a digit
		if( ( y = Character.getNumericValue(input.charAt(2)) )== -1) return false;
		// check if the range of the input coordinate is valid
		if(x < 0 || x > 2 || y < 0 || y > 2) return false;
		// check if the delimiter is space 
		if(input.charAt(1) != ' ') return false;
		return true;
	}
}
