package com.emd.simbiom.template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.upload.InventoryUploadTemplate;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * <code>TemplateModel</code> holds the templates currently available.
 *
 * Created: Sun Jul 12 09:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class TemplateModel extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(TemplateModel.class);

    public static final String COMPONENT_ID = "cbTemplate";
    public static final String RESULT = "result";
    

    public TemplateModel() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	SampleInventoryDAO dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	try {
	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

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
     * Returns the selected upload template.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    public InventoryUploadTemplate getSelectedTemplate( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	InventoryUploadTemplate templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    if( sel >= 0 )
		templ = (InventoryUploadTemplate)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Template result model context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	// combobox.addEventListener( Events.ON_SELECT, this );

	InventoryUploadTemplate[] tList = (InventoryUploadTemplate[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new InventoryUploadTemplate[0] ) );
	else {
	    log.debug( "Assigning model, number of templates: "+tList.length );
	    combobox.setModel( new ListModelArray( tList ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Template model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
	    }	    
	}	
    }
}
