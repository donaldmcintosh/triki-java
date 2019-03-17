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

import javax.inject.Inject;

import org.apache.jena.query.QuerySolution
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier

import groovy.lang.Closure;
import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.schema.Foaf;
import net.opentechnology.triki.schema.Triki;
import net.opentechnology.triki.sparql.SparqlExecutor

public class GroupDto extends BaseDto {

    @Inject
    @Qualifier("siteModel")
    private Model model

    @Inject
    private CachedPropertyStore props;

    public Resource addGroup(String groupName, String title) {
        String resName = props.getPrivateUrl() + "group/" + groupName;
        Resource group = model.createResource(resName);
        checkResource(group, RDF.type, Foaf.Group);
        checkString(group, DCTerms.title, title);
        return group;
    }

    public Resource getGroup(String name) {
        String resName = props.getPrivateUrl() + "group/" + name
        Resource group = model.createResource(resName);
        return group
    }

    public Map<String, String> getGroups() {
        Map<String, String> groups = new HashMap<>();
        String query = """
	    PREFIX foaf: <http://xmlns.com/foaf/0.1/>  
		PREFIX dc:    <http://purl.org/dc/terms/>
		SELECT ?sub ?groupTitle
		WHERE {  
			 ?sub a foaf:Group .
             ?sub dc:title ?groupTitle .
        }
""";

        SparqlExecutor sparqler = new SparqlExecutor();
        sparqler.execute(model, query) { QuerySolution soln ->
            String groupUrl = soln.get("sub").asResource().getURI().toString()
            String groupTitle = soln.get("groupTitle").asLiteral().toString()
            String name = groupUrl.minus(props.getPrivateUrl() + "group/");
            groups.put(name, groupTitle);
        }

        return groups;
    }

    void setModel(Model model) {
        this.model = model
    }

    void setProps(CachedPropertyStore props) {
        this.props = props
    }
}
