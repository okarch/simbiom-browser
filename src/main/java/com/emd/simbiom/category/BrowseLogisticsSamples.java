package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Date;
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
 * <code>BrowseLogisticsSamples</code> invokes the sample browser tab and initializes the list of samples.
 * Provides a customized implementation to modify the query path.
 *
 * Created: Fri Jan 20 19:32:34 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class BrowseLogisticsSamples extends BrowseSamples {

    private static Log log = LogFactory.getLog(BrowseLogisticsSamples.class);

    public BrowseLogisticsSamples() {
	super();
    }

    protected String modifyQueryPath( String queryPath ) {
	String[] terms = queryPath.split( "[|]" );
	StringBuilder stb = new StringBuilder();
	for( int i = 0; i < terms.length-1; i++ ) {
	    stb.append( terms[i] );
	    stb.append( "|" );
	}
	String qPath = queryPath;
	if( terms.length > 0 ) {
	    log.debug( "Extracting date from "+terms[terms.length-1] );
	    String st = terms[terms.length-1];
	    if( st.length() > 11 ) {
		long dt = Stringx.parseDate( st.substring( st.length()-11 ).trim(), "dd MMM yyyy" );
		if( dt > 0L ) {
		    stb.append(Stringx.getDateString("yyyy-MM-dd",new Date(dt)));
		    stb.append( " 00:00:00|" );
		    qPath = stb.toString();
		}
		else
		    log.warn( "Cannot parse date: "+st.substring( st.length()-11 ) );
		
	    }
	}
	log.debug( "Query path: "+qPath );
	return qPath;
    }

}
