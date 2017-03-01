@mand
Feature: Specification for date manipulation

	Scenario: Check correct dates are available and editable
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
		And get resource /blog/newblog and check has todays date
		And edit resource /graph/subject/blog_newblog and check does not contain 
		 	| month |
		 And update node with following details
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
			| proptext7:property_created | created |
			| obj7      | Wed 05 Oct, 2016, 17:10 |
		And get resource /blog/newblog and check contains
			| Wed 05 Oct, 2016, 17:10 |
			| October 2016 |

	Scenario: Check bad date gives error
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
		And get resource /blog/newblog and check has todays date
		And edit resource /graph/subject/blog_newblog and check does not contain 
		 	| month |
		And update node with following details
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
			| proptext7:property_created | created |
			| obj7      | Wed 05 Pct, 2016, 17:10 |
		And check response page contains "Could not parse date, expected format is "
