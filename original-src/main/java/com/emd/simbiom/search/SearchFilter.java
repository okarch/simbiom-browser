package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.Sample;

import com.emd.util.Stringx;

import com.emd.zk.view.VelocityView;
import com.emd.zk.view.ViewAction;

import com.emd.util.Parameter;

/**
 * <code>SearchFilter</code> is the basic class of a filter plugin.
 *
 * Created: Sat May 23 16:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SearchFilter extends VelocityView {
    private String label;
    private String initValue;
    private String format;
    private String message;
    private String filterSuffix;

    // private Textbox txtFilter;
    // private Datebox dbEndDate;

    private static Log log = LogFactory.getLog(SearchFilter.class);

    public SearchFilter() {
	this.initValue = "";
	this.label = "";
    }

    protected SearchFilter( SearchFilter sf, String suffix ) {
	String oldSuffix = sf.getFilterSuffix();
	this.setFilterSuffix( suffix );
	this.initValue = sf.getInitValue();
	this.label = sf.getLabel();
	String st = sf.getMessage();
	if( (oldSuffix != null) && (st != null) )
	    st = st.replace( "_"+oldSuffix, "_"+suffix );
	this.setMessage( st );
	this.setTemplate( sf.getTemplate() );
	st = sf.getParent();
	if( (oldSuffix != null) && (st != null) )
	    st = st.replace( "_"+oldSuffix, "_"+suffix );
	this.setParent( st );

	Parameter[] params = sf.getParameters();
	for( int i = 0; i < params.length; i++ )
	    this.setParameter( params[i] );

	ViewAction[] acts = sf.getActions();
	for( int i = 0; i < acts.length; i++ ) {
	    ViewAction va = acts[i];
	    if( va instanceof InventoryViewAction ) 
		va = ((InventoryViewAction)va).copyAction( suffix );
	    va.setView( this );
	    this.setAction( va );
	}
    }

    /**
     * Get the <code>Label</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getLabel() {
	return label;
    }

    /**
     * Set the <code>Label</code> value.
     *
     * @param label The new Label value.
     */
    public final void setLabel(final String label) {
	this.label = label;
    }

    /**
     * Get the <code>InitValue</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getInitValue() {
	return initValue;
    }

    /**
     * Set the <code>InitValue</code> value.
     *
     * @param initValue The new InitValue value.
     */
    public final void setInitValue(final String initValue) {
	this.initValue = initValue;
    }

    /**
     * Get the <code>Format</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getFormat() {
	return format;
    }

    /**
     * Set the <code>Format</code> value.
     *
     * @param format The new Format value.
     */
    public final void setFormat(final String format) {
	this.format = format;
    }

    /**
     * Get the <code>Message</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessage() {
	if( message != null )
	    return message;
	return Stringx.getDefault( getLabel(), "" );
    }

    /**
     * Set the <code>Message</code> value.
     *
     * @param message The new Message value.
     */
    public final void setMessage(final String message) {
	this.message = message;
    }

    /**
     * Get the <code>FilterSuffix</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getFilterSuffix() {
	return Stringx.getDefault(filterSuffix,"0");
    }

    /**
     * Set the <code>FilterSuffix</code> value.
     *
     * @param filterSuffix The new FilterSuffix value.
     */
    public final void setFilterSuffix(final String filterSuffix) {
	this.filterSuffix = filterSuffix;
    }

    /**
     * Returns a human readble filter representation.
     *
     * @return the label of the search filter.
     */
    public String toString() {
	return Stringx.getDefault(getLabel(),"");
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

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
    }

    /**
     * Initializes the filter settings.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initFilter( Window wnd, Map context ) {
	log.debug( "Layout filter. context: "+context );
	String fNum = Stringx.getDefault((String)context.get("filterNum"),"0");
	setFilterSuffix( fNum );
	layout( wnd, context );
	initComponents( wnd, context );
	// registerActions( wnd );
    }

    /**
     * Copies a filter description and modifies component suffixes.
     *
     * @param suffix the new component suffix.
     */
    public SearchFilter copyFilter( String suffix ) {
	return new SearchFilter( this, suffix );	
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

	return samples;
    }

}
