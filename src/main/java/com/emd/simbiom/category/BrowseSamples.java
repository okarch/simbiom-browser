package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;

// import org.zkoss.zul.Button;
import org.zkoss.zul.Area;
import org.zkoss.zul.Chart;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.Sample;

import com.emd.simbiom.search.SampleResult;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>BrowseSamples</code> invokes the sample browser tab and initializes the list of samples.
 *
 * Created: Fri Sep  7 19:32:34 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class BrowseSamples extends InventoryViewAction {

    private static Log log = LogFactory.getLog(BrowseSamples.class);

    private static final String BROWSE_BUTTON   = "btBrowseCategory";
    private static final String BROWSE_TABPANEL = "pnBrowse";


    public BrowseSamples() {
	super();
    }

    private String getNodePath( Window wnd ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( BrowseCategoryView.CATEGORY_PATH_ID );
	if( txt == null )
	    return null;
	StringBuilder stb = new StringBuilder();
	stb.append( Stringx.getDefault( txt.getValue(), "" ) );
	if( stb.length() > 0 )
	    stb.append( "|" );
	return stb.toString();
    }

    private Sample[] querySamples( Window wnd, String nodePath ) {
	SampleInventoryDAO sampleInventory = getSampleInventory();
	Sample[] samples = null;
	if( sampleInventory == null ) {
	    log.error( "Cannot access sample inventory" );
	} 
	else {
	    try {
		samples = sampleInventory.findSampleByCategory( nodePath );
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
		showMessage( wnd, 
			     "rowMessageOverview", 
			     "lbMessageOverview", 
			     "Error: "+Stringx.getDefault(sqe.getMessage(),"General database error" ) );
	    }
	}
	if( samples == null )
	    return new Sample[0];
	return samples;
    }

    private void initSampleResult( Window wnd, Sample[] samples ) {
	ModelProducer[] sResults = InventoryPreferences.getInstance( getPortletId(), getUserId() ).getResult( SampleResult.class );
	if( sResults.length > 0 ) {
	    Map context = new HashMap();
	    context.put( SampleResult.RESULT, samples );
	    sResults[0].initModel( wnd, context );
	    log.debug( "Sample result model initialized" );
	}
    }

    private void switchTab( Window wnd ) {
	Tabpanel pn = (Tabpanel)wnd.getFellowIfAny( BROWSE_TABPANEL );
	Tabbox tb = null;
	if( (pn != null) && ((tb = pn.getTabbox()) != null) ) 
	    tb.setSelectedPanel( pn );
    }

    private String extractTerm( String tip ) {
	String term = Stringx.extract( tip, ",", ")" ).trim();
	if( term.length() > 0 )
	    return term+"|";
	return "";
    }

    protected String modifyQueryPath( String queryPath ) {
	return queryPath;
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
	String qPath = getNodePath( wnd );

	// if( cmp instanceof Button ) {
	    // String qPath = getNodePath( wnd );
	    // log.debug( "Retrieve samples of "+qPath );
	    // Sample[] samps = querySamples( qPath );
	    // log.debug( "Number of samples retrieved: "+samps.length );	    
	    // if( samps.length > 0 ) {
	    // 	initSampleResult( wnd, samps );
	    // 	switchTab( wnd );
	    // }
	// }

	if( qPath != null ) {

	    if( (cmp instanceof Chart) &&
		(event instanceof MouseEvent) &&
		((cmp = ((MouseEvent)event).getAreaComponent()) != null) ) {
	
		qPath = qPath + extractTerm(Stringx.getDefault(((Area)cmp).getTooltiptext(),""));
	    }

	    qPath = modifyQueryPath(qPath)+ "samples";
	    log.debug( "Retrieve samples of "+qPath );
	    Sample[] samps = querySamples( wnd, qPath );
	    log.debug( "Number of samples retrieved: "+samps.length );	    
	    if( samps.length > 0 ) {
		initSampleResult( wnd, samps );
		switchTab( wnd );
	    }
	}
    }
}
