package com.emd.simbiom.report;

import com.emd.vutils.report.ReportGroup;
import com.emd.vutils.report.ReportItem;
import com.emd.vutils.report.ReportStrategy;
import com.emd.vutils.report.ReportFormatter;

/**
 * <code>DefaultStrategy</code> implements a default reporting strategy which is 
 * converting data items into report items by applying attribute mappings.
 *
 * Created: Wed Oct 23 16:09:40 2019
 *
 * @author <a href="mailto:okarch@deda1infr009.localdomain">Oliver</a>
 * @version 1.0
 */
public class DefaultStrategy extends AbstractReportStrategy {

    public DefaultStrategy() {
	super();
    }

    /**
     * Accepts a data item as reportable item.
     *
     * @return true if item can be accepted, false otherwise.
     */
    public boolean accept( ReportGroup group, Object data ) {
	return (data != null);
    }

    /**
     * Creates a <code>ReportItem</code> from the given data object.
     *
     * @param group the report group.
     * @param data the data object.
     * @return a <code>ReportItem</code> instance.
     */
    public ReportItem createReportItem( ReportGroup group, Object data ) {
	if( data == null )
	    return null;
	return new ReportItem( data );
    }

    /**
     * Returns a specific <code>ReportFormatter</code> object.
     *
     * @param group the report group.
     * @return a <code>ReportFormatter</code> instance or null.
     */
    public ReportFormatter getReportFormatter( ReportGroup group ) {
	return null;
    }

}
