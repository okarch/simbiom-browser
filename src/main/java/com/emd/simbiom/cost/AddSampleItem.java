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

import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.SwitchTab;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>AddSampleItem</code> creates an additional position to add samples to the cost estimate.
 *
 * Created: Wed Aug  5 10:12:34 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class AddSampleItem extends InventoryCommand {

    private static Log log = LogFactory.getLog(AddSampleItem.class);

    private static final String COST_ADD    = "btCostAdd";
    private static final String COST_REMOVE = "btCostRemove";

    private static final String ROW_ITEM    = "rowCostItem";
    private static final String ROWS_ID     = "rowsCostItems";

    private static final String SAMPLE_TYPE = "cbSampleType";
    private static final String VOLUME      = "cbCostSample";
    private static final String AMOUNT      = "intCostCount";

    private static final String MESSAGE_ID  = "rowMessageCost";

    public AddSampleItem() {
	super();
    }

    private String getSuffix( Window wnd ) {
	int k = 0;
	Row r = null;
	while( (r = (Row)wnd.getFellowIfAny( ROW_ITEM+"_"+String.valueOf(k) )) != null ) {
	    k++;
	}
	return "_"+String.valueOf(k);
    }

    private Hlayout createRow( Window wnd, String cmpId ) {
	Row r = new Row();
	r.setId( cmpId );
	Hlayout hl = new Hlayout();
	hl.setParent( r );
	Rows rows = (Rows)wnd.getFellowIfAny( ROWS_ID );
	if( rows != null ) 
	    r.setParent( rows );
	return hl;
    }

    private Hlayout createCombobox( String lbTitle, String cmpId, String width ) {
	Hlayout hl = new Hlayout();
	(new Label( lbTitle )).setParent( hl );
	Combobox cb = new Combobox();
	cb.setId( cmpId );
	cb.setAutodrop( true );
	cb.setWidth( width );
	cb.setMold( "rounded" );
	cb.setButtonVisible( true );
	cb.setParent( hl );
	return hl;
    }

    private Hlayout createIntbox( String lbTitle, String cmpId, String width ) {
	Hlayout hl = new Hlayout();
	(new Label( lbTitle )).setParent( hl );
	Intbox ib = new Intbox();
	ib.setId( cmpId );
	ib.setWidth( width );
	ib.setParent( hl );
	return hl;
    }

    private Hlayout createButtons( String suff ) {
	Hlayout hl = new Hlayout();

	Button bt = new Button();
	bt.setId( COST_ADD+suff );
	bt.setDisabled( true );
	bt.setImage( "/images/add-icon.png" );
	bt.addEventListener( "onClick", this );
	bt.setParent( hl );

	bt = new Button();
	bt.setId( COST_REMOVE+suff );
	bt.setDisabled( true );
	bt.setImage( "/images/delete-icon.png" );
	RemoveSampleItem btRemoveAction = new RemoveSampleItem();
	btRemoveAction.setCommandName( COST_REMOVE+suff ); 
	bt.addEventListener( "onClick", btRemoveAction );
	bt.setParent( hl );

	return hl;
    }

    private String addSampleCostRow( Window wnd ) {
	String suff = getSuffix( wnd );
	log.debug( "Maximum cost row suffix: "+suff );

	Hlayout parent = createRow( wnd, ROW_ITEM+suff  );
	(createCombobox( "Type", SAMPLE_TYPE+suff, "150px" )).setParent( parent );
	(createCombobox( "Volume", VOLUME+suff, "200px" )).setParent( parent );
	(createIntbox( "Amount", AMOUNT+suff, "100px" )).setParent( parent );
	(createButtons( suff ) ).setParent( parent );

	return suff;
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

	String suff = addSampleCostRow( wnd );
	registerPreferences( wnd, suff );

	showMessage( wnd, "rowMessageCost", "lbMessageCost", "Another sample cost position added" );
    }

}
