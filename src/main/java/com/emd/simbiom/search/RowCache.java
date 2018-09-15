package com.emd.simbiom.search;

import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import com.emd.simbiom.dao.SampleInventoryDAO;
import com.emd.simbiom.dao.SampleInventory;

import com.emd.simbiom.model.Accession;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleType;
import com.emd.simbiom.model.SampleDetails;
import com.emd.simbiom.model.SampleProcess;
import com.emd.simbiom.model.Study;
import com.emd.simbiom.model.Subject;

/**
 * <code>RowCache</code> stores sample rows to be displayed in list components.
 *
 * Created: Mon May  4 08:07:29 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class RowCache {
    private Map<Long,Study>             studies;
    private Map<Long,SampleType>        types;
    private Map<String,Subject>         subjects;
    private Map<String,Accession[]>     accessions;
    private Map<String,SampleProcess>   processes;
    private Map<String,SampleDetails>   details;

    private Map<String,SampleRow>   rows;

    // private SampleInventoryDAO sampleInventory;
    private SampleInventory sampleInventory;

    private static RowCache cache;

    private static Log log = LogFactory.getLog(RowCache.class);

    private RowCache() {
	this.studies = new HashMap<Long,Study>();
	this.types = new HashMap<Long,SampleType>();
	this.subjects = new HashMap<String,Subject>();
	this.accessions = new HashMap<String,Accession[]>();
	this.processes = new HashMap<String,SampleProcess>();
	this.details = new HashMap<String,SampleDetails>();
	this.rows = new HashMap<String,SampleRow>();
    }

    /**
     * Returns the current instance of the <code>RowCache</code>
     *
     * @param inventory the DAO of the sample inventory.
     * @return the <code>RowCache</code> instance.
     */
    public static synchronized RowCache getInstance( SampleInventory inventory ) {
    // public static synchronized RowCache getInstance( SampleInventoryDAO inventory ) {
	if( cache == null ) 
	    cache = new RowCache();
	cache.setSampleInventory( inventory );
	return cache;
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

    /**
     * Get the <code>SampleRow</code> value.
     *
     * @return a <code>SampleRow</code> value
     */
    public final SampleRow getSampleRow( String sampleId ) {
	return rows.get( sampleId );
    }

    
    private void decorateType( SampleRow sr, Sample sample ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	if( sr.getTypename() == null ) {
	    Long tid = new Long(sample.getTypeid());
	    SampleType tName = types.get( tid );
	    if( tName == null ) {
		try {
		    tName = inv.findSampleTypeById( sample.getTypeid() );
		    if( tName != null ) 
			types.put( tid, tName );
		}
		catch( SQLException sqe ) {
		    log.error( sqe );
		}
	    }
	    if( tName != null ) 
		sr.setTypename( tName.getTypename() );
	}
    }

    private Study decorateStudy( SampleRow sr, Sample sample ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return null;
	}
	Study study = null;
	if( sr.getStudyname() == null ) {
	    Long sid = new Long(sample.getStudyid());
	    study = studies.get( sid );
	    if( study == null ) {
		try {
		    study = inv.findStudyById( sample.getStudyid() );
		    if( study != null ) 
			studies.put( sid, study );
		}
		catch( SQLException sqe ) {
		    log.error( sqe );
		}
	    }
	    if( study != null ) 
		sr.setStudyname( study.getStudyname() );
	    else
		sr.setStudyname( "Unknown" );
	}	
	return study;
    }

    private void decorateSubject( SampleRow sr, Sample sample, Study study ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	if( (sr.getSubjectid() == null) && (study != null) ) {
	    String sKey = String.format( "%s %d", sample.getSampleid(), study.getStudyid() );
	    Subject subject = subjects.get( sKey );
	    if( subject == null ) {
	 	try {
	 	    subject = inv.findSubjectBySample( study, sample );
	 	    if( subject != null ) 
	 		subjects.put( sKey, subject );
	 	}
	 	catch( SQLException sqe ) {
	 	    log.error( sqe );
	 	}
	    }
	    if( subject != null ) 
	 	sr.setSubjectid( subject.getSubjectid() );
	}
    }

    private void decorateAccessions( SampleRow sr, Sample sample ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	if( sr.getAccessions() == null ) {
	    Accession[] accs = accessions.get( sample.getSampleid() );
	    if( accs == null ) {
	 	try {
	 	    accs = inv.findSampleAccession( sample );
	 	    if( accs != null ) 
	 		accessions.put( sample.getSampleid(), accs );
	 	}
	 	catch( SQLException sqe ) {
	 	    log.error( sqe );
	 	}
	    }
	    if( accs != null ) 
	 	sr.setAccessions( accs );
	}
    }

    private void decorateVisit( SampleRow sr, Sample sample ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	if( sr.getVisit() == null ) {
	    SampleProcess procs = processes.get( sample.getSampleid() );
	    if( procs == null ) {
	 	try {
	 	    procs = inv.findVisit( sample );
	 	    if( procs != null ) 
	 		processes.put( sample.getSampleid(), procs );
	 	}
	 	catch( SQLException sqe ) {
	 	    log.error( sqe );
	 	}
	    }
	    if( procs != null ) 
	 	sr.setVisit( procs );
	}
    }

    private void decorateDetails( SampleRow sr, Sample sample ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	if( sr.getSampleDetails() == null ) {
	    SampleDetails det = details.get( sample.getSampleid() );
	    if( det == null ) {
	 	try {
	 	    det = inv.createSampleDetails( sample );
	 	    if( det != null ) 
	 		details.put( sample.getSampleid(), det );
	 	}
	 	catch( SQLException sqe ) {
	 	    log.error( sqe );
	 	}
	    }
	    if( det != null ) 
	 	sr.setSampleDetails( det );
	}
    }

    private void decorateRow( SampleRow sr ) {
	// SampleInventoryDAO inv = getSampleInventory();
	SampleInventory inv = getSampleInventory();
	if( inv == null ) {
	    log.error( "Inventory access is invalid" );
	    return;
	}
	Sample sample = sr.getSample();

	Study study = decorateStudy( sr, sample );
	decorateType( sr, sample );
	decorateSubject( sr, sample, study );

	decorateAccessions( sr, sample );
	decorateVisit( sr, sample );

	decorateDetails( sr, sample );
    }

    /**
     * Set the <code>SampleRow</code> value.
     *
     * @param sampleRow The new SampleRow value.
     * @return a decorated <code>SampleRow</code>
     */
    public SampleRow putSampleRow(SampleRow sampleRow) {
	decorateRow( sampleRow );
	rows.put( sampleRow.getSample().getSampleid(), sampleRow );
	return sampleRow;
    }

}
