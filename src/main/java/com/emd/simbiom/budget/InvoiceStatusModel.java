package com.emd.simbiom.budget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.text.SimpleDateFormat;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

// import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventory;

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
public class InvoiceStatusModel extends DefaultModelProducer implements EventListener {

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

    // private String itemFormat( String item, Timestamp dt ) {
    // 	long noDate = (new Timestamp( 1000L )).getTime();
    // 	if( (dt == null) || (dt.getTime() <= noDate) )
    // 	    return item;
    // 	StringBuilder stb = new StringBuilder( item );
    // 	stb.append( " on " );
    // 	SimpleDateFormat formatter = new SimpleDateFormat( "dd-MMM-yyyy" );
    // 	stb.append( formatter.format(dt) );
    // 	return stb.toString();
    // }
 
    // private String[] createModel( String key, Invoice invoice ) {
    // 	List<InvoiceStatusItem> items = new ArrayList<InvoiceStatusItem>();
    // 	if( invoice == null ) {
    // 	    items.add( new InvoiceStatusItem("Unknown") );
    // 	}
    // 	else {
    // 	    items.add( new InvoiceStatusItem("Not reviewed") );

    // 	    String itemSt = itemFormat( "Checked", invoice.getVerified() );
    // 	    items.add( itemSt );
    // 	    if( itemSt.length() > "Checked".length() )
    // 		pushAfterRenderIndex( key, 1 );-

    // 	    itemSt = itemFormat( "Approved", invoice.getApproved() );
    // 	    items.add( itemSt );
    // 	    if( itemSt.length() > "Approved".length() )
    // 		pushAfterRenderIndex( key, 2 );
	    
    // 	    items.add( "Issues" );
    // 	    items.add( "Rejected" );
    // 	}
    // 	String[] iArray = new String[ items.size() ];
    // 	return (String[])items.toArray( iArray );
    // }

    private InvoiceStatusItem[] createModel( String key, Invoice invoice ) {
     	List<InvoiceStatusItem> items = new ArrayList<InvoiceStatusItem>();
     	if( invoice == null ) {
     	    items.add( new InvoiceStatusItem("Unknown") );
     	}
     	else {
	    InvoiceStatusItem isi = new InvoiceStatusItem( "Not reviewed" );
	    isi.setResetReview( true );
     	    items.add( isi );
	    
	    isi = new InvoiceStatusItem( "Checked" );
	    isi.setStatusDate( invoice.getVerified() );
	    items.add( isi );
	    if( isi.validStatusDate() )
		pushAfterRenderIndex( key, 1 );

	    isi = new InvoiceStatusItem( "Approved" );
	    isi.setStatusDate( invoice.getApproved() );
	    items.add( isi );
	    if( isi.validStatusDate() )
		pushAfterRenderIndex( key, 2 );

	    items.add( new InvoiceStatusItem( "Issues" ) );
     	    // items.add( new InvoiceStatusItem( "Rejected" ) );

	    isi = new InvoiceStatusItem( "Rejected" );
	    isi.setStatusDate( invoice.getRejected() );
	    items.add( isi );
	    if( isi.validStatusDate() )
		pushAfterRenderIndex( key, 4 );
	}
	InvoiceStatusItem[] iArray = new InvoiceStatusItem[ items.size() ];
	return (InvoiceStatusItem[])items.toArray( iArray );
    }

    private void pushAfterRenderIndex( String key, int idx ) {
	Session ses = Sessions.getCurrent();
	if( ses != null )
	    ses.setAttribute( key+".afterRenderIndex", new Integer(idx) );
    }
    private int popAfterRenderIndex( String key, int def ) {
	Session ses = Sessions.getCurrent();
	int idx = def;
	if( ses != null ) {
	    Integer ari = (Integer)ses.getAttribute( key+".afterRenderIndex" );
	    if( ari != null ) {
		idx = ari.intValue();
		ses.removeAttribute( key+".afterRenderIndex" );
	    }
	}
	return idx;
    }

    private void updateStatusModel( Combobox cb, int index ) {
	ListModel model = cb.getModel();
	if( model == null )
	    return;
	InvoiceStatusItem isi = (InvoiceStatusItem)model.getElementAt( index );
	isi.setStatusDate( new Timestamp( System.currentTimeMillis() ) );
	List<InvoiceStatusItem> items = new ArrayList<InvoiceStatusItem>();
	for( int i = 0; i < model.getSize(); i++ ) {
	    InvoiceStatusItem item = (InvoiceStatusItem)model.getElementAt(i);
	    if( index == 0 )
		item.setStatusDate( null );
	    items.add( item );
	}
	cb.setModel( new ListModelArray( items ) );
	pushAfterRenderIndex( cb.getId()+"_chg", index );
	pushAfterRenderIndex( cb.getId(), index );
    } 

    private void changeStatus( Combobox cb, int index ) {
	ListModel model = cb.getModel();
	if( model == null )
	    return;
	InvoiceStatusItem isi = (InvoiceStatusItem)model.getElementAt( index );
	final Combobox statusBox = cb;
	final int idx = index;
	if( isi.isResetReview() ) {
	    Messagebox.show( "Do you want to reset review status?",
			     "Reset Review", 
			     Messagebox.YES+Messagebox.NO, 
			     Messagebox.QUESTION,
			     new EventListener() {
				 public void onEvent(Event event) {
				     if( (Messagebox.ON_YES.equals(event.getName()))) {
					 log.debug( "Review status will be reset" );
					 updateStatusModel( statusBox, idx );
				     }
				 }
			     });
	}
	else if( isi.validStatusDate() ) {
	    Messagebox.show( "Change status date to "+(new Timestamp( System.currentTimeMillis()))+"?",
			     "Change Status", 
			     Messagebox.YES+Messagebox.NO, 
			     Messagebox.QUESTION,
			     new EventListener() {
				 public void onEvent(Event event) {
				     if( (Messagebox.ON_YES.equals(event.getName()))) {
					 log.debug( "Status about to be changed." );
					 updateStatusModel( statusBox, idx );
				     }
				 }
			     });
	}
	else {
	    updateStatusModel( cb, index );
	}
    }

    private void saveChanges( Combobox cb ) {
	int idx = popAfterRenderIndex( cb.getId()+"_chg", -1 );
	if( idx < 0 )
	    return;
	ListModel model = cb.getModel();
	if( model == null )
	    return;
	
	// retrieve invoice
	
	// String invId = StringUtils.substringBetween( cb.getId(), "cb_", "_chg" );
	String invId = StringUtils.substringAfter( cb.getId(), "cbInvoiceStatus_" );
	log.debug( "Extracted invoice id: "+invId );
	if( (invId == null) || (invId.length() <= 0) ) {
	    log.error( "Invoice id cannot be determined from "+cb.getId() );
	    return;
	}
	long invoiceId = Stringx.toLong( invId, 0L );
	if( invoiceId == 0L ) {
	    log.error( "Invoice id is invalid: "+invId );
	    return;
	}

	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    log.error( "Invalid sample inventory access" );
	    return;
	}

	Invoice invoice = null;
	try {
	    invoice = sInv.findInvoiceById( invoiceId );
	}
	catch( SQLException sqe ) {
	    log.error( "Cannot find invoice: "+invoiceId+" "+Stringx.getDefault(sqe.getMessage(),"General error") );
	    return;
	}

	// assign check/approval date

	InvoiceStatusItem item = null;
	if( model.getSize() > 0 ) {
	    item = (InvoiceStatusItem)model.getElementAt(1);
	    invoice.setVerified( item.getStatusDate() );
	}
	if( model.getSize() > 1 ) {
	    item = (InvoiceStatusItem)model.getElementAt(2);
	    invoice.setApproved( item.getStatusDate() );
	}
	
	// persist changes

	try {
	    sInv.storeInvoice( invoice );
	}
	catch( SQLException sqe ) {
	    log.error( "Cannot store invoice: "+invoice+" "+Stringx.getDefault(sqe.getMessage(),"General error") );
	    return;
	}
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	// log.debug( "Invoice status model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	    combobox.addEventListener( Events.ON_SELECT, this );

	InvoiceStatusItem[] items = createModel( combobox.getId(), (Invoice)context.get( KEY_DATA ) );
	// log.debug( "Assigning model, number of invoices: "+invoices.length );
	combobox.setModel( new ListModelArray( items ) );
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	// log.debug( "Invoice status model selected: "+event );

	Combobox cb = (Combobox)event.getTarget();

	if( "onAfterRender".equals( event.getName() ) ) {
	    int idx = popAfterRenderIndex( cb.getId(), 0 );
	    // log.debug( "Invoice status after render index: "+idx );
	    if( idx < cb.getItemCount() ) {
		cb.setSelectedIndex( idx );
	    }	    
	}	
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    if( cb.getItemCount() > 0 ) {
	     	int idx = cb.getSelectedIndex();
	     	if( idx >= 0 ) {
		    changeStatus( cb, idx );
		    saveChanges( cb );
		}
	    }
	}

	// if( templ != null ) {
	//     Window wnd = ZKUtil.findWindow( cb );
	//     updateTemplateText( wnd, templ );
	// }
    }

}
