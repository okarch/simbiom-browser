package com.emd.simbiom.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.emd.vutils.report.ReportStrategy;

/**
 * <code>ReportStrategies</code> holds report strategies applied to various reporting templates.
 *
 * Created: Wed Oct 23 06:54:41 2019
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class ReportStrategies {
    private Map<String,Class> reportStrategies;

    private static ReportStrategies strategies;

    private static Log log = LogFactory.getLog(ReportStrategies.class);

    private final static String PURCHASE_SUMMARY = "Report - Purchase summary"; 

    private ReportStrategies() {
	this.reportStrategies = new HashMap<String,Class>();
	this.addReportStrategy( "", DefaultStrategy.class );
	this.addReportStrategy( PURCHASE_SUMMARY, PurchaseSummary.class );
    }

    /**
     * Creates a <code>ReportFileWriter</code> instance based on the given format.
     *
     * @param format the format of the report destination.
     * @return a new instance of the reprot destination.
     */
    public static synchronized ReportStrategy getInstance( String templateName ) {
	if( strategies == null ) {
	    strategies = new ReportStrategies();
	}
	Class destClass = strategies.findStrategy( templateName );
	AbstractReportStrategy strat = null;
	if( destClass != null ) {
	    try {
		log.debug( "Trying to instatiate "+destClass );
		strat = (AbstractReportStrategy)destClass.newInstance();
	    }
	    catch( InstantiationException inex ) {
		log.error( inex );
		strat = null;
	    }
	    catch( IllegalAccessException iax ) {
		log.error( iax );
		strat = null;
	    }
	}
	if( strat == null ) {
	    log.debug( "Using default reporting strategy." );
	    strat = new DefaultStrategy();
	}
	
	strat.setTemplateName( templateName );
	
	return strat;
    }

    private void addReportStrategy( String templName, Class destinationClass ) {
	if( !AbstractReportStrategy.class.isAssignableFrom(destinationClass) ) 
	    return;
	reportStrategies.put( templName, destinationClass );
    } 

    private Class findStrategy( String templName ) {

	log.debug( "Find report strategy for \""+templName+"\"" );

	ReportStrategy dest = null;
	Set<String> destKeys = reportStrategies.keySet();
	for( String destType : destKeys ) {
	    if( (destType.length() > 0) && (destType.equalsIgnoreCase( templName )) ) 
		return reportStrategies.get( destType );
	}
	return null;
    }

}
