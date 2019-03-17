package com.emd.simbiom.budget;

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

import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Invoice;

import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.util.Period;

import com.emd.simbiom.view.ColumnSet;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * InvoiceResult produces the model to hold invoices.
 * Created: Wed Oct  3 18:24:09 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class InvoiceResult extends DefaultModelProducer {
    // private int lastCreatedDays;
    private ColumnSet columnSet;
    private InvoiceDetailsView details;

    private static Log log = LogFactory.getLog(InvoiceResult.class);

    public static final String COMPONENT_ID = "grInvoices";
    public static final String RESULT = "result";

    private static final int INITIAL_PERIOD = -2; 
    

    public InvoiceResult() {
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
	Invoice[] invoices = (Invoice[])context.get( RESULT );

	if( invoices == null ) {

	    SampleInventory dao = getSampleInventory();
	    if( dao == null ) {
		writeMessage( wnd, "Error: No database access configured" );
		return;
	    }
	    try {
		Period invPeriod = Period.fromQuarter( 0 );
		invPeriod.join( Period.fromQuarter( INITIAL_PERIOD ) );

		invoices = dao.findInvoiceByPeriod( invPeriod, true );
		// if( invoices.length > 0 )
		//     setLastCreatedDays( samples[0] );
		context.put( RESULT, invoices );
	    }
	    catch( SQLException sqe ) {
		writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
		log.error( sqe );
		invoices = null;
	    }
	}

	if( invoices != null ) {
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
    // public final int getLastCreatedDays() {
    // 	return lastCreatedDays;
    // }

    /**
     * Set the <code>LastCreatedDays</code> value.
     *
     * @param lastCreatedDays The new LastCreatedDays value.
     */
    // public final void setLastCreatedDays(final int lastCreatedDays) {
    // 	this.lastCreatedDays = lastCreatedDays;
    // }

    /**
     * Set the <code>LastCreatedDays</code> value.
     *
     * @param lastCreatedSample The sample from which the last created days should be determined.
     */
    // public final void setLastCreatedDays( Sample lastCreatedSample ) {
    // 	Timestamp ts = lastCreatedSample.getCreated();
    // 	if( ts != null ) {
    // 	    long diffMillis = System.currentTimeMillis() - ts.getTime();
    // 	    int days = (int)((double)diffMillis / 1000d / 60d / 60d / 24d);
    // 	    log.debug( "Last created days: "+String.valueOf(days) );
    // 	    this.setLastCreatedDays( days );
    // 	}
    // }

    /**
     * Get the <code>ColumnSet</code> value.
     *
     * @return a <code>ColumnSet</code> value
     */
    public final ColumnSet getColumnSet() {
	return columnSet;
    }

    /**
     * Set the <code>ColumnSetup</code> value.
     *
     * @param columnSetup The new ColumnSetup value.
     */
    public final void setColumnSet(final ColumnSet columnSet) {
     	this.columnSet = columnSet;
    }

    /**
     * Set the <code>ColumnSetup</code> value.
     *
     * @param columnSetup The new ColumnSetup value.
     */
    public final void setColumns(final ColumnSet columnSet) {
     	this.setColumnSet( columnSet );
    }

    /**
     * Get the <code>Details</code> value.
     *
     * @return an <code>InvoiceDetailsView</code> value
     */
    public final InvoiceDetailsView getDetails() {
	return details;
    }

    /**
     * Set the <code>Details</code> value.
     *
     * @param details The new Details value.
     */
    public final void setDetails(final InvoiceDetailsView details) {
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
    //   	Column col = (Column)grid.getFellowIfAny( colId );
    //   	if( col != null ) {
    //   	    col.setSortAscending( new InvoiceRowComparator( srr, col, new BidirectionalComparator( comp ) ) );
    //   	    col.setSortDescending( new InvoiceRowComparator( srr, col, new BidirectionalComparator( comp, true ) ) );
    //   	}
    // }

    protected void assignGrid( Grid grid, Map context ) {
	log.debug( "Invoice result model context: "+context );

	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( grid, "Error: No database access configured" );
	    return;
	}
	
	InventoryPreferences pref = getPreferences();
	// ColumnSetup def = getColumnSetup();
	// if( def == null )
	//     def = new ColumnSetup();
	
	ColumnSet cSetup = getColumnSet();
	log.debug( "Column set: "+cSetup );

	InvoiceResultRenderer srr = new InvoiceResultRenderer( getColumnSet(), pref );
	grid.setRowRenderer( srr );

	Invoice[] invoices = (Invoice[])context.get( RESULT );
	if( invoices == null )	    
	    grid.setModel( new ListModelArray( new Invoice[0] ) );
	else {
	    log.debug( "Assigning model, number of invoices: "+invoices.length );
	    grid.setModel( new ListModelArray( invoices ) );
	}
	
	cSetup.initSort( grid );

	writeMessage( grid, String.valueOf(((invoices != null)?invoices.length:0))+" invoices match the search" );
    }

}
