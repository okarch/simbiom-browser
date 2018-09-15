package com.emd.simbiom.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.view.ColumnFormatter;

import com.emd.util.BidirectionalComparator;

/**
 * <code>ColumnSetup</code> stores column setup of search results.
 *
 * Created: Tue Feb 20 09:58:22 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class ColumnSetup {
    private List<DisplayColumn> columns;
    // private InventoryPreferences preferences;

    private static Log log = LogFactory.getLog(ColumnSetup.class);

    private static final String KEY_COLUMNS = "simbiom.columnSetup";
    /**
     * Describe sampleInventory here.
     */
    // private SampleInventoryDAO sampleInventory;

    /**
     * Creates a new <code>ColumnSetup</code>
     */
    public ColumnSetup() {
	this.columns = new ArrayList<DisplayColumn>();
    }

    private ColumnSetup( ColumnSetup cSetup ) {
	this();
	this.columns.addAll( cSetup.columns );
    }

    /**
     * Returns the column setup of the current session.
     *
     * @param pref the inventory preferences.
     *
     * @return the current column setup.
     */
    public static ColumnSetup getInstance( ColumnSetup def ) {
	Session ses = Sessions.getCurrent();
	ColumnSetup setup = null;
	if( ses != null ) {
	    setup = (ColumnSetup)ses.getAttribute( KEY_COLUMNS );
	    if( setup != null )
		return setup;
	}
	setup = new ColumnSetup( def );
	if( ses != null )
	    ses.setAttribute( KEY_COLUMNS, setup );
	else 
	    log.error( "Cannot access session" );
	    
	// setup.setPreferences( def.getPreferences() );
	return setup;
    }

    /**
     * Returns the column setup of the current session.
     *
     * @param pref the inventory preferences.
     *
     * @return the current column setup.
     */
    public static ColumnSetup getInstance() {
	return getInstance( new ColumnSetup() );
    }

    /**
     * Adds a <code>DisplayColumn</code> to this setup.
     *
     * @param dCol the <code>DisplayColumn</code> to be added.
     * @return the newly added instance.
     */
    public DisplayColumn addDisplayColumn( DisplayColumn col ) {
	this.columns.add( col );
	return col;
    }

    /**
     * Adds a <code>DisplayColumn</code> to this setup.
     *
     * @param dCol the <code>DisplayColumn</code> to be added.
     * @return the newly added instance.
     */
    public DisplayColumn updateDisplayColumn( DisplayColumn col ) {
	int i = 0;
	for( DisplayColumn dCol : this.columns ) {
	    if( dCol.equals( col ) ) {
		log.debug( "Current column: "+dCol.getColumnId()+"-"+dCol.getLabel()+" new column: "+
			   col.getColumnId()+"-"+col.getLabel() );
		this.columns.set( i, col );
		return col;
	    }
	    i++;
	}
	this.columns.add( col );
	return col;
    }

    /**
     * Adds a <code>DisplayColumn</code> to this setup.
     *
     * @param dCol the <code>DisplayColumn</code> to be added.
     */
    public void setColumn( DisplayColumn col ) {
	addDisplayColumn( col );
    }

    /**
     * Returns an array of <code>DisplayColumn</code> objects.
     *
     * @return a (potentially empty) array of <code>DisplayColumn</code> objects.
     */ 
    public DisplayColumn[] getDisplayColumns() {
	DisplayColumn[] cols = new DisplayColumn[ this.columns.size() ];
	return (DisplayColumn[])this.columns.toArray( cols );
    }

    /**
     * Get the <code>Preferences</code> value.
     *
     * @return an <code>InventoryPreferences</code> value
     */
    // public final InventoryPreferences getPreferences() {
    // 	return preferences;
    // }

    /**
     * Set the <code>Preferences</code> value.
     *
     * @param preferences The new Preferences value.
     */
    // public final void setPreferences(final InventoryPreferences preferences) {
    // 	this.preferences = preferences;
    // }

    /**
     * Get the <code>SampleInventory</code> value.
     *
     * @return a <code>SampleInventoryDAO</code> value
     */
    // public final SampleInventoryDAO getSampleInventory() {
    // 	return sampleInventory;
    // }

    /**
     * Set the <code>SampleInventory</code> value.
     *
     * @param sampleInventory The new SampleInventory value.
     */
    // public final void setSampleInventory(final SampleInventoryDAO sampleInventory) {
    // 	this.sampleInventory = sampleInventory;
    // }

    // protected void initSort( Grid grid, ColumnFormatter cf, String colId, Comparator comp ) {
    //  	Column col = (Column)grid.getFellowIfAny( colId );
    //  	if( col != null ) {
    //  	    col.setSortAscending( new RowComparator( cf, col, new BidirectionalComparator( comp ) ) );
    //  	    col.setSortDescending( new RowComparator( cf, col, new BidirectionalComparator( comp, true ) ) );
    //  	}
    // }

    // public void assignGrid( Grid grid, SampleInventoryDAO dao ) {

    public void assignGrid( Grid grid, SampleInventory dao ) {
	DisplayColumn[] cols = getDisplayColumns();
	for( int i = 0; i < cols.length; i++ ) {
	    cols[i].setSampleInventory( dao );
	    Column col = (Column)grid.getFellowIfAny( cols[i].getColumnId() );
	    if( col != null ) {
		col.setSortAscending( new BidirectionalComparator( cols[i] ) );
		col.setSortDescending( new BidirectionalComparator( cols[i], true ) );
		col.setLabel( cols[i].getLabel() );
	    }
	}
	
    }

    /**
     * Checks wether a certain labe is included in the column list.
     *
     * @param label the label to check.
     * @return a string indicatingtrue or false. Return value can be used in a template.
     */
    public String getChecked( String label ) {
	for( DisplayColumn dCol : this.columns ) {
	    if( dCol.getLabel().equals( label ) )
		return "true";
	}
	return "false";
    }

    public String toString() {
	return this.columns.toString();
    }

}
