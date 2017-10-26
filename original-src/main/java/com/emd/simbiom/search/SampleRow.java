package com.emd.simbiom.search;

import com.emd.simbiom.model.Accession;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleProcess;

/**
 * <code>SampleRow</code> pulls together information about a sample.
 *
 * Created: Wed May  6 08:23:07 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class SampleRow {
    private Sample sample;

    private String studyname;
    private String typename;
    private String subjectid;

    private Accession[] accessions;
    private SampleProcess visit;

    public SampleRow( Sample sample ) {
	this.sample = sample;
    }

    /**
     * Get the <code>Sample</code> value.
     *
     * @return a <code>Sample</code> value
     */
    public final Sample getSample() {
	return sample;
    }

    /**
     * Set the <code>Sample</code> value.
     *
     * @param sample The new Sample value.
     */
    public final void setSample(final Sample sample) {
	this.sample = sample;
    }

    /**
     * Get the <code>Studyname</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getStudyname() {
	return studyname;
    }

    /**
     * Set the <code>Studyname</code> value.
     *
     * @param studyname The new Studyname value.
     */
    public final void setStudyname(final String studyname) {
	this.studyname = studyname;
    }

    /**
     * Get the <code>Typename</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getTypename() {
	return typename;
    }

    /**
     * Set the <code>Typename</code> value.
     *
     * @param typename The new Typename value.
     */
    public final void setTypename(final String typename) {
	this.typename = typename;
    }

    /**
     * Get the <code>Subjectid</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getSubjectid() {
	return subjectid;
    }

    /**
     * Set the <code>Subjectid</code> value.
     *
     * @param subjectid The new Subjectid value.
     */
    public final void setSubjectid(final String subjectid) {
	this.subjectid = subjectid;
    }

    /**
     * Get the <code>Accessions</code> value.
     *
     * @return an <code>Accession[]</code> value
     */
    public final Accession[] getAccessions() {
	return accessions;
    }

    /**
     * Set the <code>Accessions</code> value.
     *
     * @param accessions The new Accessions value.
     */
    public final void setAccessions(final Accession[] accessions) {
	this.accessions = accessions;
    }

    /**
     * Get the <code>Visit</code> value.
     *
     * @return a <code>SampleProcess</code> value
     */
    public final SampleProcess getVisit() {
	return visit;
    }

    /**
     * Set the <code>Visit</code> value.
     *
     * @param visit The new Visit value.
     */
    public final void setVisit(final SampleProcess visit) {
	this.visit = visit;
    }

}
