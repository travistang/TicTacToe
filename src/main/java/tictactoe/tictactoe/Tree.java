package tictactoe.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Tree<T> {
    private Tree<T> parent;
    private List<Tree<T>> children;
    private T data;

    public Tree(Tree<T> parent,T rootData)
    {
    	this.parent = parent;
    	data = rootData;
    	children = new ArrayList<Tree<T>>();
    }
    public Tree(T rootData) {
    	this(null,rootData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone()
    {
    	ArrayList<Tree<T>> newChildren = (ArrayList<Tree<T>>) ((ArrayList) children).clone();
    	Tree<T> res = new Tree<T>(null);
    	return res;
    }
    public boolean isRoot()
    {
    	return parent == this || parent == null;
    }
    
    public void addChild(T rootData)
    {
    	children.add(new Tree<T>(this,rootData));
    }
    
    /**
     * Print the tree under the current node
     */

    public void print()
    {
    	inOrderTraversal((n,lv) ->
    	{
    		for(int j = 0; j < 2; j++)
    		{
	    		for(int i = 0 ; i < lv - 1; i++)
	    		{
	    			System.out.print("|         ");
	    		}
	    		if(lv > 0 && j != 1)
	    			System.out.println();
    		}
    		if(lv > 0)
    			System.out.print("|---------");
    		System.out.println(n.data);
	    	for(int i = 0 ; i < lv - 1; i++)
	    	{
	    		System.out.print("|         ");
	    	}
    	},0);
    }
    public void inOrderTraversal(BiConsumer<Tree<T>,Integer> func,int level)
    {
    	func.accept(this, level);
    	for(Tree<T> node : children)
    	{
    		node.inOrderTraversal(func, level + 1);
    	}
    }
    public Tree<T> getParent()
    {
    	return parent;
    }
    
    public List<Tree<T>> getChildren()
    {
    	return children;
    }
    public void setChildren(List<Tree<T>> c)
    {
    	children = c;
    }
    public T getData()
    {
    	return data;
    }
    // not interested in implementing setter method here..
}
