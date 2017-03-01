/************************************************************************************
*
*   This file is part of triki
*
*   Written by Donald McIntosh (dbm@opentechnology.net) 
*
*   triki is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   triki is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with triki.  If not, see <http://www.gnu.org/licenses/>.
*
************************************************************************************/

package net.opentechnology.triki.core.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

public class DateIndexer {
	private final Logger logger = Logger.getLogger(this.getClass());
	@Inject
	@Qualifier("siteModel")
	private Model siteModel;
	@Value("${private_url}")
	private String privateUrl;

	public void index() {
		// Don't use iterators, they are shit and cause concurrent mod issues.
		String queryString = "SELECT ?sub ?obj " + " WHERE { " + " ?sub <" + DCTerms.created.getURI() + "> ?obj . "
				+ " } ORDER BY ?obj ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, siteModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode obj = soln.get("obj");
				RDFNode sub = soln.get("sub");
				if (obj.isLiteral() && sub.isResource()) {
					Literal createdLit = obj.asLiteral();
					Resource subject = sub.asResource();
					try {
						Date date = getDateFromStr(createdLit.getString());
						indexDate(siteModel, subject, date, new String[0]);
					} catch (ParseException e) {
						// Ignore date that could not understand
					}
				}
			}
		} finally {
			qexec.close();
		}
	}

	private Date getDateFromStr(String dateStr) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS");
		return format.parse(dateStr);
	}

	private void indexDate(Model model, Resource image, Date created, String[] restricted) {
		SimpleDateFormat monFormat = new SimpleDateFormat("MMMMM");
		String month = monFormat.format(created);
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		String year = yearFormat.format(created);
		String monthResName = TrikiConstants.resourcePrefix + year + month;
		logger.info("Creating month " + monthResName);
		Resource monthRes = model.createResource(privateUrl + monthResName, Time.Instant);
		monthRes.addLiteral(DCTerms.description, month + " " + year);
		monthRes.addLiteral(DCTerms.title, month + " " + year);
		monthRes.addProperty(Time.month, month);
		Calendar timestampCal = Calendar.getInstance();
		timestampCal.setTimeInMillis(0);
		timestampCal.add(Calendar.YEAR, Integer.parseInt(year) - 1970);
		timestampCal.add(Calendar.MONTH, Month.valueOf(month.toUpperCase().trim()).ordinal());
		Literal timestampLiteral = model.createTypedLiteral(timestampCal);
		monthRes.addProperty(DCTerms.created, timestampLiteral);
		String yearResName = TrikiConstants.resourcePrefix + year;
		logger.info("Creating year " + yearResName);
		Resource yearRes = model.createResource(privateUrl + yearResName, Time.Instant);
		yearRes.addLiteral(DCTerms.description, year);
		yearRes.addLiteral(DCTerms.title, year);
		yearRes.addProperty(Time.year, year);
		yearRes.addProperty(DCTerms.references, Time.Year);
		// TO FIX
		//yearRes.addProperty(Triki.restricted, Triki.public_);
		timestampCal.setTimeInMillis(0);
		timestampCal.add(Calendar.YEAR, Integer.parseInt(year) - 1970);
		Literal yearTimestampLiteral = model.createTypedLiteral(timestampCal);
		yearRes.addProperty(DCTerms.created, yearTimestampLiteral);
		for (String restrict : restricted) {
			Resource restriction = model.createResource(model.getNsPrefixURI("resource") + restrict);
			monthRes.addProperty(Triki.restricted, restriction);
			yearRes.addProperty(Triki.restricted, restriction);
		}
		//monthRes.addProperty(Triki.restricted, Triki.public_);
		monthRes.addProperty(Time.year, yearRes);
		image.addProperty(Time.month, monthRes);
	}

	public void setSiteModel(Model siteModel) {
		this.siteModel = siteModel;
	}
}
