package com.emd.simbiom.cost;

import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

// import org.zkoss.zul.Button;
// import org.zkoss.zul.Cell;
// import org.zkoss.zul.Checkbox;
// import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
// import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.Cost;
import com.emd.simbiom.model.CostItem;

import com.emd.util.Stringx;

/**
 * CostItemRenderer renders the cost items.
 *
 * Created: Tue Aug  2 18:40:01 2016
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class CostItemRenderer implements RowRenderer {
    private SampleInventoryDAO sampleInventory;

    private static Map<Long,String[]> costItems = new HashMap<Long,String[]>();
    private static Log log = LogFactory.getLog(CostItemRenderer.class);

    private static final int NUM_COLS = 4;

    public CostItemRenderer( SampleInventoryDAO sampleInventory ) {
	this.sampleInventory = sampleInventory;
    }

    private String[] getItem( long itemId ) {
	return costItems.get( new Long(itemId) );
    }

    private String[] putItem( CostItem item ) {
	String[] cols = new String[NUM_COLS];
	try {
	    Cost cost = sampleInventory.findCostById( item.getCostid() );
	    cols[0] = item.getItemtype();
	    if( cost != null ) {
		cols[1] = String.format( "%8.2f %s / %s", cost.getPrice(), cost.getCurrency(), cost.getUnit() );
		cols[3] = String.format( "%8.2f %s / %s", (cost.getPrice()*(float)item.getItemcount()), cost.getCurrency(), cost.getFrequency() );
	    }
	    else {
		cols[1] = "";
		cols[3] = "";
	    }
	    cols[2] = String.valueOf( item.getItemcount() );
	    costItems.put( new Long(item.getCostitemid()), cols );
	}
	catch( SQLException sqe ) {
	    log.error( sqe );
	    for( int i = 0; i < cols.length; i++ )
		cols[i] = "";
	}
	return cols;
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

	if( data instanceof CostItem ) {
	    long itemId = ((CostItem)data).getCostitemid();
	    String[] cols = getItem(  itemId );
	    if( cols == null ) 
		cols = putItem( (CostItem)data );

	    for( int i = 0; i < cols.length; i++ ) 
		(new Label( cols[i] )).setParent( row );
	}
    }

}
