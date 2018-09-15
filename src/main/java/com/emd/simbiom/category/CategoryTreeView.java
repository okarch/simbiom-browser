package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.ext.TreeSelectableModel;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.SampleSummary;

import com.emd.simbiom.view.CategoryTreeModel;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>CategoryTreeView</code> holds the category tree model.
 *
 * Created: Sat Aug  1 07:23:10 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class CategoryTreeView extends DefaultModelProducer implements EventListener {
    private List<CategoryView> categories;

    public static final String COMPONENT_ID     = "trCategory";
    public static final String DEFAULT_CATEGORY = "Molecules";

    public static final String RESULT           = "category";
    public static final String CHART_TITLE      = "chartTitle";
    public static final String SUMMARY          = "summary";
    public static final String PATH             = "path";
    public static final String MODEL_NAME       = "modelName";
    public static final String ALL_SAMPLES      = "allSamples";

    private static Log log = LogFactory.getLog(CategoryTreeView.class);

    public CategoryTreeView() {
	super();
	setModelName( COMPONENT_ID );
	this.categories = new ArrayList<CategoryView>();
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	if( context == null )
	    context = new HashMap();
	String catSt = Stringx.getDefault((String)context.get(RESULT),DEFAULT_CATEGORY);
	context.put( RESULT, catSt );
	
	Tree trCat = (Tree)wnd.getFellowIfAny( getModelName() );
	if( trCat != null ) 
	    assignModel( trCat, context );
    }

    /**
     * Adds a category view.
     *
     * @param cat the <code>CategoryView</code>
     */
    public void addCategory( CategoryView cat ) {
	categories.add( cat );
    }

    /**
     * Get the category view value.
     *
     * @param label the label of the category view.
     * @return the <code>CategoryView</code> value.
     */
    public CategoryView getCategory( String label ) {
	Iterator<CategoryView> it = categories.iterator();
	while( it.hasNext() ) {
	    CategoryView qt = it.next();
	    if( qt.getLabel().equals(label) ) 
		return qt;
	}
	return new CategoryView();
    }

    /**
     * Get all category views
     *
     * @return an array of <code>CategoryView</code>s.
     */
    public CategoryView[] getCategories() {
	CategoryView[] filts = new CategoryView[ categories.size() ];
	return (CategoryView[])categories.toArray( filts );
    }

    /**
     * Set the Category view value.
     * @param newFilter The new Category view value.
     */
    public void setCategory( CategoryView newFilter ) {
	addCategory( newFilter );
    }

    private TreeModel createTreeModel( String category ) {
	CategoryViewNode cvn = new CategoryViewNode( getSampleInventory(), category ); 
	return new CategoryTreeModel( cvn );
    }

    protected void updateActions( String pId, long uId ) {
	Iterator<CategoryView> it = categories.iterator();
	while( it.hasNext() ) {
	    CategoryView qt = it.next();
	    qt.updateActions( pId, uId );
	}
    }

    protected void assignTree( Tree tree, Map context ) {
	log.debug( "Category tree model context: "+context );

	String catSt = Stringx.getDefault((String)context.get(RESULT),DEFAULT_CATEGORY);

	tree.addEventListener( "onAfterRender", this );
	tree.addEventListener( Events.ON_SELECT, this );
	tree.setModel( createTreeModel( catSt ) );
    }

    private CategoryViewNode getSelectedNode( Tree tr ) {
	TreeSelectableModel selModel = (TreeSelectableModel)tr.getModel();
	int[][] paths = selModel.getSelectionPaths();
	List selected = new ArrayList();
	AbstractTreeModel model = (AbstractTreeModel) selModel;
	for (int i = 0; i < paths.length; i++) {
	    selected.add(model.getChild(paths[i]));
	}
	if( selected.size() > 0 )
	    return (CategoryViewNode)selected.get( 0 );
	return null;
    }

    private CategoryView selectCategoryView( CategoryViewNode cvn ) {
	String catSelector = (cvn.isTermNode()?"term_":"")+
	    cvn.getCategoryPath();
	log.debug( "Category path: "+catSelector );
	CategoryView cv = getCategory( catSelector );
	log.debug( "View found: "+((cv==null)?"null":cv.getClass().getName()) );
	return ((cv == null)?getCategory( "No category" ):cv);
    }

    /**
     * Retrieve the search filter currently selected.
     *
     * @param wnd the application window.
     * @return the search filter (or null) if nothing has been selected.
     */
    // public SearchFilter getSelectedSearchFilter( Window wnd ) {
    // 	Combobox cbFilter = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	if( cbFilter != null ) {
    // 	    int idx = cbFilter.getSelectedIndex();
    // 	    if( (idx >= 0) && (idx < filters.size()) ) 
    // 		return filters.get(idx);
    // 	}
    // 	return null;
    // }

    private String createTitle( String catPath ) {
	String[] toks = catPath.split( "[.]" );
	StringBuilder stb = new StringBuilder( "Samples" );
	if( toks.length > 0 ) {
	    stb.append( " per ");
	    stb.append( toks[toks.length-1] );
	}
	else if( catPath.trim().length() > 0 ) {
	    stb.append( " per ");
	    stb.append( catPath.trim().replace('_',' ') );
	}
	log.debug( "set "+CHART_TITLE+" = "+stb );
	return stb.toString();
    }

    private SampleSummary countAllSamples() {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao != null ) {
	    try {
		SampleSummary[] sSums = dao.createSampleSummary( "" );
		log.debug( "All samples query: "+sSums.length );
		if( sSums.length > 0 )
		    return sSums[0];
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
	    }
	}
	return null;
    }

    private Map createContext( CategoryView cv, CategoryViewNode cvn ) {
	Map ctxt = new HashMap();
	ctxt.put( RESULT, cvn );
	ctxt.put( CHART_TITLE, createTitle( cv.getLabel() ) );
	Object nd = cvn.getNodeData();
	if( (nd != null) && (nd instanceof SampleSummary) )
	    ctxt.put( SUMMARY, (SampleSummary)nd );
	ctxt.put( PATH, cvn.getNodePath() );
	ctxt.put( MODEL_NAME, getModelName() );
	SampleSummary sSum = countAllSamples();
	if( sSum != null )
	    ctxt.put( ALL_SAMPLES, sSum );
	return ctxt;
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Category tree model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Tree tr = (Tree)event.getTarget();
	    log.debug( "Tree items:"+tr.getItemCount() );
	    log.debug( "Tree model:"+tr.getModel() );
	    // if( cb.getItemCount() > 0 ) {
	    // 	cb.setSelectedIndex( 0 );
	    // 	Map ctxt = new HashMap();
	    // 	ctxt.put( "filterNum", getFilterSuffix() );
	    // 	ctxt.put( "inventory", getSampleInventory() );
	    // 	filters.get(0).initFilter( ZKContext.findWindow( cb ), ctxt );
	    // }	    
	}
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    Tree tr = (Tree)event.getTarget();
	    CategoryViewNode cvn = getSelectedNode( tr );
	    if( cvn != null ) {
		CategoryView cv = selectCategoryView( cvn );
		cv.setMessageRowId( getMessageRowId() );
		cv.initCategoryView( ZKContext.findWindow( tr ), createContext( cv, cvn ) );
	    }

	    // Treeitem ti = tr.getSelectedItem();
	    // log.debug( "tree item index: "+ti.getIndex() );

	    // if( (idx >= 0) && (idx < filters.size()) ) {
	    // 	Map ctxt = new HashMap();
	    // 	ctxt.put( "filterNum", getFilterSuffix() );
	    // 	ctxt.put( "inventory", getSampleInventory() );
	    // 	filters.get(idx).initFilter( ZKContext.findWindow( cb ), ctxt );
	    // }
	}
	
    }

}
