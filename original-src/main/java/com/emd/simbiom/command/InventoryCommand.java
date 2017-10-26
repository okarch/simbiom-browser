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

import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.view.UIUtils;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;
import com.emd.zk.command.ListenerCommand;

public class InventoryCommand extends ListenerCommand {
    private String               portletId;
    private long                 userId;

    private static Log log = LogFactory.getLog(InventoryCommand.class);

    /**
     * Creates a no-op button action
     */
    public InventoryCommand() {
	super();
	setCommandName( "btNoOp" );
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
    public SampleInventoryDAO getSampleInventory() {
	return InventoryPreferences.getInstance( portletId, userId ).getInventory();	
    }

    protected void showMessage( Window wnd, 
				String parentId,
				String labelId, 
				String msg ) {
	UIUtils.showMessage( wnd, parentId, labelId, msg );
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
