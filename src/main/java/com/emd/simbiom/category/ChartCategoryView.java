package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Chart;
import org.zkoss.zul.SimpleCategoryModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import org.zkoss.zul.impl.ChartEngine;

// import com.emd.simbiom.command.InventoryViewAction;
// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.SampleSummary;
import com.emd.simbiom.view.CategoryTreeNode;

import com.emd.util.Stringx;

// import com.emd.zk.view.VelocityView;
// import com.emd.zk.view.ViewAction;

// import com.emd.util.Parameter;

/**
 * <code>ChartCategoryView</code> is a specialized category view supporting chart visualization.
 *
 * Created: Thu Aug 27 06:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class ChartCategoryView extends CategoryView {
    private String engine;
    private ChartEngine chartEngine;

    private static Log log = LogFactory.getLog(ChartCategoryView.class);

    private static final String CHART_ID = "chrtCategory";

    public ChartCategoryView() {
	super();
    }

    /**
     * Get the <code>Engine</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getEngine() {
	return engine;
    }

    /**
     * Set the <code>Engine</code> value.
     *
     * @param engine The new Engine value.
     */
    public final void setEngine(final String engine) {
	this.engine = engine;
    }

    private ChartEngine createChartEngine() {
	if( chartEngine == null ) {
	    String eClass = getEngine();
	    if( eClass != null ) {
		try {
		    Class cl = Class.forName( eClass );
		    chartEngine = (ChartEngine)cl.newInstance();
		}
		catch( Exception ex ) {
		    log.error( ex );
		}
	    }
	}
	return chartEngine;
    }

    private String calculateHeight( CategoryModel cm ) {
	int numSeries = cm.getKeys().size();
	int height = numSeries * 120;
	String hSt = String.valueOf(Math.max( 120, height ));
	log.debug( "Calculated chart height: "+hSt );
	return hSt;
    }

    private CategoryModel createModel( Map context ) {
	CategoryModel model = new SimpleCategoryModel();
	CategoryViewNode node = (CategoryViewNode)context.get( CategoryTreeView.RESULT );
	if( node != null ) {
	    List<CategoryTreeNode> chNodes = node.getChildren();
	    for( CategoryTreeNode ctn : chNodes ) {
		if( (ctn instanceof CategoryViewNode) &&
		    (((CategoryViewNode)ctn).getNodeData() != null) &&
		    (((CategoryViewNode)ctn).getNodeData() instanceof SampleSummary) ) {
		    SampleSummary sSum = (SampleSummary)((CategoryViewNode)ctn).getNodeData();
		    model.setValue( sSum.getTerm(), sSum.getTerm(), new Long(sSum.getSamplecount()) );
		}
	    }
	}
	return model;
    }
	
    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	ChartEngine ce = createChartEngine();
	if( ce == null ) {
	    writeMessage( wnd, "Error: Cannot create chat engine" );
	    return;
	}
	Chart chrt = (Chart)wnd.getFellowIfAny( CHART_ID );
	if( chrt != null ) {
	    CategoryModel cm = createModel( context );
	    chrt.setHeight( calculateHeight( cm ) );
	    chrt.setEngine( ce );
	    chrt.setModel( cm );
	}
	Textbox txt = (Textbox)wnd.getFellowIfAny( BrowseCategoryView.CATEGORY_PATH_ID );
	if( txt != null )
	    txt.setValue( (String)context.get(CategoryTreeView.PATH ) );
    }

}
