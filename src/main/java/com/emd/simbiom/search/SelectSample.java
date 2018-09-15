package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
// import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleDetails;
import com.emd.simbiom.view.ModelProducer;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>SelectSample</code> display extended sample information.
 *
 * Created: Mon Jan 29 08:43:39 2018
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class SelectSample extends InventoryCommand {
    private String detailsTemplate;

    private static Log log = LogFactory.getLog(SelectSample.class);

    private static final String TEXT_FILTER = "Any content";


    /**
     * Creates a new command to selecte the search operator.
     */
    public SelectSample() {
	super();
    }

    /**
     * Get the <code>DetailsTemplate</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getDetailsTemplate() { 
	return detailsTemplate;
    }

    /**
     * Set the <code>DetailsTemplate</code> value.
     *
     * @param detailsTemplate The new DetailsTemplate value.
     */
    public final void setDetailsTemplate(final String detailsTemplate) {
	this.detailsTemplate = detailsTemplate;
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private SampleDetails compileDetails( Window wnd, String sampleId ) {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: invalid database access" );
	    return null;
	}
	RowCache cache = RowCache.getInstance( dao );
	SampleRow sr = cache.getSampleRow( sampleId );
	SampleDetails details = sr.getSampleDetails();
	if( details == null )
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: Cannot determine sample details of "+sampleId );
	return details;
    }

    private String extractSampleId( Component cmp ) {
	if( (cmp == null) || !(cmp instanceof Button) )
	    return null;

	String cmpId = Stringx.getDefault(cmp.getId(),"");
	return Stringx.after( cmpId, "_" );
    }

    private Window createWindow( Window wnd, String title, SampleDetailsView vDetails ) {
	Window wndDetails = new Window();
	wndDetails.setParent( wnd );
	wndDetails.setId( "wndDetails" );
	wndDetails.setTitle( title );
	wndDetails.setBorder( "normal" );
	wndDetails.setWidth( "900px" );
	wndDetails.setPosition( "center,center" );
	wndDetails.setClosable( true );
    // action="show: slideDown;hide: slideUp"

	Vlayout vl = new Vlayout();
	vl.setId( vDetails.getDetailsLayout() );
	vl.setParent( wndDetails );

	return wndDetails;
    }

    private Map createContext( SampleDetails details ) {
	Map ctxt = new HashMap();

	ctxt.put( "sampleDetails", details );
	ctxt.put( "columnSetup", ColumnSetup.getInstance() );

	ctxt.put( "dates", DateUtils.class );
	ctxt.put( "dateFormats", DateFormatUtils.class );

	// specific tools
	// tc.put( "samples", SampleType.class );
	// tc.put( "subjects", Subject.class );
	// tc.put( "studies", Study.class );

	// ctxt.put( "db", getSampleInventoryDAO.getInstance() );
	return ctxt;
    }

    private SampleDetailsView createDetailsView() {
	ModelProducer[] res = getPreferences().getResult( SampleResult.class );
	if( res.length <= 0 )
	    return null;
	return ((SampleResult)res[0]).getDetails();
    }

    private void updateModel( Window wnd ) {
	ModelProducer[] res = getPreferences().getResult( SampleResult.class );
	if( res.length <= 0 )
	    return;
	SampleResult sRes = (SampleResult)res[0];
	Grid results = (Grid)wnd.getFellowIfAny( sRes.getModelName() );
	if( results == null ) 
	    return;
	ListModelArray lm = (ListModelArray)results.getListModel();
	Sample[] samples = (Sample[])lm.getInnerArray();
	Map context = new HashMap();
	context.put( SampleResult.RESULT, samples );
	sRes.assignModel( results, context );
    }

    private void displayDetails( Window wnd, SampleDetails details ) {
	String det = details.getDetails();
	log.debug( "Display details of "+details.getSampleid()+", details length: "+det.length() );
	// log.debug( "Details to follow:\n"+det );

	SampleDetailsView vDetails = createDetailsView();
	if( vDetails == null ) {
	    showMessage( wnd, "rowMessage", "lbMessage", "Error: Cannot create sample details" );
	    log.error( "Cannot create view, configuration error" );
	    return;
	}

	Window wndDetails = createWindow( wnd, "Details of "+details.getSample(), vDetails );
	final Window parentWindow = wnd;
	wndDetails.addEventListener( Events.ON_CLOSE, new EventListener() {
		public void onEvent( Event evt ) {
		    log.debug( "Closing event received" );
		    updateModel( parentWindow );
		}
	    });

	vDetails.updateActions( getPortletId(), getUserId() );
	// vDetails.setTemplate( getDetailsTemplate() );
        vDetails.setShowMergeResult( true );
	vDetails.layout( wndDetails, createContext(details) );
	
	wndDetails.doModal();
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Sample info requested: "+event );
	Component cmp = event.getTarget();
	String sId = extractSampleId( cmp );
	if( sId == null ) {
	    log.error( "Cannot determine sample id" );
	    return;
	}

	log.debug( "Compile information on sample: "+sId );
	Window wnd = ZKContext.findWindow( cmp );

	SampleDetails sd = compileDetails( wnd, sId );
	if( sd != null )
	    displayDetails( wnd, sd );
    }
		
    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	// SampleRow sr = getSelectedSample( wnd );

	// if( sr != null )
	//     log.debug( "Sample selected: "+sr.getSample() );
	// else
	//     log.error( "No sample selected!" );

	// int nfCount = nextFilterCount( wnd );
	// if( nfCount <= 0 )
	//     return;

	// log.debug( "Next filter to be added: "+nfCount );

	// // create and add the new filter row

	// Row row = createRow( nfCount );
	// Row rowActions = (Row)wnd.getFellowIfAny( "rowFilterActions" );
	// if( rowActions == null )
	//     return;
	// Rows rows = (Rows)wnd.getFellowIfAny( "rowsFilter" );
	// if( rows == null )
	//     return;
	// rows.insertBefore( row, rowActions );

	// // initialize the row

	// initOperatorSelect( wnd, nfCount );

	// //initializes the filter model

	// FilterModel fm = createFilterModel( wnd, nfCount );
	// if( fm == null )
	//     return;
	// getPreferences().setResult( fm );
	// fm.initModel( wnd, null );

	// log.debug( "New filter model "+nfCount+" initialized" );

    }    
    
} 
