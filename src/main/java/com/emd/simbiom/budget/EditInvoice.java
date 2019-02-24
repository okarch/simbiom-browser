package com.emd.simbiom.budget;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.model.StorageProject;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>EditInvoice</code> display extended sample information.
 *
 * Created: Sat Dec  1 12:43:39 2018
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class EditInvoice extends InventoryCommand {
    private String detailsTemplate;

    private static Log log = LogFactory.getLog(EditInvoice.class);

    /**
     * Creates a new command to selecte the search operator.
     */
    public EditInvoice() {
	super();
    }

    /**
     * Get the <code>DetailsTemplate</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getDetailsTemplate() { 
	return detailsTemplate;
    }

    /**
     * Set the <code>DetailsTemplate</code> value.
     *
     * @param detailsTemplate The new DetailsTemplate value.
     */
    public final void setDetailsTemplate(final String detailsTemplate) {
	this.detailsTemplate = detailsTemplate;
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private Invoice loadInvoice( Window wnd, long invId ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: invalid database access" );
	    return null;
	}
	Invoice inv = null;
	try {
	    inv = dao.findInvoiceById( invId );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: "+Stringx.getDefault( sqe.getMessage(), "General SQL error" ) );
	    log.error( sqe );
	}
	return inv;
    }

    private long extractInvoiceId( Component cmp ) {
	if( (cmp == null) || !(cmp instanceof Button) )
	    return 0L;

	String cmpId = Stringx.getDefault(cmp.getId(),"");
	return Stringx.toLong( Stringx.after( cmpId, "_" ), 0L );
    }

    private Window createWindow( Window wnd, String title, InvoiceDetailsView vDetails ) {
	Window wndDetails = new Window();
	wndDetails.setParent( wnd );
	wndDetails.setId( "wndDetails" );
	wndDetails.setTitle( title );
	wndDetails.setBorder( "normal" );
	wndDetails.setWidth( "900px" );
	wndDetails.setPosition( "center,center" );
	wndDetails.setClosable( true );
    // action="show: slideDown;hide: slideUp"

	Vlayout vl = new Vlayout();
	vl.setId( vDetails.getDetailsLayout() );
	vl.setParent( wndDetails );

	return wndDetails;
    }

    private Map createContext( Window wnd, Invoice invoice, boolean invoiceExist ) {
	Map ctxt = new HashMap();

	InvoiceDetails det = new InvoiceDetails( invoice );
	det.setInvoiceExist( invoiceExist );

	SampleInventory dao = getSampleInventory();
	if( dao != null ) {
            try {
		StorageProject[] prjs = dao.findStorageProject( null );
		det.setAllProjects( prjs );
		Billing[] allPOs = dao.findBilling( 0L, null );
		det.setAllPurchases( allPOs );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessage", "lbMessage", "Error: "+Stringx.getDefault( sqe.getMessage(), "General SQL error" ) );
		log.error( sqe );
	    }
	}
	else
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: invalid database access" );

	ctxt.put( "details", det );

	ctxt.put( "dates", DateUtils.class );
	ctxt.put( "dateFormats", DateFormatUtils.class );

	// specific tools
	// tc.put( "samples", SampleType.class );
	// tc.put( "subjects", Subject.class );
	// tc.put( "studies", Study.class );

	// ctxt.put( "db", getSampleInventoryDAO.getInstance() );
	return ctxt;
    }

    private InvoiceDetailsView createDetailsView() {
	ModelProducer[] res = getPreferences().getResult( InvoiceResult.class );
	if( res.length <= 0 )
	    return null;
	return ((InvoiceResult)res[0]).getDetails();
    }

    private void updateModel( Window wnd ) {
	ModelProducer[] res = getPreferences().getResult( InvoiceResult.class );
	if( res.length <= 0 )
	    return;
	// SampleResult sRes = (SampleResult)res[0];
	// Grid results = (Grid)wnd.getFellowIfAny( sRes.getModelName() );
	// if( results == null ) 
	//     return;
	// ListModelArray lm = (ListModelArray)results.getListModel();
	// Sample[] samples = (Sample[])lm.getInnerArray();
	// Map context = new HashMap();
	// context.put( SampleResult.RESULT, samples );
	// sRes.assignModel( results, context );
    }

    private void displayDetails( Window wnd, Invoice invoice, boolean invoiceExist ) {
	log.debug( "Display details of "+invoice );

	InvoiceDetailsView vDetails = createDetailsView();
	if( vDetails == null ) {
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: Cannot create invoice details" );
	    log.error( "Cannot create view, check configuration" );
	    return;
	}

	Window wndDetails = createWindow( wnd, "Details of "+invoice, vDetails );
	final Window parentWindow = wnd;
	wndDetails.addEventListener( Events.ON_CLOSE, new EventListener() {
		public void onEvent( Event evt ) {
		    log.debug( "Closing event received" );
		    updateModel( parentWindow );
		}
	    });

	vDetails.updateActions( getPortletId(), getUserId() );
	// vDetails.setTemplate( getDetailsTemplate() );
        vDetails.setShowMergeResult( true );
	vDetails.initView( wndDetails, createContext(wnd, invoice, invoiceExist) );
	
	wndDetails.doModal();
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Edit invoice: "+event );
	Component cmp = event.getTarget();
	Window wnd = ZKContext.findWindow( cmp );

	long invId = extractInvoiceId( cmp );

	Invoice invoice = null;
	boolean invoiceExist = false;
	if( invId != 0L ) {
	    log.error( "Loading invoice details" );
	    invoice = loadInvoice( wnd, invId );
	    invoiceExist = true;
	}
	else {
	    log.error( "Creating new invoice" );
	    invoice = new Invoice();
	}

	if( invoice != null )
	    displayDetails( wnd, invoice, invoiceExist );
    }
		
    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	// SampleRow sr = getSelectedSample( wnd );

	// if( sr != null )
	//     log.debug( "Sample selected: "+sr.getSample() );
	// else
	//     log.error( "No sample selected!" );

	// int nfCount = nextFilterCount( wnd );
	// if( nfCount <= 0 )
	//     return;

	// log.debug( "Next filter to be added: "+nfCount );

	// // create and add the new filter row

	// Row row = createRow( nfCount );
	// Row rowActions = (Row)wnd.getFellowIfAny( "rowFilterActions" );
	// if( rowActions == null )
	//     return;
	// Rows rows = (Rows)wnd.getFellowIfAny( "rowsFilter" );
	// if( rows == null )
	//     return;
	// rows.insertBefore( row, rowActions );

	// // initialize the row

	// initOperatorSelect( wnd, nfCount );

	// //initializes the filter model

	// FilterModel fm = createFilterModel( wnd, nfCount );
	// if( fm == null )
	//     return;
	// getPreferences().setResult( fm );
	// fm.initModel( wnd, null );

	// log.debug( "New filter model "+nfCount+" initialized" );

    }    
    
} 
