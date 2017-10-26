package com.emd.simbiom.cost;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.CostEstimate;
import com.emd.simbiom.model.CostItem;
import com.emd.simbiom.model.CostSample;

import com.emd.simbiom.view.DefaultModelProducer;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>RemoveSampleItem</code> removes a sample position from the cost estimate.
 *
 * Created: Wed Aug  5 19:47:34 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class RemoveSampleItem extends InventoryCommand {

    private static Log log = LogFactory.getLog(RemoveSampleItem.class);

    private static final String COST_ADD    = "btCostAdd";
    private static final String COST_REMOVE = "btCostRemove";

    private static final String ROW_ITEM    = "rowCostItem";
    private static final String ROWS_ID     = "rowsCostItems";

    private static final String SAMPLE_TYPE = "cbSampleType";
    private static final String VOLUME      = "cbCostSample";
    private static final String AMOUNT      = "intCostCount";

    private static final String MESSAGE_ID  = "rowMessageCost";

    public RemoveSampleItem() {
	super();
    }

    private int getSuffixNum( String cmpId ) {
	int k = -1;
	if( (cmpId != null) && ((k = cmpId.lastIndexOf( "_" )) > 0) )
	    return Stringx.toInt(cmpId.substring(k+1),1000);
	return 1000;
    }

    private int getSuffixMax( Window wnd ) {
	int k = 0;
	Row r = null;
	while( (r = (Row)wnd.getFellowIfAny( ROW_ITEM+"_"+String.valueOf(k) )) != null ) {
	    k++;
	}
	return (k-1);
    }

    private void registerPreferences( Window wnd, String suffix ) {	
	InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );
	AmountChange amt = (AmountChange)pref.getCommand( AMOUNT+"_0");
	if( amt != null ) {
	    log.debug( "Copying action using new suffix: "+suffix );
	    AmountChange cAmt = amt.copyAction( suffix.substring(1) );
	    cAmt.wireComponent( wnd );
	    pref.addCommand( cAmt );
	}

	VolumeSelector vModel = new VolumeSelector();
	vModel.setModelName( VOLUME+suffix );
	vModel.setPortletId( this.getPortletId() );
	vModel.setUserId( this.getUserId() );
	vModel.setMessageRowId( MESSAGE_ID );
	pref.addResult( vModel );

	SampleTypeModel model = new SampleTypeModel();
	model.setModelName( SAMPLE_TYPE+suffix );
	model.setPortletId( this.getPortletId() );
	model.setUserId( this.getUserId() );
	model.setMessageRowId( MESSAGE_ID );
	pref.addResult( model );

	vModel.initModel( wnd, null );
	model.initModel( wnd, null );
    }

    private void removeRow( Window wnd, int suff ) {
	Row row = (Row)wnd.getFellowIfAny( ROW_ITEM+"_"+String.valueOf(suff) );
	if( row != null ) {
	    Rows rows = (Rows)wnd.getFellowIfAny( ROWS_ID );
	    if( rows != null ) 
		rows.removeChild( row );
	    row.detach();
	}
    }

    private void shiftComponentId( Window wnd, String cmpPrefix, int idx ) {
	Component cmp = wnd.getFellowIfAny( cmpPrefix+"_"+String.valueOf(idx) );
	if( cmp != null ) {
	    log.debug( "Shifting component id: "+cmp.getId()+" new id: "+cmpPrefix+"_"+String.valueOf(idx-1) );
	    cmp.setId( cmpPrefix+"_"+String.valueOf(idx-1) );	
	}
    }

    private void shiftRows( Window wnd, int suff, int maxSuff ) {

	InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );

	InventoryCommand[] cmds = pref.getCommands();
	for( int i = 0; i < cmds.length; i++ ) 
	    log.debug( "Command: "+cmds[i].getClass().getName()+" command name: "+cmds[i].getCommandName()+" event: "+cmds[i].getEvent() );

	// update component ids

	for( int i = suff+1; i <= maxSuff; i++ ) {
	    shiftComponentId( wnd, ROW_ITEM, i );

	    shiftComponentId( wnd, VOLUME, i );
	    ModelProducer mp = pref.getResult( VOLUME+"_"+String.valueOf(i) );
	    if( (mp != null) && (mp instanceof DefaultModelProducer) ) 
		((DefaultModelProducer)mp).setModelName( VOLUME+"_"+String.valueOf(i-1) );

	    shiftComponentId( wnd, SAMPLE_TYPE, i );	    
	    mp = pref.getResult( SAMPLE_TYPE+"_"+String.valueOf(i) );
	    if( (mp != null) && (mp instanceof DefaultModelProducer) )
		((DefaultModelProducer)mp).setModelName( SAMPLE_TYPE+"_"+String.valueOf(i-1) );

	    shiftComponentId( wnd, AMOUNT, i );
	    InventoryCommand amt = pref.getCommand( AMOUNT+"_"+String.valueOf(i) );
	    // AmountChange amt = (AmountChange)pref.getCommand( AMOUNT+"_"+String.valueOf(i) );
	    if( amt instanceof AmountChange ) {
		log.debug( "Amount command: "+amt );
		amt.setCommandName( AMOUNT+"_"+String.valueOf(i-1) );
		amt.wireComponent( wnd );
	    }

	    shiftComponentId( wnd, COST_ADD, i );
	    shiftComponentId( wnd, COST_REMOVE, i );
	}	

	// remove the actions and result models

	// pref.removeCommand( wnd, COST_ADD+"_"+String.valueOf(suff) );
	// pref.removeCommand( wnd, AMOUNT+"_"+String.valueOf(suff) );

	pref.removeResult( VOLUME+"_"+String.valueOf(suff) );
	pref.removeResult( SAMPLE_TYPE+"_"+String.valueOf(suff) );

    }

    private void clearAmount( Window wnd ) {
	Intbox amt = (Intbox)wnd.getFellowIfAny( AMOUNT+"_0" );
	if( amt != null ) {
	    amt.setValue( 0 );
	    amt.setText( "" );
	}
    }

    private void setDisableButton( Window wnd, String btName, boolean disable ) {
	Button bt = (Button)wnd.getFellowIfAny( btName+"_0" );
	if( bt != null ) 
	    bt.setDisabled( disable );
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
     	Component cmp = event.getTarget();
     	if( cmp == null ) {
     	    log.error( "Cannot determine event target" );
     	    return;
     	}
     	Window wnd = ZKContext.findWindow( cmp );
     	if( cmp instanceof Button ) {
	    int suff = getSuffixNum( cmp.getId() );
	    int maxSuff = getSuffixMax( wnd );
	    if( suff == 0 ) {
		log.debug( "Clearing amount" );
		clearAmount( wnd );
		setDisableButton( wnd, COST_REMOVE, true );
	    }
	    else if( maxSuff > 0 ) {
		removeRow( wnd, suff );
		shiftRows( wnd, suff, maxSuff );
		maxSuff = getSuffixMax( wnd ); // determine again
	    }

	    // disable remove if it is the last row

	    // if( maxSuff == 0 )
	    // 	setDisableButton( wnd, COST_REMOVE, true );	    
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

	log.warn( "Invalid execution state" );
    }

}
