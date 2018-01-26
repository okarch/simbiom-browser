package com.emd.simbiom.category;

import java.awt.Color;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.google.charts.GoogleChart;

import org.zkoss.google.charts.data.ColumnRole;
import org.zkoss.google.charts.data.ColumnType;
import org.zkoss.google.charts.data.DataTable;
import org.zkoss.google.charts.data.FormattedValue;
import org.zkoss.google.charts.event.DataTableSelectionEvent;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;

// import org.zkoss.zul.CategoryModel;
// import org.zkoss.zul.Chart;

import org.zkoss.zul.Div;

// import org.zkoss.zul.SimpleCategoryModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

// import org.zkoss.zul.impl.ChartEngine;

import com.emd.simbiom.model.SampleSummary;

import com.emd.simbiom.view.CategoryTreeNode;
import com.emd.simbiom.view.ChartColors;

import com.emd.util.Stringx;


/**
 * <code>GoogleChartCategoryView</code> is a specialized category view supporting chart visualization based on google charts.
 *
 * Created: Sat Jan 13 16:53:49 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class GoogleChartCategoryView extends CategoryView {
    private String engine;
    private GoogleChart chart;

    private static Log log = LogFactory.getLog(GoogleChartCategoryView.class);

    // private static final String CHART_ID = "divChartCategory";
    // private static final String CHART_ID = "chrtBarChart";

    public GoogleChartCategoryView() {
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

    private String getChartId() {
	StringBuilder stb = new StringBuilder( "chrt" );
	String eSt = Stringx.getDefault(getEngine(),".BarChart");
	int k = eSt.lastIndexOf( "." );
	if( (k >= 0) && (k+1 < eSt.length()) )
	    stb.append(eSt.substring(k+1));
	else
	    stb.append( "BarChart" );
	return stb.toString();
    }

    // private GoogleChart createChart() {
    // 	if( chart == null ) {
    // 	    String eClass = getEngine();
    // 	    if( eClass != null ) {
    // 		try {
    // 		    Class cl = Class.forName( eClass );
    // 		    chart = (GoogleChart)cl.newInstance();
    // 		}
    // 		catch( Exception ex ) {
    // 		    log.error( ex );
    // 		}
    // 	    }
    // 	}
    // 	return chart;
    // }

    private GoogleChart createChart( Window wnd ) {
	return (GoogleChart)wnd.getFellowIfAny( getChartId() );
    }

    // public DataTable getSimpleDataModel() {
    //     DataTable data = new DataTable();
    //     data.addStringColumn("Task", "task");
    //     data.addNumberColumn("Hours per Day", "hours");
    //     data.addColumn(ColumnType.STRING, ColumnRole.TOOLTIP);
    //     data.addColumn(ColumnType.STRING, ColumnRole.STYLE);
    //     data.addRow("Work", 11, null, null);
    //     data.addRow("Eat", 2, "Hello world!", "opacity: 0.2");
    //     data.addRow("Commute", 2, null, "fill-color: #76A7FA");
    //     data.addRow("Watch TV", 2, "Hello world!", "color: #76A7FA");
    //     data.addRow("Sleep", new FormattedValue(7, "7.000"), null, "opacity: 0.7");
    //     return data;
    // }

    // public DataTable getTimelineDataModel() {
    //     DataTable data = new DataTable();
    //     data.addStringColumn("Title");
    //     data.addStringColumn("Name");
    //     data.addDateColumn("Start");
    //     data.addDateColumn("End");
    //     data.addRow("President", "Washington", getDate(1789, 3, 29), getDate(1797, 2, 3));
    //     data.addRow("President", "Adams", getDate(1797, 2, 3), getDate(1801, 2, 3));
    //     data.addRow("President", "Jefferson", getDate(1801, 2, 3), getDate(1809, 2, 3));
    //     return data;
    // }

    private int getRowHeight() {
	if( getEngine().endsWith( "PieChart" ) ) 
	    return 200;
	return 120;
    }

    private String calculateHeight( DataTable cm ) {
	int numSeries = cm.getNumberOfRows();
	int rHeight = getRowHeight();
	int height = numSeries * rHeight;
	String hSt = String.valueOf(Math.max( rHeight, height ));
	log.debug( "Calculated chart height: "+hSt );
	return hSt;
    }

    private String mapStyle( String term ) {
	Color col = ChartColors.mapTerm( term );
	return "fill-color: "+ChartColors.toHtmlColor( col );
    }

    private CategoryViewNode findNode( TreeModel tModel, CategoryViewNode parent, String nodePath ) {
	if( nodePath.equals(parent.getNodePath()) )
	    return parent;
	int cCount = tModel.getChildCount( parent );
	for( int i = 0; i < cCount; i++ ) {
	    CategoryViewNode node = (CategoryViewNode)tModel.getChild( parent, i );
	    node = findNode( tModel, node, nodePath );
	    if( node != null )
		return node;
	}
	return null;
    }

    private SampleSummary findParentSummary( Window wnd, Map context, CategoryViewNode node ) {
	SampleSummary parentSummary = null;

	// Extract parent node path

	String nPath = node.getNodePath();
	log.debug( "Current node path: "+nPath );
	String[] paths = nPath.split( "[|]" );
	StringBuilder pPath = new StringBuilder();
	int pathLen = paths.length-2;
	for( int i = 0; i < pathLen; i++ ) {
	    if( i > 0 )
		pPath.append( "|" );
	    pPath.append( paths[i] );
	}
	log.debug( "Parent node path: "+pPath );
	
	// traverse tree to find parent node path

	String mName = (String)context.get( CategoryTreeView.MODEL_NAME );
	Tree cTree = null;
	TreeModel tModel = null;
	if( (mName != null) && 
	    ((cTree = (Tree)wnd.getFellowIfAny(mName)) != null) && 
	    ((tModel = cTree.getModel()) != null) ) {

	    CategoryViewNode rootNode = (CategoryViewNode)tModel.getRoot();
	    CategoryViewNode parentNode = findNode( tModel, rootNode, pPath.toString() );
	    if( parentNode != null ) {
		log.debug( "Parent node: "+parentNode+" node path: "+parentNode.getNodePath() );
		parentSummary = (SampleSummary)parentNode.getNodeData();
	    }
	    else
		log.warn( "Parent node path \""+pPath+"\" could not be found." );
		
	}
	
	return parentSummary;
    }

    private SampleSummary[] collectParentSummaries( Window wnd, Map context, CategoryViewNode node ) {
	String mName = (String)context.get( CategoryTreeView.MODEL_NAME );
	Tree cTree = null;
	TreeModel tModel = null;
	List<SampleSummary> pathNodes = new ArrayList<SampleSummary>();
	if( (mName != null) && 
	    ((cTree = (Tree)wnd.getFellowIfAny(mName)) != null) && 
	    ((tModel = cTree.getModel()) != null) ) {

	    CategoryViewNode rootNode = (CategoryViewNode)tModel.getRoot();
	    log.debug( "Root node: "+rootNode );

	    int[] pathIds = tModel.getPath( node );
	    
	    SampleSummary sSum = null;
	    for( int i = 0; i < pathIds.length-1; i++ ) {
		int[] nAccess = Arrays.copyOfRange( pathIds, 0, i);
		CategoryViewNode pNode = (CategoryViewNode)tModel.getChild( nAccess );
		if( pNode != null ) {
		    log.debug( "Parent node: "+pNode+" node path: "+pNode.getNodePath() );
		    sSum = (SampleSummary)pNode.getNodeData();
		    if( sSum != null ) {
			log.debug( "Parent node sample summary: "+sSum+" term: "+sSum.getTerm()+" sample count: "+sSum.getSamplecount() );
			pathNodes.add( sSum );
		    }
		}
	    }
	    log.debug( "Current node: "+node+" node path: "+node.getNodePath() );
	    sSum = (SampleSummary)node.getNodeData();
	    if( sSum != null ) {
		log.debug( "Parent node sample summary: "+sSum+" term: "+sSum.getTerm()+" sample count: "+sSum.getSamplecount() );
		pathNodes.add( sSum );
	    }
	}
	
	SampleSummary[] sSums = new SampleSummary[ pathNodes.size() ];
	return (SampleSummary[])pathNodes.toArray( sSums );
    }

    private DataTable createModel( Window wnd, Map context ) {
        DataTable data = new DataTable();

	CategoryViewNode node = (CategoryViewNode)context.get( CategoryTreeView.RESULT );
	if( node != null ) {

	    // SampleSummary[] pSums = collectParentSummaries( wnd, context, node );
	    // log.debug( "Number of parents: "+pSums.length );
 
	    data.addStringColumn("Category", "category");
	    data.addNumberColumn("Samples", "count");
	    data.addColumn(ColumnType.STRING, ColumnRole.TOOLTIP);
	    data.addColumn(ColumnType.STRING, ColumnRole.STYLE);
	    // data.addColumn(ColumnType.STRING, ColumnRole.ANNOTATION);

	    if( getEngine().endsWith( "PieChart" ) ) {
		SampleSummary sSum = (SampleSummary)node.getNodeData();
		if( sSum != null ) 		    
		    data.addRow( sSum.getTerm(), 
				 new Long(sSum.getSamplecount()), 
				 sSum.getTerm()+": "+String.valueOf(sSum.getSamplecount())+" samples", 
				 mapStyle( sSum.getTerm() ) ); 
		else
		    log.error( "Cannot determine sample summary of "+node );

		SampleSummary pSum = findParentSummary( wnd, context, node );
		if( pSum != null ) {
		    long sCount = pSum.getSamplecount()-sSum.getSamplecount();
		    data.addRow( pSum.getTerm(), new Long(sCount), 
				 pSum.getTerm()+": "+String.valueOf(pSum.getSamplecount())+" samples", 
				 mapStyle( pSum.getTerm() ) ); 
		}
	    }
	    else {
		List<CategoryTreeNode> chNodes = node.getChildren();
		for( CategoryTreeNode ctn : chNodes ) {
		    if( (ctn instanceof CategoryViewNode) &&
			(((CategoryViewNode)ctn).getNodeData() != null) &&
			(((CategoryViewNode)ctn).getNodeData() instanceof SampleSummary) ) {
			SampleSummary sSum = (SampleSummary)((CategoryViewNode)ctn).getNodeData();
			data.addRow( sSum.getTerm(), 
				     new Long(sSum.getSamplecount()), 
				     sSum.getTerm()+": "+String.valueOf(sSum.getSamplecount())+" samples", 
				     mapStyle( sSum.getTerm() ) ); 
		    // data.addRow( sSum.getTerm(), 
		    // 		 new Long(sSum.getSamplecount()), 
		    // 		 String.valueOf(sSum.getSamplecount()), 
		    // 		 mapStyle( sSum.getTerm() ), 
		    // 		 sSum.getTerm() );
		    }
		}
	    }
	}
	return data;
    }
	
    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	// GoogleChart ce = createChart();
	GoogleChart ce = createChart( wnd );
	if( ce == null ) {
	    writeMessage( wnd, "Error: Cannot create chart engine" );
	    return;
	}
	DataTable dt = createModel( wnd, context );
	ce.setData( dt );
	ce.setHeight( calculateHeight( dt ) );

	// Div chrt = (Div)wnd.getFellowIfAny( CHART_ID );
	// if( chrt != null ) {
	//     chrt.getChildren().clear();
	//     chrt.appendChild( ce );
	// }
	Textbox txt = (Textbox)wnd.getFellowIfAny( BrowseCategoryView.CATEGORY_PATH_ID );
	if( txt != null )
	    txt.setValue( (String)context.get(CategoryTreeView.PATH ) );
    }

}


// import org.zkoss.zk.ui.Executions;
// import org.zkoss.zk.ui.event.Event;
// import org.zkoss.zk.ui.event.EventListener;
// import org.zkoss.zk.ui.select.Selectors;

    // public void addChart() {
    //     GoogleChart chart = randomChart();
    //     if (chart instanceof Timeline) {
    //         chart.setData(getTimelineDataModel());
    //     } else {
    //         chart.setData(getSimpleDataModel());
    //     }
    //     chart.setWidth("640");
    //     chart.setHeight("320");

    //     chart.addEventListener(GoogleChartEvents.ON_READY, new EventListener<Event>() {
    //         @Override
    //         public void onEvent(Event event) {
    //             System.out.println("CHART READY");
    //         }
    //     });
    //     chart.addEventListener(GoogleChartEvents.ON_SELECT, new EventListener<DataTableSelectionEvent>() {
    //         @Override
    //         public void onEvent(DataTableSelectionEvent event) {
    //             System.out.println("CHART SELECTED: " + event.getSelections());
    //         }
    //     });
    //     chartArea.getChildren().clear();
    //     chartArea.appendChild(chart);
    // }
