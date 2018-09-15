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
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;
import com.emd.simbiom.dao.StorageCost;

import com.emd.simbiom.model.CostEstimate;
import com.emd.simbiom.model.CostSample;

import com.emd.simbiom.view.DefaultModelProducer;

import com.emd.util.Stringx;

/**
 * <code>RegistrationModel</code> holds the registration modes currently available.
 *
 * Created: Mon Jul 18 08:24:09 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class RegistrationModel extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(RegistrationModel.class);

    public static final String COMPONENT_ID = "cbRegistration";
    public static final String CMP_REGION = "cbRegion";
    public static final String RESULT = "result";
    

    public RegistrationModel() {
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
	// SampleInventoryDAO dao = getSampleInventory();
	SampleInventory dao = getSampleInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	Combobox cbReg = (Combobox)wnd.getFellowIfAny( CMP_REGION );
	String region = null;
	if( cbReg != null ) {
	    Comboitem ci = cbReg.getSelectedItem();
	    if( (ci != null) && (ci.getValue() != null) ) {
		region = ci.getValue().toString();
		log.debug( "Assigning storage region from selection: "+region ); 
	    }
	    else
		log.warn( "No region selected, using default region" );
	}

	region = Stringx.getDefault( region, CostEstimate.DEFAULT_REGION );
	log.debug( "Assigning storage region: "+region ); 

	try {
	    CostSample[] tList = dao.findCostBySampleType( StorageCost.SAMPLE_REGISTRATION, region );

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
     * Updates the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void updateModel( Window wnd, Map context ) {
	Combobox cmp = (Combobox)wnd.getFellowIfAny( this.getModelName() );
	if( (cmp != null) && (context != null) ) {
	    // CostSample cs = getSelectedCostSample( cmp );
	    log.debug( "Updating model "+this.getModelName()+" context: "+context );
	    this.assignCombobox( cmp, ((context==null)?new HashMap():context) );
	    // cmp = (Combobox)wnd.getFellowIfAny( this.getModelName() );
	    // if( cs != null )
	    // 	selectCostSampleByName( cmp, cs );
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
	log.debug( "Registration result model context: "+context );

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

	log.debug( "Registration model selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
	    }	    
	}	
    }
}
