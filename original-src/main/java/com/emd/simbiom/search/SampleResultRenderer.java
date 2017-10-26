package com.emd.simbiom.search;

import java.util.Date;

import java.sql.Timestamp;

import org.apache.commons.lang.time.DateFormatUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.emd.simbiom.dao.SampleInventoryDAO;

import com.emd.simbiom.model.Accession;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleProcess;

import com.emd.simbiom.view.ColumnFormatter;

import com.emd.util.Stringx;

/**
 * SampleResultRenderer renders the sample search result row.
 *
 * Created: Mon Apr 7 20:30:01 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class SampleResultRenderer implements RowRenderer, ColumnFormatter {
    private SampleInventoryDAO sampleInventory;

    private static Log log = LogFactory.getLog(SampleResultRenderer.class);

    private static final String[] columnNames = {
	"colStudy",
	"colSampleType",
	"colSampleId",
	"colSubject",
	"colVisit",
	"colCollection",
	"colImport"
    };

    public SampleResultRenderer( SampleInventoryDAO sampleInventory ) {
	this.sampleInventory = sampleInventory;
    }

    private void appendCheckmark( Row row, Sample sample ) {
// 	Hlayout hl = new Hlayout();

	Checkbox chk = new Checkbox();
	chk.setId( "chk_"+sample.getSampleid() );
// 	chk.setParent( hl );
	 chk.setParent( row );

 	// Button bt = new Button();
 	// bt.setId( "btEditRow_"+listRow.getContentid()+"_"+listRow.getRowindex() );
// 	EditEntry editEntry = (EditEntry)preferences.getViewAction( EditEntry.class );
// 	if( editEntry != null )
// 	    bt.addEventListener( Events.ON_CLICK, editEntry );
 	// bt.setImage( "/images/info.png" );
 	// bt.setWidth( "20px" );
 	// bt.setHeight( "20px" );
 	// bt.setParent( row );

// 	Button btr = new Button();
// 	btr.setId( "btDeleteRow_"+listRow.getContentid()+"_"+listRow.getRowindex() );
// 	DeleteEntry delEntry = (DeleteEntry)preferences.getViewAction( DeleteEntry.class );
// 	if( delEntry != null )
// 	    btr.addEventListener( Events.ON_CLICK, delEntry );
// 	btr.setImage( "/images/small-delete.png" );
// 	btr.setWidth( "20px" );
// 	btr.setHeight( "20px" );
// 	btr.setParent( hl );

// 	hl.setParent( row );
    }

    /**
     * Formats a column's content.
     *
     * @param colName the column name.
     * @param data the data object.
     * @return a string representing the content.
     */
    public String formatColumn( String colName, Object data ) {
	SampleRow sr = getSampleRow( (Sample)data );
	return formatSampleRow( colName, sr );
    }

    private String formatSampleRow( String colName, SampleRow sr ) {
	if( "colStudy".equals( colName ) ) {
	    return Stringx.getDefault(sr.getStudyname(), "" );
	}
	else if( "colSampleType".equals( colName ) ) {
	    return Stringx.getDefault(sr.getTypename(), "");
	}
	else if( "colSampleId".equals( colName ) ) {
	    Accession[] accs = sr.getAccessions();
	    String st = null;
	    if( accs != null )
		st = Stringx.toStringList( accs, ", " );
	    else
		st = Stringx.getDefault(sr.getSample().getSamplename(), "" );
	    return st;
	}
	else if( "colSubject".equals( colName ) ) {
	    return Stringx.getDefault(sr.getSubjectid(), "");
	}
	else if( "colVisit".equals( colName ) ) {
	    SampleProcess procs = sr.getVisit();
	    String st = null;
	    if( procs != null )
		st = procs.getVisit();
	    return Stringx.getDefault( st, "" );
	}
	else if( "colCollection".equals( colName ) ) {
	    SampleProcess procs = sr.getVisit();
	    String st = null;
	    if( procs != null )
		st = formatDate(procs.getProcessed(),"dd-MMM-yyyy hh:mm");
	    return Stringx.getDefault( st, "" );
	}
	else if( "colImport".equals( colName ) ) {
	    return formatDate( sr.getSample().getCreated(), "dd-MMM-yyyy" );
	}
	return "";
    }

    private SampleRow getSampleRow( Sample sample ) {
	RowCache cache = RowCache.getInstance( sampleInventory );
	SampleRow sr = cache.getSampleRow( sample.getSampleid() );
	if( sr == null ) {
	    sr = new SampleRow( sample );
	    sr = cache.putSampleRow( sr );
	}
	return sr;
    }

    private String formatDate( Date dt, String fmt ) {
	return DateFormatUtils.format( dt, fmt );
    }

    /**
     * Renders the data to the specified row.
     *
     * @param row the row to render the result.
     * @param data that is returned from ListModel.getElementAt(int) 
     * @exception java.lang.Exception
     */
    public void render( Row row, Object data, int index)
	throws java.lang.Exception {

	if( data instanceof Sample ) {
	    Sample sample = (Sample)data;
	    appendCheckmark( row, sample );

	    SampleRow sr = getSampleRow( sample );

	    for( int i = 0; i < columnNames.length; i++ ) 
		(new Label( formatSampleRow( columnNames[i], sr ) )).setParent( row );
	}
    }

}
