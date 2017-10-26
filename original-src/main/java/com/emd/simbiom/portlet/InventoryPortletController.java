package com.emd.simbiom.portlet;

/**
 * DossierController implements the controller of the bio-dossier portlet
 *
 * Created: Mon Oct  5 08:32:24 2012
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
import java.io.File;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// import javax.persistence.EntityManager;
// import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.Property;

import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Longbox;
import org.zkoss.zul.Window;

import com.emd.zk.ZKContext;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.util.DataHasher;

import com.emd.util.ClassUtils;
import com.emd.util.Stringx;

public class InventoryPortletController extends GenericForwardComposer {
    private ZKContext zkContext;

    Window wndBrowser;

    private static Log log = LogFactory.getLog(InventoryPortletController.class);

    private long mapUser( long zkUserId ) {
	Session session = Sessions.getCurrent();
	long userId = zkUserId;
	if( session != null ) {
	    Object nativeSession = session.getNativeSession();
	    if( nativeSession != null ) {
		log.debug( "Native session: "+nativeSession+" class: "+nativeSession.getClass() );
		Object sessionId = ClassUtils.get( nativeSession, "Id", nativeSession );
		userId = DataHasher.hash( sessionId.toString().getBytes() );
		if( userId > 0 )
		    userId = (-1L) * userId;
		log.debug( "Session id: "+userId );
	    }
	}
	return userId;
    }

    private void initInventory( String portletId, long userId ) {
	log.debug( "Initializing inventory view" );
	InventoryPreferences dp = InventoryPreferences.getInstance( portletId, userId );
	dp.initViews( wndBrowser, zkContext );
	log.debug( "Inventory view initialized" );
    }

    private void initButtonCommands( String portletId, long userId ) {
	InventoryPreferences dp = InventoryPreferences.getInstance( portletId, userId );	
	InventoryCommand[] cmds = dp.getCommands();
	for( int i = 0; i < cmds.length; i++ ) {
	    Component cmp = wndBrowser.getFellowIfAny( cmds[i].getCommandName() );
	    if( cmp != null ) {
		log.debug( "Wiring component "+cmp.getId()+" to action "+cmds[i].getClass().getName() );
		cmp.addEventListener( cmds[i].getEvent(), cmds[i] );
	    }
	}
	log.debug( "Inventory actions wired" );
    }

    public void doAfterCompose(Component comp) throws Exception {
	super.doAfterCompose(comp);
	
	if( zkContext == null )
	    zkContext = ZKContext.getInstance();

	String portletId = Stringx.getDefault(zkContext.getPortletId(),"n/a");
	log.debug( "Portlet id: "+portletId );

	// long userId = mapUser( zkContext.getUserId() );
	long userId = zkContext.getUserId();
	log.debug( "User id: "+userId );

	initButtonCommands( portletId, userId );

	// initSetup( portletId, userId );

	initInventory( portletId, userId );
    }

}
