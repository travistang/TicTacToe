package tictactoe.tictactoe;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private Tree<T> root;
    private Tree<T> parent;
    private List<Tree<T>> children;
    private T data;

    public Tree(Tree<T> parent,T rootData)
    {
    	root = parent;
    	data = rootData;
    	root.children = new ArrayList<Tree<T>>();
    }
    public Tree(T rootData) {
    	this(null,rootData);
    }
    
    public boolean isRoot()
    {
    	return root == this || root == null;
    }
    
    public void addChild(T rootData)
    {
    	children.add(new Tree<T>(this,rootData));
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
