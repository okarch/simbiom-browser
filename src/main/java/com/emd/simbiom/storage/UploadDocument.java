package com.emd.simbiom.storage;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;

import org.zkoss.util.media.Media;

// import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
// import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Messagebox;
// import org.zkoss.zul.Row;
// import org.zkoss.zul.Rows;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.StorageProject;
import com.emd.simbiom.model.StorageDocument;

import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;
import com.emd.simbiom.util.DataHasher;

import com.emd.io.WriterOutputStream;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>UploadDocument</code> persists changes to a storage project.
 *
 * Created: Mon Mar 25  08:50:39 2019
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class UploadDocument extends InventoryCommand {

    private static Log log = LogFactory.getLog(UploadDocument.class);

    private static final String CMD_DOCS_ADD    = "btDocumentAdd";
    private static final String CMD_DOCS_DELETE = "btDocumentDelete";

    /**
     * Creates a new command to change the invoice period.
     */
    public UploadDocument() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    // private void createNewProject() {
    //  	Session ses = Sessions.getCurrent();
    //  	if( ses == null ) 
    //  	    return;
    // 	ses.setAttribute( NEW_PROJECT, "true" );
    // }

    // private boolean isNewProject() {
    //  	Session ses = Sessions.getCurrent();
    //  	if( ses == null ) 
    //  	    return true;
    // 	String st = (String)ses.getAttribute( NEW_PROJECT );
    // 	boolean flag = Stringx.toBoolean( Stringx.getDefault(st,"false"), false );
    // 	return flag;
    // }

    // private void clearNewProject() {
    //  	Session ses = Sessions.getCurrent();
    //  	if( ses == null ) 
    //  	    return;
    // 	String st = (String)ses.getAttribute( NEW_PROJECT );
    // 	if( st != null )
    // 	    ses.removeAttribute( NEW_PROJECT );
    // }

    private StorageProject getStorageProject( Window wnd ) {
	InventoryPreferences pref = getPreferences();
	StorageProjectModel sPrjs = (StorageProjectModel)pref.getResult( "cbStorageProject" );
	StorageProject proj = null;
	if( sPrjs != null ) 
	    proj = sPrjs.getSelectedStorageProject( wnd );
	return proj;
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

    private StorageDocument upload( Event event ) throws IOException {
	File tempF = null;
	if( event instanceof UploadEvent ) {
	    Media media = ((UploadEvent)event).getMedia();
	    log.debug( "Upload file name: "+Stringx.getDefault(media.getName(),"unknown")+
		       " format: "+Stringx.getDefault(media.getContentType(),"unknown")+
		       " ("+Stringx.getDefault(media.getFormat(),"unknown")+
		       ") binary: "+media.isBinary()+" in memory: "+media.inMemory() );

	    tempF = createUploadFile( media.getName() );

	    if( tempF == null )
		throw new IOException( "Cannot prepare upload file" );

	    // OutputStream outs = new FileOutputStream( tempF );

	    StringWriter sw = new StringWriter();
	    WriterOutputStream outs = new WriterOutputStream( sw );
	    long orgSize = DataHasher.encodeTo( media.getStreamData(), outs );
	    outs.flush();
	    String updCont = sw.toString();
	    outs.close();
	    String md5 = DataHasher.calculateMd5sum( updCont );
	    log.debug( "Temporary upload file created ("+md5+"): "+tempF );
	    StorageDocument sDoc = StorageDocument.fromFile( tempF, md5 );
	    sDoc.setMime( Stringx.getDefault(media.getContentType(),"") );
	    return sDoc;
	}
	else
	    throw new IOException( "No upload detected" );
    }

    private void updateDocumentModel( Window wnd, StorageDocument storedDoc ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Invalid database access." );
	    return;
	}
	InventoryPreferences pref = getPreferences();
	StorageDocumentModel sDocs = (StorageDocumentModel)pref.getResult( "cbStorageDocs" );
	if( sDocs != null ) {
	    try {
		StorageDocument[] docs = dao.findDocuments( storedDoc.getProjectid(), null );
		log.debug( "Query documents of project "+storedDoc.getProjectid()+": "+docs.length );
		Map context = new HashMap();
		context.put( StorageDocumentModel.RESULT, docs );
		sDocs.assignModel( wnd, context );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Cannot store document: "+
			     Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    }
	}
    }

    private void updateDocument( Window wnd, StorageDocument doc ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Invalid database access." );
	    return;
	}
	StorageDocument storedDoc = null;
	try {
	    storedDoc = dao.storeDocument( doc, doc.getFile() );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Cannot store document: "+
      			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
      	    log.error( sqe );
	    return;
	}
	showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Storage document \""+storedDoc+"\" stored successfully." );

	updateDocumentModel( wnd, storedDoc );
    }

    private void storeUpload( Window wnd, StorageDocument doc ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Invalid database access." );
	    return;
	}

	StorageProject sProject = getStorageProject( wnd );
	if( sProject == null )
	    return;
	log.debug( "Selected storage project: "+sProject );

	doc.setProjectid( sProject.getProjectid() );

	StorageDocument storedDoc = null;
	try {
	    StorageDocument[] docs = dao.findDocuments( sProject.getProjectid(), doc.getMd5sum() );
	    if( docs.length <= 0 ) {
		updateDocument( wnd, doc );
	    }
	    else {
		final StorageDocument saveDoc = doc;
		final Window targetWnd = wnd;
		Messagebox.show( "Document \""+doc+"\" exists already.\nDo you want to overwrite it?",
				 "Overwrite Document", 
				 Messagebox.YES+Messagebox.NO, 
				 Messagebox.QUESTION,
				 new EventListener() {
				     public void onEvent(Event event) {
					 if( (Messagebox.ON_YES.equals(event.getName()))) {
					     log.debug( "Document "+saveDoc+" about to be saved." );
					     updateDocument( targetWnd, saveDoc );
					 }
				     }
				 });
	    }
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: Cannot query database: "+
      			 Stringx.getDefault(sqe.getMessage(),"reason unknown") );
      	    log.error( sqe );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
     	throws java.lang.Exception {

	log.debug( "Upload document: "+event );

	StorageDocument uploadDoc = null;
	Window wnd = UIUtils.getWindow( event );
	try {
	    uploadDoc = upload( event );
	}
	catch( IOException ioe ) {
	    showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error" ) );
	    log.error( ioe );
	    return;
	}

	if( uploadDoc != null ) 
	    storeUpload( wnd, uploadDoc );

    // 	if( ("onAfterRender".equals( event.getName() )) && 
    // 	    ( cb != null ) &&
    // 	    ( cb.getItemCount() > 2 ) ) {

    // 	    cb.setSelectedIndex( 1 );
    // 	    selPeriod = getSelectedPeriod( cb, 1 );
    // 	    // int idx = popAfterRenderIndex( cb.getId(), 0 );
    // 	    // // log.debug( "Invoice status after render index: "+idx );
    // 	    // if( idx < cb.getItemCount() ) {
    // 	    // 	cb.setSelectedIndex( idx );
    // 	    // }	    
    // 	}	
    // 	else if( (Events.ON_SELECT.equals( event.getName() ) ) &&
    // 		 ( cb != null ) &&
    // 		 ( cb.getItemCount() > 0 ) ) {
    // 	    int idx = cb.getSelectedIndex();
    // 	    if( idx >= 0 ) 
    // 		selPeriod = getSelectedPeriod( cb, idx );
    // 	}


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

	// log.debug( "Command: "+getCommandName()+" clicked." );

	// UIUtils.clearMessage( wnd, "lbStorageMessage" );

	// if( CMD_PROJECT_ADD.equals(getCommandName()) ) {
	//     log.debug( "Creating new storage project" );
	//     clearStorageProject( wnd, "New Storage Project" );
	//     createNewProject();
	//     showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Warning: New storage project has not been saved yet." );
	// }
	// else if( CMD_PROJECT_SAVE.equals(getCommandName()) ) {
	//     StorageProject prj = saveStorageProject( wnd );
	//     if( prj != null ) {
	// 	String poNum = saveBilling( wnd, prj );
	// 	clearNewProject();
	// 	updateStorageProjects( wnd, prj );
	// 	showMessage( wnd, "rowStorageMessage", "lbStorageMessage", "Storage project \""+
	// 		     prj.getTitle()+"\" has been stored. Storage groups: "+
	// 		     prj.getStorageGroups().length+", Purchase order: "+
	// 		     ((poNum != null)?poNum:"missing") );
	//     }
	// }
    }    
    
} 
