package com.emd.simbiom.category;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
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
 * <code>BrowseCategoryView</code> is the basic class of a filter plugin.
 *
 * Created: Sat May 23 16:53:49 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class BrowseCategoryView extends CategoryView {

    private static Log log = LogFactory.getLog(BrowseCategoryView.class);

    public static final String CATEGORY_PATH_ID = "txtBrowseCategoryPath";

    public BrowseCategoryView() {
	super();
    }

    /**
     * No op initialzation called after layout.
     * Overwritten by specialized filters.
     */
    protected void initComponents( Window wnd, Map context ) {
	Textbox txt = (Textbox)wnd.getFellowIfAny( CATEGORY_PATH_ID );
	if( txt != null )
	    txt.setValue( (String)context.get(CategoryTreeView.PATH ) );
    }

}
