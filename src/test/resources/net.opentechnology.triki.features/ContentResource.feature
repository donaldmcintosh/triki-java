@mand
Feature: Specification for content resource
	
	Scenario: Get md file
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And get content /blog.md and check contains
			| The best blog in the world. |
	
	Scenario: Get css file is expected type
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And get content /core.css and check content type is text/css

	Scenario: Upload a Markdown file
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview.md |
		And check response page contains "Successfully uploaded file bikereview.md"
		And check content exists
			| bikereview.md |
		And get resource /graph/subject/content_bikereview.md and check contains
			| bikereview.md |
			| Private |

	Scenario: Upload a file with an underscore
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bike_review.md |
		And check response page contains "Replaced underscores with dashes in filename"
		And check response page contains "Successfully uploaded file bike-review.md"

	Scenario: Upload a file with no suffix
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview |
		And check response page contains "File must have a suffix"

	Scenario: Upload a valid template
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| site.stg |
		And check response page contains "Successfully uploaded file site.stg"
		And check response page contains "Successfully validated file site.stg"
		And check content exists
			| site.stg |

	Scenario: Upload a invalid template
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| sitebad.stg |
		And check response page contains "9:27: expecting '|'"

	Scenario: Upload a markdown file and check can access content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview.md |
		And check response page contains "Successfully uploaded file bikereview.md"
		And check content exists
			| bikereview.md |
		And get resource /graph/subject/content_bikereview.md and check contains
			| /content/content |

	Scenario: Upload a markdown file and edit the content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| bikereview.md |
		And check response page contains "Successfully uploaded file bikereview.md"
		And check content exists
			| bikereview.md |
		And edit content bikereview.md and check contains
			| Review of Manitou Marvel forks |
		And update content bikereview.md with content "Review of Manitou Marvel forks updated" and check contains
			| Review of Manitou Marvel forks updated |	

			
	Scenario: Upload a CSS file and edit the content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| style.css |
		And check response page contains "Successfully uploaded file style.css"
		And check content exists
			| style.css |
		And edit content style.css and check contains
			| font-family: verdana; |
		And update content bikereview.md with content "font-family: helvetica;" and check contains
			| helvetica |

	Scenario: Upload a Javascript file and edit the content
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| switch.js |
		And check response page contains "Successfully uploaded file switch.js"
		And check content exists
			| switch.js |
		And edit content switch.js and check contains
			| minLength : 2, |
		And update content bikereview.md with content "minLength : 3," and check contains
			| minLength : 3, |	

	Scenario: Check default template exists on startup
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit content site.stg and check contains
			| header(props) |
		And update content bikereview.md with content "header2(props)" and check contains
			| header2(props) |	

	Scenario: Validate default template file and check actual file unchanged
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit content site.stg and check contains
			| header(props) |
		And validate content site.stg with content "group site;" and check contains
			| group site; |
			| Successfully validated site.stg |
		And get content /site.stg and check contains
			| header(props) |

	Scenario: Validate default invalid template file and check actual file unchanged
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit content site.stg and check contains
			| header(props) |
		And validate content site.stg with content "group$ sit" and check contains
			| group$ sit |
			| Problems validating site.stg |
		And get content /site.stg and check contains
			| header(props) |
	
	Scenario: Check renderer is updated when the template is updated
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit content site.stg and check contains
			| header(props) |
		And update content site.stg with content "group site;" and check contains
			| group site; |
			| Successfully validated site.stg |
			| Reinitialised site template. | 
		And get content /site.stg and check contains
			| group site;|
			
	Scenario: Check textarea is escaped
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And edit content site.stg and check contains
			| _textarea |
		And update content site.stg with content "group site; <textarea></textarea>" and check contains
		    | _textarea |
		And get resource /content/site.stg and check contains
			| <textarea> |
			| </textarea> |