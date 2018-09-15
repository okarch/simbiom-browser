package com.emd.simbiom.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>UpdateColumns</code> updates the column selection of the sample query results.
 *
 * Created: Tue Feb 27 18:49:40 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class UpdateColumns extends InventoryViewAction {

    private static Log log = LogFactory.getLog(UpdateColumns.class);

    public UpdateColumns() {
	super();
    }

    private void collectValues( Component pCmd, List<String> chkValues ) {
	for( Component cmp : pCmd.getChildren() ) {
	    String cmpId = cmp.getId();
	    if( (cmpId != null) && (cmpId.startsWith( "chkShow_" )) &&
		(cmp instanceof Checkbox) && (((Checkbox)cmp).isChecked()) ) {
		Object st = ((Checkbox)cmp).getValue();		
		StringBuilder stb = new StringBuilder( cmpId );
		stb.append( ":" );
		if( st != null )
		    stb.append( st.toString() );
		chkValues.add( stb.toString() );
	    }
	    collectValues( cmp, chkValues );
	}
    }

    private String[] createPaths( Window wnd, List<String> chkValues ) {
	String[] paths = new String[ chkValues.size() ];
	int i = 0;
	for( String pe : chkValues ) {
	    String cmpId = Stringx.before( pe, ":" );
	    String path = Stringx.after( pe, ":" );
	    String pName = Stringx.after( cmpId, "_" );
	    Label lb = (Label)wnd.getFellowIfAny( "lbProperty_"+pName );
	    if( lb != null ) {
		String label = lb.getValue();
		paths[i] = Stringx.getDefault( label, "" ).trim()+":"+path;
	    }
	    else
		paths[i] = pe;
	    i++;
	}
	return paths;
    }

    private ColumnSetup updateColumnSetup( String[] paths ) {
	ColumnSetup colSetup = ColumnSetup.getInstance();

	for( int i = 0; i < paths.length; i++ ) {
	    DisplayColumn dCol = new DisplayColumn();
	    dCol.setColumnId( "colSearch_"+i );
	    String lb = Stringx.before( paths[i], ":" );
	    dCol.setLabel( lb );
	    dCol.setContentField( Stringx.after( paths[i], ":" ) );
	    dCol.setSort( "string" );

	    colSetup.updateDisplayColumn( dCol );
	}
	return colSetup;
    }

    // private void assignColumnSetup( ColumnSetup colSetup ) {
    // 	ModelProducer[] mps = getPreferences().getResult( SampleResult.class );
	
    // }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Update column event: "+event );
	Component cmp = event.getTarget();
	if( cmp == null ) {
	    log.error( "Cannot determine event target" );
	    return;
	}
	Window wnd = ZKContext.findWindow( cmp );
	if( wnd == null ) {
	    log.error( "Cannot determine window" );
	    return;
	}
	List<String> chkValues = new ArrayList<String>();
	collectValues( wnd, chkValues );
	log.debug( chkValues );

	String[] paths = createPaths( wnd, chkValues );
	ColumnSetup colSetup = updateColumnSetup( paths );	
	log.debug( "Updated column setup: "+colSetup );

	Events.postEvent("onClose", wnd, null);
    }

}
