package com.emd.simbiom.report;

import java.sql.Timestamp;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
// import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
// import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import org.zkoss.zul.impl.InputElement;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.User;

import com.emd.simbiom.upload.InventoryUploadTemplate;
import com.emd.simbiom.upload.UploadBatch;
import com.emd.simbiom.upload.UploadException;
import com.emd.simbiom.upload.UploadProcessor;

import com.emd.util.Stringx;

import com.emd.vutils.report.ReportStrategy;


/**
 * <code>ReportEngine</code> runs a report.
 *
 * Created: Sun Dec 22 12:53:21 2019
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class ReportEngine extends InventoryCommand {
    private String componentPrefix;

    private static Log log = LogFactory.getLog(ReportEngine.class);

    public ReportEngine( String cmpPrefix, long userId, String portletId) {
	super();
	this.setPortletId( portletId );
	this.setUserId( userId );
	this.componentPrefix = cmpPrefix;
	this.setMessageRowId( "row"+cmpPrefix+"Message" );
    }

    /**
     * Get the <code>ComponentPrefix</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getComponentPrefix() {
	return componentPrefix;
    }

    /**
     * Set the <code>ComponentPrefix</code> value.
     *
     * @param componentPrefix The new ComponentPrefix value.
     */
    public final void setComponentPrefix(final String componentPrefix) {
	this.componentPrefix = componentPrefix;
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private String[] addHeader( Grid grid, StringBuilder stb ) {
	TreeMap<Integer,String> columnOrder = new TreeMap<Integer,String>();
	Collection<Component> children = grid.getFellows();
	for( Component cmp : children ) {
	    String cmpId = cmp.getId();
	    if( (cmpId != null) && (cmpId.startsWith( "cmp"+getComponentPrefix()+"InputColumn_" )) ) {
		int idx = Stringx.toInt(StringUtils.substringBetween( cmpId, "_" ),-1);
		String colName = StringUtils.substringAfterLast( cmpId, "_" );
		columnOrder.put( new Integer(idx), colName );
		log.debug( "Input column: "+cmpId+" idx: "+idx+" column: "+colName );
	    }
	}
	Set<Integer> keys = columnOrder.keySet();
	List<String> colNames = new ArrayList<String>();
	boolean addDelim = false;
	for( Integer idx : keys ) {
	    if( !addDelim )
		addDelim = true;
	    else
		stb.append( "|" );
	    String colName = Stringx.getDefault( columnOrder.get(idx),"Unknown" );
	    stb.append( colName );
	    colNames.add( colName );
	}
	if( addDelim )
	    stb.append( "|" );
	stb.append( "Output Columns\n" );

	String[] header = new String[ colNames.size() ];
	return (String[])colNames.toArray( header );
    }

    private void addReportParameters( Grid grid, StringBuilder stb, String[] inputCols, String[] outputCols ) {
	for( int i = 0; i < inputCols.length; i++ ) {
	    Collection<Component> children = grid.getFellows();
	    for( Component cmp : children ) {
		String cmpId = cmp.getId();
		if( (cmpId != null) && (cmpId.startsWith( "cmp"+getComponentPrefix()+"InputColumn_" )) && 
		    (cmpId.endsWith( "_"+inputCols[i])) &&
		    (cmp instanceof InputElement) ) {

		    String val = Stringx.getDefault(((InputElement)cmp).getText(), "" );

		    log.debug( "Input column: "+cmpId+" column: "+inputCols[i]+" value: "+val );
		    if( i > 0 )
			stb.append( "|" );
		    stb.append( val );
		}
	    }
	}
	if( outputCols != null ) {
	    stb.append( "|" );
	    for( int i = 0; i < outputCols.length; i++ ) {
		if( i > 0 )
		    stb.append( "," );
		stb.append( outputCols[i] );
	    }
	}
    }

    private String[] getOutputColumns( Grid grid ) {
	Collection<Component> children = grid.getFellows();
	Map<Integer,String> outColumns = new HashMap<Integer,String>();
	for( Component cmp : children ) {
	    String cmpId = cmp.getId();
	    if( (cmpId != null) && 
		(cmpId.startsWith( "chk"+getComponentPrefix()+"_" )) &&
		(cmp instanceof Checkbox) ) {

		int idx = Stringx.toInt(StringUtils.substringBetween( cmpId, "_" ),-1);
		String colName = StringUtils.substringAfterLast( cmpId, "_" );

		Spinner sp = (Spinner)grid.getFellowIfAny( "sp"+getComponentPrefix()+"_"+idx+"_"+colName ); 
		if( sp != null )
		    idx = sp.getValue();
		
		if( ((Checkbox)cmp).isChecked() )
		    outColumns.put( new Integer(idx), colName );

		log.debug( "Output column: "+cmpId+" idx: "+idx+" column: "+colName );
	    }
	}
	String[] outputCols = new String[ outColumns.size() ];
	int j = 0;
	for( int i = 0; i < outColumns.size(); i++ ) {
	    String outCol = outColumns.get( new Integer(i+1) );
	    if( outCol != null ) {
		outputCols[j] = outCol;
		j++;
	    }
	}
	return outputCols;
    }

    private UploadBatch createUploadBatch( Window wnd, InventoryUploadTemplate templ ) {
     	UploadBatch uBatch = new UploadBatch();
     	uBatch.setTemplateid( templ.getTemplateid() );

	// collect output columns

	Grid grid = (Grid)wnd.getFellowIfAny( "gr"+getComponentPrefix()+"OutputColumns" );
	String[] outColumns = null;
	if( grid != null ) {
	    outColumns = getOutputColumns( grid );
	    log.debug( "Number of output columns: "+outColumns.length );
	}
	else 
	    outColumns = new String[0];

	// collect input columns

	grid = (Grid)wnd.getFellowIfAny( "gr"+getComponentPrefix()+"InputColumns" );
	StringBuilder updContent = new StringBuilder();
	String[] columns = null;
	if( grid != null ) {
	    columns = addHeader( grid, updContent );
	    addReportParameters( grid, updContent, columns, outColumns );
	}

	if( updContent.length() > 0 ) {
	    uBatch.setUpload( updContent.toString() );
	    log.debug( "Upload content: "+updContent.toString() );
	}
	uBatch.setUploaded( new Timestamp(System.currentTimeMillis()) );
	uBatch.setUserid( getUserId() );
	return uBatch;
    }

    private boolean storeUploadBatch( Window wnd, UploadBatch uBatch, InventoryUploadTemplate templ ) {
     	templ.addUploadBatch( uBatch );
     	log.info( "Upload batch registered: "+uBatch.getUploadid() );

	try {
	    SampleInventory dao = getSampleInventory();
     	    templ = dao.storeTemplate( templ );
     	}
	catch( SQLException sqe ) {
     	    writeMessage( wnd, "Error: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
     	    log.error( sqe );
     	    return false;
	}
	return true;
    }

    private String createReportName( Window wnd, InventoryUploadTemplate templ ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txt"+getComponentPrefix()+"Name" );
	String repName = null;
	if( txt != null ) 
     	    repName = Stringx.getDefault( txt.getValue(), "" );
     	else
     	    repName = "";

     	if( repName.trim().length() <= 0 )
     	    repName = templ.getTemplatename()+" "+Stringx.currentDateString(  "dd-MMM-YYYY" );
	return repName;
    }

    private void runUpload( Window wnd,
			    User user, 
      			    InventoryUploadTemplate templ, 
     			    long batchId )
     	throws UploadException {

      	UploadProcessor uProcessor = UploadProcessor.getInstance();
     	Map ctxt = new HashMap();
     	ctxt.put( "user", user );
	ReportStrategy strategy = ReportStrategies.getInstance( templ.getTemplatename() );
	if( strategy != null )
	    ctxt.put( "reportStrategy", strategy );
	ctxt.put( "reportName", createReportName( wnd, templ ) );

     	uProcessor.processUpload( templ, batchId, ctxt );
    }	

    /**
     * Runs the report.
     *
     * @param wnd The app window.
     * @param templ The report template.
     */
    public UploadBatch runReport( Window wnd, User user, InventoryUploadTemplate templ ) {
	UploadBatch uBatch = createUploadBatch( wnd, templ );

	if( !storeUploadBatch( wnd, uBatch, templ ) )
	    return null;

	writeMessage( wnd, "Batch registered, starting report job "+uBatch.getUploadid()+"..." );
     	try {
     	    runUpload( wnd, user, templ, uBatch.getUploadid() );
     	}
     	catch( UploadException uex ) {
     	    writeMessage( wnd, "Error: "+Stringx.getDefault( uex.getMessage(), "General upload error" ) );
     	    log.error( uex );
     	    return null;
     	}
	writeMessage( wnd, "Success: Report job "+uBatch.getUploadid()+" run." );
	return uBatch;
    }
}
