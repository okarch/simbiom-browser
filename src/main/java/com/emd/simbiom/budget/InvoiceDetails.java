package com.emd.simbiom.budget;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateFormatUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang.StringUtils;

import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.model.StorageProject;

import com.emd.util.Stringx;

/**
 * <code>InvoiceDetails</code> decorates invoices to provide details to view.
 *
 * Created: Mon Dec  3 08:24:57 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class InvoiceDetails {
    private Invoice invoice;
    private StorageProject[] projects;
    private Billing[] allPurchases;
    private boolean invoiceExist;

    private static Log log = LogFactory.getLog(InvoiceDetails.class);

    private static final String[] currencies = new String[] {
        "",
	"EUR",
        "USD",
        "SGD"
    };

    private static final Comparator PO_SORTER = new Comparator<Billing>() {
	public int compare( Billing o1, Billing o2) {
	    String po1 = Stringx.getDefault( o1.getPurchase(), "" );
	    String po2 = Stringx.getDefault( o2.getPurchase(), "" );
	    return po1.compareTo( po2 );
	}
    };

    public static final Timestamp NO_DATE = new Timestamp( 1000L );

    public InvoiceDetails( Invoice invoice ) {
	this.invoice = invoice;
	this.invoiceExist = false;
    }

    /**
     * Set the list of storage projects to select from.
     *
     * @param prjs the project list.
     */
    public void setAllProjects( StorageProject[] prjs ) {
	projects = (StorageProject[])Arrays.copyOf( prjs, prjs.length );
    }

    /**
     * Returns the list of project titles.
     *
     * @return a (potentially empty) array of project titles.
     */
    public StorageProject[] getAllProjects() {
	if( projects == null )
	    return new StorageProject[0];
	return projects;
    }

    /**
     * Get the <code>ProjectIndex</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getProjectIndex() {
	StorageProject[] prjs = getAllProjects();
	String[] pTitles = invoice.getProjects();
	if( (pTitles.length <= 0) || (prjs.length <= 0) ) {
	    log.error( "No matching projects found" );
	    return 0;
	}
	for( int i = 0; i < prjs.length; i++ ) {
	    if( pTitles[0].equals(prjs[i].getTitle()) )
		return i;
	}
	return 0;
    }

    /**
     * Get the <code>AllPurchases</code> value.
     *
     * @return a <code>Billing</code> value
     */
    public final Billing[] getAllPurchases() {
	if( allPurchases == null )
	    return new Billing[0];
	return allPurchases;
    }

    /**
     * Set the <code>AllPurchases</code> value.
     *
     * @param allPurchases The new AllPurchases value.
     */
    public final void setAllPurchases(final Billing[] bills ) {
	Billing[] sBills = (Billing[])Arrays.copyOf( bills, bills.length );
	Arrays.sort( sBills, PO_SORTER );
	String lastPONum = null;
	List purchs = new ArrayList<Billing>();
	for( int i = 0; i < sBills.length; i++ ) {
	    if( lastPONum == null ) {
		lastPONum = sBills[i].getPurchase();
		purchs.add( sBills[i] );
	    }
	    else if( !lastPONum.equals( Stringx.getDefault(sBills[i].getPurchase(),"") ) ) {
		lastPONum = sBills[i].getPurchase();
		purchs.add( sBills[i] );
	    }	    
	}
	allPurchases = new Billing[ purchs.size() ];
	allPurchases = (Billing[])purchs.toArray( allPurchases );
    }

    /**
     * Get the <code>ProjectIndex</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getPurchaseIndex() {
	Billing[] bills = getAllPurchases();
	String poNum = invoice.getPurchase();
	if( (poNum == null) || (bills.length <= 0) ) {
	    log.error( "No matching PO numbers found" );
	    return 0;
	}
	for( int i = 0; i < bills.length; i++ ) {
	    if( poNum.equals(bills[i].getPurchase()) )
		return i;
	}
	return 0;
    }

    /**
     * Get the <code>CurrencyIndex</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getCurrencyIndex() {
	String cur = Stringx.getDefault(invoice.getCurrency(),"");
	int idx = -1;
	for( int i = 0; i < currencies.length; i++ ) {
	    if( currencies[i].equalsIgnoreCase(cur) )
		return i;
	}
	return 0;
    }

    /**
     * Get the <code>PeriodStart</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getPeriodStart() {	
	if( (invoice == null) || (invoice.getStarted() == null) ||
	    (invoice.getStarted().getTime() <= NO_DATE.getTime()) )
	    return "";
	return DateFormat.getDateInstance( DateFormat.MEDIUM ).format( invoice.getStarted() );
	// return DateFormatUtils.format( invoice.getStarted(), "dd.MM.yyyy" );
    }

    /**
     * Get the <code>Invoice</code> value.
     *
     * @return an <code>Invoice</code> value
     */
    public final Invoice getInvoice() {
	return invoice;
    }

    /**
     * Set the <code>Invoice</code> value.
     *
     * @param invoice The new Invoice value.
     */
    public final void setInvoice(final Invoice invoice) {
	this.invoice = invoice;
    }

    /**
     * Get the <code>InvoiceExist</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isInvoiceExist() {
	return invoiceExist;
    }

    /**
     * Set the <code>InvoiceExist</code> value.
     *
     * @param invoiceExist The new InvoiceExist value.
     */
    public final void setInvoiceExist(final boolean invoiceExist) {
	this.invoiceExist = invoiceExist;
    }

    /**
     * Returns a human readable string.
-     *
     * @return the donor's name
     */
    public String toString() {
	return Stringx.getDefault( invoice.getInvoice(), "" );
    }

}
