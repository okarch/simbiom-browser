package com.emd.simbiom.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.DocumentLoader;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.StorageDocument;
import com.emd.simbiom.model.User;

import com.emd.simbiom.upload.InventoryUploadTemplate;
import com.emd.simbiom.upload.UploadBatch;

import com.emd.simbiom.template.OutputSelector;

import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>ReportDetailsAction</code> handles report generation and download.
 *
 * Created: Wed Dec 26 10:28:40 2019
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class ReportDetailsAction extends InventoryViewAction {
    private InventoryUploadTemplate template;

    private static Log log = LogFactory.getLog(ReportDetailsAction.class);

    private static final String ACTION_CANCEL  = "btReportDetailsCancel";
    private static final String ACTION_CLOSE   = "btReportDetailsClose";
    private static final String ACTION_GENERATE= "btReportDetailsGenerate";
    private static final String ACTION_DOWNLOAD= "btReportDetailsDownload";

    private static final Timestamp TS_UNKNOWN = new Timestamp( 1000L );

    /**
     * Creates a new report view action.
     */
    public ReportDetailsAction() {
	super();
    }
    
    private InventoryPreferences getPreferences() {
      	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private User validateUser( Window wnd, long reqRole ) {
     	User user = null;
     	try {
     	    SampleInventory dao = getSampleInventory();
     	    user = dao.findUserById( getUserId() );
     	}
     	catch( SQLException sqe ) {
     	    writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
     	    log.error( sqe );
     	    return null;
     	}

     	if( user == null ) {
     	    writeMessage( wnd, "Error: User id "+getUserId()+" is unknown" );
     	    return null;
     	}

     	if( !user.hasRole( reqRole ) ) {
     	    writeMessage( wnd, "Error: User "+user+" requires permission \""+
			  Roles.roleToString( reqRole )+"\"" );
     	    return null;
     	}
     	return user;
    }

    /**
     * Get the <code>Template</code> value.
     *
     * @return an <code>InventoryUploadTemplate</code> value
     */
    public final InventoryUploadTemplate getTemplate() {
	return template;
    }

    /**
     * Set the <code>Template</code> value.
     *
     * @param template The new Template value.
     */
    public final void setTemplate(final InventoryUploadTemplate template) {
	this.template = template;
    }

    private OutputSelector getOutputSelector() {
	return (OutputSelector)getPreferences().getResult( "cb"+
							   ReportDetailsView.CMP_REPORT_PREFIX+
							   "OutputSelector" );
    }

    private void updateOutputs( Window wnd, InventoryUploadTemplate templ ) {
	OutputSelector outS = getOutputSelector();
	if( outS == null ) {
	    log.error( "Cannot determine output selection" );
	    return;
	}

	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: Invalid database access" );
	    return;
	}

	try {
	    StorageDocument[] tList = dao.findOutputByTemplate( templ );
	    log.debug( "Report outputs available: "+tList.length );
	    Map context = new HashMap();
	    context.put( OutputSelector.RESULT, tList );
	    outS.updateModel( wnd, context );
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	}
    }

    private StorageDocument getSelectedOutput( Window wnd ) {
	OutputSelector out = getOutputSelector();
	if( out == null ) {
	    log.error( "No output to select" );
	    return null;
	}
	return out.getSelectedOutput( wnd );
    }

    private File createUploadFile( String name ) 
     	throws IOException {

     	File tempF = File.createTempFile( "simbiom", null );
     	File dir = new File( tempF.getParentFile(), "simbiom" );
     	if( !dir.exists() && !dir.mkdir() ) {
     	    log.error( "Cannot create upload directory: "+dir );
     	    return null;
     	}
     	dir = new File( dir, String.valueOf(getUserId()) );
     	if( !dir.exists() && !dir.mkdir() ) {
     	    log.error( "Cannot create upload directory: "+dir );
     	    return null;
     	}
     	return new File( dir, Stringx.getDefault(name, "NONAME.temp" ) );
    }

    private boolean sendDownload( Window wnd, StorageDocument doc ) {
	File tempF = null;
	try {
	    tempF = createUploadFile( doc.getTitle() );
	}
	catch( IOException ioe ) {
	    log.error( ioe );
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error occured" ) );
	    return false;
	}

	log.debug( "Download file created: "+tempF );

	DocumentLoader dao = doc.getDocumentLoader();
	if( dao == null ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Invalid database access." );
	    return false;
	}

	boolean success = false;
	String mime = Stringx.getDefault( doc.getMime(), "application/octet-stream");
	try {
	    OutputStream fos = new FileOutputStream( tempF );
	    success = dao.writeContent( doc.getMd5sum(), mime, fos );
	    fos.close();
	    
	}
	catch( IOException ioe ) {
	    log.error( ioe );
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error occured" ) );
	    return false;
	}

	if( success ) {
	    try {
		Filedownload.save( tempF, mime );
		return true;
	    }
	    catch( FileNotFoundException fnfe ) {
		log.error( fnfe );
		showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: File "+tempF+" not found." );
	    }
	}
	return false;
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Report details ("+getComponent()+") event: "+event );

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

	User user = validateUser( wnd, Roles.INVENTORY_REPORT );
	if( user == null ) 
     	    return;

	InventoryUploadTemplate templ = getTemplate();
	if( templ == null ) {
	    log.error( "Cannot determine report template" );
	    return;
	}

	if( ACTION_CLOSE.equals(cmpId) || ACTION_CANCEL.equals(cmpId) ) {
	    Events.postEvent("onClose", wnd, null);
	    return;
	}
	else if( ACTION_GENERATE.equals(cmpId) ) {
	    log.debug( "Report generation invoked" );
	    UIUtils.clearMessage( wnd, "lbReportDetailsMessage" );
	    ReportEngine repEngine = new ReportEngine( ReportDetailsView.CMP_REPORT_PREFIX, 
						       getUserId(), 
						       getPortletId() );
	    writeMessage( wnd, "Report started..." );
	    UploadBatch uBatch = repEngine.runReport( wnd, user, templ );
	    if( uBatch == null )
		return;

	    updateOutputs( wnd, templ );
	}
	else if( ACTION_DOWNLOAD.equals(cmpId) ) {
	    log.debug( "Download output" );
	    StorageDocument doc = getSelectedOutput( wnd );
	    if( doc == null )
		return;
	    writeMessage( wnd, "Report download of "+doc );
	    log.debug( "Document to download: "+doc );
	    sendDownload( wnd, doc );
	}

	//     preEdit = loadLabelInvoice( wnd, true );
	//     if( preEdit != null )
	// 	initInvoiceDetails( wnd, preEdit );
	// }
	// else if( cmpId.indexOf( "msgIssue" ) > 0 ) {
	//     preEdit = loadInvoice( wnd );
	//     updInv = getInvoiceSession( cmpId, "updated" );
	//     if( removeIssue( wnd, cmpId ) ) 
	// 	return;
	//     else {
	// 	setDisableStore( wnd, false );
	//     }
	// }

	// if( (updInv != null) && ((updInv = storeInvoice( wnd, updInv, preEdit )) != null) ) {
	//     Period period = new Period( updInv.getStarted(), updInv.getEnded() );
	//     Events.postEvent("onClose", wnd, period );
	// }
	
    }

}
