package tictactoe.tictactoe;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TreeTest {
	Tree<Integer> tree;
	@Before
	public void setUp() throws Exception {
		tree = new Tree<Integer>(19);
	}

	@Test
	public void test() {
		for(int i = 0; i < 3 ; i++)
		{
			tree.addChild(new Integer(i));
		}
		for(int i = 0; i < tree.getChildren().size(); i++)
		{
			tree.getChildren().get(i).addChild(2);
			for(int j = 0; j < 4; j++)
			{
				tree.getChildren().get(i).getChildren().get(0).addChild(4);
			}
		}
		tree.print();
	}

}
