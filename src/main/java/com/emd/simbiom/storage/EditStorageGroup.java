package com.emd.simbiom.storage;

import java.math.BigDecimal;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
// import org.zkoss.zul.Decimalbox;
// import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.StorageGroup;
import com.emd.simbiom.model.StorageProject;

import com.emd.simbiom.view.ModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;


/**
 * <code>EditStorageGroup</code> creates / modifies storage groups.
 *
 * Created: Fri Apr 26 22:02:34 2019
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class EditStorageGroup extends InventoryCommand {

    private static Log log = LogFactory.getLog(EditStorageGroup.class);

    private static final String GROUP_ADD    = "btStorageGroupAdd";
    private static final String GROUP_DELETE = "btStorageGroupDelete";

    private static final String GROUP_STORE  = "btGroupDetailsStore";
    private static final String GROUP_CANCEL = "btGroupDetailsClose";

    private static final String MESSAGE_ID  = "rowStorageMessage";

    public EditStorageGroup() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private long extractGroupId( Component cmp ) {
	String cmpId = Stringx.getDefault(cmp.getId(),"");	
	String grpSt = StringUtils.substringBetween( cmpId, "_" );
	if( (grpSt == null) || (grpSt.length() <= 0) )
	    return 0L;
	return Stringx.toLong( grpSt.trim(), 0L ); 
    }

    private StorageGroup loadGroup( Window wnd ) {
	Grid form = (Grid)wnd.getFellowIfAny( "grGroupDetails" );
	StorageGroup grp = null;
	if( form != null ) {
	    Rows rows = form.getRows();
	    String invId = null;
	    long groupId = 0L;
	    if( rows != null ) {
		groupId = extractGroupId( rows );
		log.debug( "Storage group id: "+groupId );
		if( groupId == 0L ) {
		    log.error( "Cannot determine storage group id." );
		    return null;
		}
		SampleInventory sInv = getSampleInventory();
		if( sInv == null ) {
		    showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: Invalid data access" );
		    return null;
		}
		try {
		    grp = sInv.findStorageGroupById( groupId );
		}
		catch( SQLException sqe ) {
		    showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: "+
				 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		    log.error( sqe );
		    return null;
		}
	    }
	    else {
		log.error( "Cannot determine storage group id." );
		return null;
	    }
	    if( grp != null ) {
		log.debug( "Storage group exists: "+ grp );
	    }
	    else {
		grp = new StorageGroup();
		grp.setGroupid( groupId );
		log.debug( "New group: "+groupId );
	    }
	}
	else {
	    log.error( "Cannot determine storage group id." );
	}
	return grp;
    }

    private Window createWindow( Window wnd, String title, GroupDetailsView vDetails ) {
	Window wndDetails = new Window();
	wndDetails.setParent( wnd );
	wndDetails.setId( "wndDetails" );
	wndDetails.setTitle( title );
	wndDetails.setBorder( "normal" );
	wndDetails.setWidth( "900px" );
	wndDetails.setPosition( "center,center" );
	wndDetails.setClosable( true );
    // action="show: slideDown;hide: slideUp"

	Vlayout vl = new Vlayout();
	vl.setId( vDetails.getDetailsLayout() );
	vl.setParent( wndDetails );

	return wndDetails;
    }

    private Map createContext( StorageProject project, StorageGroup group, boolean groupExist ) {
	Map ctxt = new HashMap();

	ctxt.put( "storageProject", project );
	ctxt.put( "storageGroup", group );
	ctxt.put( "groupExist", String.valueOf(groupExist) );
	ctxt.put( "portletId", getPortletId() );
	ctxt.put( "userId", new Long(getUserId()) );

	ctxt.put( "dates", DateUtils.class );
	ctxt.put( "dateFormats", DateFormatUtils.class );

	// specific tools
	// tc.put( "samples", SampleType.class );
	// tc.put( "subjects", Subject.class );
	// tc.put( "studies", Study.class );

	// ctxt.put( "db", getSampleInventoryDAO.getInstance() );
	return ctxt;
    }

    private GroupDetailsView createDetailsView() {
	ModelProducer[] res = getPreferences().getResult( StorageGroupModel.class );
	if( res.length <= 0 ) {
	    log.error( "Cannot determine storage group model" );
	    return null;
	}
	return ((StorageGroupModel)res[0]).getDetails();
    }

    private StorageGroup collectEntry( Window wnd, StorageGroup group ) {	
	Label lb = (Label)wnd.getFellowIfAny( "lbGroupProjectId" );
	if( lb == null ) {
	    log.error( "Cannot determine storage project id" );
	    return null;
	}
	String pId = Stringx.getDefault( lb.getValue(), "0" );
	long projectId = Stringx.toLong( pId, 0L );
	log.debug( "Storage project assigned: "+projectId );
	if( projectId == 0L ) {
	    log.error( "Cannot determine storage project id" );
	    return null;
	}
	group.setProjectid( projectId );

	Textbox txt = (Textbox)wnd.getFellowIfAny( "txtGroupName" );
	if( txt == null ) {
	    log.error( "Cannot determine storage group name" );
	    return null;
	}
	String st = Stringx.getDefault( txt.getValue(), "" ).trim();
	if( st.length() <= 0 ) {
	    showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: Group name cannot be empty." );
	    return null;
	}
	group.setGroupname( st );

	txt = (Textbox)wnd.getFellowIfAny( "txtGroupRef" );
	if( txt == null ) {
	    log.error( "Cannot determine storage group reference" );
	    return null;
	}
	st = Stringx.getDefault( txt.getValue(), "" ).trim();
	if( st.length() > 0 ) 
	    group.setGroupref( st );
	    
	return group;
    }

    private boolean storeGroup( Window wnd, StorageGroup group ) {
	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: Invalid data access" );
	    return false;
	}
	try {
	    StorageProject prj = sInv.findStorageProjectById( group.getProjectid() );
	    if( prj == null ) {
		showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: Cannot determine storage project" );
		return false;
	    }
	    StorageGroup[] grps = prj.getStorageGroups();
	    boolean doUpdate = false;
	    log.debug( "Number of existing storage groups: "+grps.length );
	    for( int i = 0; i < grps.length; i++ ) {
		if( grps[i].equals( group ) ) {
		    doUpdate = true;
		    break;
		}
	    }
	    boolean groupAssigned = false;
	    for( int i = 0; i < grps.length; i++ ) {
		if( grps[i].getGroupname().equals( group.getGroupname() ) ) {
		    if( (!doUpdate && !grps[i].equals( group )) || groupAssigned ) {

			showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: Group \""+group+"\" exists already." );
			return false;
		    }
		    else if( doUpdate && grps[i].equals( group )) {
			grps[i] = group;
			groupAssigned = true;
		    }
		}
	    }
	    if( doUpdate ) {
		prj.setStorageGroups( grps );
		log.debug( "Storage group updated: "+group );
	    }
	    else {
		prj.addStorageGroup( group );
		log.debug( "Storage group added: "+group );
	    }
	    sInv.storeStorageProject( prj ); 
	    return true;
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowGroupDetailsMessage", "lbGroupDetailsMessage", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	}
	return false;
    }

    private void deleteGroup( StorageGroup group ) {
	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    log.error( "Invalid database access" );
	    return;
	}
	StorageProject prj = null;
	try {
	    prj = sInv.findStorageProjectById( group.getProjectid() );
	    if( prj == null ) {
		log.error( "Cannot find storage project of group "+group );
		return;
	    }
	    prj.removeStorageGroup( group );
	    sInv.storeStorageProject( prj );
	    log.debug( "Storage group "+group+" has been removed" );
	}
	catch( SQLException sqe ) {
	    prj = null;
	    log.error( sqe );
	}
    }

    private void updateModel( Window wnd, StorageGroup group ) {
	log.debug( "Updating storage project list" );
	ModelProducer[] res = getPreferences().getResult( StorageProjectModel.class );
	if( res.length <= 0 ) {
	    log.error( "Cannot determine storage project model" );
	    return;
	}
	StorageProjectModel pMod = (StorageProjectModel)res[0];
	pMod.reloadStorageProjects( wnd, group.getProjectid() );
	
	SampleInventory sInv = getSampleInventory();
	if( sInv == null ) {
	    log.error( "Invalid database access" );
	    return;
	}
	StorageProject prj = null;
	try {
	    prj = sInv.findStorageProjectById( group.getProjectid() );
	}
	catch( SQLException sqe ) {
	    // showMessage( wnd, MESSAGE_ID, "lbStorageMessage", "Error: "+
	    // 		 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    prj = null;
	    log.error( sqe );
	}
	
	if( prj != null ) {
	    log.debug( "Updating storage group model" );
	    SelectStorageDetails bItem = (SelectStorageDetails)getPreferences().getCommand( SelectStorageDetails.class );
	    if ( bItem == null ) { 
		log.error( "Cannot determine storage project model" );
		return;
	    }
	    bItem.setGroups( wnd, prj );
	}
    }

    private void displayDetails( Window wnd, StorageProject project, StorageGroup group, boolean groupExist ) {
	log.debug( "Display details of "+group );

	GroupDetailsView vDetails = createDetailsView();
	if( vDetails == null ) {
	    showMessage( wnd, MESSAGE_ID, "lbStorageMessage", "Error: Cannot create group details" );
	    log.error( "Cannot create view, check configuration" );
	    return;
	}

	Window wndDetails = createWindow( wnd, "Details of "+group, vDetails );
	final Window parentWindow = wnd;
	final StorageGroup sGroup = group;
	final StorageProject sProject = project;
	wndDetails.addEventListener( Events.ON_CLOSE, new EventListener() {
		public void onEvent( Event evt ) {
		    log.debug( "Closing event received. Storage group: "+sGroup+" storage project: "+sProject );
		    StorageGroup updatedGroup = (StorageGroup)evt.getData();
		    if( updatedGroup != null ) {
		     	updateModel( parentWindow, updatedGroup );
			showMessage( parentWindow, MESSAGE_ID, "lbStorageMessage", "Storage group \""+updatedGroup+"\" has been stored" );
		    }
		    else
		     	log.debug( "No change of storage group" );
		}
	    });

	vDetails.updateActions( getPortletId(), getUserId() );
	// vDetails.setTemplate( getDetailsTemplate() );
        vDetails.setShowMergeResult( true );
	vDetails.initView( wndDetails, createContext( project, group, groupExist) );
	
	wndDetails.doModal();
    }

    private void confirmDelete( Window wnd, StorageGroup group ) {
	final Window targetWnd = wnd;
	final StorageGroup sGroup = group;
	Messagebox.show( "Do you want to delete storage group \""+group+"\"?",
			 "Delete Storage Group", 
			 Messagebox.YES+Messagebox.NO, 
			 Messagebox.QUESTION,
			 new EventListener() {
			     public void onEvent(Event event) {
				 if( (Messagebox.ON_YES.equals(event.getName()))) {
				     log.debug( "Storage group "+sGroup+" delete confirmed." );
				     deleteGroup( sGroup );
				     updateModel( targetWnd, sGroup );
				     showMessage( targetWnd, MESSAGE_ID, "lbStorageMessage", "Storage group \""+sGroup+"\" has been removed" );
				 }
			     }
			 });
    }

    private StorageProject getSelectedProject( Window wnd ) {
	ModelProducer[] res = getPreferences().getResult( StorageProjectModel.class );
	if( res.length <= 0 ) {
	    log.error( "Cannot determine storage project model" );
	    return null;
	}
	StorageProjectModel pMod = (StorageProjectModel)res[0];
	StorageProject project = pMod.getSelectedStorageProject( wnd );
	if( project == null ) {
	    log.error( "Cannot determine storage project" );
	    return null;
	}
	return project;
    }

    private StorageGroup getSelectedGroup( Window wnd ) {
	ModelProducer[] res = getPreferences().getResult( StorageGroupModel.class );
	if( res.length <= 0 ) {
	    log.error( "Cannot determine storage group model" );
	    return null;
	}
	StorageGroupModel pMod = (StorageGroupModel)res[0];
	StorageGroup project = pMod.getSelectedStorageGroup( wnd );
	if( project == null ) {
	    log.error( "Cannot determine storage group" );
	    return null;
	}
	return project;
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	log.debug( "Edit storage group: "+event );

	Component cmp = event.getTarget();
	Window wnd = UIUtils.getWindow( event );

	if( GROUP_CANCEL.equals(cmp.getId() ) ) {
	    Events.postEvent( "onClose", wnd, null );
	}
	else if( GROUP_STORE.equals(cmp.getId() ) ) {
	    StorageGroup sg = loadGroup( wnd );
	    if( sg == null )
		return;
	    sg = collectEntry( wnd, sg );
	    if( sg!= null ) {
		log.debug( "Storage group loaded: "+sg );
		if( storeGroup( wnd, sg ) )
		    Events.postEvent( "onClose", wnd, sg );
	    }
	    return;
	}

	StorageProject project = getSelectedProject( wnd );
	if( project == null )
	    return;
	StorageGroup group = null;
	boolean groupExist = true;
	if( GROUP_ADD.equals(cmp.getId()) ) {
	    group = new StorageGroup();
	    group.setGroupname( "No name" );
	    group.setProjectid( project.getProjectid() );
	    groupExist = false;
	}
	else if( GROUP_DELETE.equals(cmp.getId()) ) {
	    group = getSelectedGroup( wnd );
	    log.debug( "About to delete storage group: "+group );
	    if( group != null )
		confirmDelete( wnd, group );
	    group = null;
	}

	if( group != null )
	    displayDetails( wnd, project, group, groupExist );
	
    }


    /**
     * Executes the <code>Command</code>.
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	// String suff = addBillingRow( wnd );
	// registerPreferences( wnd, suff );

    }

}
