package com.emd.simbiom.budget;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;
// import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Invoice;

import com.emd.simbiom.view.ColumnRenderer;
import com.emd.simbiom.view.ColumnSet;
import com.emd.simbiom.view.DefaultModelProducer;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

/**
 * InvoiceResultRenderer renders the invoice search result row.
 *
 * Created: Mon Oct 8 20:30:01 2018
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class InvoiceResultRenderer implements RowRenderer {
    private ColumnSet columnSet;
    // private SampleInventory sampleInventory;
    private InventoryPreferences preferences;
    // private ColumnSetup columnSetup;

    private static Log log = LogFactory.getLog(InvoiceResultRenderer.class);

    // private static final String[] columnFormats = {
    // 	"started=%1$tb %1$tY",
    // 	"purchase=%s",
    //     "invoice=%s",
    // 	"amount,currency=%f %s"
    // };

    /**
     * Creates a new <code>InvoiceResultRenderer</code>.
     * 
     * @param sampleInventory The sample database.
     * @param preferences The inventory preferences.
     */
    public InvoiceResultRenderer( ColumnSet columnSet, InventoryPreferences pref ) {
// SampleInventory sampleInventory, 
// 				 InventoryPreferences preferences ) {

	// this.sampleInventory = sampleInventory;
	this.preferences = pref;
	this.columnSet = columnSet;
    }

    private void appendCheckmark( Row row, Invoice invoice ) {
 	Hlayout hl = new Hlayout();

	Checkbox chk = new Checkbox();
	chk.setId( "chk_"+invoice.getInvoiceid() );
 	chk.setParent( hl );

 	Button bt = new Button();
 	bt.setId( "btInvInfo_"+invoice.getInvoiceid() );
	bt.setIconSclass( "z-icon-info" );
	bt.setWidth( "25px" );
	bt.setHeight( "20px" );
 	bt.setParent( hl );

	// Button btc = new Button();
	// btc.setId( "btInvCopy_"+invoice.getInvoiceid() );
	// btc.setIconSclass( "z-icon-copy" );
	// btc.setWidth( "25px" );
	// btc.setHeight( "20px" );
	// btc.setParent( hl );

	EditInvoice editInvoice = (EditInvoice)preferences.getCommand( EditInvoice.class );

	if( editInvoice != null )
	    bt.addEventListener( Events.ON_CLICK, editInvoice );

 	// bt.setImage( "/images/info.png" );
// 	Button btr = new Button();
// 	btr.setId( "btDeleteRow_"+listRow.getContentid()+"_"+listRow.getRowindex() );
// 	DeleteEntry delEntry = (DeleteEntry)preferences.getViewAction( DeleteEntry.class );
// 	if( delEntry != null )
// 	    btr.addEventListener( Events.ON_CLICK, delEntry );
// 	btr.setImage( "/images/small-delete.png" );
// 	btr.setWidth( "20px" );
// 	btr.setHeight( "20px" );
// 	btr.setParent( hl );

 	hl.setParent( row );
    }

              // <column id="colInvoiceSelect" width="80px"/>
              // <column id="colInvoice_0" label="Period"/>
              // <column id="colInvoice_1" label="PO Number"/>
              // <column id="colInvoice_2" label="Invoice"/>
              // <column id="colInvoice_3" label="Value"/>
              // <column id="colInvoice_4" label="Samples"/>
              // <column id="colInvoice_5" label="Status"/>
              // <column id="colInvoice_6" label="Project"/>

    // private SampleRow getSampleRow( Sample sample ) {
    // 	RowCache cache = RowCache.getInstance( sampleInventory );
    // 	SampleRow sr = cache.getSampleRow( sample.getSampleid() );
    // 	if( sr == null ) {
    // 	    sr = new SampleRow( sample );
    // 	    sr = cache.putSampleRow( sr );
    // 	}
    // 	return sr;
    // }

    // private String formatDate( Timestamp ts, String fmt ) {
    // }

    // private String formatString( String st, String fmt, int maxLen ) {
	
    // }
    // private String formatNumber( String st, String fmt, int maxLen ) {
	
    // }

    private DefaultModelProducer findModel( String mName ) {
	ModelProducer res = preferences.getResult( mName );
	if( (res != null ) && (res instanceof DefaultModelProducer) )
	    return (DefaultModelProducer)res;
	return null;
    }

    private Component formatComponent( ColumnRenderer column, Invoice invoice ) {
	String cmpId = null;
	DefaultModelProducer model = null;
	String cModel = column.getContentResult();
	if( cModel != null ) { 
	    model = findModel( cModel );
	    cmpId = cModel+"_"+invoice.getInvoiceid();
	}
	else {
	    cmpId = column.getColumnId()+"_"+invoice.getInvoiceid();
	}
	return column.createComponent( cmpId, invoice, model );
    }

    /**
     * Renders the data to the specified row.
     *
     * @param row the row to render the result.
     * @param data that is returned from ListModel.getElementAt(int) 
     * @exception java.lang.Exception
     */
    public void render( Row row, Object data, int index)
	throws java.lang.Exception {

	if( data instanceof Invoice ) {
	    Invoice invoice = (Invoice)data;
	    appendCheckmark( row, invoice );

	    // SampleRow sr = getSampleRow( sample );

	    int idx = 0;
	    ColumnRenderer[] cols = columnSet.getColumns();
	    // Component dispCmp = null;
	    for( int i = 0; i < cols.length; i++ ) {
		Component dispCmp = formatComponent( cols[i], invoice );
		if( dispCmp != null )
		    dispCmp.setParent( row );
	    }
	}
    }

}
