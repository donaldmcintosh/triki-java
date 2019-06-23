@mand
Feature: Specification for graph resource
	
	Scenario: Add a node with a mix of content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | prices |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
			| proptext3:property_description | description |
			| obj3 		| All product prices |
			| proplargetext4:property_include | A really big blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And get resource /graph/subject/root_prices and check contains
			| All product prices |
			| Prices |
			| title |
			| type |
			| Admin |
			#| A really big blog |
			#| Public access group |
			| /prices |
	
	Scenario: Add a node with invalid id
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | not/valid |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
		And check response page contains "Path must contain only a-zA-Z0-9 or dash or dot characters"
		And add node with following details
			| id | something_almost_valid |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
		And check response page contains "Path must contain only a-zA-Z0-9 or dash or dot characters"
		And add node with following details
			| id | search(for)something |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
		And check response page contains "Path must contain only a-zA-Z0-9 or dash or dot characters"
		And add node with following details
			| id |@something |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
		And check response page contains "Path must contain only a-zA-Z0-9 or dash or dot characters"
	
	Scenario: Add a duplicate node and check ordering on validation page
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
		And check response page contains "Added node"
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "already exists"
		And check response page contains in order
			| property_type |
			| property_title |
			| property_restricted |

	Scenario: Add a node with empty property and empty object
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink1: |  |
			| objlink1:root_blog	| Standard |
		And check response page contains "All properties must be defined."
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink1:property_type |  |
			| objlink1:	| Standard |
		And check response page contains "All properties must be defined."

	Scenario: Add a node missing type properties
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
		And check response page contains "A new node must define a type property."
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proptext2:property_title | title |
			| obj2      | Prices |
		And check response page contains "A new node must define a type property."

	Scenario: Add a node missing title properties
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
		And check response page contains all of
			| A new node must define a restricted property. |
			| A new node must define a title property. |
			| property_type |
	
	Scenario: Add a node missing restricted properties
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
		And check response page contains "A new node must define a restricted property."		

	Scenario: Add a node missing prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | newblog |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
		And check response page contains "A new node must have a prefix defined."

	Scenario: Add a node missing id i.e. the home page
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
		And check response page contains "A resource with name http://localhost:8080/ already exists"

	Scenario: Add and get object resource in graph form
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And save graph to /tmp/site2.ttl
		And get resource /graph/object/foaf_Group and check contains
			| Public access group |

	Scenario: Add a node and then edit it
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | bikes |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Bikes |
			| proptext3:property_description | description |
			| obj3 		| MTB Bikes |
			| proplargetext4:property_include | A blog all about mountain bikes... |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And get resource /graph/subject/root_bikes and check contains
			| MTB Bikes |
			| title |
			| type |
			| Admin |
			| /bikes |
		And edit resource /graph/subject/root_bikes and check contains
			| MTB Bikes |
		And update node with following details
			| action | update |
			| id | bikes |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Mountain Bikes ROCK |
			| proptext3:property_description | description |
			| obj3 		| MTB is awesome |
			| proplargetext4:property_include | A blog all about mountain bikes... |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And get resource /graph/subject/root_bikes and check contains
			| Mountain Bikes ROCK |
			| MTB is awesome |	

	Scenario: Add a new prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | mtb |
			| prefix:prefix_prefix | /prefix |
			| proplink1:property_type | type |
			| objlink1:triki_Prefix	| Prefix |
			| proptext2:property_title | title |
			| obj2      | MTB|
			| proptext3:property_description | description |
			| obj3 		| Mountain biking name space |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://singletrack.com/vocabulary/ |
		And check response page contains "Added new graph namespace prefix"
		And get resource /graph/subject/prefix_mtb and check contains
			| MTB |
			| http://singletrack.com/vocabulary/ |

	Scenario: Add a new prefix with a name that is more than one word
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | mtb |
			| prefix:prefix_prefix | /prefix |
			| proplink1:property_type | type |
			| objlink1:triki_Prefix	| Prefix |
			| proptext2:property_title | title |
			| obj2      | MTB namespace |
			| proptext3:property_description | description |
			| obj3 		| Mountain biking name space |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://singletrack.com/vocabulary/ |
		And check response page contains "When adding a new prefix"

	Scenario: Add a new prefix and associated property
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | MTB |
			| prefix:prefix_prefix | /prefix |
			| proplink1:property_type | type |
			| objlink1:triki_Prefix	| Prefix |
			| proptext2:property_title | title |
			| obj2      | MTB |
			| proptext3:property_description | description |
			| obj3 		| Mountain biking name space |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://singletrack.com/vocabulary/ |
		And check response page contains "Added new graph namespace prefix"
		And get resource /graph/subject/prefix_MTB and check contains
			| MTB |
			| Mountain biking name space |
			| http://singletrack.com/vocabulary/ |
		And add node with following details
			| id | GPX |
			| prefix:prefix_property | /property |
			| proplink1:property_type | type |
			| objlink1:triki_Property	| Standard |
			| proptext2:property_title | title |
			| obj2      | GPS Route data (gpx) |
			| proptext3:property_description | description |
			| obj3 		| GPS route data reference |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://singletrack.com/vocabulary/gpx |
		And check response page contains "Added node"
		And get resource /graph/subject/property_GPX and check contains
			| GPS Route data (gpx) |
			| http://singletrack.com/vocabulary/gpx |

	Scenario: Add a new property in an unknown namespace/prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | GPX |
			| prefix:prefix_property | /property |
			| proplink1:property_type | type |
			| objlink1:triki_Property	| Standard |
			| proptext2:property_title | title |
			| obj2      | GPS Route data (gpx) |
			| proptext3:property_description | description |
			| obj3 		| GPS route data reference |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
			| proplink6:property_identifier | identifier |
			| objlink6: | http://singletrack.com/vocabulary/gpx |
		And check response page contains "When adding a new property, there must be a prefix for the associated identifer"
		
	Scenario: Add a node and then rename it
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | prices |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
			| proptext3:property_description | description |
			| obj3 		| All product prices |
			| proplargetext4:property_include | A really big blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And edit resource /graph/subject/root_prices and check contains
			| Prices |
		And update node with following details
			| action | update |
			| id | bikes2 |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Mountain Bikes ROCK |
			| proptext3:property_description | description |
			| obj3 		| MTB is awesome |
			| proplargetext4:property_include | A blog all about mountain bikes... |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Updated node"

	Scenario: Add a node and then clone it
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | bikes |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Bikes |
			| proptext3:property_description | description |
			| obj3 		| MTB Bikes |
			| proplargetext4:property_include | A blog all about mountain bikes... |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And get resource /graph/subject/root_bikes and check contains
			| MTB Bikes |
			| title |
			| type |
			| Admin |
			| /bikes |
		And clone resource /graph/subject/root_bikes and check does not contains
			| MTB Bikes |
	
	Scenario: Add a node and then delete it
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | bikes |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Bikes |
			| proptext3:property_description | description |
			| obj3 		| MTB Bikes |
			| proplargetext4:property_include | A blog all about mountain bikes... |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And delete resource /graph/subject/root_bikes and check response contains
			| Successfully deleted all statements for node |

	Scenario: Add a new media type and check directory is created
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | MTB |
			| prefix:prefix_mediatype | /prefix/mediatype |
			| proplink1:property_type | type |
			| objlink1:triki_MediaType	| MediaType |
			| proptext2:property_title | title |
			| obj2      | gpx |
			| proptext3:property_description | description |
			| obj3 		| GPX files |
			| proptext4:property_format | format |
			| obj4 		| gpx |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
		And check response page contains "Successfully created a new directory for"
		And check content dir gpx exists

	Scenario: Add a node with check ordering
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | prices |
			| prefix:prefix_root | / |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
			| proptext3:property_description | description |
			| obj3 		| All product prices |
			| proplargetext4:property_include | A really big blog |
		And check response page contains "Added node"
		And get resource /graph/subject/root_prices and check content is ordered
			| property_type |
			| property_title |
			| property_restricted |

	Scenario: Add a node with check ordering
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add node with following details
			| id | prices |
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:root_blog	| Standard |
			| proptext2:property_title | title |
			| obj2      | Prices |
			| proptext3:property_description | description |
			| obj3 		| All product prices |
			| proplargetext4:property_include | A really big blog |
			| proplink5:property_restricted | Group |
			| objlink5:group_admin | Admin group |
		And check response page contains "Added node"
		And edit resource /graph/subject/root_prices and check content is ordered
			| property_type |
			| property_title |
			| property_restricted |
				
	Scenario: Edit home page
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit resource /graph/subject/root_ and check contains
			| Home Page |
		And update node with following details
			| action | update |
			| id ||
			| prefix:prefix_root | / |
			| proplink1:property_type | type |
			| objlink1:type_home	| Home Page |
			| proptext2:property_title | title |
			| obj2      | Home Page |
			| proplink5:property_restricted | Group |
			| objlink5:group_public | Public group |
		And check response page contains "Updated node"