package com.emd.simbiom.storage;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
// import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.view.ModelProducer;

import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.StorageGroup;
import com.emd.simbiom.model.StorageProject;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.ZKUtil;
import com.emd.zk.command.CommandException;

/**
 * The <code>SelectStorageDetails</code> action populates the storage project details.
 *
 * Created: Wed Sep 19 19:18:39 2018
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class SelectStorageDetails extends InventoryCommand {

    private static Log log = LogFactory.getLog(SelectStorageDetails.class);

    private static final String CMP_PROJECT_TITLE = "txtProjectName";
    private static final String CMP_PROJECT_CODE  = "txtProjectCode";
    private static final String CMP_PURCHASE_ORDER= "txtPurchaseOrder";
    private static final String CMP_STORAGE_GROUP = "cbStorageGroup";
 
    /**
     * Creates a new command to select from the result log.
     */
    public SelectStorageDetails() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private StorageGroupModel getStorageGroupModel() {
      	ModelProducer[] mp = getPreferences().getResult( StorageGroupModel.class );
      	if( mp.length <= 0 )
      	    return null;
      	return (StorageGroupModel)mp[0];
    }

    // private BatchEntry getSelectedEntry( Window wnd ) {
    // 	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
    // 	if( cb != null ) {
    // 	    int idx = cb.getSelectedIndex();
    // 	    if( idx >= 0 ) {
    // 		return (BatchEntry)cb.getModel().getElementAt(idx);
    // 	    }
    // 	}
    // 	return null;
    // }

    /**
     * Selects an entry by upload id.
     *
     * @param wnd the window.
     * @param uploadid the upload to select.
     *
     */
    // public BatchEntry selectUploadid( Window wnd, long uploadid ) {
    // 	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
    // 	BatchEntry be = null;
    // 	if( cb != null ) {
    // 	    ListModel model = cb.getModel();
    // 	    int numEntries = model.getSize();
    // 	    int selIdx = -1;
    // 	    for( int i = 0; i < numEntries; i++ ) {
    // 		be = (BatchEntry)cb.getModel().getElementAt(i);
    // 		if( be.getUploadid() == uploadid ) {
    // 		    selIdx = i;
    // 		    break;
    // 		}
    // 	    }
    // 	    if( selIdx >= 0 )
    // 		cb.setSelectedIndex( selIdx );
    // 	}
    // 	return be;
    // }

    private void setText( Window wnd, String cmp, String title ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( cmp );
	if( txt == null )
	    return;
	txt.setValue( title );
    }

    private void setGroups( Window wnd, StorageProject prj ) {
	StorageGroupModel mGroups = getStorageGroupModel();
	if( mGroups == null )
	    return;

	StorageGroup[] grps = null;
	if( prj == null )
	    grps = new StorageGroup[0];
	else
	    grps = prj.getStorageGroups();
	Map ctxt = new HashMap();
	ctxt.put( StorageGroupModel.RESULT, grps );

	log.debug( "Number of storage groups: "+grps.length );

	mGroups.assignModel( wnd, ctxt );
    }

    private void clearDetails( Window wnd ) {
	setText( wnd, CMP_PROJECT_TITLE, "" );
	setGroups( wnd, null );
	setText( wnd, CMP_PROJECT_CODE, "" );
	setText( wnd, CMP_PURCHASE_ORDER, "" );
    }

    private void updateBilling( Window wnd, Billing[] bills ) {
      	AddBillingItem bItem = (AddBillingItem)getPreferences().getCommand( AddBillingItem.class );
	if( bItem == null ) 
	    return;
	bItem.updateBilling( wnd, bills );
    }

    private void updateDetails( Window wnd, StorageProject prj ) {
	setText( wnd, CMP_PROJECT_TITLE, prj.getTitle() );
	setGroups( wnd, prj );
	SampleInventory dao = getSampleInventory();
	if( dao != null ) {
	    try {
		Billing[] bills = dao.findBillingByProject( prj );
		log.debug( "Number of billing records found associated with "+prj+": "+bills.length );
		updateBilling( wnd, bills );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    }
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Storage details event: "+event );

	StorageProject prj = null;

	Component cmp = event.getTarget();
	if( (cmp == null) || !(cmp instanceof Combobox) )
	    return;
	Combobox cb = (Combobox)cmp;
	Window wnd = ZKUtil.findWindow( cb );

	if( Events.ON_SELECT.equals( event.getName() ) ) {
	    if( cb.getItemCount() > 0 ) {
	 	int idx = cb.getSelectedIndex();
	 	if( idx >= 0 )
	 	    prj = (StorageProject)cb.getModel().getElementAt( idx );
	    }
	}

	clearDetails( wnd );

	if( prj != null ) {
	    log.debug( "Selected storage details: "+prj );
	    updateDetails( wnd, prj );
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
	    // showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
	    // 		 Stringx.getDefault( sqe.getMessage(), "General database error" ) );

    }    
    
} 
