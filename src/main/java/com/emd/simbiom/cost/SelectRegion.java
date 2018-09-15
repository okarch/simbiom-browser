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

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.dao.StorageCost;

import com.emd.simbiom.model.CostEstimate;
import com.emd.simbiom.model.CostItem;
import com.emd.simbiom.model.CostSample;

import com.emd.simbiom.view.DefaultModelProducer;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.SwitchTab;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>SelectRegion</code> selects a storage region from the menu.
 *
 * Created: Wed Jul 20 09:32:34 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SelectRegion extends InventoryCommand {

    private static Log log = LogFactory.getLog(SelectRegion.class);

    public SelectRegion() {
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

    private String getSelectedRegion( Window wnd ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( "cbRegion" );
	String reg = null;
	if( cb != null ) {
	    Comboitem ci = cb.getSelectedItem();
	    if( ci != null ) {
		reg = ci.getValue();
		log.debug( "Selected region: "+reg );		
	    }
	    else
		log.warn( "No region selected. Using default" );
	}
	return Stringx.getDefault( reg, CostEstimate.DEFAULT_REGION );
    }

    private CostEstimate updateCostEstimate( Window wnd, String region ) {
	SwitchTab sw = (SwitchTab)InventoryPreferences.getInstance( getPortletId(), getUserId() ).getCommand( SwitchTab.class );
	CostEstimate ce = null;
	if( sw != null )
	    ce = sw.getCostEstimate( wnd );
	if( ce == null ) {
	    log.debug( "Creating cost estimate" );
	    // SampleInventoryDAO dao = getSampleInventory();
	    SampleInventory dao = getSampleInventory();
	    try {
		ce = dao.createCostEstimate( null );
	    }
	    catch( SQLException sqe ) {
		String msg = "Error: "+Stringx.getDefault( sqe.getMessage(), "" );
		showMessage( wnd, "rowMessageCost", "lbMessageCost", msg );
		log.error( sqe );
	    }		
	}
	if( (ce != null) && (sw != null) ) {
	    ce.setRegion( region );
	    sw.setCostEstimate( wnd, ce );
	}
	return ce;
    }

    private String getSelectedSampleType( Window wnd, int k ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( "cbSampleType_"+String.valueOf(k) );
	String sType = null;
	if( cb != null ) {
	    int idx = cb.getSelectedIndex();
	    ListModel lm = cb.getModel();
	    if( (idx >= 0) && ((lm = cb.getModel()) != null) ) {
		CostSample cs = (CostSample)lm.getElementAt(idx);
		sType = cs.getTypename();
	    }
	}
	return sType;
    }

    private void updateVolumeSelectors( Window wnd, CostEstimate ce ) {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowMessageCost", "lbMessageCost", "Error: No database access configured" );
	    return;
	}
	InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );
	ModelProducer[] volSel = pref.getResult( VolumeSelector.class );

	String region = ce.getRegion();

	for( int i = 0; i < volSel.length; i++ ) {
	    if( volSel[i] instanceof DefaultModelProducer ) {
		DefaultModelProducer vSel = (DefaultModelProducer)volSel[i];

		String sType = getSelectedSampleType( wnd, i );
		if( sType == null ) {
		    log.error( "Cannot determine selected sample type. volumen selector "+i );
		    continue;
		}
		log.debug( "Updating volume selector "+i+", region: "+region+" sample type: "+sType ); 

		try {
		    CostSample[] tList = dao.findCostBySampleType( sType, region );

		    log.debug( "Updating volume selector "+i+", number of cost items available: "+tList.length );

		    Map context = new HashMap();
		    context.put( VolumeSelector.RESULT, tList );
		    vSel.updateModel( wnd, context );
		}
		catch( SQLException sqe ) {
		    showMessage( wnd, "rowMessageCost", "lbMessageCost", "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
		    log.error( sqe );
		}
	    }
	}
    }

    private void updateRegistrationModel( Window wnd, CostEstimate ce ) {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowMessageCost", "lbMessageCost", "Error: No database access configured" );
	    return;
	}
	InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );
	ModelProducer[] mps = pref.getResult( RegistrationModel.class );
	RegistrationModel regModel = null;
	if( mps.length > 0 )
	    regModel = (RegistrationModel)mps[0];
	    
	if( regModel == null ) {
	    log.error( "Cannot determine registration model" );
	    return;
	}

	String region = ce.getRegion();

	try {
	    CostSample[] tList = dao.findCostBySampleType( StorageCost.SAMPLE_REGISTRATION, region );
	    log.debug( "Updating registration model, number of cost items available: "+tList.length );
	    Map context = new HashMap();
	    context.put( RegistrationModel.RESULT, tList );
	    regModel.updateModel( wnd, context );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageCost", "lbMessageCost", "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
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

	String region = getSelectedRegion( wnd );
	CostEstimate ce = updateCostEstimate( wnd, region );
	updateVolumeSelectors( wnd, ce );
	updateRegistrationModel( wnd, ce );
    }

}
