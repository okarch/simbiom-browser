package com.emd.simbiom.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.zk.ZKContext;

/**
 * <code>FilterModel</code> holds the filter plugins.
 *
 * Created: Sat May 23 07:23:10 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class FilterModel extends DefaultModelProducer implements EventListener {
    private List<SearchFilter> filters;
    private String filterSuffix;

    public static final String COMPONENT_ID = "cbFilterSelector_0";

    private static Log log = LogFactory.getLog(FilterModel.class);

    public FilterModel() {
	super();
	setModelName( COMPONENT_ID );
	setFilterSuffix( "0" );
	this.filters = new ArrayList<SearchFilter>();
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	Combobox cbFilter = (Combobox)wnd.getFellowIfAny( getModelName() );
	if( cbFilter != null ) {
	    if( context == null )
		context = new HashMap();
	    assignModel( cbFilter, context );
	}
	
	// SampleInventoryDAO dao = getSampleInventory();
	// if( dao == null ) {
	//     writeMessage( wnd, "Error: No database access configured" );
	//     return;
	// }

	// try {
	//     Sample[] samples = dao.findSampleByAge( Age.created().newerThan(DEFAULT_90_DAYS) );
	//     Grid grSamples = (Grid)wnd.getFellowIfAny( getModelName() );
	//     if( grSamples != null ) {
	// 	if( context == null )
	// 	    context = new HashMap();
	// 	context.put( RESULT, samples );
	// 	assignModel( grSamples, context );
	//     } 
	// }
	// catch( SQLException sqe ) {
	//     writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	//     log.error( sqe );
	// }
    }

    /**
     * Updates the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void updateModel( Window wnd, Map context ) {
	log.debug( "Updating filter model" );
	InventoryPreferences pref = InventoryPreferences.getInstance( getPortletId(), getUserId() );
	ModelProducer[] sRes = pref.getResult( SampleResult.class );
	if( (sRes.length > 0) && (filters.size() > 0) ) {
	    int days = (((SampleResult)sRes[0]).getLastCreatedDays())+1;
	    SearchFilter sf = filters.get(0);
	    if( sf instanceof AgeFilter ) {
		log.debug( "Set days to last created sample date: "+String.valueOf( days ) );
		sf.setInitValue( String.valueOf(days) );
		sf.initComponents( wnd, context );
	    }
	}
    }

    /**
     * Adds a filter plugin
     *
     * @param filt the <code>SearchFilter</code>
     */
    public void addFilter( SearchFilter filt ) {
	filters.add( filt );
    }

    /**
     * Get the filter value.
     *
     * @param label the label of the filter.
     * @return the <code>SearchFilter</code> value.
     */
    public SearchFilter getFilter( String label ) {
	Iterator<SearchFilter> it = filters.iterator();
	while( it.hasNext() ) {
	    SearchFilter qt = it.next();
	    if( qt.getLabel().equals(label) ) 
		return qt;
	}
	return new SearchFilter();
    }

    /**
     * Get all filters
     *
     * @return an array of <code>SearchFilter</code>s.
     */
    public SearchFilter[] getFilters() {
	SearchFilter[] filts = new SearchFilter[ filters.size() ];
	return (SearchFilter[])filters.toArray( filts );
    }

    /**
     * Set the Filter value.
     * @param newFilter The new Filter value.
     */
    public void setFilter( SearchFilter newFilter ) {
	addFilter( newFilter );
    }

    /**
     * Get the <code>FilterSuffix</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getFilterSuffix() {
	return filterSuffix;
    }

    /**
     * Set the <code>FilterSuffix</code> value.
     *
     * @param filterSuffix The new FilterSuffix value.
     */
    public final void setFilterSuffix(final String filterSuffix) {
	this.filterSuffix = filterSuffix;
    }

    protected void updateActions( String pId, long uId ) {
	Iterator<SearchFilter> it = filters.iterator();
	while( it.hasNext() ) {
	    SearchFilter qt = it.next();
	    qt.updateActions( pId, uId );
	}
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	context.put( "filterNum", getFilterSuffix() );

	log.debug( "Filter model context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	combobox.addEventListener( Events.ON_SELECT, this );
	combobox.setModel( new ListModelArray( getFilters() ) );

	// Window wnd = ZKContext.findWindow( combobox );

	// for( SearchFilter filt : filters ) {
	//     filt.initFilter( wnd, context );
	// }	
    }

    /**
     * Retrieve the search filter currently selected.
     *
     * @param wnd the application window.
     * @return the search filter (or null) if nothing has been selected.
     */
    public SearchFilter getSelectedSearchFilter( Window wnd ) {
	Combobox cbFilter = (Combobox)wnd.getFellowIfAny( getModelName() );
	if( cbFilter != null ) {
	    int idx = cbFilter.getSelectedIndex();
	    if( (idx >= 0) && (idx < filters.size()) ) 
		return filters.get(idx);
	}
	return null;
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Filter model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    log.debug( "Combobox items:"+cb.getItemCount() );
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
		Map ctxt = new HashMap();
		ctxt.put( "filterNum", getFilterSuffix() );
		ctxt.put( "inventory", getSampleInventory() );
		filters.get(0).initFilter( ZKContext.findWindow( cb ), ctxt );
	    }	    
	}
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    int idx = cb.getSelectedIndex();
	    if( (idx >= 0) && (idx < filters.size()) ) {
		Map ctxt = new HashMap();
		ctxt.put( "filterNum", getFilterSuffix() );
		ctxt.put( "inventory", getSampleInventory() );
		filters.get(idx).initFilter( ZKContext.findWindow( cb ), ctxt );
	    }
	}
	
    }

}
