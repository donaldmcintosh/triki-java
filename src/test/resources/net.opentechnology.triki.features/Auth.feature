@mand
Feature: Authorisation and authentication

	Scenario: Login to site as admin
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And get resource /login and check contains
			| Login: |
			| Password: |
		And login with admin/admin and check response contains
			| Hello world! |

	Scenario: Login to site as admin with bad password
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And get resource /login and check contains
			| Login: |
			| Password: |
		And login with admin/blah and check response contains
			| Login: |

	Scenario: Login and logoff
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And get resource /login and check contains
			| Login: |
			| Password: |
		And login with admin/admin and check response contains
			| Hello world! |
		And get resource /auth/logoff and check contains
			| Home Page |

	Scenario: Access auth page without logging in and then log in
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And get resource /graph and check contains
			| Login: |
			| Password: |
		And login with admin/admin and check response contains
			| Upload |

	Scenario: Access upload page
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with admin/admin and check response contains
			| Hello world! |
		And get resource /content/upload and check contains
			| Select |	

		@dev
	Scenario: Check logoff
		Given initialise triki 
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080
		And login with admin/admin and check response contains
			| Hello world! |
		And logoff and check response contains
			| Home Page |