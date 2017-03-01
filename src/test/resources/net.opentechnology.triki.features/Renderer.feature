@mand
Feature: Specification for renderer

	Scenario: Upload a markdown file and get page including it
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview.md |
		And add node with following details
			| id | newblog |
			| prefix:prefix_blog | /blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
			| proplink1:property_type | type |
			| objlink1:type_blog	| Blog |
			| proptext2:property_title | title |
			| obj2      | Mountain Bike Review |
			| proptext6:property_description | title |
			| obj6      | Review of the trusty Specialiazed Rockhopper Sport 2014 |
			| proplink3:property_include | Include |
			| objlink3:content_bikereview.md | bikereview.md |
		And get resource /blog/newblog and check contains
			| Mountain Bike Review |
			| Review of the trusty Specialiazed Rockhopper Sport 2014 |
			| Review of Manitou Marvel forks |

	Scenario: Upload a few  blogs, check blogs renderer and SPARQL query
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview.md |
		# Add a few blogs
		And add node with following details
			| id | Review1 |
			| prefix:prefix_blog | /blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink1:property_type | type |
			| objlink1:type_blog	| Blog |
			| proptext2:property_title | title 1 |
			| obj2      | Mountain Bike Review 1 |
			| proptext6:property_description | title |
			| obj6      | Review of the trusty Specialiazed Rockhopper Sport 2014 |
			| proplink3:property_include | Include |
			| objlink3:content_bikereview.md | bikereview.md |
		And add node with following details
			| id | Review2 |
			| prefix:prefix_blog | /blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink1:property_type | type |
			| objlink1:type_blog	| Blog |
			| proptext2:property_title | title 2 |
			| obj2      | Mountain Bike Review 2 |
			| proptext6:property_description | title |
			| obj6      | Review of the trusty Specialiazed Rockhopper Sport 2014 |
			| proplink3:property_include | Include |
			| objlink3:content_bikereview.md | bikereview.md |
		And add node with following details
			| id | Review3 |
			| prefix:prefix_blog | /blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink1:property_type | type |
			| objlink1:type_blog	| Blog |
			| proptext2:property_title | title 3 |
			| obj2      | Mountain Bike Review 3 |
			| proptext6:property_description | title |
			| obj6      | Review of the trusty Specialiazed Rockhopper Sport 2014 |
			| proplink3:property_include | Include |
			| objlink3:content_bikereview.md | bikereview.md |		
		And get resource /blogs and check contains
			| Mountain Bike Review 1 |
			| Mountain Bike Review 2 |
			| Mountain Bike Review 3 |
		# Add a query
		And add node with following details
			| id | recentblogsquery |
			| prefix:prefix_query | /recentblogs |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink1:property_type | type |
			| objlink1:type_query	| Query |
			| proptext2:property_title | title 1 |
			| obj2      | Recent blogs query |
			| proptext6:property_description | title |
			| obj6      | Last 2 blogs added |
			| proptext3:property_sparql | sparql |
			| obj3 | prefix group: <http://localhost:8080/group/>\nprefix dc: <http://purl.org/dc/terms/>\nprefix type: <http://localhost:8080/type/>\nprefix xsd: <http://www.w3.org/2001/XMLSchema#dateTime>\nprefix triki: <http://www.opentechnology.net/triki/0.1/>\nselect ?target ?created\nWHERE  { ?target a type:blog;\ntriki:restricted group:public;\ndc:created ?created }\nORDER BY desc(?created)\nLIMIT 2\n |
		# Add new property for recent blogs query
		And add node with following details
			| id | recentblogs |
			| prefix:prefix_property | /property |
			| proplink1:property_type | type |
			| objlink1:triki_Property	| Property |
			| proptext2:property_title | title |
			| obj2      | Recent blogs property |
			| proptext4:property_order | order |
			| obj4      | 31 |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://localhost:8080/localprop/recentblogs |
		# Update home page new recent blogs property 
		And edit resource /graph/subject/root_ and check contains
			| Home Page |
		And update node with following details
			| action | update |
			| id ||
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:type_home	| Home page |
			| proptext2:property_title | title |
			| obj2      | Home page |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_recentblogs | Recent blogs |
			| objlink6:query_recentblogsquery | Recent blogs query |
		And get resource / and check content is ordered
			| Mountain Bike Review 3 |
			| Mountain Bike Review 2 |

