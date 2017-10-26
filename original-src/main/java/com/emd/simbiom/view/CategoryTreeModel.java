package com.emd.simbiom.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.AbstractTreeModel;

import com.emd.util.Copyable;

/**
 * CategoryTreeModel provides a dynamic tree model backed by self updating tree nodes
 *
 * Created: Sat Sep 24 11:13:05 2011
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class CategoryTreeModel extends AbstractTreeModel implements Copyable {
    private Map<String,CategoryTreeNode> nodeIndex;

    private static Log log = LogFactory.getLog(CategoryTreeModel.class);

    /**
     * Creates a new <code>CategoryTreeModel</code>. The prototype instance
     *
     * @param entity the query entity
     * @param prototype the tree node's prototype
     */
    public CategoryTreeModel( CategoryTreeNode root ) {
	super( root );
	nodeIndex = new HashMap<String,CategoryTreeNode>();
    }

    private CategoryTreeModel( CategoryTreeModel ctm ) {
	super( ctm.getRoot() );
	this.nodeIndex = ctm.nodeIndex;
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     *
     * @param parent a node in the tree, obtained from this data source 
     * @param index the index in the parent's child array
     * @return the child of parent at index index
     */
    public Object getChild(Object parent, int index) {
	CategoryTreeNode pNode = (CategoryTreeNode)parent;
	List cl = pNode.getChildren();
	if( index > cl.size() )
	    return null;
	CategoryTreeNode cNode = (CategoryTreeNode)cl.get( index );
	nodeIndex.put( cNode.getNodeId(), cNode );
	return cNode;
    }

    /**
     * Returns the number of children of parent.
     *
     * @param parent a node in the tree, obtained from this data source 
     * @return the number of children of the node parent
     */
    public int getChildCount(Object parent) {
	CategoryTreeNode pNode = (CategoryTreeNode)parent;
	List cl = pNode.getChildren();
	return cl.size();
    }

    public boolean isLeaf( Object node ) {
        return (getChildCount(node) == 0);
    }

    /**
     * Returns a tree node from the node index
     *
     * @return the category tree node if it exists in the node index, null otherwise
     */
    public CategoryTreeNode findNode( String nodeId ) {
	return nodeIndex.get( nodeId );
    }

    /**
     * Creates and returns a copy of this object. 
     *
     * @return a copy of the implementing object.
     */
    public Object copy() throws CloneNotSupportedException {
	return new CategoryTreeModel( this ); 
    }
 
}
