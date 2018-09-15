package com.emd.simbiom.search;

import java.math.BigDecimal;

import java.util.Comparator;
import java.util.Date;

import java.sql.Timestamp;

import org.apache.commons.lang.time.DateFormatUtils;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Accession;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleProcess;

import com.emd.simbiom.view.ColumnFormatter;
import com.emd.simbiom.view.DateComparator;

import com.emd.util.StringComparator;
import com.emd.util.NumericComparator;

import com.emd.util.Stringx;

/**
 * <code>DisplayColumn</code> formats the content of a column.
 *
 * Created: Wed Feb 21 22:18:42 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class DisplayColumn implements ColumnFormatter, Comparator {
    private String columnId;
    private String contentField;
    private String dateFormat;
    private String label;
    private String sort;
    private String numberFormat;

    private boolean caseInsensitive;

    // private SampleInventoryDAO sampleInventory;
    private SampleInventory sampleInventory;

    public DisplayColumn() {
	this.columnId = "";
	this.contentField = "";
	this.dateFormat = "dd-MMM-yyyy";
	this.label = "";
	this.numberFormat = "%d";
    }

    /**
     * Get the <code>CaseInsensitive</code> value.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isCaseInsensitive() {
	return caseInsensitive;
    }

    /**
     * Set the <code>CaseInsensitive</code> value.
     *
     * @param caseInsensitive The new CaseInsensitive value.
     */
    public final void setCaseInsensitive(final boolean caseInsensitive) {
	this.caseInsensitive = caseInsensitive;
    }

    /**
     * Get the <code>Sort</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getSort() {
	return sort;
    }

    /**
     * Set the <code>Sort</code> value.
     *
     * @param sort The new Sort value.
     */
    public final void setSort(final String sort) {
	this.sort = sort;
    }

    /**
     * Get the <code>ColumnId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getColumnId() {
	return columnId;
    }

    /**
     * Set the <code>ColumnId</code> value.
     *
     * @param columnId The new ColumnId value.
     */
    public final void setColumnId(final String columnId) {
	this.columnId = columnId;
    }

    /**
     * Get the <code>ContentField</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getContentField() {
	return contentField;
    }

    /**
     * Set the <code>ContentField</code> value.
     *
     * @param contentField The new ContentField value.
     */
    public final void setContentField(final String contentField) {
	this.contentField = contentField;
    }

    /**
     * Get the <code>DateFormat</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getDateFormat() {
	return Stringx.getDefault(dateFormat,"dd-MMM-yyyy");
    }

    /**
     * Set the <code>DateFormat</code> value.
     *
     * @param dateFormat The new DateFormat value.
     */
    public final void setDateFormat(final String dateFormat) {
	this.dateFormat = dateFormat;
    }

    /**
     * Get the <code>NumberFormat</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getNumberFormat() {
	return Stringx.getDefault( numberFormat, "%d" );
    }

    /**
     * Set the <code>NumberFormat</code> value.
     *
     * @param numberFormat The new NumberFormat value.
     */
    public final void setNumberFormat(final String numberFormat) {
	this.numberFormat = numberFormat;
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
     * Get the <code>SampleInventory</code> value.
     *
     * @return a <code>SampleInventoryDAO</code> value
     */
    public final SampleInventory getSampleInventory() {
	return sampleInventory;
    }
    // public final SampleInventoryDAO getSampleInventory() {
    // 	return sampleInventory;
    // }

    /**
     * Set the <code>SampleInventory</code> value.
     *
     * @param sampleInventory The new SampleInventory value.
     */
    public final void setSampleInventory(final SampleInventory sampleInventory) {
	this.sampleInventory = sampleInventory;
    }
    // public final void setSampleInventory(final SampleInventoryDAO sampleInventory) {
    // 	this.sampleInventory = sampleInventory;
    // }

    private Object getDefaultValue() {
	if( "date".equals(getSort()) ) {
	    return new Date(0);
	}
	else if( "number".equals(getSort()) ) {
	    return new BigDecimal(0);
	}
	return "";
    }

	// initSort( grid, srr, "colSearch_0", new StringComparator( true )  );
	// initSort( grid, srr, "colSearch_1", new StringComparator( true ) );
	// initSort( grid, srr, "colSearch_2", new StringComparator( true ) );
	// initSort( grid, srr, "colSearch_3", new StringComparator( true ) );
	// initSort( grid, srr, "colSearch_4", new StringComparator( true ) );
	// initSort( grid, srr, "colSearch_5", new DateComparator( "dd-MMM-yyyy hh:mm" ) );
	// initSort( grid, srr, "colSearch_6", new DateComparator( "dd-MMM-yyyy" ) );

    private Comparator getComparator() {
	if( "date".equals(getSort()) ) {
	    return new DateComparator();
	}
	else if( "number".equals(getSort()) ) {
	    return new NumericComparator();
	}
	else if( "string".equals(getSort()) ) {
	    return new StringComparator( isCaseInsensitive() );
	}
	return null;
    }

    private SampleRow getSampleRow( Sample sample ) {
	RowCache cache = RowCache.getInstance( getSampleInventory() );
	SampleRow sr = cache.getSampleRow( sample.getSampleid() );
	if( sr == null ) {
	    sr = new SampleRow( sample );
	    sr = cache.putSampleRow( sr );
	}
	return sr;
    }

    /**
     * Compares its two arguments for order. 
     * Returns a negative integer, zero, or a positive 
     * integer as the first argument is less than, equal to, 
     * or greater than the second.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared. 
     *
     * @return a negative integer, zero, or a positive integer as 
     * the first argument is less than, equal to, or greater than the second. 
     */
    public int compare(Object o1, Object o2) {	
	Comparator cmp = getComparator();

	Object val1 = getValue( o1 );
	Object val2 = getValue( o2 );

	if( val1 == null )
	    val1 = getDefaultValue();
	if( val2 == null )
	    val2 = getDefaultValue();

	if( (val1 == null) && (val2 == null) )
	    return 0;

	if( val1 == null )
	    return -1;
	if( val2 == null )
	    return 1;

	if( cmp != null ) 
	    return cmp.compare( val1, val2 );

	if( (val1 instanceof Comparable) &&
	    (val2 instanceof Comparable) )
	    return ((Comparable)val1).compareTo( val2 );

	return 0;
    }    

    // public int compare(Object o1, Object o2) {	
    // 	String st1 = getContentValue( o1 );
    // 	String st2 = getContentValue( o2 );
    // 	return comparator.compare( st1, st2 );
    // }    

    private String formatDate( Date dt, String fmt ) {
	return DateFormatUtils.format( dt, fmt );
    }

    private String formatNumber( Number num, String fmt ) {
	return String.format( fmt, num );
    }

    private Object getValue( Object data ) {
	SampleRow sr = null;
	if( data instanceof SampleRow ) {
	    sr = (SampleRow)data;
	}
	else if( data instanceof Sample ) {
	    sr = getSampleRow( (Sample)data );
	}
	    
	if( sr != null ) {
	    String fName = Stringx.getDefault(getContentField(), "" );

	    if( "studyname".equals( fName ) ) {
		return Stringx.getDefault(sr.getStudyname(), "" );
	    }
	    else if( "typename".equals( fName ) ) {
		return Stringx.getDefault(sr.getTypename(), "");
	    }
	    else if( "accessions".equals( fName ) ) {
		Accession[] accs = sr.getAccessions();
		String st = null;
		if( accs != null )
		    st = Stringx.toStringList( accs, ", " );
		else
		    st = Stringx.getDefault(sr.getSample().getSamplename(), "" );
		return st;
	    }
	    else if( "subjectid".equals( fName ) ) {
		return Stringx.getDefault(sr.getSubjectid(), "");
	    }
	    else if( "visit.visit".equals( fName ) ) {
		SampleProcess procs = sr.getVisit();
		String st = null;
		if( procs != null )
		    st = procs.getVisit();
		return Stringx.getDefault( st, "" );
	    }
	    else if( "visit.processed".equals( fName ) ) {
		SampleProcess procs = sr.getVisit();
		String st = null;
		if( procs != null )
		    return procs.getProcessed();
	    }
	    else if( "sample.created".equals( fName ) ) {
		return sr.getSample().getCreated();
	    }
	    else if( fName.startsWith( "//" ) ) {
		return sr.getContent( fName );
	    }
	}
	return null;
    }

    /**
     * Formats a column's content.
     *
     * @param colName the column name.
     * @param data the data object.
     * @return a string representing the content.
     */
    public String formatColumn( String colName, Object data ) {	
	Object val = getValue( data );
	if( val == null )
	    return "";
	if( val instanceof Date ) {
	    return formatDate( (Date)val, getDateFormat() );
	}
	else if( val instanceof Number ) {
	    return formatNumber( (Number)val, getNumberFormat() );
	}
	return val.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object obj) {
	if( obj instanceof DisplayColumn ) {
	    DisplayColumn f = (DisplayColumn)obj;
	    return (f.getColumnId().equals(this.getColumnId()) );
	}
	return false;
    }

    public String toString() {
	StringBuilder stb = new StringBuilder( "{" );
	stb.append( "columnId=" );
	stb.append( Stringx.getDefault( this.columnId, "" ) );
	stb.append( ",contentField=" );
	stb.append( Stringx.getDefault( this.contentField, "" ) );
	stb.append( ",dateFormat=" );
	stb.append( Stringx.getDefault( this.dateFormat, "" ) );
	stb.append( ",label=" );
	stb.append( Stringx.getDefault( this.label, "" ) );
	stb.append( ",numberFormat=" );
	stb.append( Stringx.getDefault( this.numberFormat, "" ) );
	stb.append( "}" );
	return stb.toString();
    }

    // public String formatColumn( String colName, Object data ) {	
    // 	SampleRow sr = null;

    // 	if( data instanceof SampleRow ) {
    // 	    sr = (SampleRow)data;
    // 	}
    // 	else if( data instanceof Sample ) {
    // 	    sr = getSampleRow( (Sample)data );
    // 	}
	    
    // 	if( sr != null ) {
    // 	    String fName = Stringx.getDefault(getContentField(), "" );

    // 	    if( "studyname".equals( fName ) ) {
    // 		return Stringx.getDefault(sr.getStudyname(), "" );
    // 	    }
    // 	    else if( "typename".equals( fName ) ) {
    // 		return Stringx.getDefault(sr.getTypename(), "");
    // 	    }
    // 	    else if( "accessions".equals( fName ) ) {
    // 		Accession[] accs = sr.getAccessions();
    // 		String st = null;
    // 		if( accs != null )
    // 		    st = Stringx.toStringList( accs, ", " );
    // 		else
    // 		    st = Stringx.getDefault(sr.getSample().getSamplename(), "" );
    // 		return st;
    // 	    }
    // 	    else if( "subjectid".equals( fName ) ) {
    // 		return Stringx.getDefault(sr.getSubjectid(), "");
    // 	    }
    // 	    else if( "visit.visit".equals( fName ) ) {
    // 		SampleProcess procs = sr.getVisit();
    // 		String st = null;
    // 		if( procs != null )
    // 		    st = procs.getVisit();
    // 		return Stringx.getDefault( st, "" );
    // 	    }
    // 	    else if( "visit.processed".equals( fName ) ) {
    // 		SampleProcess procs = sr.getVisit();
    // 		String st = null;
    // 		if( procs != null )
    // 		    st = formatDate(procs.getProcessed(),getDateFormat());
    // 		return Stringx.getDefault( st, "" );
    // 	    }
    // 	    else if( "sample.created".equals( fName ) ) {
    // 		return formatDate( sr.getSample().getCreated(), getDateFormat() );
    // 	    }
    // 	}
    // 	return "";
    // }

}
