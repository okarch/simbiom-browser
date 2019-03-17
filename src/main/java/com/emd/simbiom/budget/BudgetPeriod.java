package com.emd.simbiom.budget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Invoice;
import com.emd.simbiom.util.Period;
import com.emd.simbiom.util.PeriodParseException;

import com.emd.simbiom.view.DefaultModelProducer;
import com.emd.simbiom.view.UIUtils;

import com.emd.util.Stringx;
import com.emd.zk.ZKUtil;

/**
 * <code>BudgetPeriod</code> provides selection model for budget periods.
 *
 * Created: Mon Feb  4 07:58:27 2019
 *
 * @author <a href="mailto:okarch@linux.localdomain">Oliver</a>
 * @version 1.0
 */
public class BudgetPeriod extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(BudgetPeriod.class);

    public BudgetPeriod() {
	super();
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( getModelName() );
	if( cb != null ) 
	    assignModel( cb, ((context!=null)?context:new HashMap()) );
	
	// if( context == null )
	//     context = new HashMap();
	// Object[] statusElements = (Object[])context.get( KEY_RESULT );



	// if( statusElements == null ) {

	//     if( dao == null ) {
	// 	writeMessage( wnd, "Error: No database access configured" );
	// 	return;
	//     }
	//     try {
	// 	Period invPeriod = Period.fromQuarter( 0 );
	// 	invPeriod.join( Period.fromQuarter( -1 ) );

	// 	invoices = dao.findInvoiceByPeriod( invPeriod, true );
	// 	// if( invoices.length > 0 )
	// 	//     setLastCreatedDays( samples[0] );
	// 	context.put( RESULT, invoices );
	//     }
	//     catch( SQLException sqe ) {
	// }

	// if( invoices != null ) {
	// }
    }

    private void initPeriods( List<BudgetPeriodItem> periods ) {
	try {
	    BudgetPeriodItem bpi = new BudgetPeriodItem( "Current Month" ) ;
	    bpi.setPeriod( Period.parse("0M") );
	    periods.add( bpi );
	    bpi = new BudgetPeriodItem( "Current Quarter" ) ;
	    bpi.setPeriod( Period.parse("0Q") );
	    periods.add( bpi );
	    bpi = new BudgetPeriodItem( "Current Year" ) ;
	    bpi.setPeriod( Period.parse("0Y") );
	    periods.add( bpi );

	    bpi = new BudgetPeriodItem( "Last Month" ) ;
	    bpi.setPeriod( Period.parse("-1M") );
	    periods.add( bpi );
	    bpi = new BudgetPeriodItem( "Last Quarter" ) ;
	    bpi.setPeriod( Period.parse("-1Q") );
	    periods.add( bpi );
	    bpi = new BudgetPeriodItem( "Last Year" ) ;
	    bpi.setPeriod( Period.parse("-1Y") );
	    periods.add( bpi );
	}
	catch( PeriodParseException ppex ) {
	    log.error( ppex );
	}
    }	

    private void addYears( List<BudgetPeriodItem> periods, int startYear ) {
	int endYear = (new Date(System.currentTimeMillis())).getYear()+1900;
	for( int yy = endYear; yy >= startYear; yy-- ) {
	    try {
		BudgetPeriodItem bpi = new BudgetPeriodItem( String.valueOf(yy) );
		bpi.setPeriod( Period.parse( String.valueOf(yy) ) );
		periods.add( bpi );
	    }
	    catch( PeriodParseException ppex ) {
		log.error( ppex );
	    }
	}
    }

    private BudgetPeriodItem[] createModel() {
	List<BudgetPeriodItem> items = new ArrayList<BudgetPeriodItem>();
	initPeriods( items );

	SampleInventory dao = getSampleInventory();
	try {
	    Calendar cStart = Calendar.getInstance();
	    cStart.set( 2012, 1, 1 );
	    Period per = new Period( cStart.getTime(), new Date( System.currentTimeMillis() ) );
	    Invoice[] inv = dao.findInvoiceByPeriod( per, false );
	    log.debug( "Number of invoices ranging from "+per+" : "+inv.length );
	    int startYear = (new Date(System.currentTimeMillis())).getYear()+1900;
	    if( inv.length > 0 ) {
		startYear = inv[0].getStarted().getYear()+1900;
		log.debug( "Starting invoice date: "+inv[0].getStarted()+" year: "+startYear );
	    }
	    addYears( items, startYear );
	}
	catch( SQLException sqe ) {
	    // writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
 	    log.error( sqe );
	}
	BudgetPeriodItem[] bPers = new BudgetPeriodItem[ items.size() ];
	return (BudgetPeriodItem[])items.toArray( bPers );
    } 

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Budget period model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	    combobox.addEventListener( Events.ON_SELECT, this );

	BudgetPeriodItem[] items = createModel();
	combobox.setModel( new ListModelArray( items ) );
    }

    private Period getSelectedPeriod( Combobox cb, int idx ) {
	ListModel periods = cb.getModel();
	Period selPeriod = null;
	if( (periods != null) && (periods.getSize() > 0) ) {
	    BudgetPeriodItem bpi = (BudgetPeriodItem)periods.getElementAt( idx );
	    selPeriod = bpi.getPeriod();
	}
	return selPeriod;
    }

    private Invoice[] updateInvoiceList( Window wnd, Period period ) {
	log.debug( "Updating budget period: "+period );
	InvoiceResult invoices = (InvoiceResult)getPreferences().getResult( "grInvoices" );
	if( invoices == null ) {
	    log.error( "Invoice list not found" );
	    return null;
	}
	SampleInventory dao = getSampleInventory();
	Invoice[] invs = null;
	try {
	    invs = dao.findInvoiceByPeriod( period, true );
	    Map context = new HashMap();
	    context.put( InvoiceResult.RESULT, invs );
	    invoices.assignModel( wnd, context );
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
 	    log.error( sqe );
	}
	return invs;
    }

    private void updateInvoiceDates( Window wnd, Period period ) {
	log.debug( "Updating invoice period dates: "+period );
	UIUtils.clearMessage( wnd, "lbInvoiceMessage" );
	Datebox db = (Datebox)wnd.getFellowIfAny( InvoicePeriodChange.CMP_INVOICE_FROM );
	if( db != null )
	    db.setValue( period.getStartDate() );
	db = (Datebox)wnd.getFellowIfAny( InvoicePeriodChange.CMP_INVOICE_TO );
	if( db != null )
	    db.setValue( period.getEndDate() );
    }

    private void updateInvoiceSummary( Window wnd, Invoice[] invs ) {
	log.debug( "Updating invoice summary, number of invoices: "+invs.length );
	InvoiceSummary summary = (InvoiceSummary)getPreferences().getCommand( "cmdInvoiceSummary" );
	if( summary == null ) {
     	    log.error( "Invoice summary command not defined" );
     	    return;
     	}
	summary.updateSummary( wnd, invs );
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Budget period model selected: "+event );

	Combobox cb = (Combobox)event.getTarget();

	Period selPeriod = null;

	if( ("onAfterRender".equals( event.getName() )) && 
	    ( cb != null ) &&
	    ( cb.getItemCount() > 2 ) ) {

	    cb.setSelectedIndex( 1 );
	    selPeriod = getSelectedPeriod( cb, 1 );
	    // int idx = popAfterRenderIndex( cb.getId(), 0 );
	    // // log.debug( "Invoice status after render index: "+idx );
	    // if( idx < cb.getItemCount() ) {
	    // 	cb.setSelectedIndex( idx );
	    // }	    
	}	
	else if( (Events.ON_SELECT.equals( event.getName() ) ) &&
		 ( cb != null ) &&
		 ( cb.getItemCount() > 0 ) ) {
	    int idx = cb.getSelectedIndex();
	    if( idx >= 0 ) 
		selPeriod = getSelectedPeriod( cb, idx );
	}

	if( selPeriod != null ) {
	    Window wnd = ZKUtil.findWindow( cb );
	    Invoice[] invs = updateInvoiceList( wnd, selPeriod );
	    updateInvoiceDates( wnd, selPeriod );
	    if( invs != null )
		updateInvoiceSummary( wnd, invs );
	}
    }

}
