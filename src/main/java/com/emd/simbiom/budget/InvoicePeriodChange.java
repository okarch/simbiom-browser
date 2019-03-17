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
import org.zkoss.zul.Datebox;
// import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
// import org.zkoss.zul.ListModelArray;
// import org.zkoss.zul.Row;
// import org.zkoss.zul.Rows;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.model.StorageProject;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;
import com.emd.simbiom.util.Period;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>InvoicePeriodChange</code> changes period of displayed invoices.
 *
 * Created: Sat Mar  9 18:43:39 2019
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class InvoicePeriodChange extends InventoryCommand {

    private static Log log = LogFactory.getLog(InvoicePeriodChange.class);

    public static final String CMP_INVOICE_FROM = "dbInvoiceFrom"; 
    public static final String CMP_INVOICE_TO   = "dbInvoiceTo"; 

    /**
     * Creates a new command to change the invoice period.
     */
    public InvoicePeriodChange() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private Period getInvoicePeriod( Window wnd ) {
	Datebox db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_FROM );
	Date dtFrom = null;
	if( db != null )
	    dtFrom = db.getValue();
	db = (Datebox)wnd.getFellowIfAny( CMP_INVOICE_TO );
	Date dtTo = null;
	if( db != null )
	    dtTo = db.getValue();
	dtFrom = ((dtFrom==null)?new Date():dtFrom);
	dtTo = ((dtTo==null)?new Date():dtTo);
	dtTo.setHours( 23 );
	dtTo.setMinutes( 59 );
        dtTo.setSeconds( 59 );
	dtTo = DateUtils.addMilliseconds( dtTo, 999 );
	if( !dtTo.after( dtFrom ) ) {
	    log.error( "Invalid invoice period" );
     	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Invalid invoice period" );
	    return null;
	}
	return new Period( dtFrom, dtTo );
    }

    private void updateInvoiceList( Window wnd, Period period ) {
	log.debug( "Updating invoice period: "+period );
	InvoiceResult invoices = (InvoiceResult)getPreferences().getResult( "grInvoices" );
	if( invoices == null ) {
	    log.error( "Invoice list not found" );
	    return;
	}
	SampleInventory dao = getSampleInventory();
	try {
	    Invoice[] invs = dao.findInvoiceByPeriod( period, true );
	    Map context = new HashMap();
	    context.put( InvoiceResult.RESULT, invs );
	    invoices.assignModel( wnd, context );
	}
	catch( SQLException sqe ) {
     	    showMessage( wnd, "rowBudgetMessage", "lbInvoiceMessage", "Error: Cannot query database: "+
			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
 	    log.error( sqe );
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

	UIUtils.clearMessage( wnd, "lbInvoiceMessage" );

	Period invPeriod = getInvoicePeriod( wnd );
	log.debug( "Invoice period change to "+((invPeriod==null)?"INVALID":invPeriod.toString()) );
	if( invPeriod == null )
	    return;
	else
	    updateInvoiceList( wnd, invPeriod );
    }    
    
} 
