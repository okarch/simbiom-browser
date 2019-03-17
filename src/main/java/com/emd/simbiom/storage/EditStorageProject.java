package com.emd.simbiom.storage;

import java.math.BigDecimal;

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
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

// import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
// import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
// import org.zkoss.zul.Row;
// import org.zkoss.zul.Rows;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.model.StorageProject;
import com.emd.simbiom.model.StorageGroup;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;
import com.emd.simbiom.util.Period;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>EditStorageProject</code> persists changes to a storage project.
 *
 * Created: Sun Mar 13  07:20:39 2019
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class EditStorageProject extends InventoryCommand {

    private static Log log = LogFactory.getLog(EditStorageProject.class);

    private static final String CMD_PROJECT_ADD  = "btStorageProjectAdd";
    private static final String CMD_PROJECT_SAVE = "btStorageProjectSave";

    private static final String NEW_PROJECT      = "budget.project.created";


    /**
     * Creates a new command to change the invoice period.
     */
    public EditStorageProject() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }


    // private void updateInvoiceList( Window wnd, Period period ) {
    // 	log.debug( "Updating invoice period: "+period );
    // }


    // private Invoice[] searchInvoices( Window wnd, String term ) {
    // 	log.debug( "Searching invoices: "+term );
    // 	InvoiceResult invoices = (InvoiceResult)getPreferences().getResult( "grInvoices" );
    // 	if( invoices == null ) {
    //  	    log.error( "Invoice list not found" );
    //  	    return null;
    //  	}
    // 	Invoice[] invs = null;
    //  	    invs = dao.findInvoiceByTerm( term );
    //  	    Map context = new HashMap();
    //  	    context.put( InvoiceResult.RESULT, invs );
    //  	    invoices.assignModel( wnd, context );
    //  	}
    //  	}
    // 	return invs;
    // }

    /**
     * Notifies this listener that an event occurs.
     */
    // public void onEvent(Event event)
    // 	throws java.lang.Exception {

    // 	log.debug( "Budget period model selected: "+event );

    // 	Combobox cb = (Combobox)event.getTarget();

    // 	Period selPeriod = null;

    // 	if( ("onAfterRender".equals( event.getName() )) && 
    // 	    ( cb != null ) &&
    // 	    ( cb.getItemCount() > 2 ) ) {

    // 	    cb.setSelectedIndex( 1 );
    // 	    selPeriod = getSelectedPeriod( cb, 1 );
    // 	    // int idx = popAfterRenderIndex( cb.getId(), 0 );
    // 	    // // log.debug( "Invoice status after render index: "+idx );
    // 	    // if( idx < cb.getItemCount() ) {
    // 	    // 	cb.setSelectedIndex( idx );
    // 	    // }	    
    // 	}	
    // 	else if( (Events.ON_SELECT.equals( event.getName() ) ) &&
    // 		 ( cb != null ) &&
    // 		 ( cb.getItemCount() > 0 ) ) {
    // 	    int idx = cb.getSelectedIndex();
    // 	    if( idx >= 0 ) 
    // 		selPeriod = getSelectedPeriod( cb, idx );
    // 	}

    // 	if( selPeriod != null ) {
    // 	    Window wnd = ZKUtil.findWindow( cb );
    // 	    Invoice[] invs = updateInvoiceList( wnd, selPeriod );
    // 	    updateInvoiceDates( wnd, selPeriod );
    // 	    if( invs != null )
    // 		updateInvoiceSummary( wnd, invs );
    // 	}
    // }

    private void clearStorageProject( Window wnd, String title ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtProjectName" );
	if( txt != null )
	    txt.setValue( title );
	Decimalbox dec = (Decimalbox)wnd.getFellowIfAny( "decBillingRemainder" );
	if( dec != null )
	    dec.setValue( new BigDecimal(0) );
     	AddBillingItem billInfo = (AddBillingItem)getPreferences().getCommand( "btBillingAdd_0" );
	if( billInfo != null )
	    billInfo.clearBilling( wnd );
	Combobox cb = (Combobox)wnd.getFellowIfAny( "cbStorageGroup" );
	if( cb != null ) 
	    cb.setModel( new ListModelArray( new StorageGroup[0] ) );	
    }

    private void createNewProject() {
     	Session ses = Sessions.getCurrent();
     	if( ses == null ) 
     	    return;
	ses.setAttribute( NEW_PROJECT, "true" );
    }

    private boolean isNewProject() {
     	Session ses = Sessions.getCurrent();
     	if( ses == null ) 
     	    return true;
	String st = (String)ses.getAttribute( NEW_PROJECT );
	boolean flag = Stringx.toBoolean( Stringx.getDefault(st,"false"), false );
	return flag;
    }

    private void clearNewProject() {
     	Session ses = Sessions.getCurrent();
     	if( ses == null ) 
     	    return;
	String st = (String)ses.getAttribute( NEW_PROJECT );
	if( st != null )
	    ses.removeAttribute( NEW_PROJECT );
    }

    private StorageGroup[] collectStorageGroups( Window wnd ) {
	InventoryPreferences pref = getPreferences();
	StorageGroupModel sGroup = (StorageGroupModel)pref.getResult( "cbStorageGroup" );
	if( sGroup != null ) 
	    return sGroup.getStorageGroups( wnd );
	return new StorageGroup[0];
    }

    private void updateStorageProjects( Window wnd ) {
	InventoryPreferences pref = getPreferences();
	StorageProjectModel sGroup = (StorageProjectModel)pref.getResult( "cbStorageProject" );
	if( sGroup != null ) 
	    sGroup.initModel( wnd, null );
	sGroup = (StorageProjectModel)pref.getResult( "cbBudgetProject" );
	if( sGroup != null ) 
	    sGroup.initModel( wnd, null );
    }

    private StorageProject saveStorageProject( Window wnd ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtProjectName" );
	String title = null;
	if( txt != null )
	    title = Stringx.getDefault( txt.getValue(), "" ).trim();
	else
	    title = "";
	if( title.length() <= 0 ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Project title must not be empty." );
	    return null;
	}

	// collect storage groups

	StorageGroup[] grps = collectStorageGroups( wnd );

	StorageProject prj = null;

	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Invalid database access." );
	    return null;
	}
      	try {
	   StorageProject[] prjs = dao.findStorageProject( title );
	   if( prjs.length > 1 ) {
	       showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Multiple storage projects matching \""+title+"\"." );
	       return null;
	   }
	   else if( (prjs.length > 0) && (isNewProject()) ) {
	       showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Storage project already exists \""+title+"\"." );
	       return null;
	   }
	   else if( isNewProject() ) {
	       prj = dao.createStorageProject( title );
	   }
	   else if( prjs.length > 0 ) {
	       prj = prjs[0];
	       prj.setTitle( title );
	   }
	   if( prj != null ) {
	       prj.setStorageGroups( grps );
	       prj = dao.storeStorageProject( prj );
	   }
      	}
      	catch( SQLException sqe ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Cannot query database: "+
      			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
      	    log.error( sqe );
	}
	return prj;
    }

    private void saveBilling( Window wnd, StorageProject prj ) {
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

	UIUtils.clearMessage( wnd, "lbStorageMessage" );

	if( CMD_PROJECT_ADD.equals(getCommandName()) ) {
	    clearStorageProject( wnd, "New Storage Project" );
	    createNewProject();
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Warning: New storage project has not been saved yet." );
	}
	else if( CMD_PROJECT_SAVE.equals(getCommandName()) ) {
	    StorageProject prj = saveStorageProject( wnd );
	    saveBilling( wnd, prj );
	    if( prj != null ) {
		saveBilling( wnd, prj );
		clearNewProject();
		updateStorageProjects( wnd );
	    }
	}
    }    
    
} 
