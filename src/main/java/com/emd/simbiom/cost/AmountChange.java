package com.emd.simbiom.cost;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>AmountChange</code> is invoked when the amount of a cost item is changed.
 *
 * Created: Fri Jul 19 16:32:34 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class AmountChange extends InventoryCommand {

    private static Log log = LogFactory.getLog(AmountChange.class);

    private static final String COST_ADD    = "btCostAdd";
    private static final String COST_REMOVE = "btCostRemove";

    public AmountChange() {
	super();
    }

    protected AmountChange( AmountChange aic, String suffix ) {
	super( aic, suffix );
    }

    /**
     * Copies the action and adapts the suffixes of components accordingly.
     *
     * @param suffix the new suffix
     * @return a new view action.
     */
    public AmountChange copyAction( String suffix ) {
	return new AmountChange( this, suffix );
    }

    private void setEnableItemButtons( Window wnd, Component cmp, boolean enable ) {
	String cmpId = cmp.getId();
	int k = -1;
	String suffix = null;
	if( (k = cmpId.lastIndexOf( "_" )) > 0 )
	    suffix = cmpId.substring( k );
	else
	    suffix = "";
     	Button bt = (Button)wnd.getFellowIfAny( COST_ADD+suffix );
	if( bt != null )
	    bt.setDisabled( !enable );
     	bt = (Button)wnd.getFellowIfAny( COST_REMOVE+suffix );
	if( bt != null )
	    bt.setDisabled( !enable );
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	Component cmp = event.getTarget();
	if( cmp == null ) {
	    log.error( "Cannot determine event target" );
	    return;
	}
	Window wnd = ZKContext.findWindow( cmp );
	if( (cmp instanceof Intbox) && (event instanceof InputEvent) ) {
	    int diff = Stringx.toInt(((InputEvent)event).getValue(),0);
	    setEnableItemButtons( wnd, cmp, ( diff > 0 ) );
	}
    }
}
