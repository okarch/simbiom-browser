package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.Sample;

/**
 * <code>FilterOperator</code> handles the boolean expression operator.
 *
 * Created: Thu Jun 11 08:13:55 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class FilterOperator {
    private int operator;
    private int combineSets;
    private SearchFilter filter;

    private static Log log = LogFactory.getLog(FilterOperator.class);

    public static final int AND     = 0;
    public static final int OR      = 1;
    public static final int BUT_NOT = 2;

    public static final int SET_INTERSECTION = 0;
    public static final int SET_UNION        = 1;
    public static final int SET_DIFFERENCE   = 2;
    public static final int SET_CREATE       = 3;

    private FilterOperator( int operator ) {
	this.operator = operator;
	this.combineSets = SET_CREATE;
    }

    public static FilterOperator fromString( String opValue ) {
	if( opValue.equalsIgnoreCase( "and" ) )
	    return new FilterOperator( AND );
	if( opValue.equalsIgnoreCase( "or" ) )
	    return new FilterOperator( OR );
	if( opValue.equalsIgnoreCase( "but not" ) )
	    return new FilterOperator( BUT_NOT );
	return new FilterOperator( AND );
    }

    /**
     * Get the <code>Operator</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getOperator() {
	return operator;
    }

    /**
     * Set the <code>Operator</code> value.
     *
     * @param operator The new Operator value.
     */
    public final void setOperator(final int operator) {
	this.operator = operator;
    }

    /**
     * Get the <code>Filter</code> value.
     *
     * @return a <code>SearchFilter</code> value
     */
    public final SearchFilter getFilter() {
	return filter;
    }

    /**
     * Set the <code>Filter</code> value.
     *
     * @param filter The new Filter value.
     */
    public final void setFilter(final SearchFilter filter, FilterOperator prevOp ) {
	this.filter = filter;
	if( prevOp == null ) {
	    combineSets = SET_CREATE;
	}
	else if( prevOp.getOperator() == AND ) {
	    combineSets = SET_INTERSECTION;
	}
	else if( prevOp.getOperator() == OR ) {
	    combineSets = SET_UNION;
	}
	else if( prevOp.getOperator() == BUT_NOT ) {
	    combineSets = SET_DIFFERENCE;
	}
    }

    /**
     * Applies a filter and merges datasets according to the operator.
     *
     * @param samples the list of samples.
     * @param wnd the application window.
     * @param dao the database.
     */
    public void applyFilter( SortedSet<Sample> samples, Window wnd, SampleInventoryDAO dao )
	throws SQLException {

	if( (combineSets == SET_INTERSECTION) && (samples.size() <= 0) ) {
	    log.debug( "Short cut filter evaluation: sample set intersection is always empty" );
	    return;
	}
	if( filter == null ) {
	    log.warn( "No filter to be applied" );
	    return;
	}
	SortedSet<Sample> fSamples = filter.applyFilter( samples, wnd, dao );

	if( combineSets == SET_CREATE ) {
	    samples.addAll( fSamples );
	    return;
	}
	if( (fSamples.size() <= 0) && (combineSets == SET_INTERSECTION) ) {
	    samples.clear();
	    return;
	}

	if( combineSets == SET_INTERSECTION ) {
	    samples.retainAll( fSamples );
	}
	else if( combineSets == SET_UNION ) {
	    samples.addAll( fSamples );
	}
	else if( combineSets == SET_DIFFERENCE ) {
	    samples.removeAll( fSamples );
	}
    }

}
