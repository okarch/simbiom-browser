package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;

import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>FilterRemove</code> removes filter criteria.
 *
 * Created: Fri Jul  3 12:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class FilterRemove extends InventoryCommand {

    private static Log log = LogFactory.getLog(FilterRemove.class);

    private static final String TEXT_FILTER = "Any content";
    private static final String[] OPERATOR_ITEMS = {
	"",
	"And",
	"Or",
	"But not"
    };

    /**
     * Creates a new command to selecte the search operator.
     */
    public FilterRemove() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private int getFilterIndex( String btId ) {
	String[] toks = btId.split( "[_]" );
	if( toks.length < 2 ) {
	    log.warn( "Component name is invalid: "+btId );
	    return -1;
	}
	return Stringx.toInt(toks[1],-1);
    }

    private void detachRow( Window wnd, int nfCount ) {
	Row row = (Row)wnd.getFellowIfAny( "rowFilter_"+String.valueOf(nfCount) );
	if( row != null )
	    row.detach();
    }

    private void removeFilterModel( int nfCount ) {
     	ModelProducer[] fModels = getPreferences().getResult( FilterModel.class );
	String remSuff = String.valueOf(nfCount);
	// FilterModel fMod = null;
	for( int i = 0; i < fModels.length; i++ ) {
	    String fSuff = ((FilterModel)fModels[i]).getFilterSuffix();
	    if( fSuff.equals( remSuff ) ) {
		log.debug( "Removing filter model: "+fSuff );
		getPreferences().removeResult( fModels[i] );
		// fMod = fModels[i];
		break;
	    }
	}
    }	
	    

    // private Row createRow( int nfCount ) {

    // 	// <row id="rowFilter_0">
    // 	Row row = new Row();
    // 	String ext  = "_"+String.valueOf( nfCount );
    // 	row.setId( "rowFilter"+ext );

    // 	//   <combobox id="cbFilterSelector_0" autodrop="true" width="200px" mold="rounded" buttonVisible="true"/>
    // 	Hlayout hlCombo = new Hlayout();
    // 	hlCombo.setParent( row );

    // 	Combobox cb = new Combobox();
    // 	cb.setId( "cbFilterSelector"+ext );
    // 	cb.setAutodrop( true );
    // 	cb.setWidth( "200px" );
    // 	cb.setMold( "rounded" );
    // 	cb.setButtonVisible( true );
    // 	cb.setParent( hlCombo );

    // 	// <button id="btFilterRemove_0" disabled="true" image="/images/delete-icon.png" />
    // 	Button bt = new Button();
    // 	bt.setId( "btFilterRemove"+ext );
    // 	bt.setDisabled( false );
    // 	bt.setImage( "/images/delete-icon.png" );
    // 	bt.setParent( hlCombo );
    //  	FilterRemove fRemove = getPreferences().getCommand( "btFilterRemove_0" );
    // 	if( fRemove != null )
    // 	    bt.addEventListener( fRemove );

    // 	//   <hlayout id="hlFilter_0"/>
    // 	Hlayout hl = new Hlayout();
    // 	hl.setId( "hlFilter"+ext );
    // 	hl.setParent( row );

    // 	//   <combobox id="cbFilterOperator_0" autodrop="true" width="100px" mold="rounded" buttonVisible="true">
    // 	cb = new Combobox();
    // 	cb.setId( "cbFilterOperator"+ext );
    // 	cb.setAutodrop( true );
    // 	cb.setWidth( "100px" );
    // 	cb.setMold( "rounded" );
    // 	cb.setButtonVisible( true );
    // 	cb.setParent( row );

    // 	for( int i = 0; i < OPERATOR_ITEMS.length; i++ ) {
    // 	    Comboitem ci = cb.appendItem( OPERATOR_ITEMS[i] );
    // 	    ci.setValue( (OPERATOR_ITEMS[i].length() <= 0)?OPERATOR_ITEMS[1].toLowerCase():OPERATOR_ITEMS[i].toLowerCase() );
    // 	}
    // 	return row;
    // }

    // private void initOperatorSelect( Window wnd, int nfCount ) {
    // 	SelectFilterOperator cmd = new SelectFilterOperator();
    // 	cmd.setCommandName( "cbFilterOperator_"+String.valueOf(nfCount) );
    // 	cmd.setEvent( "onSelect" );
    // 	cmd.setPortletId( this.getPortletId() );
    // 	cmd.setUserId( this.getUserId() );

    // 	Combobox cb = (Combobox)wnd.getFellowIfAny( cmd.getCommandName() );
    // 	if( cb != null )
    // 	    cb.addEventListener( cmd.getEvent(), cmd );
    // }

    // private FilterModel createFilterModel( Window wnd, int nfCount ) {
    // 	FilterModel fModel = new FilterModel();
    // 	fModel.setModelName( "cbFilterSelector_"+String.valueOf(nfCount) );
    // 	fModel.setFilterSuffix( String.valueOf( nfCount ) );

    //  	ModelProducer[] fModels = getPreferences().getResult( FilterModel.class );
    // 	if( fModels.length <= 0 ) {
    // 	    log.error( "No filter model found" );
    // 	    return null;
    // 	}
    // 	SearchFilter[] filts = ((FilterModel)fModels[0]).getFilters();
    // 	for( int i = 0; i < filts.length; i++ ) {
    // 	    SearchFilter sf = filts[i].copyFilter( String.valueOf( nfCount ) );
    // 	    fModel.setFilter( sf );

    // 	    // Parameter[] params = sf.getParameters();
    // 	    // List<Parameter> paras = Arrays.asList( params );
    // 	    // ViewAction[] acts = sf.getActions();
    // 	    // for( int j = 0; j < acts.length; j++ ) {
    // 	    // 	log.debug( "Component id: "+acts[j].getComponent() );
    // 	    //  	acts[j].registerEvent( wnd, paras );
    // 	    // }
    // 	}
    // 	return fModel;
    // }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Filter remove: "+event );

	Button bt = (Button)event.getTarget();
	if( bt != null ) {
	    Window wnd = UIUtils.getWindow( event );
	    int idx = getFilterIndex( bt.getId() );
	    log.debug( "Filter row to be removed: "+String.valueOf( idx ) );
	    if( idx > 0 ) {
		detachRow( wnd, idx );
		removeFilterModel( idx );
	    }
	}


	// showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Upload received: "+upFile.getName() );

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

		
    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    // public void execute( ZKContext context, Window wnd )
    // 	throws CommandException {

    // 	log.debug( "Filter remove:" );

    // 	int nfCount = nextFilterCount( wnd );
    // 	if( nfCount <= 0 )
    // 	    return;

    // 	log.debug( "Next filter to be added: "+nfCount );

    // 	// create and add the new filter row

    // 	Row row = createRow( nfCount );
    // 	Row rowActions = (Row)wnd.getFellowIfAny( "rowFilterActions" );
    // 	if( rowActions == null )
    // 	    return;
    // 	Rows rows = (Rows)wnd.getFellowIfAny( "rowsFilter" );
    // 	if( rows == null )
    // 	    return;
    // 	rows.insertBefore( row, rowActions );

    // 	// initialize the row

    // 	initOperatorSelect( wnd, nfCount );

    // 	//initializes the filter model

    // 	FilterModel fm = createFilterModel( wnd, nfCount );
    // 	if( fm == null )
    // 	    return;
    // 	getPreferences().setResult( fm );
    // 	fm.initModel( wnd, null );

    // 	log.debug( "New filter model "+nfCount+" initialized" );

    // 	// FilterMatcher matcher = null;
    // 	// if( useFilter( wnd ) ) 
    // 	//     matcher = createExecutor( wnd );
    // 	// else
    // 	//     matcher = new FilterMatcher();

    // 	// String sText = getSearchTerm( wnd );
    // 	// if( sText.length() > 0 )
    // 	//     matcher.addFilter( createTextFilter( wnd, sText ), FilterOperator.fromString( "and" ) );

    // 	// StringBuilder msg = new StringBuilder();
    // 	// try {
    // 	//     Sample[] samples = matcher.evaluateFilters( wnd, getSampleInventory() );
	    
    // 	//     msg.append( String.valueOf( samples.length ) );
    // 	//     msg.append( " samples match the search" );

    // 	//     log.debug( msg.toString() );

    // 	//     setSampleResult( wnd, samples );
	    
    // 	// }
    // 	// catch( SQLException sqe ) {
    // 	//     log.error( sqe );
    // 	//     msg.append( "Error: "+Stringx.getDefault( sqe.getMessage(), "unknown database error" ) );
    // 	// }

    // 	// showMessage( wnd, "rowMessage", "lbMessage", msg.toString() );
    // }    
    
} 
