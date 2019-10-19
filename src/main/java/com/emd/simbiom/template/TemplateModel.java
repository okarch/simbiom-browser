package com.emd.simbiom.template;

import java.io.IOException;
import java.io.StringReader;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.upload.InventoryUploadTemplate;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKUtil;

/**
 * <code>TemplateModel</code> holds the templates currently available.
 *
 * Created: Sun Jul 12 09:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class TemplateModel extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(TemplateModel.class);

    private static final String TAG_HEADER       = "## Header structure supported:";
    private static final String TAG_OUTPUT       = "## Output columns supported:";
    private static final String TAG_END          = "## ##########";

    public static final String CMP_REPORT_OUTPUT= "grOutputColumns";
    public static final String CMP_REPORT_INPUT = "grInputColumns";
    public static final String CMP_REPORT_GROUP = "vlReportGroup_0";
    public static final String CMP_REPORT_PARAM = "vlReportParameter_0";
    public static final String CMP_REPORT_NAME  = "txtReportName";
    public static final String CMP_REPORT_GENERATE = "btOutputGenerate";

    public static final String COMPONENT_ID      = "cbTemplate";
    public static final String RESULT            = "result";

    public static final String TXT_TEMPLATE      = "txtTemplate";
    public static final String TXT_TEMPLATENAME  = "txtTemplateName";


    private static final Comparator<InventoryUploadTemplate> TEMPLATE_SORTER = new Comparator<InventoryUploadTemplate>() {
	public int compare(InventoryUploadTemplate o1, InventoryUploadTemplate o2) {
	    if( (o1 == null) && (o2 == null) )
		return 0;
	    return o1.getTemplatename().compareTo( o2.getTemplatename() );
	}
	public boolean equals(Object obj) {
	    return false;
	}
    };

    public TemplateModel() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	try {
	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

	    Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	    if( cbTempl != null ) {
		if( context == null )
		    context = new HashMap();
		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    } 
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}
    }

    /**
     * Returns the selected upload template.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    public InventoryUploadTemplate getSelectedTemplate( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	InventoryUploadTemplate templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    if( sel >= 0 )
		templ = (InventoryUploadTemplate)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    private void pushAfterRenderIndex( int idx ) {
	Session ses = Sessions.getCurrent();
	if( ses != null )
	    ses.setAttribute( getModelName()+".afterRenderIndex", new Integer(idx) );
    }
    private int popAfterRenderIndex( int def ) {
	Session ses = Sessions.getCurrent();
	int idx = def;
	if( ses != null ) {
	    Integer ari = (Integer)ses.getAttribute( getModelName()+".afterRenderIndex" );
	    if( ari != null ) {
		idx = ari.intValue();
		ses.removeAttribute( getModelName()+".afterRenderIndex" );
	    }
	}
	return idx;
    }

    /**
     * Reloads the list of templates from the database and selects the given
     * template.
     *
     * @param wnd the app window.
     * @param selTemplate the upload template currently selected (or null).
     */ 
    public void reloadTemplates( Window wnd, InventoryUploadTemplate selTemplate ) {
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	try {
	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

	    if( cbTempl != null ) {
		Map context = new HashMap();
		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    } 
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}

	ListModel tModel = null;
	if( (selTemplate != null) &&
	    (cbTempl != null) && 
	    ((tModel = cbTempl.getModel()) != null) ) {

	    log.debug( "Find index of template \""+selTemplate.getTemplatename()+"\"" );
	    int idx = -1;
	    InventoryUploadTemplate templ = null;
	    for( int i = 0; i < tModel.getSize(); i++ ) {
		templ = (InventoryUploadTemplate)tModel.getElementAt( i );
		if( templ.getTemplatename().equals( selTemplate.getTemplatename() ) ) {
		    idx = i;
		    break;
		}
	    }
	    if( idx >= 0 ) {
		log.debug( "Template index of \""+templ.getTemplatename()+"\": "+idx );
		pushAfterRenderIndex( idx );
		// cbTempl.setSelectedIndex( idx );
		// updateTemplateText( wnd, templ );
	    }		
	    else {
		log.error( "Template index "+idx+" could not be found for template \""+
			   selTemplate.getTemplatename()+"\"" ); 
	    }
	}
    }

    private String toEditText( String templ ) {
	return Stringx.getDefault( templ, "## Empty template" ).trim().replace( "\\n", "\n" );
    }

    private void updateTemplateText( Window wnd, InventoryUploadTemplate templ ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATE );
	if( txtTempl != null )
	    txtTempl.setValue( toEditText(templ.getTemplate()) );
	txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATENAME );
	if( txtTempl != null ) {
	    txtTempl.setValue( templ.getTemplatename() );
	}
    }

    private String[] parseColumns( InventoryUploadTemplate templ, String columnTag ) {
	String tCont = toEditText(templ.getTemplate());
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

    private Grid createInputOptions( String[] inColumns, String[] outColumns ) {
	Grid grid = new Grid();
	grid.setId( CMP_REPORT_INPUT );
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

		if( colSpec.startsWith( "txt" ) ) {
		    Textbox txt = new Textbox();
		    txt.setId( "cmpInputColumn_"+colIdx+"_"+inColumn );
		    txt.setParent( hl );
		}
		else if( colSpec.startsWith( "dt" ) ) {
		    Datebox dt = new Datebox();
		    dt.setId( "cmpInputColumn_"+colIdx+"_"+inColumn );
		    dt.setParent( hl );
		}
		i++;
	    }
	}
	rows.setParent( grid );
	return grid;
    }

    private Grid createOutputOptions( String[] columns ) {
	Grid grid = new Grid();
	grid.setId( CMP_REPORT_OUTPUT );
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
	    Checkbox chk = new Checkbox( colName );
	    chk.setId( "chkOutputColumns_"+i+"_"+colName );
	    chk.setChecked( checked );
	    chk.setParent( row );
	}
	rows.setParent( grid );
	return grid;
    }

    private void initReportName( Window wnd, InventoryUploadTemplate templ ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( CMP_REPORT_NAME );
	if( txt != null ) {
	    String repName = Stringx.getDefault( templ.getTemplatename(), "Report" );
	    if( repName.startsWith( "Report -" ) )
		repName = repName.substring( 8 ).trim();
	    repName = repName+" "+Stringx.currentDateString( "dd-MMM-YYYY" );
	    txt.setValue( repName );
	}
    }
 
    private void updateReportSection( Window wnd, InventoryUploadTemplate templ ) {
	Grid grid = (Grid)wnd.getFellowIfAny( CMP_REPORT_INPUT );
	if( grid != null )
	    grid.detach();
	grid = (Grid)wnd.getFellowIfAny( CMP_REPORT_OUTPUT );
	if( grid != null )
	    grid.detach();
	Button bt = (Button)wnd.getFellowIfAny( CMP_REPORT_GENERATE );
	if( bt != null )
	    bt.setDisabled( true );
	Vlayout vlg = (Vlayout)wnd.getFellowIfAny( CMP_REPORT_GROUP );
	Vlayout vlp = (Vlayout)wnd.getFellowIfAny( CMP_REPORT_PARAM );
	if( (vlg == null) || (vlp == null) )
	    return;
	String[] headerColumns = parseColumns( templ, TAG_HEADER );
	log.debug( "Number of report header columns: "+headerColumns.length );
	String[] outputColumns = parseColumns( templ, TAG_OUTPUT );
	log.debug( "Number of report output columns: "+outputColumns.length );
	if( outputColumns.length > 0 ) {
	    grid = createOutputOptions( outputColumns );
	    grid.setParent( vlg );
	    bt.setDisabled( false );
	}
	if( headerColumns.length > 0 ) {
	    grid = createInputOptions( headerColumns, outputColumns );
	    grid.setParent( vlp );
	    bt.setDisabled( false );
	}
	
	// initialize report name

	initReportName( wnd, templ );
    }

    private InventoryUploadTemplate[] sortTemplates( InventoryUploadTemplate[] templs ) {
	Arrays.sort( templs, TEMPLATE_SORTER );
	return templs;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Template result model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	    combobox.addEventListener( Events.ON_SELECT, this );

	InventoryUploadTemplate[] tList = (InventoryUploadTemplate[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new InventoryUploadTemplate[0] ) );
	else {
	    log.debug( "Assigning model, number of templates: "+tList.length );
	    combobox.setModel( new ListModelArray( sortTemplates(tList) ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Template model selected: "+event );

	InventoryUploadTemplate templ = null;

	Combobox cb = (Combobox)event.getTarget();

	if( "onAfterRender".equals( event.getName() ) ) {
	    int idx = popAfterRenderIndex( 0 );
	    if( idx < cb.getItemCount() ) {
		cb.setSelectedIndex( idx );
		templ = (InventoryUploadTemplate)cb.getModel().getElementAt( idx );
	    }	    
	}	
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    if( cb.getItemCount() > 0 ) {
		int idx = cb.getSelectedIndex();
		if( idx >= 0 )
		    templ = (InventoryUploadTemplate)cb.getModel().getElementAt( idx );
	    }
	}

	if( templ != null ) {
	    Window wnd = ZKUtil.findWindow( cb );
	    updateTemplateText( wnd, templ );
	    updateReportSection( wnd, templ );
	}
    }
}
