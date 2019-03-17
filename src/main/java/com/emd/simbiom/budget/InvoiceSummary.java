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
// import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
// import org.zkoss.zul.ListModelArray;
// import org.zkoss.zul.Row;
// import org.zkoss.zul.Rows;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Label;
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
 * <code>InvoiceSummary</code> updates the invoice summary stats.
 *
 * Created: Sun Mar 11  17:42:39 2019
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class InvoiceSummary extends InventoryCommand {

    private static Log log = LogFactory.getLog(InvoiceSummary.class);

    private static final String LB_TOTAL = "lbInvoiceSummaryTotal";


    /**
     * Creates a new command to change the invoice summary.
     */
    public InvoiceSummary() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    /**
     * Updates the summary using the given list of invoices.
     *
     * @param wnd the window.
     * @param invoices the list of invoices.
     */
    public void updateSummary( Window wnd, Invoice[] invoices ) {
	StringBuilder stb = new StringBuilder();
	stb.append( "Number of invoices: " );
	stb.append( String.valueOf( invoices.length ) );

	float totalAmt = 0f;
	for( int i = 0; i < invoices.length; i++ ) {
	    totalAmt+=invoices[i].getAmount();
	}
	stb.append( ", Total invoice amount: " );
	stb.append( totalAmt );
	Label lb = (Label)wnd.getFellowIfAny( LB_TOTAL );
	if( lb != null ) 
	    lb.setValue( stb.toString() );
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
