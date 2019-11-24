package com.emd.simbiom.report;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.emd.simbiom.model.Invoice;

import com.emd.simbiom.util.Period;
import com.emd.simbiom.util.PeriodParseException;

import com.emd.util.Stringx;

import com.emd.vutils.report.ReportGroup;
import com.emd.vutils.util.DataHasher;


/**
 * <code>PurchaseEntry</code> holds the summary of invoices related to a specfic purchase number.
 *
 * Created: Mon Nov  4 11:48:52 2019
 *
 * @author <a href="mailto:okarch@deda1infr009.localdomain">Oliver</a>
 * @version 1.0
 */
public class PurchaseEntry {
    private long summaryId;

    private String purchase;

    private float amount;
    private float numSamples;

    private int invoiceCount;
    private int verified;
    private int approved;
    private int rejected;

    private Period period;

    private List<String> projects;
    private List<String> projectCodes;

    private static Log log = LogFactory.getLog(PurchaseEntry.class);

    public PurchaseEntry( String poNum, ReportGroup group ) {
	StringBuilder stb = new StringBuilder();
	stb.append( poNum );
	stb.append( group.getGroupId() );

	this.purchase = poNum;
	this.summaryId = DataHasher.hash(stb.toString().getBytes());
	this.amount = 0f;
	this.numSamples = 0f;
	this.invoiceCount = 0;
	this.verified = 0;
	this.approved = 0;
	this.rejected = 0;
	this.projects = new ArrayList<String>();
	this.projectCodes = new ArrayList<String>();
	this.setPeriod( group.getPeriod() );
    }

    /**
     * Get the <code>SummaryId</code> value.
     *
     * @return a <code>long</code> value
     */
    public final long getSummaryId() {
	return summaryId;
    }

    /**
     * Set the <code>SummaryId</code> value.
     *
     * @param summaryId The new SummaryId value.
     */
    public final void setSummaryId(final long summaryId) {
	this.summaryId = summaryId;
    }

    /**
     * Get the <code>Amount</code> value.
     *
     * @return a <code>float</code> value
     */
    public final float getAmount() {
	return amount;
    }

    /**
     * Get the <code>Amount</code> value.
     *
     * @return a <code>float</code> value
     */
    public String getTotal() {
	float amt = getAmount();
	return String.format( "%.2f", amt );
    }

    /**
     * Set the <code>Amount</code> value.
     *
     * @param amount The new Amount value.
     */
    public final void addAmount(final float amount) {
	this.amount+=amount;
    }

    /**
     * Get the <code>NumSamples</code> value.
     *
     * @return a <code>float</code> value
     */
    public final float getNumSamples() {
	return numSamples;
    }

    /**
     * Set the <code>NumSamples</code> value.
     *
     * @param numSamples The new NumSamples value.
     */
    public final void addNumSamples(final float numSamples) {
	this.numSamples+=numSamples;
    }

    /**
     * Get the <code>InvoiceCount</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getInvoiceCount() {
	return invoiceCount;
    }

    /**
     * Get the <code>Verified</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getVerified() {
	return verified;
    }

    /**
     * Get the <code>Approved</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getApproved() {
	return approved;
    }

    /**
     * Get the <code>Rejected</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getRejected() {
	return rejected;
    }

    /**
     * Set the <code>Verified</code> value.
     *
     * @param verified The new Verified value.
     */
    public final void addVerified( Timestamp dt ) {
	if( (dt != null) && (dt.getTime() > 1000L) )
	    verified++;
    }

    /**
     * Set the <code>Verified</code> value.
     *
     * @param verified The new Verified value.
     */
    public final void addApproved( Timestamp dt ) {
	if( (dt != null) && (dt.getTime() > 1000L) )
	    approved++;
    }

    /**
     * Set the <code>Verified</code> value.
     *
     * @param verified The new Verified value.
     */
    public final void addRejected( Timestamp dt ) {
	if( (dt != null) && (dt.getTime() > 1000L) )
	    rejected++;
    }

    /**
     * Get the <code>Purchase</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getPurchase() {
	return purchase;
    }

    /**
     * Set the <code>Purchase</code> value.
     *
     * @param purchase The new Purchase value.
     */
    public final void setPurchase(final String purchase) {
	this.purchase = purchase;
    }

    /**
     * Get the <code>Period</code> value.
     *
     * @return a <code>Period</code> value
     */
    public final Period getPeriod() {
	return period;
    }

    /**
     * Set the <code>Period</code> value.
     *
     * @param period The new Period value.
     */
    public final void setPeriod(final String periodStr ) {
	try {
	    this.period = Period.parse(periodStr);
	}
	catch( PeriodParseException ppe ) {
	    log.error( ppe );
	    this.period = new Period();
	}
    }

    /**
     * Get the <code>StartDate</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getStartDate() {
	return Stringx.getDateString( "dd-MMM-YYYY", period.getStartDate() );
    }

    /**
     * Get the <code>EndDate</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getEndDate() {
	return Stringx.getDateString( "dd-MMM-YYYY", period.getEndDate() );
    }

    /**
     * Returns the project names as comma seperated list.
     *
     * @return comma-separated list or empty string.
     */
    public String getProjects() {
	StringBuilder stb = new StringBuilder();
	boolean pFirst = true;
	for( String pTitle : projects ) {
	    if( !pFirst )
		stb.append( ", " );
	    else
		pFirst = false;
	    stb.append( pTitle );
	}
	return stb.toString();
    }

    /**
     * Returns the project names as comma seperated list.
     *
     * @return comma-separated list or empty string.
     */
    public String getProjectCodes() {
	StringBuilder stb = new StringBuilder();
	boolean pFirst = true;
	for( String pTitle : projectCodes ) {
	    if( !pFirst )
		stb.append( ", " );
	    else
		pFirst = false;
	    stb.append( pTitle );
	}
	return stb.toString();
    }

// FIX ME: totals should be calculated considering the currency:
// private String currency;

    private void addProjects( String[] projs, String[] pCodes ) {
	// log.debug( "Add projects, invoice projects: "+projs.length+" existing: "+projects );
	for( int i = 0; i < projs.length; i++ ) {
	    boolean foundIt = false;
	    for( String pTitle : projects ) {
		if( pTitle.equalsIgnoreCase(projs[i]) ) {
		    foundIt = true;
		    break;
		}
	    }
	    if( !foundIt )
		this.projects.add( projs[i] );
	}
	for( int i = 0; i < pCodes.length; i++ ) {
	    boolean foundIt = false;
	    for( String pTitle : projectCodes ) {
		if( pTitle.equalsIgnoreCase(pCodes[i]) ) {
		    foundIt = true;
		    break;
		}
	    }
	    if( !foundIt )
		this.projectCodes.add( pCodes[i] );
	}
    }

    /**
     * Updates the entry with the given invoice elements.
     *
     * @param inv the invoice.
     */
    public void update( Invoice inv ) {
	addAmount( inv.getAmount() );
	addProjects( inv.getProjects(), inv.getProjectcodes() );
	addNumSamples( inv.getNumsamples() );
	addVerified( inv.getVerified() );
	addApproved( inv.getApproved() );
	addRejected( inv.getRejected() );
	invoiceCount++;
    }

    public String toString() {
	StringBuilder stb = new StringBuilder();
	stb.append( "id:" );
	stb.append( summaryId );
	stb.append( "|" );
	stb.append( purchase );
	stb.append( "|" );
	stb.append( getInvoiceCount() );
	stb.append( "|" );
	stb.append( getAmount() );
	return stb.toString();
    }



}
