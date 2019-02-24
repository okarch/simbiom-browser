package com.emd.simbiom.budget;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;

import com.emd.zk.view.VelocityView;
import com.emd.zk.view.ViewAction;

import com.emd.util.Parameter;

/**
 * <code>InvoiceDetailsView</code> presents details of an invoice.
 *
 * Created: Sun Dec  2 09:13:49 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class InvoiceDetailsView extends VelocityView {
    private String messageRowId;
    private String detailsLayout;

    private static Log log = LogFactory.getLog(InvoiceDetailsView.class);

    public InvoiceDetailsView() {
    }

    /**
     * Updates the view actions of this command.
     */
    public void updateActions( String portletId, long userId ) {
	ViewAction[] acts = getActions();
	for( int i = 0; i < acts.length; i++ ) {
	    if( acts[i] instanceof InventoryViewAction ) {
		((InventoryViewAction)acts[i]).setUserId( userId );
		((InventoryViewAction)acts[i]).setPortletId( portletId );
	    }
	}
    }

    /**
     * Get the <code>DetailsLayout</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getDetailsLayout() {
	return detailsLayout;
    }

    /**
     * Set the <code>DetailsLayout</code> value.
     *
     * @param detailsLayout The new DetailsLayout value.
     */
    public final void setDetailsLayout(final String detailsLayout) {
	this.detailsLayout = detailsLayout;
    }

    /**
     * Get the <code>MessageRowId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessageRowId() {
	return messageRowId;
    }

    /**
     * Set the <code>MessageRowId</code> value.
     *
     * @param messageRowId The new MessageRowId value.
     */
    public final void setMessageRowId(final String messageRowId) {
	this.messageRowId = messageRowId;
    }

    private String getLabelId() {
	return Stringx.getDefault(getMessageRowId(),"rowMessage").replace( "row", "lb" );
    }

    protected void writeMessage( Window wnd, String msg ) {
	UIUtils.showMessage( wnd, Stringx.getDefault(getMessageRowId(),"rowMessage"), getLabelId(), msg );
    }

    // protected void registerActions( Window wnd ) {
    // 	Parameter[] params = this.getParameters();
    // 	List<Parameter> paras = Arrays.asList( params );
    // 	ViewAction[] acts = this.getActions();
    // 	for( int j = 0; j < acts.length; j++ ) {
    // 	    Component cmp = wnd.getFellowIfAny( acts[j].getComponent() );
    // 	    acts[j].registerEvent( wnd, paras );
    // 	    log.debug( "Component id ("+((cmp==null)?"NOT existing":"existing")+"): "+acts[j].getComponent()+" event: "+acts[j].getEvent() );
    // 	    log.debug( "Event listener: "+cmp.getEventListeners( acts[j].getEvent() ) );
    // 	}
    // }

    private Timestamp validDate( Timestamp dt ) {
	if( (dt == null) || (dt.getTime() <= InvoiceDetails.NO_DATE.getTime()) )
	    return null;
	return dt;
    }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	log.debug( "Initializing components, context: "+context );
	Datebox db = (Datebox)wnd.getFellowIfAny( "dbInvoiceStarted" );
	InvoiceDetails details = (InvoiceDetails)context.get( "details" );
	if( (db != null) && (details != null) )
	    db.setValue( details.getInvoice().getStarted() );
	db = (Datebox)wnd.getFellowIfAny( "dbInvoiceEnded" );
	if( (db != null) && (details != null) )
	    db.setValue( details.getInvoice().getEnded() );
	db = (Datebox)wnd.getFellowIfAny( "dbInvoiceVerified" );
	if( (db != null) && (details != null) ) {
	    Timestamp ts = validDate( details.getInvoice().getVerified() );
	    if( ts != null )
		db.setValue( ts );
	}
	db = (Datebox)wnd.getFellowIfAny( "dbInvoiceApproved" );
	if( (db != null) && (details != null) ) {
	    Timestamp ts = validDate( details.getInvoice().getApproved() );
	    if( ts != null )
		db.setValue( ts );
	}
	db = (Datebox)wnd.getFellowIfAny( "dbInvoiceRejected" );
	if( (db != null) && (details != null) ) {
	    Timestamp ts = validDate( details.getInvoice().getRejected() );
	    if( ts != null )
		db.setValue( ts );
	}
    }

    /**
     * Initializes the filter settings.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initView( Window wnd, Map context ) {
	log.debug( "Layout category view. context: "+context );
	layout( wnd, context );
	initComponents( wnd, context );
	// registerActions( wnd );
    }

}
