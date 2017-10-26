package com.emd.simbiom.search;

/**
 * SelectFilterOperator selects the boolean search operator and adds new filter criteria if needed
 *
 * Created: Fri Jul  3 12:43:39 2015
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

// import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.view.ModelProducer;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

public class ResultMenu extends InventoryCommand {

    private static Log log = LogFactory.getLog(ResultMenu.class);


    /**
     * Creates a new command to selecte the search operator.
     */
    public ResultMenu() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    // private int nextFilterCount( Window wnd ) {
    // 	String[] toks = getCommandName().split( "[_]" );
    // 	if( toks.length < 2 ) {
    // 	    log.warn( "Component name is invalid: "+getCommandName() );
    // 	    return -1;
    // 	}
    // 	int suff = Stringx.toInt(toks[1],0) + 1;
    // 	return (((Combobox)wnd.getFellowIfAny( "cbFilterOperator_"+String.valueOf(suff) ) != null)?-1:suff);
    // }

    // private Row createRow( int nfCount ) {

    // 	// <row id="rowFilter_0">
    // 	Row row = new Row();
    // 	String ext  = "_"+String.valueOf( nfCount );
    // 	row.setId( "rowFilter"+ext );

    // 	//   <combobox id="cbFilterSelector_0" autodrop="true" width="200px" mold="rounded" buttonVisible="true"/>
    // 	Combobox cb = new Combobox();
    // 	cb.setId( "cbFilterSelector"+ext );
    // 	cb.setAutodrop( true );
    // 	cb.setWidth( "200px" );
    // 	cb.setMold( "rounded" );
    // 	cb.setButtonVisible( true );
    // 	cb.setParent( row );

    // 	//   <hlayout id="hlFilter_0"/>
    // 	Hlayout hl = new Hlayout();
    // 	hl.setId( "hlFilter"+ext );
    // 	hl.setParent( row );

    // 	//   <combobox id="cbFilterOperator_0" autodrop="true" width="100px" mold="rounded" buttonVisible="true">
    // 	cb = new Combobox();
    // 	cb.setId( "cbFilterOperator"+ext );
    // 	cb.setAutodrop( true );
    // 	cb.setWidth( "100px" );
    // 	cb.setMold( "rounded" );
    // 	cb.setButtonVisible( true );
    // 	cb.setParent( row );

    // 	for( int i = 0; i < OPERATOR_ITEMS.length; i++ ) {
    // 	    Comboitem ci = cb.appendItem( OPERATOR_ITEMS[i] );
    // 	    ci.setValue( (OPERATOR_ITEMS[i].length() <= 0)?OPERATOR_ITEMS[1].toLowerCase():OPERATOR_ITEMS[i].toLowerCase() );
    // 	}
    // 	return row;
    // }

		
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

	log.debug( "On open event" );

    }    
    
} 
