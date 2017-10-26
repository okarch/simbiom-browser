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
import com.emd.simbiom.view.ModelProducer;

import com.emd.util.Stringx;

import com.emd.zk.ZKUtil;

/**
 * <code>SampleTypeModel</code> holds the sample types and the costs associated to it.
 *
 * Created: Mon Jul 18 16:24:09 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SampleTypeModel extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(SampleTypeModel.class);

    public static final String COMPONENT_ID = "cbSampleType_0";
    public static final String RESULT = "result";
    

    public SampleTypeModel() {
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
	    CostSample[] tList = dao.findCostBySampleType( null );

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
    public CostSample getSelectedCostSample( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	CostSample templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    if( sel >= 0 )
		templ = (CostSample)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Sample type result model context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	combobox.addEventListener( Events.ON_SELECT, this );

	CostSample[] tList = (CostSample[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new CostSample[0] ) );
	else {
	    log.debug( "Assigning model, number of cost items: "+tList.length );
	    combobox.setModel( new ListModelArray( tList ) );
	}
    }

    private VolumeSelector getVolumeSelector() {
     	ModelProducer[] mp = getPreferences().getResult( VolumeSelector.class );
     	if( mp.length <= 0 )
     	    return null;
	int k = -1;
	String st = getModelName();
	String suff = null;
	if( (k = st.indexOf( "_" )) >= 0 )
	    suff = st.substring( k );
	else
	    suff = "";
	for( int i = 0; i < mp.length; i++ ) {
	    if( (mp[i] instanceof DefaultModelProducer) && 
		(((DefaultModelProducer)mp[i]).getModelName().endsWith( suff )) )
		return (VolumeSelector)mp[i];
	}
     	return null;
    }

    private void selectVolume( Window wnd, CostSample sampleType ) {
	SampleInventoryDAO dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	VolumeSelector vs = getVolumeSelector();
	if( vs == null ) {
	    writeMessage( wnd, "Error: Invalid volume selection" );
	    return;
	}
	Combobox cbVol = (Combobox)wnd.getFellowIfAny( vs.getModelName() );
	if( cbVol == null ) {
	    writeMessage( wnd, "Error: Cannot determine volume selection" );
	    return;
	}
	log.debug( "Volume selection model: "+vs.getModelName() );

	try {
	    CostSample[] tList = dao.findCostBySampleType( sampleType.getTypename() );

	    Map context = new HashMap();
	    context.put( RESULT, tList );
	    vs.assignModel( cbVol, context );
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Sample type model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
		CostSample cs = (CostSample)cb.getModel().getElementAt( 0 );
		Window wnd = ZKUtil.findWindow( cb );
		selectVolume( wnd, cs );
	    }
	}
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		int idx = cb.getSelectedIndex();
		CostSample cs = (CostSample)cb.getModel().getElementAt( idx );
		Window wnd = ZKUtil.findWindow( cb );
		selectVolume( wnd, cs );
	    }
	}
    }
}
