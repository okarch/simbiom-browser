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

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;
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
public class TextFilter extends SearchFilter {
    private String initContentOperator;
    private String term;

    private static Log log = LogFactory.getLog(TextFilter.class);

    private static final String DEFAULT_TXTCONT   = "txtFilterTerm_";
    private static final String DEFAULT_CBCONTENT = "cbFilterContent_";

    /**
     * Create a new text term based filter.
     */
    public TextFilter() {
	super();
    }

    protected TextFilter( TextFilter sf, String suffix ) {
	super( sf, suffix );
	this.initContentOperator = sf.getInitContentOperator();
	this.term = sf.getTerm();
    }

    /**
     * Get the <code>InitContentOperator</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getInitContentOperator() {
	return initContentOperator;
    }

    /**
     * Set the <code>InitContentOperator</code> value.
     *
     * @param initContentOperator The new InitContentOperator value.
     */
    public final void setInitContentOperator(final String initContentOperator) {
	this.initContentOperator = initContentOperator;
    }

    /**
     * Get the <code>Term</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getTerm() {
	return term;
    }

    /**
     * Set the <code>Term</code> value.
     *
     * @param term The new Term value.
     */
    public final void setTerm(final String term) {
	this.term = term;
    }

    /**
     * Copies a filter description and modifies component suffixes.
     *
     * @param suffix the new component suffix.
     */
    public TextFilter copyFilter( String suffix ) {
	return new TextFilter( this, suffix );	
    }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	// String fNum = Stringx.getDefault((String)context.get("filterNum"),"0");
	// setFilterSuffix( fNum );

	String fNum = getFilterSuffix();

	log.debug( "Initialize components: "+DEFAULT_TXTCONT+fNum );

	Textbox tb = (Textbox)wnd.getFellowIfAny( DEFAULT_TXTCONT+fNum );
	if( tb != null )
	    tb.setValue( Stringx.getDefault( getInitValue(), ""));

	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBCONTENT+fNum );
	if( cb != null ) {
	    String cItem = getInitContentOperator();
	    if( cItem != null ) {
		int i = -1;
		log.debug( "Initializing content operator to "+cItem+" number of items: "+cb.getItemCount() );
		for( Comboitem ci : cb.getItems() ) {
		    i++;
		    if( ci.getLabel().equals( cItem ) ) 
			break;
		    if( (ci.getValue() != null) && (ci.getValue().toString().equals( cItem )) ) 
			break;
		}
		if( i >= 0 )
		    cb.setSelectedIndex( i );
	    }
	}
	    
	// selectUnit( wnd, fNum, Stringx.getDefault(getInitUnit(),"days") );

	// Datebox db = (Datebox)wnd.getFellowIfAny( DEFAULT_DBBOX+fNum );
	// if( db != null ) {
	//     Date dt = calcDate( Stringx.toInt(getInitValue(),90), Stringx.getDefault(getInitUnit(),"days") );
	//     db.setValue( dt );
	// }
    }

    private String createQuery( Window wnd ) 
	throws SQLException {

	if( term != null )
	    return term;
	return null;
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

	String querySt = createQuery( wnd );
	if( querySt == null ) {
	    log.warn( "No query term provided" );
	    return samples;
	}

	Sample[] samps = dao.findSampleByContent( querySt );

	return new TreeSet( (List<Sample>)Arrays.asList( samps ) );
    }

}
