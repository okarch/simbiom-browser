package com.emd.simbiom.search;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
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
 * <code>AgeInputChange</code> is invoked when the unit of the age is selected.
 *
 * Created: Fri May 29 16:32:34 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class AgeInputChange extends InventoryViewAction {

    private static Log log = LogFactory.getLog(AgeInputChange.class);

    public AgeInputChange() {
	super();
    }

    protected AgeInputChange( AgeInputChange aic, String suffix ) {
	super( aic, suffix );
    }

    /**
     * Copies the action and adapts the suffixes of components accordingly.
     *
     * @param suffix the new suffix
     * @return a new view action.
     */
    public AgeInputChange copyAction( String suffix ) {
	return new AgeInputChange( this, suffix );
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Age filter input event: "+event );
	Component cmp = event.getTarget();
	if( cmp == null ) {
	    log.error( "Cannot determine event target" );
	    return;
	}
	Window wnd = ZKContext.findWindow( cmp );
	AgeFilter ageF = (AgeFilter)getView();
	if( cmp instanceof Combobox ) {
	    Comboitem ci = ((Combobox)cmp).getSelectedItem();
	    String unitSt = (String)ci.getValue();
	    ageF.updateComponents( wnd, unitSt );
	}
	else if( (cmp instanceof Intbox) && (event instanceof InputEvent) ) {
	    int diff = Stringx.toInt(((InputEvent)event).getValue(),0);
	    ageF.updateComponents( wnd, diff );
	}
	else if( (cmp instanceof Datebox) && (event instanceof InputEvent) ) {
	    Date dt = ((Datebox)cmp).getValue();
	    ageF.updateComponents( wnd, dt );
	}
    }
}
