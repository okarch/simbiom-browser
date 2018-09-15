package com.emd.simbiom.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.emd.simbiom.config.InventoryPreferences;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Sample;

import com.emd.util.Stringx;

/**
 * SampleResultRenderer renders the sample search result row.
 *
 * Created: Mon Apr 7 20:30:01 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class SampleResultRenderer implements RowRenderer {
    // private SampleInventoryDAO sampleInventory;
    private SampleInventory sampleInventory;
    private InventoryPreferences preferences;
    private ColumnSetup columnSetup;

    private static Log log = LogFactory.getLog(SampleResultRenderer.class);

    /**
     * Creates a new <code>SampleResultRenderer</code>.
     * 
     * @param sampleInventory The sample database.
     * @param preferences The inventory preferences.
     * @param cSetup The current column setup.
     */
    public SampleResultRenderer( SampleInventory sampleInventory, 
				 InventoryPreferences preferences, 
				 ColumnSetup cSetup ) {
    // public SampleResultRenderer( SampleInventoryDAO sampleInventory, 
    // 				 InventoryPreferences preferences, 
    // 				 ColumnSetup cSetup ) {

	this.sampleInventory = sampleInventory;
	this.preferences = preferences;
	this.columnSetup = cSetup;
    }

    private void appendCheckmark( Row row, Sample sample ) {
 	Hlayout hl = new Hlayout();

	Checkbox chk = new Checkbox();
	chk.setId( "chk_"+sample.getSampleid() );
 	chk.setParent( hl );

 	Button bt = new Button();
 	bt.setId( "btInfoRow_"+sample.getSampleid() );
	bt.setIconSclass( "z-icon-info" );
	bt.setWidth( "20px" );
	bt.setHeight( "20px" );
 	bt.setParent( hl );

 	SelectSample selectSample = (SelectSample)preferences.getCommand( SelectSample.class );

	if( selectSample != null )
 	    bt.addEventListener( Events.ON_CLICK, selectSample );

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

    private SampleRow getSampleRow( Sample sample ) {
	RowCache cache = RowCache.getInstance( sampleInventory );
	SampleRow sr = cache.getSampleRow( sample.getSampleid() );
	if( sr == null ) {
	    sr = new SampleRow( sample );
	    sr = cache.putSampleRow( sr );
	}
	return sr;
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

	if( data instanceof Sample ) {
	    Sample sample = (Sample)data;
	    appendCheckmark( row, sample );

	    SampleRow sr = getSampleRow( sample );

	    DisplayColumn[] cols = columnSetup.getDisplayColumns();
	    for( int i = 0; i < cols.length; i++ ) {
		(new Label( cols[i].formatColumn( "", sr ) )).setParent( row );
	    }
	}
    }

}
