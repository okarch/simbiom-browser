package com.emd.simbiom.view;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.Timestamp;

import org.apache.commons.lang.time.DateFormatUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Vlayout;

import com.emd.simbiom.dao.SampleInventory;

import com.emd.util.StringComparator;
import com.emd.util.NumericComparator;

import com.emd.util.ClassUtils;
import com.emd.util.Stringx;

/**
 * <code>ColumnRenderer</code> formats the content of a column.
 *
 * Created: Thu Oct 11 11:18:42 2018
 *
 * @author <a href="mailto:okarch@linux">Oliver</a>
 * @version 1.0
 */
public class ColumnRenderer implements ColumnFormatter, Comparator {
    private String columnId;
    private String contentField;
    private String dateFormat;
    private String label;
    private String sort;
    private String numberFormat;
    private String contentResult;
    private String layout;

    private boolean caseInsensitive;

    private SampleInventory sampleInventory;

    private static Log log = LogFactory.getLog(ColumnRenderer.class);

    public ColumnRenderer() {
	this.columnId = "";
	this.contentField = "";
	this.dateFormat = "dd-MMM-yyyy";
	this.label = "";
	this.numberFormat = "%d";
	this.layout = "label";
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
     * Get the <code>ContentResult</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getContentResult() {
	return contentResult;
    }

    /**
     * Set the <code>ContentResult</code> value.
     *
     * @param contentResult The new ContentResult value.
     */
    public final void setContentResult(final String contentResult) {
	this.contentResult = contentResult;
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
     * Get the <code>Layout</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getLayout() {
	return layout;
    }

    /**
     * Set the <code>Layout</code> value.
     *
     * @param layout The new Layout value.
     */
    public final void setLayout(final String layout) {
	this.layout = layout;
    }

    /**
     * Get the <code>SampleInventory</code> value.
     *
     * @return a <code>SampleInventoryDAO</code> value
     */
    public final SampleInventory getSampleInventory() {
	return sampleInventory;
    }

    /**
     * Set the <code>SampleInventory</code> value.
     *
     * @param sampleInventory The new SampleInventory value.
     */
    public final void setSampleInventory(final SampleInventory sampleInventory) {
	this.sampleInventory = sampleInventory;
    }

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

    // private SampleRow getSampleRow( Sample sample ) {
    // 	RowCache cache = RowCache.getInstance( getSampleInventory() );
    // 	SampleRow sr = cache.getSampleRow( sample.getSampleid() );
    // 	if( sr == null ) {
    // 	    sr = new SampleRow( sample );
    // 	    sr = cache.putSampleRow( sr );
    // 	}
    // 	return sr;
    // }

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

	Object[] val1 = getValue( o1 );
	Object[] val2 = getValue( o2 );

	Object cVal1 = null;
	Object cVal2 = null;
	if( val1.length <= 0 )
	    cVal1 = getDefaultValue();
	else
	    cVal1 = val1[0];
	if( val2.length <= 0 )
	    cVal2 = getDefaultValue();
	else
	    cVal2 = val2[0];

	if( (cVal1 == null) && (cVal2 == null) )
	    return 0;

	if( cVal1 == null )
	    return -1;
	if( cVal2 == null )
	    return 1;

	if( cmp != null ) 
	    return cmp.compare( cVal1, cVal2 );

	if( (cVal1 instanceof Comparable) &&
	    (cVal2 instanceof Comparable) )
	    return ((Comparable)cVal1).compareTo( cVal2 );

	return 0;
    }    
    // public int compare(Object o1, Object o2) {	
    // 	Comparator cmp = getComparator();

    // 	Object val1 = getValue( o1 );
    // 	Object val2 = getValue( o2 );

    // 	if( val1 == null )
    // 	    val1 = getDefaultValue();
    // 	if( val2 == null )
    // 	    val2 = getDefaultValue();

    // 	if( (val1 == null) && (val2 == null) )
    // 	    return 0;

    // 	if( val1 == null )
    // 	    return -1;
    // 	if( val2 == null )
    // 	    return 1;

    // 	if( cmp != null ) 
    // 	    return cmp.compare( val1, val2 );

    // 	if( (val1 instanceof Comparable) &&
    // 	    (val2 instanceof Comparable) )
    // 	    return ((Comparable)val1).compareTo( val2 );

    // 	return 0;
    // }    

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

    private Object[] getValue( Object data ) {
	String[] fNames = Stringx.getDefault(getContentField(), "" ).split( ";" );
	// log.debug( "Content field: "+getContentField()+" field names: "+fNames.length );
	List vals = new ArrayList();
	for( int i = 0; i < fNames.length; i++ ) {
	    Object retVal = null;
	    if( fNames[i].trim().length() > 0 ) {
		retVal = ClassUtils.get( data, fNames[i].trim(), null );
		// log.debug( "  called property "+fNames[i]+": "+((retVal==null)?"NULL":retVal.toString()));
	    }
	    if( retVal == null )
		vals.add( "" );
	    else if( retVal.getClass().isArray() ) {
		Object[] arrObj = (Object[])retVal;
		for( int j = 0; j < arrObj.length; j++ ) {
		    if( arrObj[j] != null )
			vals.add( arrObj[j] );
		    else
			vals.add( "" );
		}
	    }
	    else {
		vals.add( retVal );
	    }
	}
	Object[] values = new Object[ vals.size() ];
	return (Object[])vals.toArray( values );
    }

    private String[] getStringValues( Object data ) {
	String[] fNames = Stringx.getDefault(getContentField(), "" ).split( ";" );
	List vals = new ArrayList();
	for( int i = 0; i < fNames.length; i++ ) {
	    Object retVal = null;
	    if( fNames[i].trim().length() > 0 ) 
		retVal = ClassUtils.get( data, fNames[i].trim(), null );
	    if( retVal == null )
		vals.add( "" );
	    else if( retVal.getClass().isArray() ) {
		Object[] arrObj = (Object[])retVal;
		for( int j = 0; j < arrObj.length; j++ ) {
		    if( arrObj[j] != null )
			vals.add( arrObj[j] );
		    else
			vals.add( "" );
		}
	    }
	    else {
		vals.add( retVal );
	    }
	}

	String[] values = new String[ vals.size() ];
	int i = 0;
	for( Object val : vals ) {
	    if( val == null ) {
		values[i] = "";
		continue;
	    }
	    else if( val instanceof Date ) {
		values[i] = formatDate( (Date)val, getDateFormat() );
	    }
	    else if( val instanceof Number ) {
		values[i] = formatNumber( (Number)val, getNumberFormat() );
	    }
	    else {
		values[i] = val.toString();
	    }
	    i++;
	}
	return values;
    }

    /**
     * Formats a column's content.
     *
     * @param colName the column name.
     * @param data the data object.
     * @return a string representing the content.
     */
    public String formatColumn( String colName, Object data ) {	
	Object[] vals = getValue( data );
	if( vals.length <= 0 )
	    return "";
	StringBuilder stb = new StringBuilder();
	for( int i = 0; i < vals.length; i++ ) {
	    if( vals[i] == null )
		continue;
	    if( i > 0 )
		stb.append( " " );
	    if( vals[i] instanceof Date ) {
		stb.append( formatDate( (Date)vals[i], getDateFormat() ) );
	    }
	    else if( vals[i] instanceof Number ) {
		stb.append( formatNumber( (Number)vals[i], getNumberFormat() ) );
	    }
	    else {
		stb.append( vals[i] );
	    }
	}
	return stb.toString().trim();
    }

    /**
     * Creates a component to display the column content.
     *
     * @param cmpId component id (can be null).
     * @param data the data to be displayed.
     * @param model a model producer (can be null).
     *
     * @return an initialized component.
     */
    public Component createComponent( String cmpId, Object data, DefaultModelProducer model ) {
	Component cmp = null;

	String cmpType = this.getLayout();

	String cModel = this.getContentResult();
	if( (cModel != null) && (model != null) ) {
	    if( "combobox".equals( cmpType ) ) {
		cmp = new Combobox();
	    }
	    else if( "listbox".equals( cmpType ) ) {
		cmp = new Listbox();
	    }
	    else if( "grid".equals( cmpType ) ) {
		cmp = new Grid();
	    }
	    else if( "tree".equals( cmpType ) ) {
		cmp = new Tree();
	    }
	    if( cmp != null ) {
		cmp.setId( cmpId );
		Map ctxt = new HashMap();
		ctxt.put( "data", data );
		ctxt.put( "result", getValue( data ) );
		model.assignModel( cmp, ctxt );
	    }
	}
	else if( "vlayout".equals(cmpType) ) {
	    cmp = new Vlayout();
	    String[] vals = getStringValues( data );
	    // log.debug( "number of column values: "+vals.length );
	    for( int i = 0; i < vals.length; i++ ) {
		// log.debug( "  label value: "+vals[i] );
		Label lb = new Label( vals[i] );
		lb.setParent( cmp );
	    }
	}
	else {
	    cmp = new Label();
	    ((Label)cmp).setValue( formatColumn( getLabel(), data ) );
	}

	if( cmpId != null )
	    cmp.setId( cmpId );
		
	return cmp;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object obj) {
	if( obj instanceof ColumnRenderer ) {
	    ColumnRenderer f = (ColumnRenderer)obj;
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
