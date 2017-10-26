package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * Describe class UseFilter here.
 *
 *
 * Created: Mon Jun  8 08:10:42 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class UseFilter extends InventoryCommand {

    private static Log log = LogFactory.getLog(UseFilter.class);

    private static final String FILTER_ROW_ID = "rowFilter";

    /**
     * Creates a new command to activate or deactivate filter usage.
     */
    public UseFilter() {
	super();
    }

    private void setFilterVisible( Window wnd, boolean visible ) {
     	Row fRow = (Row)wnd.getFellowIfAny( FILTER_ROW_ID );
	if( fRow != null ) 
	    fRow.setVisible( visible );
    }

    /**
     * Tests whether the filter criteria are active.
     *
     * @param wnd the application window.
     * @return checkbox value.
     */
    public boolean isUseFilterChecked( Window wnd ) {
     	Checkbox chk = (Checkbox)wnd.getFellowIfAny( getCommandName() );
     	return (( chk != null )? chk.isChecked() : false);
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

	boolean useFilter = isUseFilterChecked( wnd );
	setFilterVisible( wnd, useFilter );

	StringBuilder stb = new StringBuilder();
	if( !useFilter )
	    stb.append( "Filter is inactive" );
	showMessage( wnd, "rowMessage", "lbMessage", stb.toString() );
    }
}
