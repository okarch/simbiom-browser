package com.emd.simbiom.storage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.StorageProject;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKUtil;

/**
 * <code>StorageProjectModel</code> holds the storage projects.
 *
 * Created: Mon Sep 17 17:05:09 2018
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class StorageProjectModel extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(StorageProjectModel.class);

    // public static final String COMPONENT_ID = "cbTemplate";
    public static final String RESULT = "result";

    // public static final String TXT_TEMPLATE      = "txtTemplate";
    // public static final String TXT_TEMPLATENAME  = "txtTemplateName";

    private static final Comparator<StorageProject> PROJECT_SORTER = new Comparator<StorageProject>() {
	public int compare(StorageProject o1, StorageProject o2) {
	    if( (o1 == null) && (o2 == null) )
		return 0;
	    return o1.getTitle().compareTo( o2.getTitle() );
	}
	public boolean equals(Object obj) {
	    return false;
	}
    };

    public StorageProjectModel() {
	super();
	setModelName( "unknown" );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	try {
	    StorageProject[] tList = dao.findStorageProject( "" );

	    Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	    if( cbTempl != null ) {
		if( context == null )
		    context = new HashMap();

		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    }
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}
    }

    /**
     * Returns the selected storage project.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    public StorageProject getSelectedStorageProject( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	StorageProject templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    if( sel >= 0 )
		templ = (StorageProject)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    /**
     * Selects a given storage project.
     *
     * @param wnd the app window.
     * @param title the storage project title.
     */ 
    public void setSelectedStorageProject( Window wnd, String title ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	ListModel model = null;
	if( (cbTempl != null) && ((model = cbTempl.getModel()) != null) ) {
	    int idx = -1;
	    for( int i = 0; i < model.getSize(); i++ ) {
		StorageProject prj = (StorageProject)model.getElementAt(i);
		if( title.equals(prj.getTitle()) ) {
		    idx = i;
		    break;
		}
	    }
	    if( idx >= 0 )
		pushAfterRenderIndex( idx );
	}
    }

    private void pushAfterRenderIndex( int idx ) {
	Session ses = Sessions.getCurrent();
	if( ses != null )
	    ses.setAttribute( getModelName()+".afterRenderIndex", new Integer(idx) );
    }
    private int popAfterRenderIndex( int def ) {
	Session ses = Sessions.getCurrent();
	int idx = def;
	if( ses != null ) {
	    Integer ari = (Integer)ses.getAttribute( getModelName()+".afterRenderIndex" );
	    if( ari != null ) {
		idx = ari.intValue();
		ses.removeAttribute( getModelName()+".afterRenderIndex" );
	    }
	}
	return idx;
    }

    /**
     * Reloads the list of templates from the database and selects the given
     * template.
     *
     * @param wnd the app window.
     * @param selTemplate the upload template currently selected (or null).
     */ 
    // public void reloadTemplates( Window wnd, InventoryUploadTemplate selTemplate ) {
    // 	// SampleInventoryDAO dao = getSampleInventory();
    // 	SampleInventory dao = getSampleInventory();
    // 	if( dao == null ) {
    // 	    writeMessage( wnd, "Error: No database access configured" );
    // 	    return;
    // 	}

    // 	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	try {
    // 	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

    // 	    if( cbTempl != null ) {
    // 		Map context = new HashMap();
    // 		context.put( RESULT, tList );
    // 		assignModel( cbTempl, context );
    // 	    } 
    // 	}
    // 	catch( SQLException sqe ) {
    // 	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
    // 	    log.error( sqe );
    // 	}

    // 	ListModel tModel = null;
    // 	if( (selTemplate != null) &&
    // 	    (cbTempl != null) && 
    // 	    ((tModel = cbTempl.getModel()) != null) ) {

    // 	    log.debug( "Find index of template \""+selTemplate.getTemplatename()+"\"" );
    // 	    int idx = -1;
    // 	    InventoryUploadTemplate templ = null;
    // 	    for( int i = 0; i < tModel.getSize(); i++ ) {
    // 		templ = (InventoryUploadTemplate)tModel.getElementAt( i );
    // 		if( templ.getTemplatename().equals( selTemplate.getTemplatename() ) ) {
    // 		    idx = i;
    // 		    break;
    // 		}
    // 	    }
    // 	    if( idx >= 0 ) {
    // 		log.debug( "Template index of \""+templ.getTemplatename()+"\": "+idx );
    // 		pushAfterRenderIndex( idx );
    // 		// cbTempl.setSelectedIndex( idx );
    // 		// updateTemplateText( wnd, templ );
    // 	    }		
    // 	    else {
    // 		log.error( "Template index "+idx+" could not be found for template \""+
    // 			   selTemplate.getTemplatename()+"\"" ); 
    // 	    }
    // 	}
    // }


    // private void updateTemplateText( Window wnd, InventoryUploadTemplate templ ) {
    // 	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATE );
    // 	if( txtTempl != null )
    // 	    txtTempl.setValue( toEditText(templ.getTemplate()) );
    // 	txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATENAME );
    // 	if( txtTempl != null ) {
    // 	    txtTempl.setValue( templ.getTemplatename() );
    // 	}
    // }

    private StorageProject[] sortStorageProjects( StorageProject[] templs ) {
	Arrays.sort( templs, PROJECT_SORTER );
	// for( int i = 0; i < templs.length; i++ )
	//     log.debug( "Storage project groups of "+templs[i]+": "+templs[i].getStorageGroups().length );
	return templs;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Storage project result model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	// if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	//     combobox.addEventListener( Events.ON_SELECT, this );

	StorageProject[] tList = (StorageProject[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new StorageProject[0] ) );
	else {
	    log.debug( "Assigning model, number of storage projects: "+tList.length );
	    combobox.setModel( new ListModelArray( sortStorageProjects(tList) ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Storage project model selected: "+event );

	StorageProject templ = null;

	Combobox cb = (Combobox)event.getTarget();

	if( "onAfterRender".equals( event.getName() ) ) {
	    int idx = popAfterRenderIndex( 0 );
	    if( idx < cb.getItemCount() ) {
		cb.setSelectedIndex( idx );
		templ = (StorageProject)cb.getModel().getElementAt( idx );
	    }	    
	}	
	// else if( Events.ON_SELECT.equals( event.getName() ) ) {
	//     if( cb.getItemCount() > 0 ) {
	// 	int idx = cb.getSelectedIndex();
	// 	if( idx >= 0 )
	// 	    templ = (StorageProject)cb.getModel().getElementAt( idx );
	//     }
	// }

	// if( templ != null ) {
	//     Window wnd = ZKUtil.findWindow( cb );
	//     notifyComponent( wnd, templ );
	//     // updateTemplateText( wnd, templ );
	// }
    }
}
