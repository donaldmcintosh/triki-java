@mand
Feature: Search resources

	Scenario: Search for content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And search for term Administrator and expect following response
			| value 		| id 							| encodeurl 	|
			| Administrator group | /graph/subject/group_admin | group_admin |
			| Administrator | /graph/subject/user_administrator | user_administrator |

	Scenario: Search for properties
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And search properties for term tr and expect following response
			| value 		| id 							| encodeurl 	|
			| restricted    | /graph/subject/property_restricted | property_restricted |

	Scenario: Search for local prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And search local prefixes for term root and expect following response
			| value 		| id 							| encodeurl|
			| /  | /graph/subject/prefix_root	| prefix_root |

	Scenario: Search for local prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And search local prefixes for term content and expect following response
			| value 		  | id 							| encodeurl|
			| /content/  | /graph/subject/prefix_content	| prefix_content |
			
	Scenario: Search for non-local prefix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And search local prefixes for term foaf and expect following response
			| value 		  | id 							| encodeurl|
