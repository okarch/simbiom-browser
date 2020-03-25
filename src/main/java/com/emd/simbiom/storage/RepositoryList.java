package com.emd.simbiom.storage;

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

import com.emd.simbiom.model.RepositoryRecord;

import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.util.Period;

import com.emd.simbiom.view.ColumnSet;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * <code>RepositoryList</code> produces the model to hold the repository samples.
 *
 * Created: Fri Mar 20 22:12:09 2020
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class RepositoryList extends DefaultModelProducer {
    private ColumnSet columnSet;
    private RegistrationView details;

    private static Log log = LogFactory.getLog(RepositoryList.class);

    public static final String COMPONENT_ID = "grStorageSamples";
    public static final String RESULT = "result";


    public RepositoryList() {
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
	RepositoryRecord[] registrations = (RepositoryRecord[])context.get( RESULT );

	// if( invoices == null ) {

	//     SampleInventory dao = getSampleInventory();
	//     if( dao == null ) {
	// 	writeMessage( wnd, "Error: No database access configured" );
	// 	return;
	//     }
	//     try {
	// 	Period invPeriod = Period.fromQuarter( 0 );
	// 	invPeriod.join( Period.fromQuarter( INITIAL_PERIOD ) );

	// 	invoices = dao.findInvoiceByPeriod( invPeriod, true );
	// 	// if( invoices.length > 0 )
	// 	//     setLastCreatedDays( samples[0] );
	// 	context.put( RESULT, invoices );
	//     }
	//     catch( SQLException sqe ) {
	// 	writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	// 	log.error( sqe );
	// 	invoices = null;
	//     }
	// }

	if( registrations != null ) {
	    Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	    if( grSamples != null ) 
		assignModel( grSamples, context );
	}
    }

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
     * @return an <code>RegistrationView</code> value
     */
    public final RegistrationView getDetails() {
	return details;
    }

    /**
     * Set the <code>Details</code> value.
     *
     * @param details The new Details value.
     */
    public final void setDetails(final RegistrationView details) {
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
	log.debug( "Repository list model context: "+context );

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

	RepositoryListRenderer srr = new RepositoryListRenderer( cSetup, pref );
	grid.setRowRenderer( srr );

	RepositoryRecord[] registrations = (RepositoryRecord[])context.get( RESULT );
	if( registrations == null )	    
	    grid.setModel( new ListModelArray( new RepositoryRecord[0] ) );
	else {
	    log.debug( "Assigning model, number of registered samples: "+registrations.length );
	    grid.setModel( new ListModelArray( registrations ) );
	}
	
	cSetup.initSort( grid );

	writeMessage( grid, String.valueOf(((registrations != null)?registrations.length:0))+" registered samples match the search" );
    }

}
