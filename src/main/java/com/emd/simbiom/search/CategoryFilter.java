package com.emd.simbiom.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.Sample;

import com.emd.util.Stringx;

/**
 * <code>TextFilter</code> defines a filter to select entries based on text content.
 *
 * Created: Tue Jun 16 07:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class CategoryFilter extends SearchFilter implements EventListener {
    private String category;

    private static Log log = LogFactory.getLog(CategoryFilter.class);

    private static final String DEFAULT_TXTCONT   = "txtFilterTerm_";
    private static final String DEFAULT_CBCONTENT = "cbFilterCategory_";

    /**
     * Create a new text term based filter.
     */
    public CategoryFilter() {
	super();
    }

    protected CategoryFilter( CategoryFilter sf, String suffix ) {
	super( sf, suffix );
	this.category = sf.category;
    }

    /**
     * Get the <code>Category</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getCategory() {
	return category;
    }

    /**
     * Set the <code>Category</code> value.
     *
     * @param category The new Category value.
     */
    public final void setCategory(final String category) {
	this.category = category;
    }

    /**
     * Copies a filter description and modifies component suffixes.
     *
     * @param suffix the new component suffix.
     */
    public CategoryFilter copyFilter( String suffix ) {
	return new CategoryFilter( this, suffix );	
    }

    private String[] createTerms( Map context ) {
	SampleInventoryDAO dao = (SampleInventoryDAO)context.get( "inventory" );
	if( dao == null ) {
	    log.error( "Missing or invalid database access" );
	    return new String[0];
	}
	String[] terms = null;
	String catSt = Stringx.getDefault( getCategory(), "" );
	try {
	    if( "study".equals(catSt) ) {
		terms = dao.findStudyTerms();
	    }
	    else if( "sampletype".equals(catSt) ) {
		terms = dao.findSampleTypeTerms();
	    }
	    else if( catSt.startsWith( "donor:" ) ) {
		terms = dao.findDonorPropertyTerms( Stringx.after( catSt, ":" ) );
	    }
	}
	catch(SQLException sqe ) {
	    log.error( sqe );
	}
	return ((terms == null)?new String[0]:terms);
    }

    private String getTerm( Window wnd ) {
	String fNum = getFilterSuffix();

	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBCONTENT+fNum );
	if( cb != null ) {
	    int idx = cb.getSelectedIndex();
	    if( idx >= 0 )
		return (String)cb.getModel().getElementAt(idx);
	}
	return "";
    }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	String fNum = getFilterSuffix();

	log.debug( "Initialize components ("+fNum+"), category: "+getCategory() );

	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBCONTENT+fNum );
	if( cb != null ) {
	    cb.setModel( new ListModelArray( createTerms(context) ) );
	    cb.addEventListener( "onAfterRender", this );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    log.debug( "Combobox items:"+cb.getItemCount() );
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
	    }	    
	}
    }

    /**
     * Applies a filter and merges datasets according to the operator.
     * The default implementation doesn't do anything.
     *
     * @param samples a list of samples which might be used to initialize the filter.
     * @param wnd the application window.
     * @param dao the database.
     *
     * @param a set of samples matching the filter ordered by sample id. 
     */
    public SortedSet<Sample> applyFilter( SortedSet<Sample> samples, Window wnd, SampleInventoryDAO dao )
	throws SQLException {

	String querySt = getTerm( wnd );
	Sample[] samps = null;
	String catSt = Stringx.getDefault( getCategory(), "" );
	if( "study".equals(catSt) ) {
	    samps = dao.findSampleByStudy( querySt );
	}
	else if( "sampletype".equals(catSt) ) {
	    samps = dao.findSampleByType( querySt );
	}
	else if( catSt.startsWith( "donor:" ) ) {
	    samps = dao.findSampleByDonorProperty( Stringx.after(catSt,":"), querySt );
	}

	return new TreeSet( (List<Sample>)Arrays.asList( ((samps!=null)?samps:new Sample[0]) ) );
    }

}
