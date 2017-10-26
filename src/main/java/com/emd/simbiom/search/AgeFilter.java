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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.Age;
import com.emd.simbiom.model.Sample;

import com.emd.util.Stringx;

import com.emd.zk.view.ViewAction;

/**
 * <code>AgeFilter</code> defines a filter to select entries according to age.
 *
 * Created: Sun May 24 16:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class AgeFilter extends SearchFilter {
    private String initUnit;

    private static Log log = LogFactory.getLog(AgeFilter.class);

    private static final String DEFAULT_INTBOX    = "intFilterCriteria_";
    private static final String DEFAULT_CBUNIT    = "cbFilterUnit_";
    private static final String DEFAULT_DBBOX     = "dbEndDate_";

    private static final Map<String, Long> UNIT_MILLIS = createUnitMillis();

    private static Map<String, Long> createUnitMillis() {
        Map<String, Long> result = new HashMap<String, Long>();
        result.put( "days", new Long( 24L * 60L * 60L *1000L ) );
        result.put( "hours", new Long( 60L * 60L * 1000L ) );
        result.put( "months", new Long( 30L * 24L * 60L * 60L * 1000L ) );
        result.put( "weeks", new Long( 7L * 24L * 60L * 60L * 1000L ) );
        result.put( "years", new Long( 365L * 24L * 60L * 60L * 1000L ) );
        return Collections.unmodifiableMap(result);
    }

    /**
     * Create a new age based filter.
     */
    public AgeFilter() {
	super();
    }

    protected AgeFilter( AgeFilter sf, String suffix ) {
	super( sf, suffix );
	this.initUnit = sf.getInitUnit();
    }

    /**
     * Get the <code>InitUnit</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getInitUnit() {
	return initUnit;
    }

    /**
     * Set the <code>InitUnit</code> value.
     *
     * @param initUnit The new InitUnit value.
     */
    public final void setInitUnit(final String initUnit) {
	this.initUnit = initUnit;
    }

    private void selectUnit( Window wnd, String fNum, String unit ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBUNIT+fNum );
	if( cb != null ) {
	    Comboitem item = null;
	    boolean found = false;
	    for( Comboitem ci : cb.getItems() ) {
		if( ci.getLabel().equals(unit) ) {
		    item = ci;
		    break;
		}
	    }
	    if( item != null )
		cb.setSelectedItem( item );
	}
    }

    private Date calcDate( int age, String unit ) {
	Long millis = UNIT_MILLIS.get( unit );
	long diff = 0L;
	if( millis != null )
	    diff = (long)age * millis.longValue();
	    
	return new Date( System.currentTimeMillis() - diff ); 
    }

    private int calcDays( Date dt ) {
	long msDiff = System.currentTimeMillis() - dt.getTime();
	long days = msDiff / (24L * 60L * 60L *1000L);
	return (int)days;
    }

    /**
     * Update filter components on age unit change.
     *
     * @param wnd the window.
     * @param unit the unit.
     */
    public void updateComponents( Window wnd, String unit ) {
	String fNum = getFilterSuffix();

	log.debug( "Update component on unit select (filter "+fNum+"): "+unit );

	Intbox ib = (Intbox)wnd.getFellowIfAny( DEFAULT_INTBOX+fNum );
	int diff = 0;
	if( ib != null ) 
	    diff = ib.intValue();

	Datebox db = (Datebox)wnd.getFellowIfAny( DEFAULT_DBBOX+fNum );
	if( db != null ) {
	    Date dt = calcDate( diff, unit );
	    db.setValue( dt );
	}
    }

    /**
     * Update filter components on age textbox change.
     *
     * @param wnd the window.
     * @param diff the value of the intbox.
     */
    public void updateComponents( Window wnd, int diff ) {
	String fNum = getFilterSuffix();

	log.debug( "Update component on text change: "+String.valueOf(diff) );

	String unit = null;
	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBUNIT+fNum );
	if( cb != null ) {
	    Comboitem ci = cb.getSelectedItem();
	    unit = (String)ci.getValue();
	}

	Datebox db = (Datebox)wnd.getFellowIfAny( DEFAULT_DBBOX+fNum );
	if( (unit != null) && (db != null) ) {
	    Date dt = calcDate( diff, unit );
	    db.setValue( dt );
	}
    }

    /**
     * Update filter components on age textbox change.
     *
     * @param wnd the window.
     * @param diff the value of the intbox.
     */
    public void updateComponents( Window wnd, Date dt ) {
	String fNum = getFilterSuffix();

	int days = calcDays( dt );
	Intbox ib = (Intbox)wnd.getFellowIfAny( DEFAULT_INTBOX+fNum );
	if( ib != null )
	    ib.setValue(new Integer( days ) );

	selectUnit( wnd, fNum, "days" );
    }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	// String fNum = Stringx.getDefault((String)context.get("filterNum"),"0");
	// setFilterSuffix( fNum );

	String fNum = getFilterSuffix();

	log.debug( "Initialize components: "+DEFAULT_INTBOX+fNum );

	Intbox ib = (Intbox)wnd.getFellowIfAny( DEFAULT_INTBOX+fNum );
	if( ib != null )
	    ib.setValue(new Integer(Stringx.toInt(getInitValue(),90)) );
	else
	    log.error( "Cannot find component: "+DEFAULT_INTBOX+fNum );

	selectUnit( wnd, fNum, Stringx.getDefault(getInitUnit(),"days") );

	Datebox db = (Datebox)wnd.getFellowIfAny( DEFAULT_DBBOX+fNum );
	if( db != null ) {
	    Date dt = calcDate( Stringx.toInt(getInitValue(),90), Stringx.getDefault(getInitUnit(),"days") );
	    db.setValue( dt );
	}

	// ViewAction[] vActs = this.getActions();
	// for( int i = 0; i < vActs.length; i++ ) {
	//     if( vActs[i].getComponent().equals( DEFAULT_INTBOX+fNum ) ) {
	// 	log.debug( vActs[i]+": component "+DEFAULT_INTBOX+fNum+" match" );
	// 	ib.addEventListener( vActs[i].getEvent(), vActs[i] );
	//     }
	// } 
    }

    private Age createAge( Window wnd ) 
	throws SQLException {

	Age age = null;
	if( getLabel().toLowerCase().indexOf( "sample import" ) >= 0 ) {
	    age = Age.created();
	}
	else if( getLabel().toLowerCase().indexOf( "collection date" ) >= 0 ) {
	    age = Age.created();
	}

	if( age == null ) 
	    return null;
	
	String fNum = getFilterSuffix();
	
	Intbox ib = (Intbox)wnd.getFellowIfAny( DEFAULT_INTBOX+fNum );
	int diff = 0;
	if( ib != null ) 
	    diff = ib.intValue();

	Combobox cb = (Combobox)wnd.getFellowIfAny( DEFAULT_CBUNIT+fNum );
	String unit = "days";
	if( cb != null ) {
	    Comboitem item = cb.getSelectedItem();
	    if( item != null )
		unit = ((item.getValue() != null)?item.getValue().toString():"days");
	}

	Long millis = UNIT_MILLIS.get( unit );
	if( millis == null ) 
	    return null;

	long diffMillis = millis.longValue() * (long)diff;
	if( getLabel().toLowerCase().indexOf( "newer than" ) >= 0 )
	    return age.newerThan( diffMillis );
	if( getLabel().toLowerCase().indexOf( "older than" ) >= 0 )
	    return age.olderThan( diffMillis );
	return null;
    }

    /**
     * Copies a filter description and modifies component suffixes.
     *
     * @param suffix the new component suffix.
     */
    public AgeFilter copyFilter( String suffix ) {
	return new AgeFilter( this, suffix );	
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

	Age age = createAge( wnd );
	if( age == null ) {
	    String msg = "Query age cannot be determined";
	    log.error( msg );
	    throw new SQLException( msg );
	}

	Sample[] samps = dao.findSampleByAge( age );
	log.debug( "Number of samples found: "+samps.length );
	

	// Sample[] samps = dao.findSampleByAge( Age.created().newerThan(DEFAULT_90_DAYS) );

	return new TreeSet( (List<Sample>)Arrays.asList( samps ) );
    }

}
