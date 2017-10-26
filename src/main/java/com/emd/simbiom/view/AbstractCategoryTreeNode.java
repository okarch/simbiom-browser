package com.emd.simbiom.view;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AbstractCategoryTreeNode partially implements a <code>CategoryTreeNode</code> 
 * representing a node in a <code>CategoryTreeModel</code>
 *
 * Created: Sat Sep 24 07:37:25 2011
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public abstract class AbstractCategoryTreeNode implements CategoryTreeNode {
    private boolean selected;
    private String  nodeId;
    private String  term;
    private List<CategoryTreeNode> children;

    private static Log log = LogFactory.getLog(AbstractCategoryTreeNode.class);

    protected AbstractCategoryTreeNode( String nodeId ) {
	this.nodeId = nodeId;
	this.term = nodeId;
	this.selected = false;
	this.children = null;
    }

    /**
     * Get the Selected value.
     * @return the Selected value.
     */
    public boolean isSelected() {
	return selected;
    }

    /**
     * Set the Selected value.
     * @param newSelected The new Selected value.
     */
    public void setSelected(boolean newSelected) {
	this.selected = newSelected;
    }
    
    /**
     * Get the Term value.
     * @return the Tem value.
     */
    public String getTerm() {
	return term;
    }

    /**
     * Set the Term value.
     * @param newTerm The new Term value.
     */
    public void setTerm(String newTerm) {
	this.term = newTerm;
    }

    /**
     * Get the NodeId value.
     * @return the NodeId value.
     */
    public String getNodeId() {
	return nodeId;
    }

    /**
     * Set the NodeId value.
     * @param newNodeId The new NodeId value.
     */
    public void setNodeId(String newNodeId) {
	this.nodeId = newNodeId;
    }

    /**
     * Get the Children value.
     * @return the Children value.
     */
    public List<CategoryTreeNode> getChildren() {
	loadChildren();
	return children;
    }

    /**
     * Set the Children value.
     * @param newChildren The new Children value.
     */
    public synchronized void setChildren(List newChildren) {
	this.children = newChildren;
    }

    /**
     * Returns a human readable string of this tree node
     *
     * @return the label of this node
     */
    public String toString() {
	return getTerm();
    }

    /**
     * Returns true iff this object equals to the given object
     * 
     * @param o the object to compare to
     * @return true iff this object equals to the given object
     */
    public boolean equals( Object o ) {
	if( o instanceof AbstractCategoryTreeNode ) {
	    AbstractCategoryTreeNode spec = (AbstractCategoryTreeNode)o;
	    return spec.getNodeId().equals(this.getNodeId());
	}
	return false;
    }

    /**
     * Loads the child nodes
     */
    protected abstract void loadChildren();
    
}
