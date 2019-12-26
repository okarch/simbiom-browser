package com.emd.simbiom.report;

import java.io.IOException;
import java.io.StringReader;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;

import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.StorageDocument;

import com.emd.simbiom.upload.InventoryUploadTemplate;

import com.emd.simbiom.template.OutputSelector;

import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;

import com.emd.zk.view.VelocityView;
import com.emd.zk.view.ViewAction;

import com.emd.util.Parameter;

/**
 * <code>ReportDetailsView</code> supports configuration of report details.
 *
 * Created: Sun Dec  1 07:53:49 2019
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class ReportDetailsView extends VelocityView implements EventListener {
    private String               portletId;
    private long                 userId;
    private String messageRowId;
    private String detailsLayout;

    private static Log log = LogFactory.getLog(ReportDetailsView.class);

    public static final String TAG_HEADER       = "## Header structure supported:";
    public static final String TAG_OUTPUT       = "## Output columns supported:";
    public static final String TAG_END          = "## ##########";

    public static final String CMP_REPORT_OUTPUT= "grReportDetailsOutputColumns";
    public static final String CMP_REPORT_INPUT = "grReportDetailsInputColumns";
    public static final String CMP_REPORT_GROUP = "vlReportDetailsGroup_0";
    public static final String CMP_REPORT_PARAM = "vlReportDetailsParameter_0";
    public static final String CMP_REPORT_NAME  = "txtReportDetailsName";
    public static final String CMP_REPORT_GENERATE = "btReportDetailsGenerate";

    public static final String CMP_REPORT_PREFIX= "ReportDetails";

    public static final String EVENT_LISTENER   = "eventListener";
    public static final String PORTLETID        = "portletId";
    public static final String TEMPLATE         = "reportTemplate";
    public static final String USERID           = "userId";

    public ReportDetailsView() {
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
	this.setPortletId( portletId );
	this.setUserId( userId );
    }

    private void updateTemplate( InventoryUploadTemplate templ ) {
	ViewAction[] acts = getActions();
	for( int i = 0; i < acts.length; i++ ) {
	    if( acts[i] instanceof ReportDetailsAction ) 
		((ReportDetailsAction)acts[i]).setTemplate( templ );
	}
    }

    /**
     * Get the PortletId value.
     * @return the PortletId value.
     */
    public String getPortletId() {
	return portletId;
    }

    /**
     * Set the PortletId value.
     * @param newPortletId The new PortletId value.
     */
    public void setPortletId(String newPortletId) {
	this.portletId = newPortletId;
    }

    /**
     * Get the UserId value.
     * @return the UserId value.
     */
    public long getUserId() {
	return userId;
    }

    /**
     * Set the UserId value.
     * @param newUserId The new UserId value.
     */
    public void setUserId(long newUserId) {
	this.userId = newUserId;
    }

    /**
     * Creates a grid listing report output options
     *
     * @param cmpPrefix the prefix for component ids generated.
     * @param columns array of column descriptions.
     * @param evtListener the event listener.
     *
     * @return a grid containing the output column options.
     */
    public static Grid createOutputOptions( String cmpPrefix, String[] columns, EventListener evtListener ) {
	Grid grid = new Grid();
	grid.setId( "gr"+cmpPrefix+"OutputColumns" );
	grid.setFixedLayout( true );
	grid.setSpan( true );

	// <columns>
	Columns cols = new Columns();
	for( int i = 0; i < 4; i++ ) {
	    Column col = new Column();
	    col.setHflex( "min" );
	    col.setParent( cols );
	}
	cols.setParent( grid );

	// <rows>
	Rows rows = new Rows();
	Row row = null;
	int k = -1;
	int nOrder = 0;
	for( int i = 0; i < columns.length; i++) {
	    if( ((i+4) % 4) == 0 ) {
		row = new Row();
		row.setParent( rows );
	    }
	    String colName = columns[i];
	    boolean checked = false;
	    if( columns[i].endsWith( "*" ) ) {
		colName = columns[i].substring(0,columns[i].length()-1).trim();
		checked = true;
	    }
	    if( (k = colName.indexOf( "[" )) > 0 )
		colName = colName.substring( 0, k ).trim();
	    Hlayout hl = new Hlayout();
	    hl.setParent( row );
	    Checkbox chk = new Checkbox();
	    chk.setId( "chk"+cmpPrefix+"_"+i+"_"+colName );
	    chk.setChecked( checked );
	    chk.addEventListener( Events.ON_CHECK, evtListener );
	    chk.setParent( hl );
	    Spinner spOrder = new Spinner( 0 );
	    if( checked ) {
		nOrder++;
		spOrder.setValue( nOrder ); 
	    }
	    else
		spOrder.setDisabled( true );
	    spOrder.setWidth( "50px" );
	    spOrder.setId( "sp"+cmpPrefix+"_"+i+"_"+colName );
	    spOrder.setParent( hl );
	    Label lb = new Label( colName );
	    lb.setParent( hl );
	}
	rows.setParent( grid );
	return grid;
    }

    /**
     * Creates a grid listing report output options
     *
     * @param cmpPrefix the prefix for component ids generated.
     * @param inColumns array of input column descriptions.
     * @param outColumns array of output column descriptions.
     * @param evtListener the event listener.
     *
     * @return a grid containing the output column options.
     */
    public static Grid createInputOptions( String cmpPrefix, 
					   String[] inColumns, 
					   String[] outColumns,
					   EventListener evtListener ) {
	Grid grid = new Grid();
	grid.setId( "gr"+cmpPrefix+"InputColumns" );
	grid.setFixedLayout( true );
	grid.setSpan( true );

	// <columns>
	Columns cols = new Columns();
	for( int i = 0; i < 2; i++ ) {
	    Column col = new Column();
	    col.setHflex( "min" );
	    col.setParent( cols );
	}
	cols.setParent( grid );

	// sort the colspecs first

	TreeMap<Integer,List<String>> sortedSpecs = new TreeMap<Integer,List<String>>();
	Map<String,Integer> inputOrder = new HashMap<String,Integer>();
	int nIdx = 0;
	int orderIndex = 0;
	for( int i = 0; i < inColumns.length; i++) {
	    if( !inColumns[i].startsWith( "Output Columns" ) ) {
		String colSpec = null;
		for( int j = 0; j < outColumns.length; j++ ) {
		    int k = -1;
		    if( (outColumns[j].startsWith(inColumns[i])) &&
			((k = outColumns[j].indexOf( "[" )) > 0) ) {
			colSpec = StringUtils.substringBetween( outColumns[j], "[", "]" ).trim();
			break;
		    }
		}
                inputOrder.put( inColumns[i], new Integer(i) );
		colSpec = Stringx.getDefault( colSpec, "txt" );
		int actualIndex = nIdx;
		boolean orderSet = false;
		String[] toks = colSpec.split( ";" );
		for( int k = 0; k < toks.length; k++ ) {
		    int l = -1;
		    int m = -1;
		    if( ((l = toks[k].indexOf( "order" )) >= 0) &&
			((m = toks[k].indexOf( "=" )) > 0 ) ) {
			actualIndex = Stringx.toInt(toks[k].substring(m+1).trim(),actualIndex);
			if( actualIndex > orderIndex )
			    orderIndex = actualIndex;
			orderSet = true;
			break;
		    }
		}

		if( !orderSet && (orderIndex >= nIdx) ) {
		    nIdx = orderIndex+1;
		    actualIndex = nIdx; 
		}
		Integer key = new Integer( actualIndex );
		List<String> specs = sortedSpecs.get( key );
		if( specs == null ) {
		    // log.debug( "Creating option key : "+key );
		    specs = new ArrayList<String>();
		    sortedSpecs.put( key, specs );
		    nIdx++;
		}
		specs.add( inColumns[i]+";"+colSpec );
		// log.debug( "Adding specs : "+key+" specs: "+inColumns[i]+";"+colSpec );
	    }
	}

	// <rows>
	Rows rows = new Rows();
	int k = -1;
	Set<Integer> keys = sortedSpecs.keySet();
	for( Integer idx : keys ) {
	    Row row = new Row();
	    row.setParent( rows );
	    Hlayout hl = new Hlayout();
	    hl.setParent( row );
	    List<String> specs = sortedSpecs.get( idx );
	    int i = 0;
	    for( String cSpec : specs ) {
		String inColumn = StringUtils.substringBefore(cSpec,";");
		String colSpec = StringUtils.substringAfter(cSpec,";");
		
		log.debug( "Index: "+idx+" column: "+inColumn+" specs: "+colSpec );

		Label lb = new Label( inColumn );
		lb.setParent( hl );

		Integer colIdx = inputOrder.get( inColumn );

		String[] tspecs = colSpec.split( ";" );
		if( colSpec.startsWith( "txt" ) ) {
		    Textbox txt = new Textbox();
		    txt.setId( "cmp"+cmpPrefix+"InputColumn_"+colIdx+"_"+inColumn );
		    for( int l = 0; l < tspecs.length; l++ ) {
			if( tspecs[l].startsWith( "width" ) ) {
			    txt.setWidth( StringUtils.substringAfter(tspecs[l],"=").trim() );
			}
			else if( tspecs[l].startsWith( "init" ) ) {
			    txt.setValue( StringUtils.substringAfter(tspecs[l],"=").trim() );
			}
		    }
		    txt.setParent( hl );
		}
		else if( colSpec.startsWith( "dt" ) ) {
		    Datebox dt = new Datebox();
		    dt.setId( "cmp"+cmpPrefix+"InputColumn_"+colIdx+"_"+inColumn );
		    dt.setParent( hl );
		    for( int l = 0; l < tspecs.length; l++ ) {
			if( tspecs[l].startsWith( "init" ) ) {
			    String dtp = StringUtils.substringAfter(tspecs[l],"=").trim();
			    Calendar cal = Calendar.getInstance();
			    if( "yearstart".equals(dtp) ) {
				cal.set( Calendar.DAY_OF_MONTH, 1 );
				cal.set( Calendar.MONTH, 0 );
				dt.setValue( cal.getTime() );
			    }
			    else if( "currentdate".equals( dtp ) ) {
				dt.setValue( cal.getTime() );
			    }
			}
		    }
		}
		i++;
	    }
	}
	rows.setParent( grid );
	return grid;
    }

    /**
     * Parses the column descriptions from the upload template.
     *
     * @param templ the template.
     * @param columnTag the tag indicating the column specs to be parsed.
     * @return a list of column specs.
     */
    public static String[] parseColumns( InventoryUploadTemplate templ, String columnTag ) {
	String tCont = Stringx.getDefault( templ.getTemplate(), "" ).trim().replace( "\\n", "\n" );
	int k = -1;
	String[] columns = null;
	if( (k = tCont.indexOf( columnTag )) >= 0 ) {
	    k+=columnTag.length();
	    int l = -1;
	    if( (l = tCont.indexOf( TAG_END, k )) > k ) {
		List<String> lines = null;
		try {
		    lines = IOUtils.readLines( new StringReader( tCont.substring( k, l ) ));
		}
		catch( IOException ioe ) {
		    lines = null;
		    return new String[0];
		}
		for( String ln : lines ) {
		    ln = ln.trim();
		    if( ln.length() > 2 ) 
			columns = ln.split( "[|]" );
		}
	    }
	}
	if( columns == null )
	    return new String[0];
	if( (columns.length > 0) && (columns[0].startsWith( "##" )) ) 
	    columns[0] = columns[0].substring(2).trim();
	return columns;
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

    // private Timestamp validDate( Timestamp dt ) {
    // 	if( (dt == null) || (dt.getTime() <= InvoiceDetails.NO_DATE.getTime()) )
    // 	    return null;
    // 	return dt;
    // }

    /**
     * Initializes the report name.
     *
     * @param wnd the app window.
     * @param cmpPrefix report prefix for components.
     * @param templ the report template.
     */
    public static void initReportName( Window wnd, String cmpPrefix, InventoryUploadTemplate templ ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( "txt"+cmpPrefix+"Name" );
	if( txt != null ) {
	    String repName = Stringx.getDefault( templ.getTemplatename(), "Report" );
	    if( repName.startsWith( "Report -" ) )
		repName = repName.substring( 8 ).trim();
	    repName = repName+" "+Stringx.currentDateString( "dd-MMM-YYYY" );
	    txt.setValue( repName );
	}
    }

    private SampleInventory getSampleInventory() {
	return InventoryPreferences.getInstance( portletId, userId ).getInventory();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( portletId, userId );
    }

    private OutputSelector getOutputSelector() {
	return (OutputSelector)getPreferences().getResult( "cb"+CMP_REPORT_PREFIX+"OutputSelector" );
    }

    private void appendReportOutputs( Window wnd, InventoryUploadTemplate templ ) {
	OutputSelector outS = getOutputSelector();
	if( outS == null ) {
	    log.error( "Cannot determine output selection" );
	    return;
	}

	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
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
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}
    }

    protected void initComponents( Window wnd, Map context ) {
     	log.debug( "Initializing components, context: "+context );	

	Grid grid = (Grid)wnd.getFellowIfAny( CMP_REPORT_INPUT );
	if( grid != null )
	    grid.detach();
	grid = (Grid)wnd.getFellowIfAny( CMP_REPORT_OUTPUT );
	if( grid != null )
	    grid.detach();
	Button bt = (Button)wnd.getFellowIfAny( CMP_REPORT_GENERATE );
	if( bt != null ) 
	    bt.setDisabled( true );

	OutputSelector outS = getOutputSelector();
	if( outS != null ) 
	    outS.updateModel( wnd, null );
	Vlayout vlg = (Vlayout)wnd.getFellowIfAny( CMP_REPORT_GROUP );
	Vlayout vlp = (Vlayout)wnd.getFellowIfAny( CMP_REPORT_PARAM );
	if( (vlg == null) || (vlp == null) )
	    return;

	InventoryUploadTemplate templ = (InventoryUploadTemplate)context.get( TEMPLATE );
	if( templ == null ) {
	    log.error( "Context is mssing report template information." );
	    writeMessage( wnd, "Error: Cannot determine report template." );
	    return;
	}
	updateTemplate( templ ); 

	String[] headerColumns = parseColumns( templ, TAG_HEADER );
	log.debug( "Number of report header columns: "+headerColumns.length );
	String[] outputColumns = parseColumns( templ, TAG_OUTPUT );
	log.debug( "Number of report output columns: "+outputColumns.length );
	if( outputColumns.length > 0 ) {
	    grid = createOutputOptions( CMP_REPORT_PREFIX, outputColumns, this );
	    grid.setParent( vlg );
	    bt.setDisabled( false );
	}
	if( headerColumns.length > 0 ) {
	    grid = createInputOptions( CMP_REPORT_PREFIX, headerColumns, outputColumns, this );
	    grid.setParent( vlp );
	    bt.setDisabled( false );
	}
	
	initReportName( wnd, CMP_REPORT_PREFIX, templ );
	appendReportOutputs( wnd, templ );
    }

    private void initPreferences( Map context ) {
	String pId = (String)context.get( PORTLETID );
	if( pId != null )
	    this.setPortletId( pId );
	Long uId = (Long)context.get( USERID );
	if( uId != null )
	    this.setUserId( uId.longValue() );
    }

    /**
     * Initializes the filter settings.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initView( Window wnd, Map context ) {
	log.debug( "Layout category view. context: "+context );
	initPreferences( context );
	layout( wnd, context );
	initComponents( wnd, context );
	// registerActions( wnd );
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.warn( "No Op event listener invoked" );
    }

}
