package com.emd.simbiom.command;

/**
 * <code>InventoryCommand</code> is a no-op implementation of inventory browser portlet's button actions
 * which can be used to extend it.
 *
 * Created: Sun Mar 22 08:28:07 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.config.InventoryPreferences;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.view.UIUtils;
import com.emd.simbiom.view.ModelProducer;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;
import com.emd.zk.command.ListenerCommand;

import com.emd.util.Stringx;

public class InventoryCommand extends ListenerCommand {
    private String               portletId;
    private long                 userId;
    private String messageRowId;

    private static Log log = LogFactory.getLog(InventoryCommand.class);

    /**
     * Creates a no-op button action
     */
    public InventoryCommand() {
	super();
	setCommandName( "btNoOp" );
    }

    protected InventoryCommand( InventoryCommand va, String suffix ) {
	super();
	this.portletId = va.getPortletId();
	this.userId = va.getUserId();
	this.setEvent( va.getEvent() );
	String cmpId = va.getCommandName();
	int k = -1;
	if( (cmpId != null) && ((k = cmpId.lastIndexOf( "_" )) > 0) ) {
	    if( suffix.startsWith( "_" ) )
		cmpId = cmpId.substring( 0, k )+suffix;
	    else
		cmpId = cmpId.substring( 0, k )+"_"+suffix;
	}
	this.setCommandName( cmpId );
    }

    /**
     * Get the PortletId value.
     * @return the PortletId value.
     */
    public String getPortletId() {
	return portletId;
    }

    /**
     * Set the PortletId value.
     * @param newPortletId The new PortletId value.
     */
    public void setPortletId(String newPortletId) {
	this.portletId = newPortletId;
    }

    /**
     * Get the UserId value.
     * @return the UserId value.
     */
    public long getUserId() {
	return userId;
    }

    /**
     * Set the UserId value.
     * @param newUserId The new UserId value.
     */
    public void setUserId(long newUserId) {
	this.userId = newUserId;
    }

    /**
     * Convenience method to return the dossier database.
     * @return dossier preferences
     */
    public SampleInventory getSampleInventory() {
	return InventoryPreferences.getInstance( portletId, userId ).getInventory();	
    }
    // public SampleInventoryDAO getSampleInventory() {
    // 	return InventoryPreferences.getInstance( portletId, userId ).getInventory();	
    // }

    protected ModelProducer findModelProducer( Class modelClass ) {
	ModelProducer[] mps = InventoryPreferences.getInstance( portletId, userId ).getResult( modelClass );
	return (( mps.length > 0 )?mps[0]:null);
    }

    protected void showMessage( Window wnd, 
				String parentId,
				String labelId, 
				String msg ) {
	UIUtils.showMessage( wnd, parentId, labelId, msg );
    }

    /**
     * Get the <code>MessageRowId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessageRowId() {
	return messageRowId;
    }

    /**
     * Set the <code>MessageRowId</code> value.
     *
     * @param messageRowId The new MessageRowId value.
     */
    public final void setMessageRowId(final String messageRowId) {
	this.messageRowId = messageRowId;
    }

    private String getLabelId() {
	return Stringx.getDefault(getMessageRowId(),"rowMessage").replace( "row", "lb" );
    }

    protected void writeMessage( Window wnd, String msg ) {
	UIUtils.showMessage( wnd, Stringx.getDefault(getMessageRowId(),"rowMessage"), getLabelId(), msg );
    }

    protected void writeMessage( Grid grid, String msg ) {
	Window wnd = ZKContext.findWindow( grid );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }
    protected void writeMessage( Listbox listbox, String msg ) {
	Window wnd = ZKContext.findWindow( listbox );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }
    protected void writeMessage( Combobox combobox, String msg ) {
	Window wnd = ZKContext.findWindow( combobox );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }

    /**
     * Adds an event listener to the component.
     *
     * @param wnd 
     *     an {@link  org.zkoss.zul.Window} object representing the form
     */
    public void wireComponent( Window wnd ) {
	Component cmp = wnd.getFellowIfAny( this.getCommandName() );
	if( cmp != null ) {
	    log.debug( "Wiring component "+cmp.getId()+" to action "+this.getClass().getName() );
	    cmp.addEventListener( this.getEvent(), this );
	}
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

	log.info( "Inventory command not implemented, yet" );
    }

}
