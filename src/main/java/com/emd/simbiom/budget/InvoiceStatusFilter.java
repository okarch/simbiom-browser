package com.emd.simbiom.budget;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

// import org.zkoss.zul.Button;
// import org.zkoss.zul.Datebox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
// import org.zkoss.zul.Grid;
// import org.zkoss.zul.ListModelArray;
// import org.zkoss.zul.Row;
// import org.zkoss.zul.Rows;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.view.UIUtils;
// import com.emd.simbiom.util.Period;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>InvoiceStatusFilter</code> enables filtering according to invoice status.
 *
 * Created: Sat Apr 24 9:23:39 2019
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class InvoiceStatusFilter extends InventoryCommand {

    private static Log log = LogFactory.getLog(InvoiceStatusFilter.class);

    // public static final String CMP_INVOICE_FROM = "dbInvoiceFrom"; 
    // public static final String CMP_INVOICE_TO   = "dbInvoiceTo"; 

    /**
     * Creates a new command to filter according to invoice status.
     */
    public InvoiceStatusFilter() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    // private Period getInvoicePeriod( Window wnd ) {
    // 	Datebox db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_FROM );
    // 	Date dtFrom = null;
    // 	if( db != null )
    // 	    dtFrom = db.getValue();
    // 	db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_TO );
    // 	Date dtTo = null;
    // 	if( db != null )
    // 	    dtTo = db.getValue();
    // 	dtFrom = ((dtFrom==null)?new Date():dtFrom);
    // 	dtTo = ((dtTo==null)?new Date():dtTo);
    // 	dtTo.setHours( 23 );
    // 	dtTo.setMinutes( 59 );
    //     dtTo.setSeconds( 59 );
    // 	dtTo = DateUtils.addMilliseconds( dtTo, 999 );
    // 	if( !dtTo.after( dtFrom ) ) {
    // 	    log.error( "Invalid invoice period" );
    //  	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Invalid invoice period" );
    // 	    return null;
    // 	}
    // 	return new Period( dtFrom, dtTo );
    // }

    /**
     * Sets the invoice period and updates the list of invoices depicted.
     *
     * @param wnd the app window.
     * @param period the period.
     */
    // public void setInvoicePeriod( Window wnd, Period period ) {
    // 	if( period == null )
    // 	    return;
    // 	period.clearTime();
    // 	Datebox db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_FROM );
    // 	if( db != null ) 
    // 	    db.setValue( period.getStartDate() );
    // 	db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_TO );
    // 	if( db != null )
    // 	    db.setValue( period.getEndDate() );
    // 	updateInvoiceList( wnd, period );
    // }

    /**
     * Updates the period of displayed invoices.
     *
     * @param wnd the app window.
     * @param period the period to be displayed.
     */
    // public void updateInvoiceList( Window wnd, Period period ) {
    // 	log.debug( "Updating invoice period: "+period );
    // 	SampleInventory dao = getSampleInventory();
    // 	try {
    // 	    Invoice[] invs = dao.findInvoiceByPeriod( period, true );
    // 	    log.debug( "Number of invoices found: "+invs.length );
    // 	    Map context = new HashMap();
    // 	    context.put( InvoiceResult.RESULT, invs );
    // 	    invoices.assignModel( wnd, context );
    // 	}
    // 	catch( SQLException sqe ) {
    //  	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Cannot query database: "+
    // 			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
    // 	    log.error( sqe );
    // 	}
    // }

    private int getSelectedStatus( Combobox cb ) {
	Comboitem cbi = cb.getSelectedItem();
	if( cbi == null )
	    return -1;
	Object ciVal = cbi.getValue();
	if( ciVal == null )
	    return -1;
	return Stringx.toInt( Stringx.before( ciVal.toString(), "." ).trim(), -1 );
    }

    private Invoice[] findInvoices( Window wnd, int status ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
      	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Cannot access database" );
	    log.error( "Invalid database access" );
	    return null;
	}
	Invoice[] invs = null;
	try {
     	    invs = dao.findInvoiceByStatus( status );
     	    log.debug( "Number of invoices found: "+invs.length );
     	}
     	catch( SQLException sqe ) {
      	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Cannot query database: "+
     			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
     	    log.error( sqe );
	    invs = null;
     	}
	return invs;
    }

    private void updateInvoiceSummary( Window wnd, Invoice[] invs ) {
	log.debug( "Updating invoice summary, number of invoices: "+invs.length );
	InvoiceSummary summary = (InvoiceSummary)getPreferences().getCommand( "cmdInvoiceSummary" );
	if( summary == null ) {
     	    log.error( "Invoice summary command not defined" );
     	    return;
     	}
	summary.updateSummary( wnd, invs );
    }

    private void displayInvoices( Window wnd, Invoice[] invs ) {
	InvoiceResult invoices = (InvoiceResult)getPreferences().getResult( "grInvoices" );
     	if( invoices == null ) {
     	    log.error( "Invoice list not found" );
     	    return;
     	}
	Map context = new HashMap();
	context.put( InvoiceResult.RESULT, invs );
	invoices.assignModel( wnd, context );
	updateInvoiceSummary( wnd, invs );
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	Window wnd = UIUtils.getWindow( event );

	if( Events.ON_SELECT.equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    // UIUtils.clearMessage( wnd, "lbInvoiceMessage" );
	    
	    int status = getSelectedStatus( cb );
	    log.debug( "Invoice status selected: "+status );
	    if( status < 0 ) {
		log.error( "Invalid invoice status" );
		return;
	    }

	    Invoice[] invs = findInvoices( wnd, status );
	    if( invs != null )
		displayInvoices( wnd, invs );
	}
	else if( Events.ON_OPEN.equals( event.getName() ) ) {
	    log.debug( "Invoice status refresh received" );
	    Combobox cb = (Combobox)wnd.getFellowIfAny( "cbBudgetPeriod" );
	    Events.postEvent("onSelect", cb, null);
	}

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

    }    
    
} 
