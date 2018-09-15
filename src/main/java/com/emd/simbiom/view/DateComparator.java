package com.emd.simbiom.view;

/**
 * Describe class StringComparator here.
 *
 *
 * Created: Fri Nov 30 20:54:13 2012
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
import java.util.Comparator;
import java.util.Date;

import com.emd.util.Stringx;

public class DateComparator implements Comparator {
    private String dateFormat;

    public DateComparator( String fmt ) {
	this.dateFormat = fmt;
    }
    public DateComparator() {
	this( null );
    }

    public int compare(Object o1, Object o2) {
	long dt1 = -1L;
	long dt2 = -1L;

	if( o1 instanceof Date ) 
	    dt1 = ((Date)o1).getTime();
	if( o2 instanceof Date ) 
	    dt2 = ((Date)o2).getTime();

	if( (dt1 < 0L) || (dt2 < 0L) ) {
	    if( dateFormat == null ) {
		dt1 = Stringx.parseDate( o1.toString() );
		dt2 = Stringx.parseDate( o2.toString() );
	    }
	    else {
		dt1 = Stringx.parseDate( o1.toString(), dateFormat );
		dt2 = Stringx.parseDate( o2.toString(), dateFormat );
	    }
	}
	Date dat1 = new Date( ((dt1 < 0L)?0L:dt1) );
	Date dat2 = new Date( ((dt2 < 0L)?0L:dt2) );
	return dat1.compareTo( dat2 );
    }
}
