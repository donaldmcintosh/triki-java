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

package net.opentechnology.triki.core.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap.Iter;

import javax.inject.Inject
import javax.servlet.http.HttpSession

import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.xsd.XSDDateTime
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement
import org.apache.jena.rdf.model.StmtIterator
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier

import groovy.lang.Closure
import NodeFormModel
import net.opentechnology.triki.auth.resources.AuthenticateResource;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.renderer.DateRenderer;
import net.opentechnology.triki.core.resources.LinkProperty
import net.opentechnology.triki.core.resources.NodeFormModel
import net.opentechnology.triki.core.resources.TextProperty
import net.opentechnology.triki.schema.Dcterms
import net.opentechnology.triki.schema.Time;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class ResourceDto extends BaseDto {

	@Inject	@Qualifier("siteModel")
	private Model siteModel;
	
	@Inject
	private CachedPropertyStore props;
	
	@Inject
	private SettingDto settingDto;
	
	def boolean resourceExists(def resurl)
	{
		boolean found = false
		SparqlExecutor sparqler = new SparqlExecutor();
		String query = """
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	    PREFIX triki: <http://www.opentechnology.net/triki/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?anyobj
		WHERE {  
			 <${resurl}> ?anypred ?anyobj .  
        }
"""
	
		sparqler.execute(siteModel, query){ QuerySolution soln ->
			found=true
		}
		
		return found;
	}
	
	public void deleteResource(String url, List msgs)
	{
		Resource res = siteModel.createResource(url)
		List stmts = res.listProperties().toList()
		siteModel.remove(stmts);
		msgs << "Successfully deleted all statements for node ${url}."
	}
	
	public void createdHandling(NodeFormModel formModel)
	{
		if(formModel.textProperties.any {
			TextProperty text -> text.getPropertyField().property == Dcterms.created })
		{
			TextProperty created = formModel.textProperties.find { TextProperty text -> text.getPropertyField().property == DCTerms.created }
			Date now = new Date()
			String webPattern = DateRenderer.DATE_RENDERER_FORMAT;
			SimpleDateFormat formatter = new SimpleDateFormat(webPattern);
			try {
				if(formatter.parse(created.valueField.text) == null)
				{
					formModel.addError("Could not parse date, expected format is " + DateRenderer.DATE_RENDERER_FORMAT)
				}
			}
			catch (Exception e)
			{
				formModel.addError("Could not parse date, expected format is " + DateRenderer.DATE_RENDERER_FORMAT + " :" + e.getMessage())
			}
		}
	}

	public void addCreatedNowAndCreator(HttpSession session, Resource resource)
	{
		addCreatedNow(resource);
		addCreator(session, resource);
	}
	
	public void addCreatedAndCreator(HttpSession session, Resource resource, Date date)
	{
		addCreatedNow(resource, date);
		addCreator(session, resource);
	}

	public addCreatedNow(Resource resource) {
		Calendar timestampCal = Calendar.getInstance();
		Literal timestampLiteral = siteModel.createTypedLiteral(timestampCal);
		resource.addProperty(Dcterms.created, timestampLiteral)
		indexDate(resource, timestampCal.getTime())
	}
	
	public Literal getLiteralTimpstamp(String date)
	{
		Calendar timestampCal = Calendar.getInstance();
		Date actualTime = getDateFromString(date)
		timestampCal.setTime(actualTime)
		Literal timestampLiteral = siteModel.createTypedLiteral(timestampCal);
		return timestampLiteral
	}

	public Date getDateFromString(String date) {
		String webPattern = DateRenderer.DATE_RENDERER_FORMAT;
		SimpleDateFormat formatter = new SimpleDateFormat(webPattern);
		Date actualTime = formatter.parse(date)
		return actualTime
	}
	
	public String getFormattedLiteral(Literal dateLiteral)
	{
		XSDDateTime dateTime = (XSDDateTime) XSDDatatype.XSDdateTime.parse(dateLiteral.getLexicalForm());
		Date time = dateTime.asCalendar().getTime()
		String webPattern = DateRenderer.DATE_RENDERER_FORMAT;
		SimpleDateFormat formatter = new SimpleDateFormat(webPattern);
		return formatter.format(time)
	}
	
	public addCreated(Resource resource, Literal dateLiteral) {
		resource.addProperty(Dcterms.created, dateLiteral)
		XSDDateTime dateTime = (XSDDateTime) XSDDatatype.XSDdateTime.parse(dateLiteral.getLexicalForm());
		indexDate(resource, dateTime.asCalendar().getTime())
	}

	public addCreator(HttpSession session, Resource resource) {
		resource.addProperty(Dcterms.creator, session.getAttribute(AuthenticateResource.SESSION_PERSON))
	}
	
	private void indexDate(Resource resource, Date created) {
		SimpleDateFormat monFormat = new SimpleDateFormat("MMMMM");
		String month = monFormat.format(created);
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		String year = yearFormat.format(created);
		
		String monthResName = year + month;
		Resource monthRes = siteModel.createResource(props.getPrivateUrl() + "month/" + monthResName, Time.Instant);
		if(siteModel.containsResource(monthRes))
		{
			monthRes.addLiteral(DCTerms.description, month + " " + year);
			monthRes.addLiteral(DCTerms.title, month + " " + year);
			monthRes.addProperty(Time.month, month);
			monthRes.addProperty(Triki.restricted, settingDto.getSettingAsResource(SettingDto.Settings.YEARMONTHRESTRICTION.name()))
		}
		
		String yearResName = year;
		Resource yearRes = siteModel.createResource(props.getPrivateUrl() + "year/" + yearResName, Time.Instant);
		if(siteModel.containsResource(yearRes))
		{
			yearRes.addLiteral(DCTerms.description, year);
			yearRes.addLiteral(DCTerms.title, year);
			yearRes.addProperty(Time.year, year);
			yearRes.addProperty(Triki.restricted, settingDto.getSettingAsResource(SettingDto.Settings.YEARMONTHRESTRICTION.name()))
			monthRes.addProperty(Time.year, yearRes)
		}
		
		resource.addProperty(Time.month, monthRes);
	}
	
}
