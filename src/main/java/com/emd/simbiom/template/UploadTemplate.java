package com.emd.simbiom.template;

/**
 * The <code>UploadTemplate</code> action populates the result log selector.
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
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.User;

import com.emd.simbiom.upload.InventoryUploadTemplate;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

public class UploadTemplate extends InventoryCommand {

    private static Log log = LogFactory.getLog(UploadTemplate.class);

    private static final String MIME_JSON = "application/json";

    /**
     * Creates a new command to upload samples from a file.
     */
    public UploadTemplate() {
	super();
    }

    // private InventoryPreferences getPreferences() {
    // 	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    // }

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

	    if( !MIME_JSON.equals(media.getContentType() ) )
		throw new IOException( "Unable to upload format "+
				       Stringx.getDefault(media.getContentType(),"unknown") );

	    tempF = createUploadFile( media.getName() );
	    if( tempF == null )
		throw new IOException( "Cannot prepare upload file" );
	    FileWriter fw = new FileWriter( tempF ); 
	    Reader r = new InputStreamReader( media.getStreamData() );
	    IOUtils.copy( r, fw );
	    r.close();
	    fw.close();
	    log.debug( "Temporary upload file created: "+tempF );
	    return tempF;
	}
	else
	    throw new IOException( "No upload detected" );
    }

    // private User validateUser( Window wnd, long reqRole ) {
    // 	User user = null;
    // 	try {
    // 	    SampleInventoryDAO dao = getSampleInventory();
    // 	    user = dao.findUserById( getUserId() );
    // 	}
    // 	catch( SQLException sqe ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
    // 			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
    // 	    log.error( sqe );
    // 	    return null;
    // 	}

    // 	if( user == null ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User id "+
    // 			 getUserId()+" is unknown" );
    // 	    return null;
    // 	}

    // 	if( !user.hasRole( reqRole ) ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User "+
    // 			 user+" requires permission \""+Roles.roleToString( reqRole )+"\"" );
    // 	    return null;
    // 	}
    // 	return user;
    // }

    // private List<InventoryUploadTemplate> readFromJSON( Window wnd, File templF ) {
    // 	ObjectMapper mapper = new ObjectMapper();
    // 	TypeReference<List<InventoryUploadTemplate>> typeReference = new TypeReference<List<InventoryUploadTemplate>>(){};
    // 	List<InventoryUploadTemplate> templates = null;
    // 	try {
    // 	    templates = mapper.readValue( templF, typeReference );
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Templates read from "+templF+": "+templates.size() );
    // 	    log.debug( templates.size()+" template(s) read from "+templF );
    // 	} 
    // 	catch (IOException ioe) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
    // 			 Stringx.getDefault( ioe.getMessage(), "Cannot read templates from "+templF ) );
    // 	    log.error( ioe );
    // 	}
    // 	return templates;
    // }

    private InventoryUploadTemplate readFromJSON( Window wnd, File templF ) {
	ObjectMapper mapper = new ObjectMapper();
	mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
	TypeReference<InventoryUploadTemplate> typeReference = new TypeReference<InventoryUploadTemplate>(){};
	InventoryUploadTemplate template = null;
	try {
	    template = mapper.readValue( templF, typeReference );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Template read from "+templF+": "+
			 Stringx.getDefault(template.getTemplatename(),"") );
	    log.debug( "Template "+template.getTemplatename()+" read from "+templF );
	} 
	catch (IOException ioe) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "Cannot read templates from "+templF ) );
	    log.error( ioe );
	}
	return template;
    }

    private void setTemplateName( Window wnd, String newName ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATENAME );
	if( txtTempl != null ) {
	    txtTempl.setValue( newName );
	}
    }

    private void setEditArea( Window wnd, String cont  ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATE );
	if( txtTempl != null )
	    txtTempl.setValue( cont.trim().replace( "\\n", "" ) );
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
	    // updContent = FileUtils.readFileToString( upFile );
	}
	catch( IOException ioe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error" ) );
	    log.error( ioe );
	    return;
	}

	// List<InventoryUploadTemplate> templates = readFromJSON( wnd, upFile );
	// if( (templates == null) || (templates.size() < 0) )
	//     return;
	// InventoryUploadTemplate templ = templates.get(0);

	InventoryUploadTemplate templ = readFromJSON( wnd, upFile );
	if( templ == null )
	    return;

	setTemplateName( wnd, templ.getTemplatename() );
	setEditArea( wnd, templ.getTemplate() );
	
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

                       // read JSON and load json
                        // ObjectMapper mapper = new ObjectMapper();
                        // TypeReference<List<User>> typeReference = new TypeReference<List<User>>(){};
                        // InputStream inputStream = TypeReference.class.getResourceAsStream("/json/users.json");
                        // try {
                        //         List<User> users = mapper.readValue(inputStream,typeReference);
                        //         userService.save(users);
                        //         System.out.println("Users Saved!");
                        // } catch (IOException e){
                        //         System.out.println("Unable to save users: " + e.getMessage());
                        // }

} 
