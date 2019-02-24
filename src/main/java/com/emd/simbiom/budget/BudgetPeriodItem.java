package com.emd.simbiom.budget;

import com.emd.simbiom.util.Period;

import com.emd.util.Stringx;

/**
 * Describe class BudgetPeriodItem here.
 *
 * Created: Fri Feb  8 20:17:15 2019
 *
 * @author <a href="mailto:okarch@deda1infr005.localdomain">Oliver</a>
 * @version 1.0
 */
public class BudgetPeriodItem {
    private String label;
    private Period period;

    public BudgetPeriodItem( String label ) {
	this.label = Stringx.getDefault( label, "" );
    }

    /**
     * Get the <code>Label</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getLabel() {
	return label;
    }

    /**
     * Set the <code>Label</code> value.
     *
     * @param label The new Label value.
     */
    public final void setLabel(final String label) {
	this.label = label;
    }

    /**
     * Get the <code>Period</code> value.
     *
     * @return a <code>Period</code> value
     */
    public final Period getPeriod() {
	return period;
    }

    /**
     * Set the <code>Period</code> value.
     *
     * @param period The new Period value.
     */
    public final void setPeriod(final Period period) {
	this.period = period;
    }

    /**
     * Returns the human readable label.
     * @return a human readable string.
     */
    public String toString() {
	return this.getLabel();
    }
}
