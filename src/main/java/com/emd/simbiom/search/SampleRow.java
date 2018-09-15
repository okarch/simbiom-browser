package com.emd.simbiom.search;

import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// import org.jdom2.Document;
// import org.jdom2.Element;
// import org.jdom2.JDOMException;
// import org.jdom2.input.SAXBuilder;
// import org.jdom2.filter.Filters;
// import org.jdom2.xpath.XPathExpression;
// import org.jdom2.xpath.XPathFactory;

import com.emd.simbiom.model.Accession;
import com.emd.simbiom.model.Sample;
import com.emd.simbiom.model.SampleDetails;
import com.emd.simbiom.model.SampleProcess;

import com.emd.io.ReaderInputStream;
import com.emd.util.Stringx;

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
    private SampleDetails sampleDetails;

    private Document xmlDoc;

    private Map<String,String> contents;

    private static Log log = LogFactory.getLog(SampleRow.class);

    public SampleRow( Sample sample ) {
	this.sample = sample;
	this.contents = new HashMap<String,String>();
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

    /**
     * Get the <code>SampleDetails</code> value.
     *
     * @return a <code>SampleDetails</code> value
     */
    public final SampleDetails getSampleDetails() {
	return sampleDetails;
    }

    /**
     * Set the <code>SampleDetails</code> value.
     *
     * @param sampleDetails The new SampleDetails value.
     */
    public final void setSampleDetails(final SampleDetails sampleDetails) {
	this.sampleDetails = sampleDetails;
    }

    /**
     * Retrieves content from an xpath expression.
     *
     * @param xpath the query path.
     * @return the content.
     */
    public String getContent( String xpath ) {
	String cont = contents.get( xpath );
	if( cont != null )
	    return cont;
	if( (xmlDoc == null) && (sampleDetails != null) ) {
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    try {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		xmlDoc = builder.parse( new ReaderInputStream(new StringReader(sampleDetails.getDetails())) );
	    }
	    catch( ParserConfigurationException pce ) {
		log.error( pce.getMessage() );
	    }
	    catch( SAXException sex ) {
		log.error( sex.getMessage() );
	    }
	    catch( IOException ioe ) {
		log.error( ioe.getMessage() );
	    }
	}

	if( xmlDoc != null ) {
	    XPath xPath = XPathFactory.newInstance().newXPath();
	    try {
		NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(xmlDoc, XPathConstants.NODESET);
		int nNodes = nodeList.getLength();
		List<String> vals = new ArrayList<String>();
		log.debug( "Nodes selected for: "+xpath+": "+nNodes );
		for( int i = 0; i < nNodes; i++ ) {
		    Node node = nodeList.item( i );
		    String ps = Stringx.getDefault( node.getTextContent(), "" ).trim();
		    if( (ps.length() > 0) && !(vals.contains( ps )) )
		 	vals.add( ps );
		}
		StringBuilder stb = new StringBuilder();
		boolean first = true;
		for( String val : vals ) {
		    if( first )
			first = false;
		    else
			stb.append( ", " );
		    stb.append( val );
		}
		contents.put( xpath, stb.toString() );
		return stb.toString();
	    }
	    catch( XPathExpressionException xee ) {
		log.error( xee.getMessage() );
	    }
	}
	return "";
    }

//     public String getContent( String xpath ) {
// 	String cont = contents.get( xpath );
// 	if( cont != null )
// 	    return cont;
// 	if( (xmlDoc == null) && (sampleDetails != null) ) {

// FileInputStream fileIS = new FileInputStream(this.getFile());
// DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
// DocumentBuilder builder = builderFactory.newDocumentBuilder();
// Document xmlDocument = builder.parse(fileIS);
// XPath xPath = XPathFactory.newInstance().newXPath();
// String expression = "/Tutorials/Tutorial";
// nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

// 	    SAXBuilder sax = new SAXBuilder();
// 	    sax.setValidation( false );
// 	    try {
// 		xmlDoc = sax.build( new StringReader(sampleDetails.getDetails()) );
// 	    }
// 	    catch( IOException ioe ) {
// 		log.error( ioe );
// 	    }
// 	    catch( JDOMException jdex ) {
// 		log.error( jdex );
// 	    }
// 	}
// 	if( xmlDoc != null ) {
// 	    // try {
// 		XPathFactory xFactory = XPathFactory.instance();
// 		XPathExpression<Element> expr = xFactory.compile( xpath, Filters.element());
// 		List<Element> valElements = expr.evaluate( xmlDoc );
// 		List<String> vals = new ArrayList<String>();
// 		for( Element valElement : valElements ) {
// 		    String ps = valElement.getTextTrim();
// 		    if( (ps.length() > 0) && !(vals.contains( ps )) )
// 			vals.add( ps );
// 		}
// 		StringBuilder stb = new StringBuilder();
// 		boolean first = true;
// 		for( String val : vals ) {
// 		    if( first )
// 			first = false;
// 		    else
// 			stb.append( ", " );
// 		    stb.append( val );
// 		}
// 		contents.put( xpath, stb.toString() );
// 		return stb.toString();
// 	    // }
// 	    // catch( JDOMException jdex ) {
// 	    // 	log.error( jdex );
// 	    // }
// 	}
// 	return "";
//     }

}
