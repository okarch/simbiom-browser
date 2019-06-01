package com.emd.simbiom.storage;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import org.zkoss.zul.impl.InputElement;
// import org.zkoss.zul.impl.NumberInputElement;

import com.emd.simbiom.command.InventoryCommand;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>PurchaseChange</code> is invoked when the purchase value is changing.
 *
 * Created: Mon May 20 8:32:34 2019
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class PurchaseChange extends InventoryCommand {

    private static Log log = LogFactory.getLog(PurchaseChange.class);

    private static final String COST_ADD    = "btBillingAdd";
    private static final String COST_REMOVE = "btBillingRemove";

    public PurchaseChange() {
	super();
    }

    protected PurchaseChange( PurchaseChange aic, String suffix ) {
	super( aic, suffix );
    }

    /**
     * Copies the action and adapts the suffixes of components accordingly.
     *
     * @param suffix the new suffix
     * @return a new view action.
     */
    public PurchaseChange copyAction( String suffix ) {
	return new PurchaseChange( this, suffix );
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
	if( bt != null ) {
	    if( "_0".equals(suffix) )
		bt.setDisabled( true );
	    else
		bt.setDisabled( !enable );
	}
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
	if( (cmp instanceof InputElement) && (event instanceof InputEvent) ) {
	    // double diff = Stringx.toDouble(((InputEvent)event).getValue(),0d);
	    String diff = Stringx.getDefault(((InputEvent)event).getValue(),"");
	    setEnableItemButtons( wnd, cmp, ( diff.length() > 0 ) );
	}
    }
}
