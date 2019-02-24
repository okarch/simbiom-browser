package com.emd.simbiom.budget;

import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import com.emd.util.Stringx;

/**
 * <code>InvoiceStatusItem</code> represents an item describing the invoice status.
 *
 * Created: Mon Nov 12 08:15:02 2018
 *
 * @author <a href="mailto:okarch@deda1infr005.localdomain">Oliver</a>
 * @version 1.0
 */
public class InvoiceStatusItem {
    private String item;
    private Timestamp statusDate;
    private boolean resetReview;

    private static final long NO_DATE = (new Timestamp( 1000L )).getTime();

    public static final String UNKNOWN = "Unknown";

    public InvoiceStatusItem() {
	this.item = UNKNOWN;
	this.resetReview = false;
    }

    public InvoiceStatusItem( String item ) {
	this.item = item;
	this.resetReview = false;
    }

    /**
     * Get the <code>Item</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getItem() {
	return item;
    }

    /**
     * Set the <code>Item</code> value.
     *
     * @param item The new Item value.
     */
    public final void setItem(final String item) {
	this.item = item;
    }

    /**
     * Get the <code>StatusDate</code> value.
     *
     * @return a <code>Timestamp</code> value
     */
    public final Timestamp getStatusDate() {
	return statusDate;
    }

    /**
     * Set the <code>StatusDate</code> value.
     *
     * @param statusDate The new StatusDate value.
     */
    public final void setStatusDate(final Timestamp statusDate) {
	this.statusDate = statusDate;
    }

    /**
     * Get the <code>ResetReview</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isResetReview() {
	return resetReview;
    }

    /**
     * Set the <code>ResetReview</code> value.
     *
     * @param resetReview The new ResetReview value.
     */
    public final void setResetReview(final boolean resetReview) {
	this.resetReview = resetReview;
    }

    private String getStatusDateString() {
	StringBuilder stb = new StringBuilder();
	Timestamp dt = getStatusDate();

	if( (dt != null) && (dt.getTime() > NO_DATE) ) {
	    stb.append( " on " );
	    SimpleDateFormat formatter = new SimpleDateFormat( "dd-MMM-yyyy" );
	    stb.append( formatter.format(dt) );
	}
	return stb.toString();
    }

    /**
     * Returns if status date is available.
     *
     * @return true if status date has been set.
     */
    public boolean validStatusDate() {
	Timestamp dt = getStatusDate();
	return ( (dt != null) && (dt.getTime() > NO_DATE) );
    }

    /**
     * Returns a human readable string.
     *
     * @return a human readable string.
     */
    public String toString() {
	StringBuilder stb = new StringBuilder( Stringx.getDefault( getItem(), "" ) );
	stb.append( getStatusDateString() );
	return stb.toString();
    }

}
