package com.emd.simbiom.storage;

import java.math.BigDecimal;

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
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.model.Billing;

import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>AddBillingItem</code> creates an additional position to add billing information.
 *
 * Created: Thu Sep 27 08:12:34 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class AddBillingItem extends InventoryCommand {

    private static Log log = LogFactory.getLog(AddBillingItem.class);

    private static final String BILL_ADD    = "btBillingAdd";
    private static final String BILL_REMOVE = "btBillingRemove";

    private static final String ROW_ITEM    = "rowBillingItem";
    private static final String ROWS_ID     = "rowsBillingItems";

    public static final String BILL_ACTIVE = "btBillingActive";
    public static final String PROJECT_CODE= "txtProjectCode";
    public static final String PO_NUM      = "txtPurchaseOrder";
    public static final String AMOUNT      = "decPurchaseValue";
    public static final String CURRENCY    = "cbPurchaseCurrency";

    private static final String MESSAGE_ID  = "rowStorageMessage";

    public AddBillingItem() {
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

    private void removeRow( Window wnd, int suff ) {
	Row row = (Row)wnd.getFellowIfAny( ROW_ITEM+"_"+String.valueOf(suff) );
	if( row != null ) {
	    Rows rows = (Rows)wnd.getFellowIfAny( ROWS_ID );
	    if( rows != null ) 
		rows.removeChild( row );
	    row.detach();
	}
    }
                              //   <button id="btBillingActive_0" iconSclass="z-icon-check" />
                              //   <hlayout>
                              //     <label value="Project code"/>
                              //     <textbox id="txtProjectCode_0" width="150px"/>
		              //   </hlayout>
                              //   <hlayout>
                              //     <label value="Purchase order"/>
                              //     <textbox id="txtPurchaseOrder_0" width="150px"/>
                              //   </hlayout>
                              //   <hlayout>
                              //     <label value="Value"/>
                              //     <decimalbox id="decPurchaseValue_0" width="100px" format="###,###.##"/>
                              //     <combobox id="cbPurchaseCurrency" autodrop="true" width="80px" mold="rounded" buttonVisible="true">
                              //       <attribute name="onCreate">self.setSelectedIndex(0);</attribute>
                              //       <comboitem label="EUR" value="EUR" />
                              //       <comboitem label="USD" value="USD" />
                              //       <comboitem label="SGD" value="SGD" />
                              //     </combobox>
                              //   </hlayout>
                              //   <hlayout>
                              //     <button id="btBillingAdd_0" disabled="true" image="/images/add-icon.png" />
                              //     <button id="btBillingRemove_0" disabled="true" image="/images/delete-icon.png" />
		              //   </hlayout>

    // private Hlayout createCombobox( String lbTitle, String cmpId, String width ) {
    // 	Hlayout hl = new Hlayout();
    // 	(new Label( lbTitle )).setParent( hl );
    // 	Combobox cb = new Combobox();
    // 	cb.setId( cmpId );
    // 	cb.setAutodrop( true );
    // 	cb.setWidth( width );
    // 	cb.setMold( "rounded" );
    // 	cb.setButtonVisible( true );
    // 	cb.setParent( hl );
    // 	return hl;
    // }

    private Button createEnableButton( String suff ) {
	Button bt = new Button();
	bt.setId( BILL_ACTIVE+suff );
	bt.setIconSclass( "z-icon-check" );
	bt.addEventListener( "onClick", this );
	return bt;
    }

    private Hlayout createTextbox( String lbTitle, String cmpId, String width ) {
	Hlayout hl = new Hlayout();
	(new Label( lbTitle )).setParent( hl );
	Textbox ib = new Textbox();
	ib.setId( cmpId );
	ib.setWidth( width );
	ib.setParent( hl );
	return hl;
    }

    // private Hlayout createDecimalbox( String lbTitle, String cmpId, String width ) {
    // 	Hlayout hl = new Hlayout();
    // 	(new Label( lbTitle )).setParent( hl );
    // 	Decimalbox ib = new Decimalbox();
    // 	ib.setId( cmpId );
    // 	ib.setWidth( width );
    // 	ib.setParent( hl );
    // 	return hl;
    // }

    private Hlayout createValuebox( String lbTitle, String suff, String width ) {
	Hlayout hl = new Hlayout();
	(new Label( lbTitle )).setParent( hl );
	Decimalbox ib = new Decimalbox();
	ib.setId( AMOUNT+suff );
	ib.setWidth( width );
	ib.setParent( hl );

	Combobox cb = new Combobox();
     	cb.setId( CURRENCY+suff );
     	cb.setAutodrop( true );
     	cb.setWidth( "80px" );
     	cb.setMold( "rounded" );
	cb.setButtonVisible( true );

	Comboitem cbi = cb.appendItem( "EUR" );
	cbi.setValue( "EUR" );
	cbi = cb.appendItem( "USD" );
	cbi.setValue( "USD" );
	cbi = cb.appendItem( "SGD" );
	cbi.setValue( "SGD" );

     	cb.setParent( hl );

	return hl;
    }

    private Hlayout createButtons( String suff ) {
	Hlayout hl = new Hlayout();

	Button bt = new Button();
	bt.setId( BILL_ADD+suff );
	bt.setDisabled( true );
	bt.setImage( "/images/add-icon.png" );
	bt.addEventListener( "onClick", this );
	bt.setParent( hl );

	bt = new Button();
	bt.setId( BILL_REMOVE+suff );
	bt.setDisabled( true );
	bt.setImage( "/images/delete-icon.png" );

	// RemoveSampleItem btRemoveAction = new RemoveSampleItem();
	// btRemoveAction.setCommandName( BILL_REMOVE+suff ); 
	// bt.addEventListener( "onClick", btRemoveAction );
	bt.setParent( hl );

	return hl;
    }

    private String addBillingRow( Window wnd ) {
	String suff = getSuffix( wnd );
	log.debug( "Maximum billing row suffix: "+suff );

	Hlayout parent = createRow( wnd, ROW_ITEM+suff  );
	(createEnableButton( suff ) ).setParent( parent );
	(createTextbox( "Project code", PROJECT_CODE+suff, "150px" )).setParent( parent );
	(createTextbox( "Purchase order", PO_NUM+suff, "150px" )).setParent( parent );
	(createValuebox( "Value", suff, "100px" )).setParent( parent );
	(createButtons( suff ) ).setParent( parent );

	return suff;
    }

    private void selectCurrency( Combobox cb, String currency ) {
	int idx = -1;
	for( int i = 0; i < cb.getItemCount(); i++ ) {
	    Comboitem ci = cb.getItemAtIndex( i );
	    if( currency.equals(ci.getValue().toString()) ) {
		idx = i;
		break;
	    }
	}
	if( idx >= 0 )
	    cb.setSelectedIndex( idx );
	else
	    log.error( "Currency "+currency+" cannot be selected" );
    }

    private void initBillingRow( Window wnd, String suf, Billing bill ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( PROJECT_CODE+suf );
	if( txt != null )
	    txt.setValue( Stringx.getDefault(bill.getProjectcode(),"") );
	txt = (Textbox)wnd.getFellowIfAny( PO_NUM+suf );
	if( txt != null )
	    txt.setValue( Stringx.getDefault(bill.getPurchase(),"") );
	Decimalbox dec = (Decimalbox)wnd.getFellowIfAny( AMOUNT+suf );
	if( dec != null )
	    dec.setValue( String.valueOf(bill.getTotal()) );

	Combobox cb = (Combobox)wnd.getFellowIfAny( CURRENCY+suf );
	if( cb != null )
	    selectCurrency( cb, Stringx.getDefault(bill.getCurrency(),"EUR") );
    }

    private boolean billingRowExists( Window wnd, int idx ) {
	return (wnd.getFellowIfAny( ROW_ITEM+"_"+String.valueOf(idx) ) != null );
    }

    /**
     * Clears billing information.
     *
     * @param wnd the current window.
     */
    public void clearBilling( Window wnd ) {
	int k = 1;
	while( billingRowExists( wnd, k ) ) {
	    removeRow( wnd, k );
	    k++;
	}

	Textbox txt = (Textbox)wnd.getFellowIfAny( PROJECT_CODE+"_0" );
	if( txt != null )
	    txt.setValue( "" );
	txt = (Textbox)wnd.getFellowIfAny( PO_NUM+"_0" );
	if( txt != null )
	    txt.setValue( "" );
	Decimalbox dec = (Decimalbox)wnd.getFellowIfAny( AMOUNT+"_0" );
	if( dec != null )
	    dec.setValue( new BigDecimal(0d) );

	Combobox cb = (Combobox)wnd.getFellowIfAny( CURRENCY+"_0" );
	if( cb != null )
	    selectCurrency( cb, "EUR" );
    }

    /**
     * Updates the billing information and adds as many rows as needed.
     *
     * @param wnd the current app window.
     * @param bills an array of billing information.
     */
    public void updateBilling( Window wnd, Billing[] bills ) {
	for( int i = 0; i < bills.length; i++ ) {
	    String suf = null;
	    if( !billingRowExists( wnd, i ) ) 
		suf = addBillingRow( wnd );
	    else
		suf = "_"+String.valueOf(i);
	    initBillingRow( wnd, suf, bills[i] );
	}

	// remove remaining rows

	if( bills.length > 0 ) {
	    int k = bills.length;  
	    boolean rowExist = false;
	    do {
		rowExist = billingRowExists( wnd, k );
		if( rowExist ) 
		    removeRow( wnd, k );
		k++;
	    }
	    while( rowExist );
	}
    }

    private void registerPreferences( Window wnd, String suffix ) {	
	// InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );
	// AmountChange amt = (AmountChange)pref.getCommand( AMOUNT+"_0");
	// if( amt != null ) {
	//     log.debug( "Copying action using new suffix: "+suffix );
	//     AmountChange cAmt = amt.copyAction( suffix.substring(1) );
	//     cAmt.wireComponent( wnd );
	//     pref.addCommand( cAmt );
	// }

	// VolumeSelector vModel = new VolumeSelector();
	// vModel.setModelName( VOLUME+suffix );
	// vModel.setPortletId( this.getPortletId() );
	// vModel.setUserId( this.getUserId() );
	// vModel.setMessageRowId( MESSAGE_ID );
	// pref.addResult( vModel );

	// SampleTypeModel model = new SampleTypeModel();
	// model.setModelName( SAMPLE_TYPE+suffix );
	// model.setPortletId( this.getPortletId() );
	// model.setUserId( this.getUserId() );
	// model.setMessageRowId( MESSAGE_ID );
	// pref.addResult( model );

	// vModel.initModel( wnd, null );
	// model.initModel( wnd, null );
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

	String suff = addBillingRow( wnd );
	registerPreferences( wnd, suff );

	showMessage( wnd, MESSAGE_ID, "lbStorageMessage", "Billing information added" );
    }

}
