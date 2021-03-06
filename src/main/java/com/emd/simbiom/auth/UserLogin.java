package com.emd.simbiom.auth;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Roles;
import com.emd.simbiom.model.User;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.CookieUtil;
import com.emd.zk.command.CommandException;

/**
 * UserLogin evaluates username and password.
 *
 * Created: Sun Feb 12 12:43:39 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class UserLogin extends InventoryCommand {

    private static Log log = LogFactory.getLog(UserLogin.class);

    private static final int COOKIE_AGE = 160 * 24 * 60 * 60;  // 160 days

    private static final String CHK_REMEMBER = "chkLoginRember";

    private static final String TXT_USER     = "txtLoginUser";
    private static final String TXT_PASSWORD = "txtLoginPassword";
    private static final String GUEST        = "guest";

    public static final String USER_KEY     = "simbiom.login.user";
    public static final String USER_COOKIE  = "simbiomMUID";
    public static final String LABEL_USER   = "lbCurrentUser_";

    public static final String TAB_UPLOAD   = "tabUpload";
    public static final String TAB_STORAGE  = "tabStorage";

    public static final String GBOX_INVOICE = "gInvoices";

    public static final String LIST_ROLES   = "liLoginRole";

    /**
     * Creates a new command to login user.
     */
    public UserLogin() {
	super();
    }

    // private InventoryPreferences getPreferences() {
    // 	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    // }

    private String getUsername( Window wnd ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( TXT_USER );
	String term = null;
	if( tb != null ) 
	    term = Stringx.getDefault(tb.getValue(),GUEST).trim();
	else
	    term = GUEST;
	return term;
    }

    private String getPassword( Window wnd ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( TXT_PASSWORD );
	String term = null;
	if( tb != null ) 
	    term = Stringx.getDefault(tb.getValue(),"").trim();
	else
	    term = "";
	return term;
    }

    private User validateUser( Window wnd, String usrName, String pwd ) {
	User usr = null;
	SampleInventory dao = getSampleInventory();
	// SampleInventoryDAO dao = getSampleInventory();
	try {
	    usr = dao.findUserByMuid( usrName );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}

	boolean auth = false;
	if( (usr != null) && (pwd.equals(usr.getApikey())) ) 
	    auth = true;

	if( !auth ) {
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Warning: User "+usrName+
			 " could not be authenticated. Guest account will be used" );
	    try {
		usr = dao.findUserByMuid( GUEST );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		log.error( sqe );
		return null;
	    }
	}
	return usr;
    }

    /**
     * Initializes the GUI according the user roles.
     *
     * @param wnd the app window.
     * @param usr the app user.
     *
     */
    public void setupUser( Window wnd, User usr, boolean checkCookie ) {
	updateRoleList( wnd, usr );
	storeUser( wnd, usr, checkCookie );
	setEnableUpload( wnd, usr.hasRole( Roles.INVENTORY_UPLOAD ) );	
	setEnableStorage( wnd, usr.hasRole( Roles.STORAGE_EDIT ) );
	setEnableInvoice( wnd, usr.hasRole( Roles.INVOICE_EDIT ) );
    }

    private void setEnableUpload( Window wnd, boolean enable ) {
	Tab tab = (Tab)wnd.getFellowIfAny( TAB_UPLOAD );
	if( tab != null ) 
	    tab.setDisabled( !enable );
    }
    private void setEnableStorage( Window wnd, boolean enable ) {
	Tab tab = (Tab)wnd.getFellowIfAny( TAB_STORAGE );
	if( tab != null ) 
	    tab.setDisabled( !enable );
    }
    private void setEnableInvoice( Window wnd, boolean enable ) {
	Groupbox gb = (Groupbox)wnd.getFellowIfAny( GBOX_INVOICE );
	if( gb != null ) 
	    gb.setVisible( enable );
    }

    private void storeUser( Window wnd, User usr, boolean checkCookie ) {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    ses.setAttribute( USER_KEY, usr );
	    if( checkCookie ) {
		Checkbox cb = (Checkbox)wnd.getFellowIfAny( CHK_REMEMBER );
		if( (cb != null) && (cb.isChecked()) ) {
		    String cValue = usr.getMuid()+":"+String.valueOf(usr.getUserid());
		    Cookie cookie = CookieUtil.addCookie( USER_COOKIE, cValue, COOKIE_AGE );
		    log.debug( "Cookie "+USER_COOKIE+" has been set to "+cValue );
		}
	    }
	}
	for( int i = 1; i < Integer.MAX_VALUE; i++ ) {
	    Label lb = (Label)wnd.getFellowIfAny( LABEL_USER+String.valueOf(i) );
	    if( lb != null )
		lb.setValue( usr.getMuid()+" - "+usr.getUsername() );
	    else
		break;
	}
    }

    private void updateRoleList( Window wnd, User usr ) {
	Listbox lBox = (Listbox)wnd.getFellowIfAny( LIST_ROLES );
	if( lBox != null ) {
	    String[] roles = Roles.assignedRoles( usr.getRoles() );
	    log.debug( "Number of roles assigned to "+usr+": "+roles.length );
	    lBox.setModel( new ListModelArray( roles ) );
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

	log.debug( "Login user" );

	String userName = getUsername( wnd );
	String password = getPassword( wnd );

	User usr = validateUser( wnd, userName, password );
	if( usr == null )
	    return;

	showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "User "+usr+" logged in sucessfully" );

	updateRoleList( wnd, usr );
	storeUser( wnd, usr, true );
	setEnableUpload( wnd, usr.hasRole( Roles.INVENTORY_UPLOAD ) );	
	setEnableStorage( wnd, usr.hasRole( Roles.STORAGE_EDIT ) );
	setEnableInvoice( wnd, usr.hasRole( Roles.INVOICE_EDIT ) );
    }    
    
}
