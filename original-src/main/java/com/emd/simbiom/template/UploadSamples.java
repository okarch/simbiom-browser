package com.emd.simbiom.template;

/**
 * The <code>UploadSamples</code> action populates the result log selector.
 *
 * Created: Thu Jul 16 08:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
import java.sql.Timestamp;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.util.media.Media;

// import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.User;

import com.emd.simbiom.upload.InventoryUploadTemplate;
import com.emd.simbiom.upload.UploadBatch;
import com.emd.simbiom.upload.UploadException;
import com.emd.simbiom.upload.UploadProcessor;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

public class UploadSamples extends InventoryCommand {

    private static Log log = LogFactory.getLog(UploadSamples.class);

    /**
     * Creates a new command to upload samples from a file.
     */
    public UploadSamples() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
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

    private File storeUpload( Event event ) throws IOException {
	File tempF = null;
	if( event instanceof UploadEvent ) {
	    Media media = ((UploadEvent)event).getMedia();
	    log.debug( "Upload file name: "+Stringx.getDefault(media.getName(),"unknown")+
		       " format: "+Stringx.getDefault(media.getContentType(),"unknown")+
		       " ("+Stringx.getDefault(media.getFormat(),"unknown")+
		       ") binary: "+media.isBinary()+" in memory: "+media.inMemory() );

	    if( media.isBinary() ) 
		throw new IOException( "Unable to upload binary file" );

	    tempF = createUploadFile( media.getName() );
	    if( tempF == null )
		throw new IOException( "Cannot prepare upload file" );
	    FileWriter fw = new FileWriter( tempF ); 
	    Reader r = media.getReaderData();
	    IOUtils.copy( media.getReaderData(), fw );
	    r.close();
	    fw.close();
	    log.debug( "Temporary upload file created: "+tempF );
	    return tempF;
	}
	else
	    throw new IOException( "No upload detected" );
    }

    private InventoryUploadTemplate getTemplate( Window wnd ) {
	ModelProducer[] mp = getPreferences().getResult( TemplateModel.class );
	if( mp.length <= 0 )
	    return null;
	return ((TemplateModel)mp[0]).getSelectedTemplate( wnd );
    }

    private UploadBatch createUploadBatch( InventoryUploadTemplate templ, String updContent ) {
     	UploadBatch uBatch = new UploadBatch();
     	uBatch.setTemplateid( templ.getTemplateid() );
	if( updContent != null )
	    uBatch.setUpload( updContent );
	uBatch.setUploaded( new Timestamp(System.currentTimeMillis()) );
	uBatch.setUserid( getUserId() );
	return uBatch;
    }

    private User validateUser( Window wnd, long reqRole ) {
	User user = null;
	try {
	    SampleInventoryDAO dao = getSampleInventory();
	    user = dao.findUserById( getUserId() );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}

	if( user == null ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User id "+
			 getUserId()+" is unknown" );
	    return null;
	}

	if( !user.hasRole( reqRole ) ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User "+
			 user+" requires permission \""+Roles.roleToString( reqRole )+"\"" );
	    return null;
	}
	return user;
    }

    private void runUpload( Window wnd,
			    User user, 
			    InventoryUploadTemplate templ, 
			    long batchId )
	throws UploadException {

     	UploadProcessor uProcessor = UploadProcessor.getInstance();
	Map ctxt = new HashMap();
	ctxt.put( "user", user );
	uProcessor.processUpload( templ, batchId, ctxt );
    }	

    private void updateLog( Window wnd, InventoryUploadTemplate templ, long uploadid ) {
	OpenResultLog orl = (OpenResultLog)getPreferences().getCommand( OpenResultLog.class );
	if( orl != null ) {
	    try {
		orl.initLogs( wnd, templ );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		log.error( sqe );
	    }
	}
	SelectResultLog selLog = (SelectResultLog)getPreferences().getCommand( SelectResultLog.class );
	if( (selLog != null) && (selLog.selectUploadid( wnd, uploadid ) != null) ) {
	    try {
		selLog.execute( ZKContext.getInstance(), wnd );
	    }
	    catch( CommandException cex ) {
		log.error( cex );
	    }
	}
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	File upFile = null;
	String updContent = null;
	
	Window wnd = UIUtils.getWindow( event );
	try {
	    upFile = storeUpload( event );
	    updContent = FileUtils.readFileToString( upFile );
	}
	catch( IOException ioe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error" ) );
	    log.error( ioe );
	    return;
	}
	if( (updContent == null) || (updContent.trim().length() <= 0 ) ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: file content is empty" );
	    log.error( upFile+" content is empty" );
	    return;
	}
	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Upload received: "+upFile.getName() );

	InventoryUploadTemplate templ = getTemplate( wnd );
	if( templ == null ) {
	    String msg = "Error: Cannot determine template";
	    log.error( msg );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", msg );
	    return;
	}

	User user = validateUser( wnd, Roles.INVENTORY_UPLOAD );
	if( user == null ) 
	    return;

	UploadBatch uBatch = createUploadBatch( templ, updContent );
	templ.addUploadBatch( uBatch );
	log.info( "Upload batch registered: "+uBatch.getUploadid()+" content length: "+updContent.length() );

	try {
	    SampleInventoryDAO dao = getSampleInventory();
	    templ = dao.storeTemplate( templ );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return;
	}

	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Batch registered, starting upload "+upFile.getName()+"..." );
	try {
	    runUpload( wnd, user, templ, uBatch.getUploadid() );
	}
	catch( UploadException uex ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( uex.getMessage(), "General upload error" ) );
	    log.error( uex );
	    return;
	}

	updateLog( wnd, templ, uBatch.getUploadid() );
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
    }        
} 
