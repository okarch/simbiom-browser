package com.emd.simbiom.budget;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Billing;
import com.emd.simbiom.model.Invoice;

import com.emd.simbiom.util.Period;

import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>UpdateColumns</code> updates the column selection of the sample query results.
 *
 * Created: Tue Feb 27 18:49:40 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class UpdateInvoice extends InventoryViewAction {
    private Map<String,IssueMessage> messages;

    private static Log log = LogFactory.getLog(UpdateInvoice.class);

    private static final String ACTION_CLOSE   = "btInvDetailsClose";
    private static final String ACTION_STORE   = "btInvDetailsStore";

    private static final String KEY_ISSUES     = "invoiceIssues";

    private static final String MSG_AMOUNT     = "msgIssueAmount";
    private static final String MSG_NUMSAMPLES = "msgIssueNumsamples";
    private static final String MSG_PERIOD     = "msgIssuePeriod";
    private static final String MSG_CURRENCY   = "msgIssueCurrency";

    private static final Timestamp TS_UNKNOWN = new Timestamp( 1000L );

    public UpdateInvoice() {
	super();
	initMessages();
    }

    private void initMessages() {
	this.messages = new HashMap<String,IssueMessage>();

	IssueMessage iMsg = new IssueMessage( MSG_AMOUNT, "Warning: Invoice amount is <= 0. Store anyway?" );
	iMsg.addButton( "label=Yes;width=80px" );
	iMsg.addButton( "label=No;width=80px;action=dismiss" );
	this.messages.put( MSG_AMOUNT, iMsg );

	iMsg = new IssueMessage( MSG_NUMSAMPLES, "Warning: Number of samples is <= 0. Store anyway?" );
	iMsg.addButton( "label=Yes;width=80px" );
	iMsg.addButton( "label=No;width=80px;action=dismiss" );
	this.messages.put( MSG_NUMSAMPLES, iMsg );

	iMsg = new IssueMessage( MSG_PERIOD, "Warning: Period has been charged to PO already. Store anyway?" );
	iMsg.addButton( "label=Yes;width=80px" );
	iMsg.addButton( "label=No;width=80px;action=dismiss" );
	this.messages.put( MSG_PERIOD, iMsg );

	iMsg = new IssueMessage( MSG_CURRENCY, "Warning: Currency differs from PO. Store anyway?" );
	iMsg.addButton( "label=Yes;width=80px" );
	iMsg.addButton( "label=No;width=80px;action=dismiss" );
	this.messages.put( MSG_CURRENCY, iMsg );
    }

        // <datebox id="dbInvoiceVerified" width="150px" />
        // <datebox id="dbInvoiceApproved" width="150px" />
    
    private String getSelectedItem( Window wnd, String cbName, String def ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( cbName );
	if( cb == null ) 
	    return def;
	int idx = cb.getSelectedIndex();
	if( idx < 0 )
	    return def;
	Comboitem ci = cb.getItemAtIndex( idx );
	if( ci == null )
	    return def;
	Object val = ci.getValue();
	return ((val == null)?def:val.toString());
    }

    private Timestamp getDateField( Window wnd, String dbName, Timestamp def ) {
	Datebox db = (Datebox)wnd.getFellowIfAny( dbName );
	if( db == null ) 
	    return def;
	Date dt = db.getValue();
	if( dt == null )
	    return def;
	return new Timestamp( dt.getTime() );
    }

    private IssueMessage getMessage( String msgId ) {
	return this.messages.get( msgId );
    }

    private int countIssues( long invId ) {
	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return 0;

	Set<String> keys = messages.keySet();
	int numIssues = 0;
	for( String msgId : keys ) {
	    String st = (String)ses.getAttribute( KEY_ISSUES+"."+msgId+"."+invId );
	    if( st != null )
		numIssues++;
	}
	return numIssues;
    }

    private boolean removeIssue( Window wnd, String issueStr ) {
	log.debug( "Remove Issue string: "+issueStr );
	String[] toks = issueStr.split( "_" );
	int nn = toks.length-1;
	if( nn > 2 )
	    nn--;
	if( nn <= 1 )
	    return false;
	long invId = Stringx.toLong( toks[nn], 0L );
	if( invId == 0L ) {
	    log.error( "Invalid component id" );
	    return false;
	}
	nn--;
	String msgId = toks[nn];
	IssueMessage iMsg = getMessage( msgId );
	if( iMsg == null ) {
	    log.error( "Cannot find message id: "+msgId );
	    return false;
	}
	iMsg.clearMessageRow( wnd );

	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return false;
	String st = (String)ses.getAttribute( KEY_ISSUES+"."+msgId+"."+invId );
	if( st != null )
	    ses.removeAttribute( KEY_ISSUES+"."+msgId+"."+invId );

	return (countIssues( invId ) > 0);	
    }	

    private void removeAllIssues( Window wnd, String issueStr ) {
	String[] toks = issueStr.split( "_" );
	int nn = toks.length-1;
	if( nn > 2 )
	    nn--;
	if( nn <= 1 )
	    return;
	long invId = Stringx.toLong( toks[nn], 0L );
	if( invId == 0L ) {
	    log.error( "Invalid component id" );
	    return;
	}
	// nn--;
	// String msgId = toks[nn];
	// IssueMessage iMsg = getMessage( msgId );
	// if( iMsg == null ) {
	//     log.error( "Cannot find message id: "+msgId );
	//     return false;
	// }
	// iMsg.clearMessageRow( wnd );

	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return;

	Set<String> keys = messages.keySet();
	int numIssues = 0;
	for( String msgId : keys ) {
	    String st = (String)ses.getAttribute( KEY_ISSUES+"."+msgId+"."+invId );
	    if( st != null ) {
		IssueMessage iMsg = getMessage( msgId );
		if( iMsg != null )
		    iMsg.clearMessageRow( wnd );
		ses.removeAttribute( KEY_ISSUES+"."+msgId+"."+invId );
	    }
	}
    }	

    private void setDisableStore( Window wnd, boolean disable ) {
	Button bt = (Button)wnd.getFellowIfAny( ACTION_STORE );
	if( bt != null )
	    bt.setDisabled( disable );
    }

    private void addMessage( Window wnd, long invoiceId, String msgId ) {
	IssueMessage iMsg = getMessage( msgId );
	if( iMsg == null )
	    return;

	// disable store button

	setDisableStore( wnd, true );

	// store sessions

	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return;	
	// Set<Long> issueInvoices = (Set<Long>)ses,getAttribute( KEY_ISSUES );
	Long invId = new Long( invoiceId );
	// if( issueInvoices == null ) 
	//     issueInvoices = new HashSet<Long>();
	// if( !issueInvoices.contains(invId) )
	//     issueInvoices.add( invId );
	// ses.setAttribute( KEY_ISSUES, issueInvoices );
	ses.setAttribute( KEY_ISSUES+"."+msgId+"."+invId.toString(), msgId );

	// display message

	Cell msgCell = (Cell)wnd.getFellowIfAny( msgId );
	if( msgCell == null ) {
	    Rows msgRows = (Rows)wnd.getFellowIfAny( "rsInvoiceIssueRows" );
	    if( msgRows == null )
		return;
	    iMsg.createMessageRow( invoiceId, msgRows, this );
	}
    }

    private Invoice loadInvoice( Window wnd ) {
	Grid form = (Grid)wnd.getFellowIfAny( "grInvoiceDetails" );
	Invoice inv = null;
	if( form != null ) {
	    Rows rows = form.getRows();
	    String invId = null;
	    if( (rows != null) && ((invId = rows.getId()) != null) ) {
		long invoiceId = Stringx.toLong( Stringx.after( invId, "_" ), 0 );
		log.debug( "Invoice id: "+invoiceId );

		SampleInventory sInv = getSampleInventory();
		if( sInv == null ) {
		    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid data access" );
		    return null;
		}
		try {
		    inv = sInv.findInvoiceById( invoiceId );
		}
		catch( SQLException sqe ) {
		    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
				 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		    log.error( sqe );
		}
		if( inv != null )
		    log.debug( "Invoice exists: "+ inv );
		else
		    log.debug( "New invoice: "+invoiceId );
	    }
	}
	return inv;
    }

    private boolean isValidReference( Window wnd, String invRef, Invoice preEdit ) {
	boolean isValid = true;
	if( (preEdit == null) || (!invRef.equalsIgnoreCase(preEdit.getInvoice())) ) {
	    isValid = false;
	    SampleInventory sInv = getSampleInventory();
	    if( sInv == null ) {
		showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid data access" );
	    }
	    else {
		Invoice inv = null;
		try {
		    inv = sInv.findInvoice( invRef );
		}
		catch( SQLException sqe ) {
		    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
				 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		    log.error( sqe );
		}
		if( inv != null ) 
		    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
				 "Invoice "+invRef+" exists already" );
		else
		    isValid = true;
	    }
	}
	return isValid;
    }

    private float getDecimalValue( Window wnd, String decId, float limit ) {
	Decimalbox db = (Decimalbox)wnd.getFellowIfAny( decId );
	BigDecimal val = null;
	if( db != null )
	    val = db.getValue();
	if( (val == null) || (val.floatValue() <= limit) )
	    return limit;
	return val.floatValue();
    }

    private Billing loadBilling( Window wnd, String billRef ) {
	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid data access" );
	    return null;
	}
	long billId = Stringx.toLong( billRef, 0L );
	if( billId == 0L ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid billing access" );
	    return null;
	}
	String purchase = null;
	Billing bill = null;
	try {
	    bill = sInv.findBillingById( billId );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}
	if( bill == null ) 
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", 
			 "Error: Cannot find billing information id: "+billRef );
	return bill;
    }

    private boolean storeInvoiceSession( Invoice preEdit, Invoice updInv ) {
	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return false;

	String invId = String.valueOf(updInv.getInvoiceid());
	if( preEdit != null )
	    ses.setAttribute( KEY_ISSUES+".previous."+invId, preEdit );
	ses.setAttribute( KEY_ISSUES+".updated."+invId, updInv );
	return true;
    }
    // private void clearInvoiceSession() {
    // 	Session ses = Sessions.getCurrent();
    // 	if( ses == null ) 
    // 	    return;

    // 	String invId = String.valueOf(updInv.getInvoiceid());
    // 	if( preEdit != null )
    // 	    ses.setAttribute( KEY_ISSUES+".previous."+invId, preEdit );
    // 	ses.setAttribute( KEY_ISSUES+".updated."+invId, updInv );
    // 	return true;
    // }

    private Invoice getInvoiceSession( String cmpId, String key ) {
	String[] toks = cmpId.split( "_" );
	int nn = toks.length-1;
	if( nn > 2 )
	    nn--;
	if( nn <= 1 )
	    return null;
	long invId = Stringx.toLong( toks[nn], 0L );
	if( invId == 0L ) {
	    log.error( "Invalid component id" );
	    return null;
	}
	Session ses = Sessions.getCurrent();
	if( ses == null ) 
	    return null;
	return (Invoice)ses.getAttribute( KEY_ISSUES+"."+key+"."+invId );
    }

    private Period checkInvoicePeriod( Window wnd, Billing bill, long invId ) {
	Timestamp started = getDateField( wnd, "dbInvoiceStarted", TS_UNKNOWN );
	if( TS_UNKNOWN.equals( started ) ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invoice period start is invalid" );
	    return null;
	}
	if( started.after( new Date() ) ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invoice period start is invalid" );
	    return null;
	}

	Timestamp ended = getDateField( wnd, "dbInvoiceEnded", TS_UNKNOWN );
	if( TS_UNKNOWN.equals( ended ) ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invoice period end is invalid" );
	    return null;
	}
	if( started.after( ended ) ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invoice period end is invalid" );
	    return null;
	}
	Period invPeriod = new Period( started, ended );

	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid data access" );
	    return null;
	}
	Invoice[] invoices = null;
	try {
	    invoices = sInv.findInvoiceByPeriod( invPeriod, true );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}
	if( (invoices != null) && (invoices.length > 0) ) {
	    for( int i = 0; i < invoices.length; i++ ) {
		if( bill.getPurchase().equals(invoices[i].getPurchase()) ) {
		    addMessage( wnd, invId, MSG_PERIOD );
		    break;
		}
	    }
	}

	return invPeriod;
    }

    private String checkCurrency( Window wnd, Billing bill, long invId ) {
	String curr = getSelectedItem( wnd, "cbInvoiceCurrency", "" );
	if( curr.length() <= 0 ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid currency" );
	    return null;
	}
	if( !curr.equals(bill.getCurrency()) ) 
	    addMessage( wnd, invId, MSG_CURRENCY );
	return curr;
    }

    private boolean updateInvoiceStatus( Window wnd, Invoice invoice ) {
	Timestamp verified = getDateField( wnd, "dbInvoiceVerified", TS_UNKNOWN );
	if( !TS_UNKNOWN.equals( verified ) ) {
	    if( verified.after( new Date() ) ) {
		showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Future verification date is not valid" );
		return false;
	    }
	    invoice.setVerified( verified );
	}
	Timestamp approved = getDateField( wnd, "dbInvoiceApproved", TS_UNKNOWN );
	boolean approvalGiven = false;
	if( !TS_UNKNOWN.equals( approved ) ) {
	    if( approved.after( new Date() ) ) {
		showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Future approval date is not valid" );
		return false;
	    }
	    invoice.setApproved( approved );
	    approvalGiven = true;
	}

	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtInvoiceRejected" );
	String reason = "";
	if( txt != null ) 
	    reason = Stringx.getDefault(txt.getValue(),"").trim();
	invoice.setReason( reason );

	Timestamp rejected = getDateField( wnd, "dbInvoiceRejected", TS_UNKNOWN );
	if( !TS_UNKNOWN.equals( rejected ) ) {
	    if( rejected.after( new Date() ) ) {
		showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Future rejectance date is not valid" );
		return false;
	    }
	    if( reason.length() <= 0 ) {
		showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Rejected reason not provided" );
		return false;
	    }
	    if( approvalGiven ) 
		invoice.setApproved( TS_UNKNOWN );
	    invoice.setRejected( rejected );
	}
	else if( reason.length() > 0 ) {
	    invoice.setRejected( new Timestamp(System.currentTimeMillis()) );
	}
	return true;
    }

    private Invoice updateInvoice( Window wnd, Invoice preEdit ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtInvoiceRef" );
	String invRef = null;
	if( txt != null ) {
	    invRef = Stringx.getDefault(txt.getValue(),"").trim();
	}
	if( (invRef == null) || (invRef.length() <= 0) ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invoice reference must not be empty" );
	    return null;
	}
	log.debug( "Invoice reference: "+invRef );

	String poNum = getSelectedItem( wnd, "cbInvoicePO", "" );
	if( poNum.length() <= 0 ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Purchase order must not be empty" );
	    return null;
	}
	log.debug( "PO Number: "+poNum );

	// check if invoice reference exists

	if( !isValidReference( wnd, invRef, preEdit ) )
	    return null;

	Invoice invoice = new Invoice();
	if( preEdit != null )
	    invoice.setInvoiceid( preEdit.getInvoiceid() );
	invoice.setInvoice( invRef );

	// check if billing entry needs to be created

	Billing bill = loadBilling( wnd, poNum );
	if( bill == null )
	    return null;
	log.debug( "Billing record: "+bill );
	invoice.setPurchase( bill.getPurchase() );

	// check if period is valid and if it has not been charged already

	Period invPeriod = checkInvoicePeriod( wnd, bill, invoice.getInvoiceid() );
	if( invPeriod == null )
	    return null;
	Timestamp[] ts = invPeriod.toTimestamp();
	invoice.setStarted( ts[0] );
	invoice.setEnded( ts[1] );
	
	// check amount > 0

	float amount = getDecimalValue( wnd, "decInvoiceAmount", 0f );
	if( amount == 0f ) 
	    addMessage( wnd, invoice.getInvoiceid(), MSG_AMOUNT );
	invoice.setAmount( amount );

	// check num samples > 0

	amount = getDecimalValue( wnd, "decInvoiceNumsamples", 0f );
	if( amount == 0f ) 
	    addMessage( wnd, invoice.getInvoiceid(), MSG_NUMSAMPLES );
	invoice.setNumsamples( amount );

	// check currency
	String curr = checkCurrency( wnd, bill, invoice.getInvoiceid() );
	if( curr == null )
	    return null;
	invoice.setCurrency( curr );

	// update invoice status

	if( !updateInvoiceStatus( wnd, invoice ) )
	    return null;

	// store invoice to session

	storeInvoiceSession( preEdit, invoice );

	return invoice;	    
    }

    private Invoice storeInvoice( Window wnd, Invoice inv, Invoice preEdit ) {
	log.debug( "Storing invoice "+inv );
	if( countIssues( inv.getInvoiceid() ) > 0 ) {
	    log.warn( "Invoice "+inv+" has issues." );
	    return null;
	}
	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: Invalid data access" );
	    return null;
	}
	Invoice updated = null;
	try {
	    if( preEdit == null ) {
		log.debug( "Creating new invoice: "+inv );
		updated = sInv.createInvoice( inv );
	    }
	    else {
		log.debug( "Updating invoice: "+inv );
		updated = sInv.storeInvoice( inv );
	    }
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowInvoiceDetailsMessage", "lbInvoiceDetailsMessage", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    updated = null;
	}
	
	return updated;
    }

    // private InventoryPreferences getPreferences() {
    //  	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    // }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Update invoice event: "+event );
	Component cmp = event.getTarget();
	if( cmp == null ) {
	    log.error( "Cannot determine event target" );
	    return;
	}
	Window wnd = ZKContext.findWindow( cmp );
	if( wnd == null ) {
	    log.error( "Cannot determine window" );
	    return;
	}

	String cmpId = cmp.getId();
	if( cmpId == null )
	    return;

	log.debug( "Update invoice, component: "+cmpId );
 
	Invoice updInv = null;
	Invoice preEdit = null;
	if( ACTION_CLOSE.equals(cmpId) ) {
	    Events.postEvent("onClose", wnd, null);
	    return;
	}
	else if( ACTION_STORE.equals(cmpId) ) {
	    log.debug( "Invoice store action launched" );
	    UIUtils.clearMessage( wnd, "lbInvoiceDetailsMessage" );
	    preEdit = loadInvoice( wnd );
	    updInv = updateInvoice( wnd, preEdit );
	}
	else if( (cmpId.indexOf( "msgIssue" ) > 0) && (cmpId.endsWith( "_dismiss" )) ) {
	    removeAllIssues( wnd, cmpId );
	    setDisableStore( wnd, false );
	    return;
	}
	else if( cmpId.indexOf( "msgIssue" ) > 0 ) {
	    preEdit = loadInvoice( wnd );
	    updInv = getInvoiceSession( cmpId, "updated" );
	    if( removeIssue( wnd, cmpId ) ) 
		return;
	    else {
		setDisableStore( wnd, false );
	    }
	}

	if( (updInv != null) && ((updInv = storeInvoice( wnd, updInv, preEdit )) != null) ) {
	    Period period = new Period( updInv.getStarted(), updInv.getEnded() );
	    Events.postEvent("onClose", wnd, period );
	}
	
    }

}

class IssueMessage {
    private String messageId;
    private String message;
    private List<String> buttons;

    private static Log log = LogFactory.getLog(IssueMessage.class);

    public IssueMessage( String msgId, String msg ) {
	this.messageId = msgId;
	this.message = msg;
	this.buttons = new ArrayList<String>();
    }

    /**
     * Get the <code>MessageId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessageId() {
	return messageId;
    }

    /**
     * Set the <code>MessageId</code> value.
     *
     * @param messageId The new MessageId value.
     */
    public final void setMessageId(final String messageId) {
	this.messageId = messageId;
    }

    /**
     * Get the <code>Message</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessage() {
	return message;
    }

    /**
     * Set the <code>Message</code> value.
     *
     * @param message The new Message value.
     */
    public final void setMessage(final String message) {
	this.message = message;
    }

    public void addButton( String buttonDesc ) {
	buttons.add( buttonDesc );
    }

    public Row createMessageRow( long invId, Component parent, EventListener listener ) {
	log.debug( "Creating message row: "+invId ); 
	Row row = new Row();
	row.setId( "row_"+getMessageId() );
	Cell cell = UIUtils.styledCell( null, getMessage() );
	// cell.setColspan( 2 );
	this.createLabel( cell );
	this.createButtons( invId, cell, listener );
	cell.setParent( row );
	row.setParent( parent );
	return row;
    }
    
    public void clearMessageRow( Window wnd ) {
	Row row = (Row)wnd.getFellowIfAny( "row_"+getMessageId() );
	if( row != null )
	    row.detach();
    }

    public Label createLabel( Component parent ) {
	Label label = new Label( getMessage() );
	// label.setId( "lb_"+getMessageId() );
	label.setParent( parent );
	return label;
    }

    private Properties getButtonProperties( int idx, long invId, String buttonMsg ) {
	Properties props = null;
	if( buttonMsg.indexOf(";") >= 0 )
	    props = Stringx.toProperties( buttonMsg, ";" );
	else {
	    props = new Properties();
	    props.put( "label", buttonMsg );
	}
	props.put( "id", "bt"+String.valueOf(idx)+"_"+getMessageId()+"_"+String.valueOf(invId) );
	return props;
    }

    public Hlayout createButtons( long invId, Component parent, EventListener listener ) {
	Hlayout hl = new Hlayout();
	int idx = 0;
	for( String buttonDesc : buttons ) {
	    Properties props = getButtonProperties( idx, invId, buttonDesc );
	    Button bt = new Button( props.getProperty("label") );
	    String pv = props.getProperty( "width" );
	    if( pv != null )
		bt.setWidth( pv );
	    pv = props.getProperty( "id" );	    
	    String dismiss = props.getProperty( "action" );	    
	    if( pv != null ) {
		log.debug( "button id: "+pv );
		if( dismiss != null )
		    bt.setId( pv+"_dismiss" );
		else
		    bt.setId( pv );
	    }
	    bt.addEventListener( Events.ON_CLICK, listener );
	    bt.setParent( hl );
	    idx++;
	}
	hl.setParent( parent );
	return hl;
    }

}
