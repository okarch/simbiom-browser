package com.emd.simbiom.category;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.SampleSummary;

import com.emd.simbiom.view.AbstractCategoryTreeNode;
import com.emd.simbiom.view.CategoryTreeNode;

/**
 * <code>CategoryViewNode</code> represents a view node in the category tree.
 *
 * Created: Mon Aug  3 17:30:46 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class CategoryViewNode extends AbstractCategoryTreeNode {
    private SampleInventoryDAO sampleInventory;
    private List<CategoryTreeNode> children;
    private String parentPath;
    private Object nodeData;
    private boolean termNode;

    private static Log log = LogFactory.getLog(CategoryViewNode.class);

    private static final String[] GROUPINGS = {
	"Diseases",
	"Locations",
	"Molecules",
	"Sample types",
	"Studies"
    };

    /**
     * Creates a <code>CategoryViewNode</code> under the given parent path.
     *
     * @param dao the sample inventory database.
     * @param parentPath the full parent node path (e.g. root|xyz|child1).
     * @param category the node name.
     */
    public CategoryViewNode( SampleInventoryDAO dao, String parentPath, String category ) {
	super( category );
	this.sampleInventory = dao;
	this.children = null;
	this.parentPath = parentPath;
    }

    /**
     * Creates a new root <code>CategoryViewNode</code>.
     *
     * @param dao the sample inventory database.
     * @param category the node name.
     */
    public CategoryViewNode( SampleInventoryDAO dao, String category ) {
	this( dao, "", category );
    }

    /**
     * Get the <code>NodeData</code> value.
     *
     * @return an <code>Object</code> value
     */
    public final Object getNodeData() {
	return nodeData;
    }

    /**
     * Set the <code>NodeData</code> value.
     *
     * @param nodeData The new NodeData value.
     */
    public final void setNodeData(final Object nodeData) {
	this.nodeData = nodeData;
    }

    /**
     * Get the <code>TermNode</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isTermNode() {
	return termNode;
    }

    /**
     * Set the <code>TermNode</code> value.
     *
     * @param termNode The new TermNode value.
     */
    public final void setTermNode(final boolean termNode) {
	this.termNode = termNode;
    }

    /**
     * Returns the node's query path.
     *
     * @return the full node path used for querying.
     */
    public String getNodePath() {
	return ((parentPath.length() > 0)?parentPath+"|"+getNodeId():getNodeId());
    }

    /**
     * Returns the category path of this node. The category path is built from the node path by
     * stripping of the term elements.
     *
     * @return a category path.
     */
    public String getCategoryPath() {
	String[] terms = getNodePath().split( "[|]" );
	String catName = null;
	boolean isValue = false;
	StringBuilder stb = new StringBuilder();
	for( int i = 0; i < terms.length; i++ ) {
	    if( isValue ) {
		isValue = false;
	    }
	    else {
		catName = terms[i].replace( ' ', '_' );
		if( i > 0 )
		    stb.append( "." );
		stb.append( catName );
		isValue = true;
	    }
	}
	return stb.toString();
    }

    private SampleSummary[] querySummary() {
	if( (!isTermNode()) && (sampleInventory != null) ) {
	    try {
		return sampleInventory.createSampleSummary( getNodePath() );
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
	    }
	}
	return new SampleSummary[0];
    }

    private void appendGroupings( List<CategoryTreeNode> cList ) {
	if( isTermNode() ) {
	    String[] parts = parentPath.split( "[|]" );
	    Set<String> grps = new HashSet<String>( Arrays.asList( GROUPINGS ) );
	    for( int i = 0; i < GROUPINGS.length; i++ ) {
		for( int j = 0; j < parts.length; j++ ) {
		    if( parts[j].equals( GROUPINGS[i] ) )
			grps.remove( GROUPINGS[i] );
		}
	    }
	    for( String st : grps ) 
		cList.add( new CategoryViewNode( sampleInventory, getNodePath(), st ) );
	}
    }

    protected void loadChildren() {
	if( children == null ) {
	    List<CategoryTreeNode> cl = new ArrayList<CategoryTreeNode>();
	    SampleSummary[] sSums = querySummary();
	    log.debug( "Category node: "+getNodePath()+" number of sample groups: "+sSums.length );
	    for( int i = 0; i < sSums.length; i++ ) {
		CategoryViewNode cvn = new CategoryViewNode( sampleInventory, getNodePath(), sSums[i].getTerm() );
		cvn.setNodeData( sSums[i] );
		cvn.setTermNode( true );
		cl.add( cvn );
	    }
	    appendGroupings( cl );
	    this.setChildren( cl );
	}
    }    

    
}
