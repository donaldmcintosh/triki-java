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

package net.opentechnology.triki.core.resources

import groovy.util.logging.Log4j
import java.awt.event.ItemEvent
import java.util.List;
import java.util.Map

import javax.inject.Inject
import javax.swing.plaf.basic.BasicButtonListener.Actions;

import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.rdf.model.StmtIterator
import org.apache.jena.shared.PropertyNotFoundException;

import net.opentechnology.triki.core.boot.CachedPropertyStore;
import net.opentechnology.triki.core.boot.Utilities
import net.opentechnology.triki.core.dto.PrefixDto
import net.opentechnology.triki.core.dto.PropertyDto
import net.opentechnology.triki.core.dto.ResourceDto
import net.opentechnology.triki.core.resources.NodeFormModel.Action
import net.opentechnology.triki.schema.Triki;

import org.springframework.beans.factory.annotation.Qualifier
import org.apache.jena.vocabulary.DCTerms

import org.apache.commons.lang.StringEscapeUtils

/*
 * Need getters in this Groovy file because StringTemplate relies on reflection
 * 
 * Deciding to write server-side validation was a tough one.  On balance, due to fact there are so
 * few forms and they are quite complicated, this seemed like the best place for it.  StringTemplate
 * is so good for the view side and nothing (Wicket, Faces) is sufficiently good to justify plumbing it
 * in just for a few forms.
 */
@Log4j
public class NodeFormModel {
	private static final String OBJLINK_PREFIX = "objlink"
	private static final String PROPLINK_PREFIX = "proplink"
	private static final String PROPTEXT_PREFIX = "proptext"
	private static final String PREFIX_PREFIX = "prefix"
	private static final String OBJ_PREFIX = "obj"
	
	public enum Action  {
		add,
		view,
		edit,
		delete,
		clone
	}
	
	@Inject @Qualifier("siteModel")
	protected Model siteModel;
	
	@Inject
	private Utilities utils;
	
	@Inject
	private PropertyDto propertyDto;
	
	@Inject
	private ResourceDto resourceDto;
	
	@Inject
	private PrefixDto prefixDto;
	
	@Inject
	private CachedPropertyStore propStore;
	
	private Action action;
	private NodeAddress nodeAddress = new NodeAddress();
	private List<LinkProperty> linkProperties = new ArrayList<LinkProperty>();
	private List<TextProperty> textProperties = new ArrayList<TextProperty>();
	private List<String> msgs = new ArrayList<>();
	private List<String> errors = new ArrayList<>();
	private List<NodeProperty> allProperties;
	
	public addFormData(Map formData)
	{
		addAction(formData)
		addAddress(formData)
		addTextFields(formData)
		addLinkFields(formData)
		linkPropertyDefined("property_type", "A new node must define a type property.")
		linkPropertyDefined("property_restricted", "A new node must define a restricted property.")
		textPropertyDefined("property_title", "A new node must define a title property.")
		
		allProperties = textProperties + linkProperties;
	}

	private addAction(Map formData) {
		if(formData."action" == "add")
			action = Action.add
		else if (formData."action" == "edit")
			action = Action.edit
		else if (formData."action" == "clone")
			action = Action.clone
	}
	
	public getFormData(String encodedId, Action action)
	{
		this.action = action
		Resource res = getAddress(encodedId);
		populateProperties(res)
		if(action == Action.clone)
		{
			clearValues()
		}
		allProperties = textProperties + linkProperties
	}
	
	private getAddress(String encodedId)
	{
		String url = utils.decodeShortResource(encodedId);
		nodeAddress.url = url;
		nodeAddress.id = encodedId.replaceFirst('^.*_', "")
		nodeAddress.path = nodeAddress.url.replace(propStore.getPrivateUrl(),"/");
		
		String prefixShortCode = encodedId.replaceFirst('_.*$', "")
		prefixDto.getPrefixFromCode(prefixShortCode) { String title, Resource prefixResource ->
			nodeAddress.prefix = prefixResource
			nodeAddress.prefixName = PREFIX_PREFIX + ":" + utils.encodeResource(prefixResource.URI.toString())
			nodeAddress.prefixValue = title
		}
		
		Resource res = siteModel.getResource(url)
		return res;
	}
	
	private populateProperties(Resource res)
	{
		StmtIterator stmtIter = res.listProperties()
		int index=0
		while(stmtIter.hasNext())
		{
			index++
			Statement stmt = stmtIter.next()
			Property pred = stmt.getPredicate();
			RDFNode object = stmt.object;
			if(object.isLiteral())
			{
				TextProperty textProp = new TextProperty()
				propertyDto.getPropertyFromUrl(pred.URI) { String title, String localUrl ->
					textProp.propertyField.name = PROPTEXT_PREFIX + "${index}:" + utils.encodeResource(localUrl);
					textProp.propertyField.value = title
					textProp.propertyField.property = pred
				}
				textProp.valueField.name = OBJ_PREFIX + index
				if(!textProp.propertyField.name.endsWith("property_created")){
					textProp.valueField.value = object.asLiteral().getString()
					
				}
				else
				{
					textProp.valueField.value = resourceDto.getFormattedLiteral(object.asLiteral())
				}
				textProp.valueField.text = textProp.valueField.value
				
				textProperties.add(textProp)
			}
			else if(object.isResource())
			{
				LinkProperty linkProp = new LinkProperty()
				propertyDto.getPropertyFromUrl(pred.URI) { String title, String localUrl ->
					linkProp.propertyField.name = PROPLINK_PREFIX + "${index}:" + utils.encodeResource(localUrl);
					linkProp.propertyField.value = title
					linkProp.propertyField.property = pred
				}
				Resource objectResource = siteModel.getResource(object.asResource().URI);
				linkProp.valueField.name = OBJLINK_PREFIX + "${index}:" + utils.encodeResource(objectResource.URI);
				Statement titleStmt = objectResource.getProperty(DCTerms.title)
				if(titleStmt != null)
				{
					linkProp.valueField.value = titleStmt.object.asLiteral();
				}
				else
				{
					linkProp.valueField.value = objectResource.getURI().toString()
				}
				if(isEditableProperty(pred))
				{
					linkProperties.add(linkProp)
				}
			}
		}
	}
	
	private boolean isEditableProperty(Property pred)
	{
		return pred.asResource().getURI() != "http://www.w3.org/2006/time#month"
	}
	
	private addAddress(Map formData)
	{
		try	{
			if(formData."id" != null)
				nodeAddress.id = formData."id".trim()
			else
				nodeAddress.id = ""
			
			formData.find { it.key.startsWith(PREFIX_PREFIX) }.each { prefix ->
				nodeAddress.prefixName = prefix.key 
				nodeAddress.prefixValue = prefix.value
				if (!nodeAddress.prefixName?.trim()) throw new FormValidationException("No prefix defined")
				
				def encodedPrefix = nodeAddress.prefixName.replace(PREFIX_PREFIX + ":", "");
				if (!encodedPrefix?.trim()) throw new FormValidationException("Invalid prefix recieved ${nodeAddress.prefixName}" )
				String prefixUrl = utils.decodeShortResource(encodedPrefix);
				if (!prefixUrl?.trim()) throw new FormValidationException("Invalid prefix URL received ${prefixUrl}" )
				nodeAddress.prefix = siteModel.createResource(prefixUrl);
				String localPrefix = nodeAddress.prefix.getPropertyResourceValue(DCTerms.identifier).URI;
				nodeAddress.url = localPrefix + nodeAddress.id
				nodeAddress.path = nodeAddress.url.replace(propStore.getPrivateUrl(),"/");
			}
			
			if (nodeAddress.prefix ==  null) throw new FormValidationException("A new node must have a prefix defined.")
			//if (!nodeAddress.id?.trim()) throw new FormValidationException("A new node must have an ID defined.")
			if(nodeAddress.id != "" && !(nodeAddress.id ==~ /[a-zA-Z0-9\-\.]+/)) throw new FormValidationException("Path must contain only a-zA-Z0-9 or dash or dot characters.")
			if(!nodeAddress.path?.trim()) throw new FormValidationException("No path defined.")
			if (!nodeAddress.url?.trim()) throw new FormValidationException("No URL defined.")
			if ((action == Action.add) && resourceDto.resourceExists(nodeAddress.url)) throw new FormValidationException("A resource with name ${nodeAddress.url} already exists.")
//			if ((action == Action.edit) && !resourceDto.resourceExists(nodeAddress.url)) throw new FormValidationException("A resource with name ${nodeAddress.url} does not exist.")
		}
		catch(FormValidationException e)
		{
			errors << e.getMessage()
		}
	}
	
	private addTextFields(Map formData) {
		try{
			formData.findAll { it.key.startsWith(PROPTEXT_PREFIX) }.each { prop ->
				TextProperty textProp = new TextProperty()
				String predicateUrl = getResourceFromFormParam(prop.key, PROPTEXT_PREFIX);
				
				textProp.index = getPropIndex(prop.key, PROPTEXT_PREFIX)
				if (textProp.index == -1) throw new FormValidationException("Invalid index in field ${prop.key}")
				textProp.propertyField.name = prop.key
				textProp.propertyField.value = prop.value
				textProp.propertyField.property = propertyDto.getPropertyFromType(predicateUrl);
				if (textProp.propertyField.property == null) throw new FormValidationException("No property resolved for ${predicateUrl}")
				textProp.valueField.name = OBJ_PREFIX + textProp.index
				textProp.valueField.value = formData."${textProp.valueField.name}"
				textProp.valueField.text = textProp.valueField.value
				if (!textProp.valueField.text?.trim()) throw new FormValidationException("No text value provided for ${textProp.textField.name}")
				addTextProp(textProp)
			}
		}
		catch(FormValidationException e)
		{
			errors << e.getMessage()
		}
	}
	
	private addLinkFields(Map formData) {
		try {
			formData.findAll { it.key.startsWith(PROPLINK_PREFIX) }.each { prop ->
				LinkProperty linkProp = new LinkProperty()
				String predicateUrl = getResourceFromFormParam(prop.key, PROPLINK_PREFIX)
				
				String objKey = OBJLINK_PREFIX + getPropIndex(prop.key, PROPLINK_PREFIX);
				def objParam = formData.find {it.key.startsWith("${objKey}:")}.key
				String objValue = formData.find {it.key.startsWith("${objKey}:")}.value
				String objUrl = (!objValue.startsWith("http")) ? getResourceFromFormParam(objParam, objKey) : objValue
				
				linkProp.index = getPropIndex(prop.key, PROPLINK_PREFIX)
				if (linkProp.index == -1) throw new FormValidationException("Invalid index in field ${prop.key}")
				linkProp.propertyField.name = prop.key
				linkProp.propertyField.value = prop.value
				linkProp.propertyField.property = propertyDto.getPropertyFromType(predicateUrl)
				if (linkProp.propertyField.property == null) throw new FormValidationException("No property resolved for ${predicateUrl}")
				linkProp.valueField.name = objParam
				linkProp.valueField.value = objValue
				linkProp.valueField.link = siteModel.getResource(objUrl)
				if (!linkProp.valueField.value?.trim()) throw new FormValidationException("No link value provided for ${linkProp.linkField.name}")
				
				addLinkProp(linkProp)
			}
		}
		catch(FormValidationException e)
		{
			errors << e.getMessage()
		}
	}
	
	private clearValues()
	{
		nodeAddress.id = null;
		nodeAddress.url = null;
		nodeAddress.path = null;
		
		textProperties.each { TextProperty textProp ->
			textProp.valueField.value = ""
			textProp.valueField.text = ""
		}
		
		linkProperties.findAll {LinkProperty prop ->
			!prop.propertyField.name.endsWith("property_type")
			}.each { 
			LinkProperty linkProp ->
				linkProp.valueField.value = ""
				linkProp.valueField.link = null
		}
	}
	
	public removeCreatedCreator()
	{
		linkProperties.removeAll {LinkProperty prop ->
			prop.propertyField.name.endsWith("property_creator")
		}
		textProperties.removeAll {TextProperty prop ->
			prop.propertyField.name.endsWith("property_created")
		}
		linkProperties.removeAll {LinkProperty prop ->
			prop.propertyField.name.endsWith("property_month")
		}
		
		allProperties = textProperties + linkProperties
	}
	
	private saveTextParams(Resource res) {
		textProperties.each { textProp ->
			if(!textProp.propertyField.name.endsWith("property_created")){
				res.addProperty(textProp.propertyField.property, textProp.valueField.text);
			}
			else
			{
				Literal created = resourceDto.getLiteralTimpstamp(textProp.valueField.text)
				res.addProperty(textProp.propertyField.property, created);
				resourceDto.indexDate(res, resourceDto.getDateFromString(textProp.valueField.text))
			}
		}
	}	

	private saveLinkParams(Resource res) {
		linkProperties.each { linkProp ->
			res.addProperty(linkProp.propertyField.property, linkProp.valueField.link);
		}
	}
	
	private String getResourceFromFormParam(String key, String prefix)
	{
		String propCode = key.replaceAll("^.*:", "");
		if(propCode == "")
		{
			throw new FormValidationException("All properties must be defined.")
		}
		return utils.decodeShortResource(propCode);
	}
	
	private linkPropertyDefined(String encodedProperty, String error) {
		boolean found = false
		linkProperties.each { linkProperty ->
			(linkProperty.propertyField.name.contains(encodedProperty)) ? (found = true) : false
		}
		!found ? errors << error : true
	}
	
	private textPropertyDefined(String encodedProperty, String error) {
		boolean found = false
		textProperties.each { textProperty ->
			(textProperty.propertyField.name.contains(encodedProperty)) ? (found = true) : false
		}
		!found ? errors << error : true
	}
	
	private int getPropIndex(String key, String prefix) {
		String indexProp = key.replaceAll(prefix, "");
		String propIndex = indexProp.replaceAll(":.*\$", "")
		return Integer.parseInt(propIndex)
	}
	
	public String getAddAction()
	{
		// Odd method but needed for StringTemplate
		if (action == action.add)
			return "yes"
		else
			return null
	}
	
	public String getEditAction()
	{
		// Odd method but needed for StringTemplate
		if (action == action.edit)
			return "yes"
		else
			return null
	}
	
	public String getCloneAction()
	{
		// Odd method but needed for StringTemplate
		if (action == action.clone)
			return "yes"
		else
			return null
	}
	
	public void addTextProp(TextProperty textProp)
	{
		textProperties.add(textProp)
	}
	
	public void addLinkProp(LinkProperty linkProp)
	{
		linkProperties.add(linkProp)
	}

	public List<LinkProperty> getLinkProperties() {
		return linkProperties;
	}

	public List<TextProperty> getTextProperties() {
		return textProperties;
	}

	public NodeAddress getNodeAddress() {
		return nodeAddress;
	}

	public List<String> getMsgs() {
		return msgs;
	}

	public List<String> getErrors() {
		// cannot do collect escape for some reason
		return errors
	}
	
	public void addError(String error) {
		errors << error
	}

	public List<NodeProperty> getAllProperties() {
		def props = allProperties.sort { item ->
			int order = 0;
			Property property = item.propertyField.property
			propertyDto.getPropertyFromUrl(item.propertyField.property.URI){String title, String url ->
				Resource propertyRes = siteModel.getResource(url);
				try
				{
				    Literal orderLit = propertyRes.getRequiredProperty(Triki.order).getLiteral()
				    order = orderLit.getInt()
				}
				catch(PropertyNotFoundException pnfe)
				{
					order = 100;
				}
			}
			order
		}
		
		return props
	}

	public Action getAction() {
		return action.toString();
	}
	
	public int getSize()
	{
		return (allProperties != null) ? allProperties.size() : 0
	}
	
}

/*
 * http://localhost:8080/{path}
 * {prefix}/{id}
 */
public class NodeAddress {
	private String id;
	private String prefixName
	private String prefixValue
	private String path;
	private String url;
	private Resource prefix;
	
	public String getId() {
		return id;
	}

	public String getPath() {
		path;
	}
	
	public Resource getPrefix() {
		return prefix;
	}

	public String getPrefixName() {
		return prefixName;
	}
	
	public String getPrefixValue() {
		return prefixValue;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPrefixName(String prefixName) {
		this.prefixName = prefixName;
	}

	public void setPrefixValue(String prefixValue) {
		this.prefixValue = prefixValue;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setPrefix(Resource prefix) {
		this.prefix = prefix;
	}
	
}

public class LinkProperty extends NodeProperty {
	public LinkProperty()
	{
		valueField = new LinkField();
	}
}

public class TextProperty extends NodeProperty {
	public TextProperty()
	{
		valueField = new TextField();
	}
}

public abstract class NodeProperty<T extends Field>
{
	private int index = -1;
	private PropertyField propertyField = new PropertyField();
	private T valueField;
	
	public int getIndex() {
		return index;
	}
	
	public PropertyField getPropertyField() {
		return propertyField;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setPropertyField(PropertyField propertyField) {
		this.propertyField = propertyField;
	}

	public T getValueField() {
		return valueField;
	}

	public void setValueField(T valueField) {
		this.valueField = valueField;
	}
	
}

public class LinkField extends Field {
	private Resource link;

	public Resource getLink() {
		return link;
	}

	public void setLink(Resource link) {
		this.link = link;
	}
}

public class TextField extends Field {
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

public class PropertyField extends Field {
	private Property property;

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}
	
}

public class Field {
	private String name;
	private String value;
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
