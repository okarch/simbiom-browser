package com.emd.simbiom.report;

import com.emd.vutils.report.ReportGroup;
import com.emd.vutils.report.ReportItem;
import com.emd.vutils.report.ReportStrategy;
import com.emd.vutils.report.ReportFormatter;

/**
 * Describe class AbstractReportStrategy here.
 *
 *
 * Created: Sun Nov  3 17:12:27 2019
 *
 * @author <a href="mailto:okarch@deda1infr009.localdomain">Oliver</a>
 * @version 1.0
 */
public abstract class AbstractReportStrategy implements ReportStrategy {
    private String templateName;

    protected AbstractReportStrategy() {
	this.templateName = "";
    }

    /**
     * Get the <code>TemplateName</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getTemplateName() {
	return templateName;
    }

    /**
     * Set the <code>TemplateName</code> value.
     *
     * @param templateName The new TemplateName value.
     */
    public final void setTemplateName(final String templateName) {
	this.templateName = templateName;
    }

    /**
     * Accepts a data item as reportable item.
     *
     * @return true if item can be accepted, false otherwise.
     */
    public abstract boolean accept( ReportGroup group, Object data );


    /**
     * Creates a <code>ReportItem</code> from the given data object.
     *
     * @param group the report group.
     * @param data the data object.
     * @return a <code>ReportItem</code> instance.
     */
    public abstract ReportItem createReportItem( ReportGroup group, Object data );

    /**
     * Returns a specific <code>ReportFormatter</code> object.
     *
     * @param group the report group.
     * @return a <code>ReportFormatter</code> instance or null.
     */
    public abstract ReportFormatter getReportFormatter( ReportGroup group );

}
