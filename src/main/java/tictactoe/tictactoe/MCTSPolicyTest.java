package tictactoe.tictactoe;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MCTSPolicyTest {

	MCTSPolicy policy;
	char[][] emptyBoard = 
	{
			{'_','_','_'},
			{'_','_','_'},
			{'_','_','_'}
	};
	@Before
	public void setUp() throws Exception {
		policy = new MCTSPolicy('X');
	}

	@Test
	public void testMCTSPolicyChar() {
		assertEquals("rep initialized correctly",'X',policy.getRep());
	}

	@Test
	public void testMCTSPolicyCharFloat() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetUCTConstant() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDecide() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testUpdateRoot() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void expandTest()
	{
		policy.updateRoot(this.emptyBoard);
		policy.expandTest(policy.getRoot());
		assertEquals("expand test on empty board",9,policy.getRoot().getChildren().size());
	
	}
}
