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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.Property;

import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

import com.emd.zk.CookieUtil;
import com.emd.zk.ZKContext;

import com.emd.simbiom.auth.UserLogin;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.User;

import com.emd.simbiom.util.DataHasher;

import com.emd.util.ClassUtils;
import com.emd.util.Stringx;

public class InventoryPortletController extends GenericForwardComposer {
    private ZKContext zkContext;

    Window wndBrowser;

    private static Log log = LogFactory.getLog(InventoryPortletController.class);

    private static final String TABBOX_ID = "tbBrowser";

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

    private void initTabs() {
     	String showPanels = Stringx.getDefault(Executions.getCurrent().getParameter("show"),"all");
     	String hidePanels = Stringx.getDefault(Executions.getCurrent().getParameter("hide"),""); 
    	Tabbox tb = (Tabbox)wndBrowser.getFellowIfAny( TABBOX_ID );
     	List<String> aPanels = new ArrayList<String>();
     	List<String> aPanelIds = new ArrayList<String>();
     	if( tb != null ) {
     	    Tabs tabs = tb.getTabs();
     	    if( tabs != null ) {
     		List<Component> cmps = tabs.getChildren();
     		for( Component cmp : cmps ) {
     		    if( cmp instanceof Tab ) 
     			aPanels.add( Stringx.getDefault(((Tab)cmp).getLabel(),"") );
     		}
     	    }
     	    Tabpanels tPanels = tb.getTabpanels();
     	    if( tPanels != null ) {
     		List<Component> cmps = tPanels.getChildren();
     		for( Component cmp : cmps ) {
     		    if( cmp instanceof Tabpanel )
     			aPanelIds.add( Stringx.getDefault(cmp.getId(),"") );
     		}
     	    }
     	}
     	String[] panels = new String[ aPanels.size() ];
     	panels = (String[])aPanels.toArray( panels );
     	if( panels.length <= 0 ) {
     	    log.debug( "Using default panel setup" );
     	    return;
     	}
     	String[] sPanels = showPanels.split( "," );
     	List<String> panelIds = new ArrayList<String>();
     	for( int i = 0; i < sPanels.length; i++ ) {
     	    if( "all".equalsIgnoreCase(sPanels[i]) ) {
     		panelIds.addAll(aPanelIds);
     		break;
     	    }
     	    for( int j = 0; j < panels.length; j++ ) {
     		if( panels[j].equalsIgnoreCase(sPanels[i]) ) {
     		    if( j < aPanelIds.size() ) {
     			panelIds.add( aPanelIds.get(j) );
     			break;
     		    }
     		    else {
     			log.debug( "Cannot find panel id of \""+sPanels[i]+"\"" );
     		    }
     		}
     	    }
     	}
     	if( panelIds.size() <= 0 )
     	    panelIds.addAll(aPanelIds);

     	log.debug( "panels to show: "+panelIds );
	
     	String[] hPanels = hidePanels.split( "," );
     	for( int i = 0; i < hPanels.length; i++ ) {
     	    for( int j = 0; j < panels.length; j++ ) {
     		if( panels[j].equalsIgnoreCase(hPanels[i]) ) {
     		    if( j < aPanelIds.size() ) {
     			String dPanelId = aPanelIds.get(j);
     			int delIdx = 0;
     			boolean foundIt = false;
     			for( String sId : panelIds ) {
     			    if( sId.equals( dPanelId ) ) {
     				foundIt = true;
     				break;
     			    }
     			    delIdx++;
     			}
     			if( foundIt )
     			    panelIds.remove( delIdx );
     			break;
     		    }
     		    else {
     			log.debug( "Cannot find panel id of \""+hPanels[i]+"\"" );
     		    }
     		}
     	    }
     	}

     	for( String pId : aPanelIds ) {
     	    Component cmp = wndBrowser.getFellowIfAny( pId );
     	    if( cmp != null ) 
     		cmp.setVisible( false ); 
	    String tabId = "tab"+pId.substring(2);
	    cmp = wndBrowser.getFellowIfAny( tabId );
     	    if( cmp != null ) 
     		cmp.setVisible( false ); 
     	}

	String firstPanelId = null;
     	for( String pId : panelIds ) {
     	    Component cmp = wndBrowser.getFellowIfAny( pId );
     	    if( cmp != null ) {
     		cmp.setVisible( true ); 
		if( firstPanelId == null )
		    firstPanelId = pId;
	    }
	    String tabId = "tab"+pId.substring(2);
	    cmp = wndBrowser.getFellowIfAny( tabId );
     	    if( cmp != null ) 
     		cmp.setVisible( true ); 
     	}

	if( tb != null ) {
	    log.debug( "Set selected panel: "+firstPanelId );
     	    Tabpanel tp = (Tabpanel)wndBrowser.getFellowIfAny( firstPanelId );
	    if( tp != null )
		tb.setSelectedPanel( tp );
	}
	
    }

    private void initUserSession( String portletId, long userId ) {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    // SampleInventoryDAO dao = InventoryPreferences.getInstance( portletId, userId ).getInventory();
	    SampleInventory dao = InventoryPreferences.getInstance( portletId, userId ).getInventory();
	    try {
		User usr = dao.findUserById( userId );
		if( usr != null ) {
		    InventoryPreferences dp = InventoryPreferences.getInstance( portletId, userId );	
		    UserLogin uCmd = (UserLogin)dp.getCommand( UserLogin.class );
		    String uid = CookieUtil.getCookieValue( UserLogin.USER_COOKIE );
		    if( uCmd != null )
			uCmd.setupUser( wndBrowser, usr, (uid==null) );

		    // ses.setAttribute( UserLogin.USER_KEY, usr );
		    // boolean disable = !usr.hasRole( Roles.INVENTORY_UPLOAD );
		    // Tab tab = (Tab)wndBrowser.getFellowIfAny( UserLogin.TAB_UPLOAD );
		    // if( tab != null ) 
		    // 	tab.setDisabled( disable );
		    // for( int i = 1; i < Integer.MAX_VALUE; i++ ) {
		    // 	Label lb = (Label)wndBrowser.getFellowIfAny( UserLogin.LABEL_USER+String.valueOf(i) );
		    // 	if( lb != null )
		    // 	    lb.setValue( usr.getMuid()+" - "+usr.getUsername() );
		    // 	else
		    // 	    break;
		    // }
		}
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
	    }
	}
    }

    private long getUserId() {
	String uid = CookieUtil.getCookieValue( UserLogin.USER_COOKIE );
	long userId = zkContext.getUserId();
	if( uid != null ) {
	    userId = Stringx.toLong(Stringx.after(uid,":"),userId);
	    log.info( "Remembering user id "+uid );
	}
	return userId;
    }
	
    public void doAfterCompose(Component comp) throws Exception {
	super.doAfterCompose(comp);
	
	if( zkContext == null )
	    zkContext = ZKContext.getInstance();

	String portletId = Stringx.getDefault(zkContext.getPortletId(),"n/a");
	log.debug( "Portlet id: "+portletId );

	// long userId = mapUser( zkContext.getUserId() );
	// long userId = zkContext.getUserId();

	long userId = getUserId();
	log.debug( "User id: "+userId );

	initButtonCommands( portletId, userId );

	// initSetup( portletId, userId );

	initInventory( portletId, userId );

	initTabs();

	initUserSession( portletId, userId );
    }

}
