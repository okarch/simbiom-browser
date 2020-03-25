package com.emd.simbiom.storage;

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
import com.emd.simbiom.model.RepositoryRecord;

import com.emd.simbiom.view.ColumnRenderer;
import com.emd.simbiom.view.ColumnSet;
import com.emd.simbiom.view.DefaultModelProducer;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

/**
 * RepositoryListRenderer renders the samples registered at the repository.
 *
 * Created: Mon Mar 23 10:54:01 2020
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class RepositoryListRenderer implements RowRenderer {
    private ColumnSet columnSet;
    private InventoryPreferences preferences;

    private static Log log = LogFactory.getLog(RepositoryListRenderer.class);

    // private static final String[] columnFormats = {
    // 	"started=%1$tb %1$tY",
    // 	"purchase=%s",
    //     "invoice=%s",
    // 	"amount,currency=%f %s"
    // };

    /**
     * Creates a new <code>RepositoryListRenderer</code>.
     * 
     * @param columnSet The column set.
     * @param pref The inventory preferences.
     */
    public RepositoryListRenderer( ColumnSet columnSet, InventoryPreferences pref ) {
	this.preferences = pref;
	this.columnSet = columnSet;
    }

    private void appendCheckmark( Row row, RepositoryRecord registration ) {
 	Hlayout hl = new Hlayout();

	Checkbox chk = new Checkbox();
	chk.setId( "chk_"+registration.getRegistrationid() );
 	chk.setParent( hl );

 	Button bt = new Button();
 	bt.setId( "btRegistrationInfo_"+registration.getRegistrationid() );
	bt.setIconSclass( "z-icon-info" );
	bt.setWidth( "25px" );
	bt.setHeight( "20px" );
 	bt.setParent( hl );

	// EditInvoice editInvoice = (EditInvoice)preferences.getCommand( EditInvoice.class );

	// if( editInvoice != null )
	//     bt.addEventListener( Events.ON_CLICK, editInvoice );

 	hl.setParent( row );
    }

    private DefaultModelProducer findModel( String mName ) {
	ModelProducer res = preferences.getResult( mName );
	if( (res != null ) && (res instanceof DefaultModelProducer) )
	    return (DefaultModelProducer)res;
	return null;
    }

    private Component formatComponent( ColumnRenderer column, RepositoryRecord registration ) {
	String cmpId = null;
	DefaultModelProducer model = null;
	String cModel = column.getContentResult();
	if( cModel != null ) { 
	    model = findModel( cModel );
	    cmpId = cModel+"_"+registration.getRegistrationid();
	}
	else {
	    cmpId = column.getColumnId()+"_"+registration.getRegistrationid();
	}
	return column.createComponent( cmpId, registration, model );
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

	if( data instanceof RepositoryRecord ) {
	    RepositoryRecord registration = (RepositoryRecord)data;
	    appendCheckmark( row, registration );

	    int idx = 0;
	    ColumnRenderer[] cols = columnSet.getColumns();
	    for( int i = 0; i < cols.length; i++ ) {
		Component dispCmp = formatComponent( cols[i], registration );
		if( dispCmp != null )
		    dispCmp.setParent( row );
	    }
	}
    }

}
