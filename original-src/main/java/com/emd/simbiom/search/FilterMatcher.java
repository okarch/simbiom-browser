package com.emd.simbiom.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.Sample;

/**
 * <code>FilterMatcher</code> executes a chain of filters.
 *
 * Created: Thu Jun 11 18:51:15 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class FilterMatcher {
    private List<FilterOperator> operators;
    private FilterOperator lastOperator;

    private static Log log = LogFactory.getLog(FilterMatcher.class);

    /**
     * Creates a new filter execution chain.
     */ 
    public FilterMatcher() {
	this.operators = new ArrayList<FilterOperator>();
	this.lastOperator = null;
    }

    /**
     * Adds a new filter predicate to the chain
     *
     * @param filt the filter.
     * @param op the operator.
     */
    public void addFilter( SearchFilter filt, FilterOperator op ) {
	op.setFilter( filt, lastOperator );
	lastOperator = op;
	operators.add( op );
    }

    /**
     * Evaluate the filter chain.
     *
     */
    public Sample[] evaluateFilters( Window wnd, SampleInventoryDAO db ) 
	throws SQLException {

	log.debug( "Evaluate filter chain, length "+operators.size() );

	Iterator<FilterOperator> it = operators.iterator();
	SortedSet<Sample> samples = new TreeSet<Sample>();
	while( it.hasNext() ) {
	    FilterOperator fOp = it.next();
	    fOp.applyFilter( samples, wnd, db );
	}
	Sample[] samps = new Sample[ samples.size() ];
	return (Sample[])samples.toArray( samps );
    }

}
