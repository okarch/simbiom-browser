package com.emd.simbiom.view;

import java.util.List;

/**
 * CategoryTreeNode represents a node of the <code>CategoryTreeModel</code>
 *
 * Created: Sat Mar 19 07:37:25 2011
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public interface CategoryTreeNode {

    /**
     * Get the Term value.
     * @return the Term value.
     */
    public String getTerm();

    /**
     * Get the NodeId value.
     * @return the NodeId value.
     */
    public String getNodeId();

    /**
     * Get the Children value.
     * @return the Children value.
     */
    public List<CategoryTreeNode> getChildren();
    
}
