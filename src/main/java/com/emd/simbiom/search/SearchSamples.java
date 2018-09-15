package com.emd.simbiom.search;

/**
 * SearchSamples starts the sample ineventory search
 *
 * Created: Tue Mar 24 12:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

public class SearchSamples extends InventoryCommand {

    private static Log log = LogFactory.getLog(SearchSamples.class);

    private static final String TEXT_FILTER = "Any content";

    /**
     * Creates a new command to search samples.
     */
    public SearchSamples() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private boolean useFilter( Window wnd ) {
	UseFilter uf = (UseFilter)getPreferences().getCommand( UseFilter.class );
	return ( (uf != null)?uf.isUseFilterChecked( wnd ):false );
    }

    // private FilterOperator[] collectOperators( Window wnd ) {
    // 	List<FilterOperator> fOps = new ArrayList<FilterOperator>();
    // 	for( int i = 0; i < Integer.MAX_VALUE; i++ ) {
    // 	    Combobox cb = (Combobox)wnd.getFellowIfAny( "cbFilterOperator_"+String.valueOf(i) );
    // 	    if( cb == null )
    // 		break;
    // 	    Comboitem ci = cb.getSelectedItem();
    // 	    FilterOperator fOp = null;
    // 	    if( ci != null ) 
    // 		fOp = FilterOperator.fromString( ci.getValue().toString() );
    // 	    else
    // 		fOp = FilterOperator.fromString( "and" );
    // 	    fOps.add( fOp );
    // 	}
    // 	FilterOperator[] operators = new FilterOperator[ fOps.size() ];
    // 	return (FilterOperator[])fOps.toArray( operators );
    // }

    private FilterOperator[] collectOperators( Window wnd ) {
	ModelProducer[] filters = (ModelProducer[])getPreferences().getResult( FilterModel.class );
	List<FilterOperator> fOps = new ArrayList<FilterOperator>();
	for( int i = 0; i < filters.length; i++ ) {
	    String idx  = ((FilterModel)filters[i]).getFilterSuffix();
	    Combobox cb = (Combobox)wnd.getFellowIfAny( "cbFilterOperator_"+idx );
	    if( cb != null ) {
		Comboitem ci = cb.getSelectedItem();
		FilterOperator fOp = null;
		if( ci != null ) 
		    fOp = FilterOperator.fromString( ci.getValue().toString() );
		else
		    fOp = FilterOperator.fromString( "and" );
		fOps.add( fOp );
	    }
	}
	FilterOperator[] operators = new FilterOperator[ fOps.size() ];
	return (FilterOperator[])fOps.toArray( operators );
    }

    private FilterMatcher createExecutor( Window wnd ) {
	ModelProducer[] filters = (ModelProducer[])getPreferences().getResult( FilterModel.class );
        FilterOperator[] operators = collectOperators( wnd );
	log.debug( "Operators collected: "+operators.length );
	FilterMatcher fm = new FilterMatcher();
	for( int i = 0; i < filters.length; i++ ) {
	    SearchFilter sf = ((FilterModel)filters[i]).getSelectedSearchFilter( wnd );
	    if( sf != null ) {
		if( i < operators.length )
		    fm.addFilter( sf, operators[i] );
		else
		    fm.addFilter( sf, FilterOperator.fromString( "and" ) );
	    }
	}
	return fm;
    }

    private String getSearchTerm( Window wnd ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( "txtSampleSearch" );
	String term = null;
	if( tb != null ) 
	    term = Stringx.getDefault(tb.getValue(),"").trim();
	else
	    term = "";
	return term;
    }

    private SearchFilter createTextFilter( Window wnd, String term ) {
	ModelProducer[] fModels = getPreferences().getResult( FilterModel.class );
	int maxSuffix = -1;
	for( int i = 0; i < fModels.length; i++ ) {
	    int idx  = Stringx.toInt(((FilterModel)fModels[i]).getFilterSuffix(),0);
	    if( idx > maxSuffix )
		maxSuffix = idx;
	}
	if( (maxSuffix < 0 ) || (fModels.length <= 0) ) {
	    log.error( "Cannot determine maximum number of filter criteria" );
	    return new SearchFilter();
	}
	maxSuffix++;
	
	TextFilter tf = (TextFilter)((FilterModel)fModels[0]).getFilter( TEXT_FILTER );    
	if( tf == null ) {
	    log.error( "Cannot find text filter: "+TEXT_FILTER );
	    return new SearchFilter();
	}
	
	TextFilter newFilter = tf.copyFilter( String.valueOf(maxSuffix) );
	newFilter.setTerm( term );
	return newFilter;
    }

    private void setSampleResult( Window wnd, Sample[] samples ) {
	ModelProducer[] sModels = getPreferences().getResult( SampleResult.class );
	if( sModels.length <= 0 ) {
	    log.warn( "Cannot update sample result model" );
	    return;
	}
	 
	SampleResult sr = (SampleResult)sModels[0];
	Grid grSamples = (Grid)wnd.getFellowIfAny( sr.getModelName() );
	if( grSamples != null ) {
	    Map context = new HashMap();
	    context.put( SampleResult.RESULT, samples );
	    sr.assignModel( grSamples, context );
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

	log.debug( "Search sample information" );

	FilterMatcher matcher = null;
	if( useFilter( wnd ) ) 
	    matcher = createExecutor( wnd );
	else
	    matcher = new FilterMatcher();

	String sText = getSearchTerm( wnd );
	if( sText.length() > 0 )
	    matcher.addFilter( createTextFilter( wnd, sText ), FilterOperator.fromString( "and" ) );

	StringBuilder msg = new StringBuilder();
	try {
	    Sample[] samples = matcher.evaluateFilters( wnd, getSampleInventory() );
	    
	    msg.append( String.valueOf( samples.length ) );
	    msg.append( " samples match the search" );

	    log.debug( msg.toString() );

	    setSampleResult( wnd, samples );
	    
	}
	catch( SQLException sqe ) {
	    log.error( sqe );
	    msg.append( "Error: "+Stringx.getDefault( sqe.getMessage(), "unknown database error" ) );
	}

	showMessage( wnd, "rowMessage", "lbMessage", msg.toString() );
    }    
    
} // SetupSave
