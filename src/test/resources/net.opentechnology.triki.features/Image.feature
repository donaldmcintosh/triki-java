@mand
Feature: Specification for content resource

	Scenario: Upload an image file
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And save graph to /tmp/site.ttl
		And login with user admin password admin
		And add content from files
			| IMG_0154_mob.JPG |
		And check response page contains "Successfully uploaded file IMG-0154-mob.JPG"
		And check content exists
			| IMG-0154-mob-thumb.jpg |
			| IMG-0154-mob-web.jpg |
		And get resource /graph/subject/content_IMG-0154-mob-web.jpg and check contains
			| IMG-0154-mob-web.jpg |
			| Width |
			| Height |
			| Mon 28 Jul, 2014, 11:12 |
		And get resource /graph/subject/content_IMG-0154-mob-thumb.jpg and check contains
			| IMG-0154-mob-thumb.jpg |
		And get image /content/IMG-0154-mob-web.jpg
		And get image /content/IMG-0154-mob-thumb.jpg
		And get resource /image/IMG-0154-mob-web.jpg and check contains
			| Image IMG-0154-mob-web.jpg |
			| Mon 28 Jul, 2014, 11:12 |
	
	Scenario: Upload image with long filename
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| tmp5915-IMG20170102134601341HDR-483121498.jpg |
		And check response page contains "Successfully uploaded file tmp5915-IMG20170102134601341HDR-483121498.jpg"
		And check content exists
			| tmp5915-IMG20170102134601341HDR-483121498-web.jpg |
			| tmp5915-IMG20170102134601341HDR-483121498-thumb.jpg |
		And get resource /graph/subject/content_tmp5915-IMG20170102134601341HDR-483121498-web.jpg and check contains
			| tmp5915-IMG20170102134601341HDR-483121498-web.jpg |
			| Private |
		And get image /content/tmp5915-IMG20170102134601341HDR-483121498-web.jpg

	Scenario: Upload a few images and check images page
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with user admin password admin
		And add content from files
			| DSC_0138.JPG |
			| DSC_0681.JPG |
			| IMG_0289.JPG |
		And get resource /images and check contains
			| Posted: Sun 13 Apr, 2014, 09:15 |
			| Posted: Tue 29 Sep, 2015, 08:15 |
			| Posted: Thu 22 May, 2014, 11:26 |
			
	Scenario: Upload an image file with no date
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And save graph to /tmp/site.ttl
		And login with user admin password admin
		And add content from files
			| photo599687055428134892.jpg |
		And check response page contains "Successfully uploaded file photo599687055428134892.jpg"