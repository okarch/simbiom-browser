package com.emd.simbiom.cost;

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

import com.emd.simbiom.model.CostSample;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * <code>VolumeSelector</code> holds the sample type specific options for storage volume.
 *
 * Created: Mon Jul 18 18:24:09 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class VolumeSelector extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(VolumeSelector.class);

    public static final String COMPONENT_ID = "cbCostSample_0";
    public static final String RESULT = "result";
    

    public VolumeSelector() {
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
    }

    /**
     * Returns the selected upload template.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    // public CostSample getSelectedCostSample( Window wnd ) {
    // 	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	CostSample templ = null;
    // 	if( cbTempl != null ) {
    // 	    int sel = cbTempl.getSelectedIndex();
    // 	    if( sel >= 0 )
    // 		templ = (CostSample)cbTempl.getModel().getElementAt( sel );
    // 	}
    // 	return templ;
    // }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Sample volume model context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	// combobox.addEventListener( Events.ON_SELECT, this );

	CostSample[] tList = (CostSample[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new CostSample[0] ) );
	else {
	    log.debug( "Assigning model, number of cost items: "+tList.length );
	    combobox.setModel( new ListModelArray( tList ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Sample volume model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
		CostSample cs = (CostSample)cb.getModel().getElementAt( 0 );
	    }
	}	
    }
}
