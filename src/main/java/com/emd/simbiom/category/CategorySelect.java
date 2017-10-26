package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.simbiom.command.InventoryCommand;
import com.emd.simbiom.config.InventoryPreferences;
import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.view.ModelProducer;

import com.emd.simbiom.upload.InventoryUploadTemplate;
import com.emd.simbiom.upload.UploadBatch;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>CategorySelect</code> action selects the entry point for browsing.
 *
 * Created: Tue Jul 28 12:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class CategorySelect extends InventoryCommand {

    private static Log log = LogFactory.getLog(CategorySelect.class);

    /**
     * Creates a new command to select the category.
     */
    public CategorySelect() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private CategoryTreeView getCategoryTree() {
	ModelProducer[] mp = getPreferences().getResult( CategoryTreeView.class );
	if( mp.length <= 0 )
	    return null;
	return (CategoryTreeView)mp[0];
    }

    private String getSelectedCategory( Window wnd ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
	String catSt = CategoryTreeView.DEFAULT_CATEGORY;
	if( cb != null ) {
	    Comboitem ci = cb.getSelectedItem();
	    if( ci != null )
		catSt = ci.getValue();
	}
	return catSt;
    }

    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	if( "onAfterRender".equals( getEvent() ) ) {
	    Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
	    if( (cb != null) && (cb.getItemCount() > 0) )
		cb.setSelectedIndex( 0 );
	}
	else {
	    String catSt = getSelectedCategory( wnd );
	    log.debug( "Category selected: "+catSt );

	    CategoryTreeView ct = getCategoryTree();
	    if( ct != null ) {
		Map ctxt = new HashMap();
		ctxt.put( CategoryTreeView.RESULT, catSt );
		ct.assignModel( wnd, ctxt );
	    }
	}	
    }    
    
} 
