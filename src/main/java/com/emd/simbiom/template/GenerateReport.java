package com.emd.simbiom.template;

import java.sql.Timestamp;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.util.media.Media;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import org.zkoss.zul.impl.InputElement;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.simbiom.model.StorageDocument;
import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.User;

import com.emd.simbiom.report.ReportDetailsView;
import com.emd.simbiom.report.ReportEngine;
import com.emd.simbiom.report.ReportStrategies;

import com.emd.simbiom.upload.InventoryUploadTemplate;
import com.emd.simbiom.upload.UploadBatch;
import com.emd.simbiom.upload.UploadException;
import com.emd.simbiom.upload.UploadProcessor;

import com.emd.util.Stringx;

import com.emd.vutils.report.ReportStrategy;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>GenerateReport</code> action runs reports and generates output accordingly.
 *
 * Created: Mon Oct 14 2019 19:44:39 
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class GenerateReport extends InventoryCommand {
    private String reportName;

    private static Log log = LogFactory.getLog(GenerateReport.class);

    /**
     * Creates a new command to generate report output.
     */
    public GenerateReport() {
	super();
	this.reportName = null;
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    // private File createUploadFile( Window wnd, InventoryUploadTemplate templ ) 
    //  	throws IOException {


    //  	File tempF = File.createTempFile( "simbiom", null );
    //  	File dir = new File( tempF.getParentFile(), "simbiom" );
    //  	if( !dir.exists() && !dir.mkdir() ) {
    //  	    log.error( "Cannot create upload directory: "+dir );
    //  	    return null;
    //  	}

    //  	dir = new File( dir, String.valueOf(getUserId()) );
    //  	if( !dir.exists() && !dir.mkdir() ) {
    //  	    log.error( "Cannot create upload directory: "+dir );
    //  	    return null;
    //  	}

    //  	return new File( dir, repName+".out" );
    // }

    // private File storeUpload( Event event ) throws IOException {
    // 	File tempF = null;
    // 	if( event instanceof UploadEvent ) {
    // 	    Media media = ((UploadEvent)event).getMedia();
    // 	    log.debug( "Upload file name: "+Stringx.getDefault(media.getName(),"unknown")+
    // 		       " format: "+Stringx.getDefault(media.getContentType(),"unknown")+
    // 		       " ("+Stringx.getDefault(media.getFormat(),"unknown")+
    // 		       ") binary: "+media.isBinary()+" in memory: "+media.inMemory() );

    // 	    if( media.isBinary() ) 
    // 		throw new IOException( "Unable to upload binary file" );

    // 	    tempF = createUploadFile( media.getName() );
    // 	    if( tempF == null )
    // 		throw new IOException( "Cannot prepare upload file" );
    // 	    FileWriter fw = new FileWriter( tempF ); 
    // 	    Reader r = media.getReaderData();
    // 	    IOUtils.copy( media.getReaderData(), fw );
    // 	    r.close();
    // 	    fw.close();
    // 	    log.debug( "Temporary upload file created: "+tempF );
    // 	    return tempF;
    // 	}
    // 	else
    // 	    throw new IOException( "No upload detected" );
    // }

    private InventoryUploadTemplate getTemplate( Window wnd ) {
	ModelProducer[] mp = getPreferences().getResult( TemplateModel.class );
	if( mp.length <= 0 )
	    return null;
	return ((TemplateModel)mp[0]).getSelectedTemplate( wnd );
    }

    private InventoryUploadTemplate loadTemplate( Window wnd, String rName ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: Invalid database access" );
	    return null;
	}

	try {
	    InventoryUploadTemplate[] tList = dao.findTemplateByName( rName );
	    log.debug( "Reports matching: "+tList.length );
	    if( tList.length > 0 )
		return tList[0];
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	}
	return null;
    }

// "cmpInputColumn_"+i+"_"+inColumn

    // private String[] addHeader( Grid grid, StringBuilder stb ) {
    // 	TreeMap<Integer,String> columnOrder = new TreeMap<Integer,String>();
    // 	Collection<Component> children = grid.getFellows();
    // 	for( Component cmp : children ) {
    // 	    String cmpId = cmp.getId();
    // 	    if( (cmpId != null) && (cmpId.startsWith( "cmpInputColumn_" )) ) {
    // 		int idx = Stringx.toInt(StringUtils.substringBetween( cmpId, "_" ),-1);
    // 		String colName = StringUtils.substringAfterLast( cmpId, "_" );
    // 		columnOrder.put( new Integer(idx), colName );
    // 		log.debug( "Input column: "+cmpId+" idx: "+idx+" column: "+colName );
    // 	    }
    // 	}
    // 	Set<Integer> keys = columnOrder.keySet();
    // 	List<String> colNames = new ArrayList<String>();
    // 	boolean addDelim = false;
    // 	for( Integer idx : keys ) {
    // 	    if( !addDelim )
    // 		addDelim = true;
    // 	    else
    // 		stb.append( "|" );
    // 	    String colName = Stringx.getDefault( columnOrder.get(idx),"Unknown" );
    // 	    stb.append( colName );
    // 	    colNames.add( colName );
    // 	}
    // 	if( addDelim )
    // 	    stb.append( "|" );
    // 	stb.append( "Output Columns\n" );

    // 	String[] header = new String[ colNames.size() ];
    // 	return (String[])colNames.toArray( header );
    // }

	// chk.setId( "chkOutputColumns_"+i+"_"+colName );

    // private String[] getOutputColumns( Grid grid ) {
    // 	Collection<Component> children = grid.getFellows();
    // 	Map<Integer,String> outColumns = new HashMap<Integer,String>();
    // 	for( Component cmp : children ) {
    // 	    String cmpId = cmp.getId();
    // 	    if( (cmpId != null) && 
    // 		(cmpId.startsWith( "chkOutputColumns_" )) &&
    // 		(cmp instanceof Checkbox) ) {

    // 		int idx = Stringx.toInt(StringUtils.substringBetween( cmpId, "_" ),-1);
    // 		String colName = StringUtils.substringAfterLast( cmpId, "_" );

    // 		Spinner sp = (Spinner)grid.getFellowIfAny( "spOutputColumns_"+idx+"_"+colName ); 
    // 		if( sp != null )
    // 		    idx = sp.getValue();
		
    // 		if( ((Checkbox)cmp).isChecked() )
    // 		    outColumns.put( new Integer(idx), colName );

    // 		log.debug( "Output column: "+cmpId+" idx: "+idx+" column: "+colName );
    // 	    }
    // 	}
    // 	String[] outputCols = new String[ outColumns.size() ];
    // 	int j = 0;
    // 	for( int i = 0; i < outColumns.size(); i++ ) {
    // 	    String outCol = outColumns.get( new Integer(i+1) );
    // 	    if( outCol != null ) {
    // 		outputCols[j] = outCol;
    // 		j++;
    // 	    }
    // 	}
    // 	return outputCols;
    // }

    // private void addReportParameters( Grid grid, StringBuilder stb, String[] inputCols, String[] outputCols ) {
    // 	for( int i = 0; i < inputCols.length; i++ ) {
    // 	    Collection<Component> children = grid.getFellows();
    // 	    for( Component cmp : children ) {
    // 		String cmpId = cmp.getId();
    // 		if( (cmpId != null) && (cmpId.startsWith( "cmpInputColumn_" )) && 
    // 		    (cmpId.endsWith( "_"+inputCols[i])) &&
    // 		    (cmp instanceof InputElement) ) {

    // 		    String val = Stringx.getDefault(((InputElement)cmp).getText(), "" );

    // 		    log.debug( "Input column: "+cmpId+" column: "+inputCols[i]+" value: "+val );
    // 		    if( i > 0 )
    // 			stb.append( "|" );
    // 		    stb.append( val );
    // 		}
    // 	    }
    // 	}
    // 	if( outputCols != null ) {
    // 	    stb.append( "|" );
    // 	    for( int i = 0; i < outputCols.length; i++ ) {
    // 		if( i > 0 )
    // 		    stb.append( "," );
    // 		stb.append( outputCols[i] );
    // 	    }
    // 	}
    // }

    // private UploadBatch createUploadBatch( Window wnd, InventoryUploadTemplate templ ) {
    //  	UploadBatch uBatch = new UploadBatch();
    //  	uBatch.setTemplateid( templ.getTemplateid() );

    // 	// collect output columns

    // 	Grid grid = (Grid)wnd.getFellowIfAny( TemplateModel.CMP_REPORT_OUTPUT );
    // 	String[] outColumns = null;
    // 	if( grid != null ) {
    // 	    outColumns = getOutputColumns( grid );
    // 	    log.debug( "Number of output columns: "+outColumns.length );
    // 	}
    // 	else 
    // 	    outColumns = new String[0];

    // 	// collect input columns

    // 	grid = (Grid)wnd.getFellowIfAny( TemplateModel.CMP_REPORT_INPUT );
    // 	StringBuilder updContent = new StringBuilder();
    // 	String[] columns = null;
    // 	if( grid != null ) {
    // 	    columns = addHeader( grid, updContent );
    // 	    addReportParameters( grid, updContent, columns, outColumns );
    // 	}

    // 	if( updContent.length() > 0 ) {
    // 	    uBatch.setUpload( updContent.toString() );
    // 	    log.debug( "Upload content: "+updContent.toString() );
    // 	}
    // 	uBatch.setUploaded( new Timestamp(System.currentTimeMillis()) );
    // 	uBatch.setUserid( getUserId() );
    // 	return uBatch;
    // }

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

    // private String createReportName( Window wnd, InventoryUploadTemplate templ ) {
    // 	Textbox txt = (Textbox)wnd.getFellowIfAny( TemplateModel.CMP_REPORT_NAME );
    // 	String repName = null;
    // 	if( txt != null ) 
    //  	    repName = Stringx.getDefault( txt.getValue(), "" );
    //  	else
    //  	    repName = "";

    //  	if( repName.trim().length() <= 0 )
    //  	    repName = templ.getTemplatename()+" "+Stringx.currentDateString(  "dd-MMM-YYYY" );
    // 	return repName;
    // }

    // private void runUpload( Window wnd,
    // 			    User user, 
    //   			    InventoryUploadTemplate templ, 
    //  			    long batchId )
    //  	throws UploadException {

    //   	UploadProcessor uProcessor = UploadProcessor.getInstance();
    //  	Map ctxt = new HashMap();
    //  	ctxt.put( "user", user );
    // 	ReportStrategy strategy = ReportStrategies.getInstance( templ.getTemplatename() );
    // 	if( strategy != null )
    // 	    ctxt.put( "reportStrategy", strategy );
    // 	ctxt.put( "reportName", createReportName( wnd, templ ) );
    //  	uProcessor.processUpload( templ, batchId, ctxt );
    // }	

    private void updateLog( Window wnd, InventoryUploadTemplate templ, long uploadid ) {
     	OpenResultLog orl = (OpenResultLog)getPreferences().getCommand( OpenResultLog.class );
     	if( orl != null ) {
	    try {
     		orl.initLogs( wnd, templ );
     	    }
     	    catch( SQLException sqe ) {
     		writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
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

    private OutputSelector getOutputSelector() {
	ModelProducer[] mp = getPreferences().getResult( OutputSelector.class );
	if( mp.length <= 0 )
	    return null;
	return (OutputSelector)mp[0];
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

    // private boolean storeUploadBatch( Window wnd, UploadBatch uBatch, InventoryUploadTemplate templ ) {
    //  	templ.addUploadBatch( uBatch );
    //  	log.info( "Upload batch registered: "+uBatch.getUploadid() );

    // 	try {
    // 	    SampleInventory dao = getSampleInventory();
    //  	    templ = dao.storeTemplate( templ );
    //  	}
    // 	catch( SQLException sqe ) {
    //  	    writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
    //  	    log.error( sqe );
    //  	    return false;
    // 	}
    // 	return true;
    // }

    private Map createContext( Window wnd, InventoryUploadTemplate templ ) {
     	Map ctxt = new HashMap();

    // 	InvoiceDetails det = new InvoiceDetails( invoice );
    // 	det.setInvoiceExist( invoiceExist );

    // 	SampleInventory dao = getSampleInventory();
    // 	if( dao != null ) {
    //         try {
    // 		StorageProject[] prjs = dao.findStorageProject( null );
    // 		det.setAllProjects( prjs );
    // 		Billing[] allPOs = dao.findBilling( 0L, null );
    // 		det.setAllPurchases( allPOs );
    // 	    }
    // 	    catch( SQLException sqe ) {
    // 		showMessage( wnd, "rowMessage", "lbMessage", "Error: "+Stringx.getDefault( sqe.getMessage(), "General SQL error" ) );
    // 		log.error( sqe );
    // 	    }
    // 	}
    // 	else
    // 	    showMessage( wnd, "rowMessage", "lbMessage", "Error: invalid database access" );

     	ctxt.put( ReportDetailsView.TEMPLATE, templ );
     	ctxt.put( ReportDetailsView.EVENT_LISTENER, this );
	ctxt.put( ReportDetailsView.USERID, new Long(getUserId()) );
	ctxt.put( ReportDetailsView.PORTLETID, getPortletId());

	ctxt.put( "dates", DateUtils.class );
     	ctxt.put( "dateFormats", DateFormatUtils.class );

    // 	// ctxt.put( "db", getSampleInventoryDAO.getInstance() );
     	return ctxt;
    }

    private Window createWindow( Window wnd, String title, ReportDetailsView vDetails ) {
     	Window wndDetails = new Window();
     	wndDetails.setParent( wnd );
     	wndDetails.setId( "wndReportDetails" );
     	wndDetails.setTitle( title );
     	wndDetails.setBorder( "normal" );
     	wndDetails.setWidth( "900px" );
     	wndDetails.setPosition( "center,center" );
     	wndDetails.setClosable( true );
	// action="show: slideDown;hide: slideUp"

     	Vlayout vl = new Vlayout();
     	vl.setId( vDetails.getDetailsLayout() );
     	vl.setParent( wndDetails );

     	return wndDetails;
    }

    private ReportDetailsView createDetailsView() {
	ModelProducer[] res = getPreferences().getResult( TemplateModel.class );
	if( res.length <= 0 )
	    return null;
	return ((TemplateModel)res[0]).getDetails();
    }

    private void displayReportDetails( Window wnd, InventoryUploadTemplate templ ) {
     	log.debug( "Display report details of "+templ );

     	ReportDetailsView vDetails = createDetailsView();
     	if( vDetails == null ) {
     	    showMessage( wnd, "rowMessage", "lbMessage", "Error: Cannot create report details" );
     	    log.error( "Cannot create view, check configuration" );
     	    return;
     	}

     	Window wndDetails = createWindow( wnd, templ.getTemplatename(), vDetails );
     	final Window parentWindow = wnd;
     	wndDetails.addEventListener( Events.ON_CLOSE, new EventListener() {
     		public void onEvent( Event evt ) {
     		    log.debug( "Closing event received" );
    // 		    Period period = (Period)evt.getData();
    // 		    if( period != null )
    // 			updateModel( parentWindow, period );
    // 		    else
    // 			log.debug( "No change of invoice period" );
     		}
     	    });

     	vDetails.updateActions( getPortletId(), getUserId() );
     	// vDetails.setTemplate( getDetailsTemplate() );
	vDetails.setShowMergeResult( true );
     	vDetails.initView( wndDetails, createContext(wnd, templ) );
	
     	wndDetails.doModal();
    }

    /**
     * Get the <code>ReportName</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getReportName() {
	return reportName;
    }

    /**
     * Set the <code>ReportName</code> value.
     *
     * @param reportName The new ReportName value.
     */
    public final void setReportName(final String reportName) {
	this.reportName = reportName;
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    // public void onEvent( Event event )	throws Exception {
    // 	File upFile = null;
    // 	String updContent = null;
	
    // 	Window wnd = UIUtils.getWindow( event );
    // 	try {
    // 	    upFile = storeUpload( event );
    // 	    updContent = FileUtils.readFileToString( upFile );
    // 	}
    // 	catch( IOException ioe ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
    // 			 Stringx.getDefault( ioe.getMessage(), "General I/O error" ) );
    // 	    log.error( ioe );
    // 	    return;
    // 	}
    // 	if( (updContent == null) || (updContent.trim().length() <= 0 ) ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: file content is empty" );
    // 	    log.error( upFile+" content is empty" );
    // 	    return;
    // 	}
    // 	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Upload received: "+upFile.getName() );

    // 	InventoryUploadTemplate templ = getTemplate( wnd );
    // 	if( templ == null ) {
    // 	    String msg = "Error: Cannot determine template";
    // 	    log.error( msg );
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", msg );
    // 	    return;
    // 	}

    // 	User user = validateUser( wnd, Roles.INVENTORY_UPLOAD );
    // 	if( user == null ) 
    // 	    return;

    // 	UploadBatch uBatch = createUploadBatch( templ, updContent );
    // 	templ.addUploadBatch( uBatch );
    // 	log.info( "Upload batch registered: "+uBatch.getUploadid()+" content length: "+updContent.length() );

    // 	try {
    // 	    // SampleInventoryDAO dao = getSampleInventory();
    // 	    SampleInventory dao = getSampleInventory();
    // 	    templ = dao.storeTemplate( templ );
    // 	}
    // 	catch( SQLException sqe ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
    // 			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
    // 	    log.error( sqe );
    // 	    return;
    // 	}

    // 	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Batch registered, starting upload "+upFile.getName()+"..." );
    // 	try {
    // 	    runUpload( wnd, user, templ, uBatch.getUploadid() );
    // 	}
    // 	catch( UploadException uex ) {
    // 	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
    // 			 Stringx.getDefault( uex.getMessage(), "General upload error" ) );
    // 	    log.error( uex );
    // 	    return;
    // 	}

    // 	updateLog( wnd, templ, uBatch.getUploadid() );
    // }

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

	log.debug( "Report generation initiated." );

	User user = validateUser( wnd, Roles.INVENTORY_REPORT );
	if( user == null ) 
     	    return;

	InventoryUploadTemplate templ = null;
	String rName = this.getReportName();
	boolean createWindow = false;
	if( rName != null ) {
	    templ = loadTemplate( wnd, rName );
	    createWindow = true;
	}
	else {
	    templ = getTemplate( wnd );
	}

	if( templ == null ) {
	    String msg = "Error: Cannot determine template";
	    log.error( msg );
	    writeMessage( wnd, msg );
	    return;
	}

	if( createWindow ) {
	    displayReportDetails( wnd, templ );
	    return;
	}

	ReportEngine repEngine = new ReportEngine( TemplateModel.CMP_REPORT_PREFIX, getUserId(), getPortletId() );
	UploadBatch uBatch = repEngine.runReport( wnd, user, templ );
	if( uBatch == null )
	    return;

	// UploadBatch uBatch = createUploadBatch( wnd, templ );

	// if( !storeUploadBatch( wnd, uBatch, templ ) )
	//     return;

	// writeMessage( wnd, "Batch registered, starting upload "+uBatch.getUploadid()+"..." );
     	// try {
     	//     runUpload( wnd, user, templ, uBatch.getUploadid() );
     	// }
     	// catch( UploadException uex ) {
     	//     writeMessage( wnd, "Error: "+Stringx.getDefault( uex.getMessage(), "General upload error" ) );
     	//     log.error( uex );
     	//     return;
     	// }

	updateOutputs( wnd, templ );
     	updateLog( wnd, templ, uBatch.getUploadid() );

    }        
} 
