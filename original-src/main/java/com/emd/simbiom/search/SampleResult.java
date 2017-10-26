package com.emd.simbiom.search;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.Age;
import com.emd.simbiom.model.Sample;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.BidirectionalComparator;
import com.emd.util.DateComparator;
import com.emd.util.StringComparator;
import com.emd.util.Stringx;

/**
 * Describe class SampleResult here.
 *
 * Created: Sun Mar 29 09:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SampleResult extends DefaultModelProducer {

    private static final long   DEFAULT_30_DAYS = 30L * 24L * 60L * 60L * 1000L; // 30 days
    private static final long   DEFAULT_90_DAYS = 90L * 24L * 60L * 60L * 1000L; // 90 days

    private static Log log = LogFactory.getLog(SampleResult.class);

    public static final String COMPONENT_ID = "grSearchResult";
    public static final String RESULT = "result";
    

    public SampleResult() {
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

	if( context == null )
	    context = new HashMap();
	Sample[] samples = (Sample[])context.get( RESULT );

	if( samples == null ) {
	    SampleInventoryDAO dao = getSampleInventory();
	    if( dao == null ) {
		writeMessage( wnd, "Error: No database access configured" );
		return;
	    }
	    try {
		samples = dao.findSampleByAge( Age.created().newerThan(DEFAULT_90_DAYS) );
		context.put( RESULT, samples );
	    }
	    catch( SQLException sqe ) {
		writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
		log.error( sqe );
		samples = null;
	    }
	}

	if( samples != null ) {
	    Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	    if( grSamples != null ) 
		assignModel( grSamples, context );
	}
    }

    // private void initSort( Grid grid, SampleResultRenderer srr, String colId, Comparator comp ) {
    //  	Column col = (Column)grid.getFellowIfAny( colId );
    //  	if( col != null ) {
    //  	    col.setSortAscending( new SampleRowComparator( srr, col, new BidirectionalComparator( comp ) ) );
    //  	    col.setSortDescending( new SampleRowComparator( srr, col, new BidirectionalComparator( comp, true ) ) );
    //  	}
    // }

    protected void assignGrid( Grid grid, Map context ) {
	log.debug( "Sample result model context: "+context );

	SampleInventoryDAO dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( grid, "Error: No database access configured" );
	    return;
	}
	
	SampleResultRenderer srr = new SampleResultRenderer( dao );
	grid.setRowRenderer( srr );

	Sample[] samples = (Sample[])context.get( RESULT );
	if( samples == null )	    
	    grid.setModel( new ListModelArray( new Sample[0] ) );
	else {
	    log.debug( "Assigning model, number of samples: "+samples.length );
	    grid.setModel( new ListModelArray( samples ) );
	}
	
	initSort( grid, srr,"colStudy", new StringComparator( true )  );
	initSort( grid, srr, "colSampleType", new StringComparator( true ) );
	initSort( grid, srr, "colSampleId", new StringComparator( true ) );
	initSort( grid, srr, "colSubject", new StringComparator( true ) );
	initSort( grid, srr, "colVisit", new StringComparator( true ) );
	initSort( grid, srr, "colCollection", new DateComparator( "dd-MMM-yyyy hh:mm" ) );
	initSort( grid, srr, "colImport", new DateComparator( "dd-MMM-yyyy" ) );

	writeMessage( grid, String.valueOf(((samples != null)?samples.length:0))+" samples match the search" );
    }

}
