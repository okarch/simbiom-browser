package com.emd.simbiom.template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.StorageDocument;

import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>OutputSelector</code> holds the list of outputs.
 *
 * Created: Sat Nov 23 19:11:09 2019
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class OutputSelector extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(OutputSelector.class);

    public static final String COMPONENT_ID = "cbOutputSelector";
    public static final String RESULT = "result";
    
    // private static final Comparator<StorageDocument> REPORT_SORTER = new Comparator<StorageDocument>() {
    // 	public int compare(StorageDocument o1, StorageDocument o2) {
    // 	    if( (o1 == null) && (o2 == null) )
    // 		return 0;
    // 	    return o1.getTemplatename().compareTo( o2.getTemplatename() );
    // 	}
    // 	public boolean equals(Object obj) {
    // 	    return false;
    // 	}
    // };

    public OutputSelector() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    // public void initModel( Window wnd, Map context ) {
    // 	SampleInventoryDAO dao = getSampleInventory();
    // 	if( dao == null ) {
    // 	    writeMessage( wnd, "Error: No database access configured" );
    // 	    return;
    // 	}

    // 	try {
    // 	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

    // 	    Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	    if( cbTempl != null ) {
    // 		if( context == null )
    // 		    context = new HashMap();
    // 		context.put( RESULT, tList );
    // 		assignModel( cbTempl, context );
    // 	    } 
    // 	}
    // 	catch( SQLException sqe ) {
    // 	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
    // 	    log.error( sqe );
    // 	}
    // }

    /**
     * Updates the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void updateModel( Window wnd, Map context ) {
	Component cmp = wnd.getFellowIfAny( this.getModelName() );
	if( cmp != null )
	    this.assignModel( cmp, ((context==null)?new HashMap():context) );
    }

    /**
     * Returns the number of entries.
     *
     * @param wnd the app window.
     * @return the number of entries.
     */ 
    public int getEntryCount( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
     	if( cbTempl != null ) 
	    return cbTempl.getItemCount();
	return 0;
    }	

    /**
     * Returns the selected output.
     *
     * @param wnd the app window.
     * @return the output currently selected (or null).
     */ 
    public StorageDocument getSelectedOutput( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	StorageDocument templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    log.debug( "Selected document index: "+sel );
	    if( sel >= 0 )
		templ = (StorageDocument)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Upload selector context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	// combobox.addEventListener( Events.ON_SELECT, this );

	StorageDocument[] tList = (StorageDocument[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new StorageDocument[0] ) );
	else {
	    log.debug( "Assigning model, number of output files: "+tList.length );
	    combobox.setModel( new ListModelArray( tList ) );
	}
    }

    private Window getWindow( Event evt ) {
	Component cmp = evt.getTarget();
	Window wnd = null;
	if( cmp != null )
	    wnd = ZKContext.findWindow( cmp );
	return wnd;
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Upload batch selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
		// SelectResultLog selLog = (SelectResultLog)InventoryPreferences.getInstance
		//     ( getPortletId(), getUserId() ).getCommand( SelectResultLog.class );
		// if( selLog != null )
		//     selLog.execute( ZKContext.getInstance(), getWindow( event ) );
	    }	    
	}	
    }
}
