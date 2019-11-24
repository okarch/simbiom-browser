package com.emd.simbiom.report;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.emd.simbiom.model.Invoice;

import com.emd.vutils.report.ReportFormatter;
import com.emd.vutils.report.ReportGroup;
import com.emd.vutils.report.ReportItem;
import com.emd.vutils.report.ReportStrategy;

import com.emd.util.Stringx;

/**
 * <code>PurchaseSummary</code> implements a default reporting strategy which is 
 * converting data items into report items by applying attribute mappings.
 *
 * Created: Wed Oct 23 16:09:40 2019
 *
 * @author <a href="mailto:okarch@deda1infr009.localdomain">Oliver</a>
 * @version 1.0
 */
public class PurchaseSummary extends AbstractReportStrategy {
    private Map<String,PurchaseEntry> summary;
    private Map<String,String> columnMap;

    private static Log log = LogFactory.getLog(PurchaseSummary.class);

    public PurchaseSummary() {
	this.summary = new HashMap<String,PurchaseEntry>();
	this.columnMap = new HashMap<String,String>();
	initColumnMap();
    }

// Quarter[pd:QN/YYYY]|Period[dt:MMM YYYY]|Year[dt:YYYY]|Mean Sample Count

    private void initColumnMap() {
	columnMap.put( "Purchase Order", "purchase" );
	columnMap.put( "Checked Invoices", "verified" );
	columnMap.put( "Approved Invoices", "approved" );
	columnMap.put( "Rejected Invoices", "rejected" );
	columnMap.put( "Start Date", "startDate" );
	columnMap.put( "End Date", "endDate" );
	columnMap.put( "Total", "total" );
	columnMap.put( "Project", "projects" );
	columnMap.put( "Project Code", "projects" );
    }

    /**
     * Accepts a data item as reportable item.
     *
     * @return true if item can be accepted, false otherwise.
     */
    public boolean accept( ReportGroup group, Object data ) {
	if( data instanceof Invoice ) {
	    Invoice inv = (Invoice)data;
	    // log.debug( "Invoice projects: "+(inv.getProjects().length) );
	    String poNum = Stringx.getDefault(inv.getPurchase(),"").trim();
	    PurchaseEntry poSum = summary.get( poNum );
	    if( poSum == null ) {
		poSum = new PurchaseEntry( poNum, group );
		summary.put( poNum, poSum );
	    }
	    poSum.update( inv );
	    return true;
	}
	return false;
    }

    /**
     * Creates a <code>ReportItem</code> from the given data object.
     *
     * @param group the report group.
     * @param data the data object.
     * @return a <code>ReportItem</code> instance.
     */
    public ReportItem createReportItem( ReportGroup group, Object data ) {
	if( !(data instanceof Invoice) )
	    return null;
	
	Invoice inv = (Invoice)data;
	    
	String poNum = Stringx.getDefault(inv.getPurchase(),"").trim();
	PurchaseEntry poSum = summary.get( poNum );
	ReportItem ri = null;
	if( poSum != null ) {
	    ri = new ReportItem( poSum );
	    ri.setItemId( poSum.getSummaryId() );
	    ri.setColumnMap( columnMap );
	    log.debug( "Purchase summary: "+poSum );
	}
	return ri;
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
