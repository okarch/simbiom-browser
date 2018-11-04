package com.emd.simbiom.budget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.text.SimpleDateFormat;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Invoice;

// import com.emd.simbiom.config.InventoryPreferences;

// import com.emd.simbiom.util.Period;

// import com.emd.simbiom.view.ColumnSet;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * InvoiceStatusModel produces the model to hold invoice status.
 * Created: Mon Oct 29 08:24:09 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class InvoiceStatusModel extends DefaultModelProducer {

    private static Log log = LogFactory.getLog(InvoiceStatusModel.class);

    public static final String KEY_RESULT = "result";
    public static final String KEY_DATA   = "data";
    

    public InvoiceStatusModel() {
	super();
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	// if( context == null )
	//     context = new HashMap();
	// Object[] statusElements = (Object[])context.get( KEY_RESULT );

	// if( statusElements == null ) {

	//     SampleInventory dao = getSampleInventory();
	//     if( dao == null ) {
	// 	writeMessage( wnd, "Error: No database access configured" );
	// 	return;
	//     }
	//     try {
	// 	Period invPeriod = Period.fromQuarter( 0 );
	// 	invPeriod.join( Period.fromQuarter( -1 ) );

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

	// if( invoices != null ) {
	//     Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	//     if( grSamples != null ) 
	// 	assignModel( grSamples, context );
	// }
    }

    /**
     * Get the <code>Details</code> value.
     *
     * @return a <code>SampleDetailsView</code> value
     */
    // public final SampleDetailsView getDetails() {
    // 	return details;
    // }

    /**
     * Set the <code>Details</code> value.
     *
     * @param details The new Details value.
     */
    // public final void setDetails(final SampleDetailsView details) {
    // 	this.details = details;
    // }

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

    private String itemFormat( String item, Timestamp dt ) {
	if( dt == null )
	    return item;
	StringBuilder stb = new StringBuilder( item );
	stb.append( " on " );
	SimpleDateFormat formatter = new SimpleDateFormat( "dd-MMM-yyyy" );
	stb.append( formatter.format(dt) );
	return stb.toString();
    }
 
    private String[] createModel( Invoice invoice ) {
	List<String> items = new ArrayList<String>();
	if( invoice == null ) {
	    items.add( "Unknown" );
	}
	else {
	    items.add( "Not reviewed" );
	    items.add( itemFormat( "Checked", invoice.getVerified() ) );
	    items.add( itemFormat( "Approved", invoice.getApproved() ) );
	    items.add( "Issues" );
	    items.add( "Rejected" );
	}
	String[] iArray = new String[ items.size() ];
	return (String[])items.toArray( iArray );
    }

    protected void assignCombobox( Combobox grid, Map context ) {
	log.debug( "Invoice status model context: "+context );

	// SampleInventory dao = getSampleInventory();
	// if( dao == null ) {
	//     writeMessage( grid, "Error: No database access configured" );
	//     return;
	// }
	
	// InventoryPreferences pref = getPreferences();
	// ColumnSetup def = getColumnSetup();
	// if( def == null )
	//     def = new ColumnSetup();
	
	// ColumnSetup cSetup = ColumnSetup.getInstance( def );
	// log.debug( "Column setup: "+cSetup );

	// InvoiceResultRenderer srr = new InvoiceResultRenderer( getColumnSet(), pref );
	// grid.setRowRenderer( srr );

	String[] items = createModel( (Invoice)context.get( KEY_DATA ) );
	// if( invoice == null )	    
	//     grid.setModel( new ListModelArray( new Invoice[0] ) );
	// else {
	// log.debug( "Assigning model, number of invoices: "+invoices.length );
	grid.setModel( new ListModelArray( items ) );
	// }
	
	// writeMessage( grid, String.valueOf(((invoices != null)?invoices.length:0))+" invoices match the search" );
    }

}
