package com.emd.simbiom.search;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryViewAction;
import com.emd.simbiom.view.UIUtils;
// import com.emd.simbiom.dao.SampleInventoryDAO;
// import com.emd.simbiom.model.Sample;

import com.emd.util.Stringx;

import com.emd.zk.view.VelocityView;
import com.emd.zk.view.ViewAction;

import com.emd.util.Parameter;

/**
 * <code>CategoryView</code> is the basic class of a filter plugin.
 *
 * Created: Sat May 23 16:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SampleDetailsView extends VelocityView {
    private String messageRowId;
    private String detailsLayout;

    private static Log log = LogFactory.getLog(SampleDetailsView.class);

    public SampleDetailsView() {
    }

    /**
     * Updates the view actions of this command.
     */
    public void updateActions( String portletId, long userId ) {
	ViewAction[] acts = getActions();
	for( int i = 0; i < acts.length; i++ ) {
	    if( acts[i] instanceof InventoryViewAction ) {
		((InventoryViewAction)acts[i]).setUserId( userId );
		((InventoryViewAction)acts[i]).setPortletId( portletId );
	    }
	}
    }

    /**
     * Get the <code>DetailsLayout</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getDetailsLayout() {
	return detailsLayout;
    }

    /**
     * Set the <code>DetailsLayout</code> value.
     *
     * @param detailsLayout The new DetailsLayout value.
     */
    public final void setDetailsLayout(final String detailsLayout) {
	this.detailsLayout = detailsLayout;
    }

    /**
     * Get the <code>MessageRowId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessageRowId() {
	return messageRowId;
    }

    /**
     * Set the <code>MessageRowId</code> value.
     *
     * @param messageRowId The new MessageRowId value.
     */
    public final void setMessageRowId(final String messageRowId) {
	this.messageRowId = messageRowId;
    }

    private String getLabelId() {
	return Stringx.getDefault(getMessageRowId(),"rowMessage").replace( "row", "lb" );
    }

    protected void writeMessage( Window wnd, String msg ) {
	UIUtils.showMessage( wnd, Stringx.getDefault(getMessageRowId(),"rowMessage"), getLabelId(), msg );
    }

    // protected void registerActions( Window wnd ) {
    // 	Parameter[] params = this.getParameters();
    // 	List<Parameter> paras = Arrays.asList( params );
    // 	ViewAction[] acts = this.getActions();
    // 	for( int j = 0; j < acts.length; j++ ) {
    // 	    Component cmp = wnd.getFellowIfAny( acts[j].getComponent() );
    // 	    acts[j].registerEvent( wnd, paras );
    // 	    log.debug( "Component id ("+((cmp==null)?"NOT existing":"existing")+"): "+acts[j].getComponent()+" event: "+acts[j].getEvent() );
    // 	    log.debug( "Event listener: "+cmp.getEventListeners( acts[j].getEvent() ) );
    // 	}
    // }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
    }

    /**
     * Initializes the filter settings.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initView( Window wnd, Map context ) {
	log.debug( "Layout category view. context: "+context );
	layout( wnd, context );
	initComponents( wnd, context );
	// registerActions( wnd );
    }

}
