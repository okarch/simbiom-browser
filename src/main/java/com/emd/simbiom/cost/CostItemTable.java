package com.emd.simbiom.cost;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.CostItem;

import com.emd.simbiom.view.DefaultModelProducer;

// import com.emd.util.BidirectionalComparator;
// import com.emd.util.DateComparator;
// import com.emd.util.StringComparator;
// import com.emd.util.Stringx;

/**
 * <code>CostItemTable</code> holds the individual cost items.
 *
 * Created: Tue Aug  2 08:24:09 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class CostItemTable extends DefaultModelProducer {

    private static Log log = LogFactory.getLog(CostItemTable.class);

    public static final String COMPONENT_ID = "grCostItems";
    public static final String RESULT = "result";
    

    public CostItemTable() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {

	// NOTHING TO DO FOR NOW

	// if( context == null )
	//     context = new HashMap();
	// CostItem[] samples = (Sample[])context.get( RESULT );

	// if( samples == null ) {
	//     SampleInventoryDAO dao = getSampleInventory();
	//     if( dao == null ) {
	// 	writeMessage( wnd, "Error: No database access configured" );
	// 	return;
	//     }
	//     try {
	// 	samples = dao.findSampleLastCreated();
	// 	if( samples.length > 0 )
	// 	    setLastCreatedDays( samples[0] );
	// 	context.put( RESULT, samples );
	//     }
	//     catch( SQLException sqe ) {
	// 	writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	// 	log.error( sqe );
	// 	samples = null;
	//     }
	// }

	// if( samples != null ) {
	//     Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	//     if( grSamples != null ) 
	// 	assignModel( grSamples, context );
	// }
    }

    protected void assignGrid( Grid grid, Map context ) {
	log.debug( "Cost item table context: "+context );

	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( grid, "Error: No database access configured" );
	    return;
	}
	
	CostItemRenderer cir = new CostItemRenderer( dao );
	grid.setRowRenderer( cir );

	CostItem[] items = (CostItem[])context.get( RESULT );
	if( items == null )	    
	    grid.setModel( new ListModelArray( new CostItem[0] ) );
	else {
	    log.debug( "Assigning model, number of cost items: "+items.length );
	    grid.setModel( new ListModelArray( items ) );
	}
	
	// initSort( grid, srr,"colStudy", new StringComparator( true )  );
	// initSort( grid, srr, "colSampleType", new StringComparator( true ) );
	// initSort( grid, srr, "colSampleId", new StringComparator( true ) );
	// initSort( grid, srr, "colSubject", new StringComparator( true ) );
	// initSort( grid, srr, "colVisit", new StringComparator( true ) );
	// initSort( grid, srr, "colCollection", new DateComparator( "dd-MMM-yyyy hh:mm" ) );
	// initSort( grid, srr, "colImport", new DateComparator( "dd-MMM-yyyy" ) );

	// writeMessage( grid, String.valueOf(((samples != null)?samples.length:0))+" samples match the search" );
    }

    /**
     * Updates the cost items.
     *
     * @param wnd the window.
     * @param items cost items.
     */
    public void updateCostItems( Window wnd, CostItem[] items ) {
	Grid gr = (Grid)wnd.getFellowIfAny( COMPONENT_ID );
	if( gr != null ) {
	    Map ctxt = new HashMap();
	    ctxt.put( RESULT, items );
	    assignModel( gr, ctxt );
	}
    }

}
