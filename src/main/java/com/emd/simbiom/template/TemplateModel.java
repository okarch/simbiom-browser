package com.emd.simbiom.template;

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

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.upload.InventoryUploadTemplate;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKUtil;

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

    public static final String TXT_TEMPLATE      = "txtTemplate";
    public static final String TXT_TEMPLATENAME  = "txtTemplateName";

    private static final Comparator<InventoryUploadTemplate> TEMPLATE_SORTER = new Comparator<InventoryUploadTemplate>() {
	public int compare(InventoryUploadTemplate o1, InventoryUploadTemplate o2) {
	    if( (o1 == null) && (o2 == null) )
		return 0;
	    return o1.getTemplatename().compareTo( o2.getTemplatename() );
	}
	public boolean equals(Object obj) {
	    return false;
	}
    };

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
    public void reloadTemplates( Window wnd, InventoryUploadTemplate selTemplate ) {
	SampleInventoryDAO dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	try {
	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

	    if( cbTempl != null ) {
		Map context = new HashMap();
		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    } 
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}

	ListModel tModel = null;
	if( (selTemplate != null) &&
	    (cbTempl != null) && 
	    ((tModel = cbTempl.getModel()) != null) ) {

	    log.debug( "Find index of template \""+selTemplate.getTemplatename()+"\"" );
	    int idx = -1;
	    InventoryUploadTemplate templ = null;
	    for( int i = 0; i < tModel.getSize(); i++ ) {
		templ = (InventoryUploadTemplate)tModel.getElementAt( i );
		if( templ.getTemplatename().equals( selTemplate.getTemplatename() ) ) {
		    idx = i;
		    break;
		}
	    }
	    if( idx >= 0 ) {
		log.debug( "Template index of \""+templ.getTemplatename()+"\": "+idx );
		pushAfterRenderIndex( idx );
		// cbTempl.setSelectedIndex( idx );
		// updateTemplateText( wnd, templ );
	    }		
	    else {
		log.error( "Template index "+idx+" could not be found for template \""+
			   selTemplate.getTemplatename()+"\"" ); 
	    }
	}
    }

    private String toEditText( String templ ) {
	return Stringx.getDefault( templ, "## Empty template" ).trim().replace( "\\n", "\n" );
    }

    private void updateTemplateText( Window wnd, InventoryUploadTemplate templ ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATE );
	if( txtTempl != null )
	    txtTempl.setValue( toEditText(templ.getTemplate()) );
	txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATENAME );
	if( txtTempl != null ) {
	    txtTempl.setValue( templ.getTemplatename() );
	}
    }

    private InventoryUploadTemplate[] sortTemplates( InventoryUploadTemplate[] templs ) {
	Arrays.sort( templs, TEMPLATE_SORTER );
	return templs;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Template result model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	    combobox.addEventListener( Events.ON_SELECT, this );

	InventoryUploadTemplate[] tList = (InventoryUploadTemplate[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new InventoryUploadTemplate[0] ) );
	else {
	    log.debug( "Assigning model, number of templates: "+tList.length );
	    combobox.setModel( new ListModelArray( sortTemplates(tList) ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Template model selected: "+event );

	InventoryUploadTemplate templ = null;

	Combobox cb = (Combobox)event.getTarget();

	if( "onAfterRender".equals( event.getName() ) ) {
	    int idx = popAfterRenderIndex( 0 );
	    if( idx < cb.getItemCount() ) {
		cb.setSelectedIndex( idx );
		templ = (InventoryUploadTemplate)cb.getModel().getElementAt( idx );
	    }	    
	}	
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    if( cb.getItemCount() > 0 ) {
		int idx = cb.getSelectedIndex();
		if( idx >= 0 )
		    templ = (InventoryUploadTemplate)cb.getModel().getElementAt( idx );
	    }
	}

	if( templ != null ) {
	    Window wnd = ZKUtil.findWindow( cb );
	    updateTemplateText( wnd, templ );
	}
    }
}
