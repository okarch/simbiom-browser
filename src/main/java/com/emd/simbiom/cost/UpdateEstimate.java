package com.emd.simbiom.cost;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.CostEstimate;
import com.emd.simbiom.model.CostItem;
import com.emd.simbiom.model.CostSample;

import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.SwitchTab;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>UpdateEstimate</code> updates the cost estimate.
 *
 * Created: Wed Jul 20 09:32:34 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class UpdateEstimate extends InventoryCommand {

    private static Log log = LogFactory.getLog(UpdateEstimate.class);

    private static final String COST_ADD    = "btCostAdd";
    private static final String COST_REMOVE = "btCostRemove";

    public UpdateEstimate() {
	super();
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    // public void onEvent( Event event )	throws Exception {
    // 	Component cmp = event.getTarget();
    // 	if( cmp == null ) {
    // 	    log.error( "Cannot determine event target" );
    // 	    return;
    // 	}
    // 	Window wnd = ZKContext.findWindow( cmp );
    // 	if( (cmp instanceof Intbox) && (event instanceof InputEvent) ) {
    // 	    int diff = Stringx.toInt(((InputEvent)event).getValue(),0);
    // 	    setEnableItemButtons( wnd, cmp, ( diff > 0 ) );
    // 	}
    // }

    private CostEstimate getCostEstimate( Window wnd ) {
	SwitchTab sw = (SwitchTab)InventoryPreferences.getInstance( getPortletId(), getUserId() ).getCommand( SwitchTab.class );
	CostEstimate ce = null;
	if( sw != null )
	    ce = sw.getCostEstimate( wnd );
	if( ce == null ) {
	    log.debug( "Creating cost estimate" );
	    SampleInventoryDAO dao = getSampleInventory();
	    try {
		ce = dao.createCostEstimate( null );
	    }
	    catch( SQLException sqe ) {
		String msg = "Error: "+Stringx.getDefault( sqe.getMessage(), "" );
		showMessage( wnd, "rowMessageCost", "lbMessageCost", msg );
		log.error( sqe );
	    }
	    if( (ce != null) && (sw != null) )
		sw.setCostEstimate( wnd, ce );
	}
	return ce;
    }

    private CostEstimate updateCostEstimate( Window wnd, CostEstimate ce ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtCostProject" );
	if( txt != null ) {
	    String st = Stringx.getDefault(txt.getValue(),"").trim();
	    if( st.length() > 0 )
		ce.setProjectname( st );
	}
	Combobox cb = (Combobox)wnd.getFellowIfAny( "cbDuration" );
	if( cb != null ) {
	    Object val = cb.getSelectedItem().getValue();
	    String st = Stringx.getDefault(val.toString(),"").trim();
	    if( st.length() > 0 )
		ce.setDuration( Stringx.toInt(st,0) );
	}
	
	cb = (Combobox)wnd.getFellowIfAny( "cbRegistration" );
	int idx = -1;
	ListModel model = null;

	// clears all costs.

	ce.clearCosts();

	// add costs due to registration mode.

	if( (cb != null) && 
	    ((idx = cb.getSelectedIndex()) >= 0) && 
	    ((model = cb.getModel()) != null) ) {
	    CostSample cs = (CostSample)model.getElementAt( idx );
	    CostItem ci = ce.addCostItem( cs, 0L );
	}

	// add sample related costs.

	int k = 0;
	while( (cb = (Combobox)wnd.getFellowIfAny( "cbCostSample_"+String.valueOf(k) )) != null ) {
	    CostSample cs = null;
	    if( ((idx = cb.getSelectedIndex()) >= 0) &&
		((model = cb.getModel()) != null) ) {
		cs = (CostSample)model.getElementAt( idx );
	    }
	    int itemCount = 0;
	    Intbox ib = (Intbox)wnd.getFellowIfAny( "intCostCount_"+String.valueOf(k) );
	    if( ib != null ) 
		itemCount = ib.intValue();
	    if( (itemCount > 0) && (cs != null) )
		ce.addCostItem( cs, (long)(itemCount) );
	    k++;
	}

	// update inventory.

	SampleInventoryDAO dao = getSampleInventory();
	try {
	    ce = dao.updateCostEstimate( ce );
	}
	catch( SQLException sqe ) {
	    ce = null;
	    String msg = "Error: "+Stringx.getDefault( sqe.getMessage(), "" );
	    showMessage( wnd, "rowMessageCost", "lbMessageCost", msg );
	    log.error( sqe );
	}

	// update the session's cost estimete.

	if( ce != null ) {
	    SwitchTab sw = (SwitchTab)InventoryPreferences.getInstance( getPortletId(), getUserId() ).getCommand( SwitchTab.class );
	    if( sw != null )
		sw.setCostEstimate( wnd, ce );
	}
	return ce;
    }

    private CostItemTable getCostItemTable() {
	ModelProducer[] mps = InventoryPreferences.getInstance( getPortletId(), getUserId() ).getResult( CostItemTable.class );
	if( mps.length > 0 )
	    return (CostItemTable)mps[0];
	return null;
    }

    private void updateCostView( Window wnd, CostEstimate ce ) {
	Caption cap = (Caption)wnd.getFellowIfAny( "capCostEstimate" );
	if( cap != null ) {
	    String st = Stringx.getDefault( ce.getProjectname(), "" ).trim();
	    log.debug( "Set caption title: "+st );
	    if( st.length() > 0 )
		cap.setLabel( ce.getProjectname() );
	    else
		cap.setLabel( "Cost estimate" );
	}
	Label lb = (Label)wnd.getFellowIfAny( "lbEstimateTotal" );
	if( lb != null ) 
	    lb.setValue( String.format( "Sample storage for %d month(s): %8.2f EUR", ce.getDuration(), ce.getTotal() ) );

	CostItemTable ciTab = getCostItemTable();
	if( ciTab != null ) {
	    ciTab.updateCostItems( wnd, ce.getCosts() );
	}
    }

    /**
     * Executes the <code>Command</code>.
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	CostEstimate ce = getCostEstimate( wnd );
	if( ce == null )
	    return;
	ce = updateCostEstimate( wnd, ce );
	if( ce != null ) {
	    updateCostView( wnd, ce );
	    showMessage( wnd, "rowMessageCost", "lbMessageCost", "Cost estimate updated" );
	}
    }

}
