package com.emd.simbiom.search;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Age;
import com.emd.simbiom.model.Sample;

import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * SampleResult produces the model to hold sample querying results.
 * Created: Sun Mar 29 09:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SampleResult extends DefaultModelProducer {
    private int lastCreatedDays;
    private ColumnSetup columnSetup;
    private SampleDetailsView details;

    private static final long   DEFAULT_30_DAYS = 30L * 24L * 60L * 60L * 1000L; // 30 days
    private static final long   DEFAULT_90_DAYS = 90L * 24L * 60L * 60L * 1000L; // 90 days

    private static Log log = LogFactory.getLog(SampleResult.class);

    public static final String COMPONENT_ID = "grSearchResult";
    public static final String RESULT = "result";
    

    public SampleResult() {
	super();
	setModelName( COMPONENT_ID );
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
	Sample[] samples = (Sample[])context.get( RESULT );

	if( samples == null ) {
	    // SampleInventoryDAO dao = getSampleInventory();
	    SampleInventory dao = getSampleInventory();
	    if( dao == null ) {
		writeMessage( wnd, "Error: No database access configured" );
		return;
	    }
	    try {
		// samples = dao.findSampleByAge( Age.created().newerThan(DEFAULT_90_DAYS) );
		samples = dao.findSampleLastCreated();
		if( samples.length > 0 )
		    setLastCreatedDays( samples[0] );
		context.put( RESULT, samples );
	    }
	    catch( SQLException sqe ) {
		writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
		log.error( sqe );
		samples = null;
	    }
	}

	if( samples != null ) {
	    Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	    if( grSamples != null ) 
		assignModel( grSamples, context );
	}
    }

    /**
     * Get the <code>LastCreatedDays</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getLastCreatedDays() {
	return lastCreatedDays;
    }

    /**
     * Set the <code>LastCreatedDays</code> value.
     *
     * @param lastCreatedDays The new LastCreatedDays value.
     */
    public final void setLastCreatedDays(final int lastCreatedDays) {
	this.lastCreatedDays = lastCreatedDays;
    }

    /**
     * Set the <code>LastCreatedDays</code> value.
     *
     * @param lastCreatedSample The sample from which the last created days should be determined.
     */
    public final void setLastCreatedDays( Sample lastCreatedSample ) {
	Timestamp ts = lastCreatedSample.getCreated();
	if( ts != null ) {
	    long diffMillis = System.currentTimeMillis() - ts.getTime();
	    int days = (int)((double)diffMillis / 1000d / 60d / 60d / 24d);
	    log.debug( "Last created days: "+String.valueOf(days) );
	    this.setLastCreatedDays( days );
	}
    }

    /**
     * Get the <code>ColumnSetup</code> value.
     *
     * @return a <code>ColumnSetup</code> value
     */
    public final ColumnSetup getColumnSetup() {
	return columnSetup;
    }

    /**
     * Set the <code>ColumnSetup</code> value.
     *
     * @param columnSetup The new ColumnSetup value.
     */
    public final void setColumnSetup(final ColumnSetup columnSetup) {
	this.columnSetup = columnSetup;
    }

    /**
     * Set the <code>ColumnSetup</code> value.
     *
     * @param columnSetup The new ColumnSetup value.
     */
    public final void setColumns(final ColumnSetup columnSetup) {
	this.setColumnSetup( columnSetup );
    }

    /**
     * Get the <code>Details</code> value.
     *
     * @return a <code>SampleDetailsView</code> value
     */
    public final SampleDetailsView getDetails() {
	return details;
    }

    /**
     * Set the <code>Details</code> value.
     *
     * @param details The new Details value.
     */
    public final void setDetails(final SampleDetailsView details) {
	this.details = details;
    }

    // protected void updateActions( String pId, long uId ) {
    // 	Iterator<SearchFilter> it = filters.iterator();
    // 	while( it.hasNext() ) {
    // 	    SearchFilter qt = it.next();
    // 	    qt.updateActions( pId, uId );
    // 	}
    // }

    // private void initSort( Grid grid, SampleResultRenderer srr, String colId, Comparator comp ) {
    //  	Column col = (Column)grid.getFellowIfAny( colId );
    //  	if( col != null ) {
    //  	    col.setSortAscending( new SampleRowComparator( srr, col, new BidirectionalComparator( comp ) ) );
    //  	    col.setSortDescending( new SampleRowComparator( srr, col, new BidirectionalComparator( comp, true ) ) );
    //  	}
    // }


    protected void assignGrid( Grid grid, Map context ) {
	log.debug( "Sample result model context: "+context );

	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( grid, "Error: No database access configured" );
	    return;
	}
	
	InventoryPreferences pref = getPreferences();
	ColumnSetup def = getColumnSetup();
	if( def == null )
	    def = new ColumnSetup();
	// def.setPreferences( pref );
	
	ColumnSetup cSetup = ColumnSetup.getInstance( def );
	log.debug( "Column setup: "+cSetup );

	SampleResultRenderer srr = new SampleResultRenderer( dao, pref, cSetup );
	grid.setRowRenderer( srr );

	Sample[] samples = (Sample[])context.get( RESULT );
	if( samples == null )	    
	    grid.setModel( new ListModelArray( new Sample[0] ) );
	else {
	    log.debug( "Assigning model, number of samples: "+samples.length );
	    grid.setModel( new ListModelArray( samples ) );
	}
	
	cSetup.assignGrid( grid, dao );

	writeMessage( grid, String.valueOf(((samples != null)?samples.length:0))+" samples match the search" );
    }

}
