package com.emd.simbiom.search;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>ContentOperatorChange</code> is invoked when the the text filter elements are modified.
 *
 * Created: Tue Jun 16 16:32:34 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class ContentOperatorChange extends InventoryViewAction {

    private static Log log = LogFactory.getLog(ContentOperatorChange.class);

    public ContentOperatorChange() {
	super();
    }

    protected ContentOperatorChange( ContentOperatorChange aic, String suffix ) {
	super( aic, suffix );
    }

    /**
     * Copies the action and adapts the suffixes of components accordingly.
     *
     * @param suffix the new suffix
     * @return a new view action.
     */
    public ContentOperatorChange copyAction( String suffix ) {
	return new ContentOperatorChange( this, suffix );
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event ) throws Exception {
	Component cmp = event.getTarget();
	if( cmp == null ) {
	    log.error( "Cannot determine event target" );
	    return;
	}
	Window wnd = ZKContext.findWindow( cmp );
	TextFilter ageF = (TextFilter)getView();

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    log.debug( "Combobox items:"+cb.getItemCount() );
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 1 );
		// Map ctxt = new HashMap();
		// ctxt.put( "filterNum", "0" );
		// filters.get(0).initFilter( ZKContext.findWindow( cb ), ctxt );
	    }	    
	}
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    log.debug( "Combobox items:"+cb.getItemCount() );
	    // Combobox cb = (Combobox)event.getTarget();
	    // int idx = cb.getSelectedIndex();
	    // if( (idx >= 0) && (idx < filters.size()) ) {
	    // 	Map ctxt = new HashMap();
	    // 	ctxt.put( "filterNum", "0" );
	    // 	filters.get(idx).initFilter( ZKContext.findWindow( cb ), ctxt );
	    // }
	}

	// AgeFilter ageF = (AgeFilter)getView();
	// if( cmp instanceof Combobox ) {
	//     Comboitem ci = ((Combobox)cmp).getSelectedItem();
	//     String unitSt = (String)ci.getValue();
	//     ageF.updateComponents( wnd, unitSt );
	// }
	// else if( (cmp instanceof Intbox) && (event instanceof InputEvent) ) {
	//     int diff = Stringx.toInt(((InputEvent)event).getValue(),0);
	//     ageF.updateComponents( wnd, diff );
	// }
	// else if( (cmp instanceof Datebox) && (event instanceof InputEvent) ) {
	//     Date dt = ((Datebox)cmp).getValue();
	//     ageF.updateComponents( wnd, dt );
	// }
    }
}
