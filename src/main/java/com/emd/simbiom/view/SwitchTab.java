package com.emd.simbiom.view;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.CostEstimate;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.ZKUtil;
import com.emd.zk.command.CommandException;

/**
 * The <code>SwitchTab</code> action is invoked when a new tab is selected.
 *
 * Created: Tue Jul 18 12:43:39 2016
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class SwitchTab extends InventoryCommand {

    private static Log log = LogFactory.getLog(SwitchTab.class);

    private static Map<String,CostEstimate> estimates;

    /**
     * Creates a new command to select from the result log.
     */
    public SwitchTab() {
	super();
	this.estimates = new HashMap<String,CostEstimate>();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private String getSessionId() {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    Object hSes = ses.getNativeSession();
	    if( (hSes != null) && (hSes instanceof HttpSession) )
		return ((HttpSession)hSes).getId();
	}
	return String.valueOf(getUserId());
    }

    /**
     * Returns the current sessions cost estimate.
     *
     * @return cost estimate or null if not available.
     */
    public CostEstimate getCostEstimate( Window wnd ) {
	String sessionId = getSessionId();
	log.debug( "Session identified: "+sessionId );
	CostEstimate ce = estimates.get( sessionId );
	if( ce == null ) {
	    SampleInventoryDAO dao = getSampleInventory();
	    if( dao == null ) {
		log.error( "No database access configured" );
		return null;
	    }
	    try {
		ce = dao.createCostEstimate( null );
		log.debug( "Cost estimate created: "+ce );
		estimates.put( sessionId, ce );
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
	    }
	}
	if( ce != null ) {
	    Textbox txt = (Textbox)wnd.getFellowIfAny( "txtCostProject" );
	    if( txt != null )
		txt.setValue( ce.getProjectname() );
	}
	return ce;
    }

    /**
     * Assigns the cost estimate to the current session.
     *
     * @param wnd the window.
     * @param ce the cost estimate.
     * @return the session identifier.
     */
    public String setCostEstimate( Window wnd, CostEstimate ce ) {
	String sessionId = getSessionId();
	estimates.put( sessionId, ce );
	return sessionId;
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	Component cmp = event.getTarget();
	if( cmp instanceof Tab ) {

	    log.debug( "Tab switched: "+event );

	    Tab tb = (Tab)cmp;
	    String tabSt = Stringx.getDefault(tb.getLabel(), "" );
	    if( "Costs".equals( tabSt ) )
		getCostEstimate( ZKUtil.findWindow(cmp) );
	}
    }

    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	// BatchEntry be = getSelectedEntry( wnd );
	// log.debug( "Selected result log: "+((be != null)?be.toString():"null") );

	// String levelFilter = getLevelFilter( wnd );
	// log.debug( "Levels to select: "+levelFilter );

	// ResultLog rl = getResultLog();
	// if( rl == null ) {
	//     log.error( "Cannot determine result log" );
	//     return;
	// }
	
	// UploadLog[] logs = null;
	// try {
	//     logs = getLogEntries( be, levelFilter );
	// }
	// catch( SQLException sqe ) {
	//     showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
	// 		 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	// }

	// Map ctxt = new HashMap();
	// if( logs == null )
	//     logs = new UploadLog[0];
	// ctxt.put( ResultLog.RESULT, logs );

	// log.debug( "Number of relevant log entries: "+logs.length );

	// rl.assignModel( wnd, ctxt );
    }    
    
} 
