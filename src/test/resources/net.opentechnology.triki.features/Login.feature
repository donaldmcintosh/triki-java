@wip
Feature: Triki sending messages

	Scenario: Send in a Note and syndicate to Twitter with short link
		Given add intercept routes
		And initialise triki with test.properties
		And create a new note with following parameters
			| Note      | Check out my new Album http://donaldmcintosh.net/resource/ben-nevis	|
			| Subject  | New Ben Nevis Album   	|
			| Twitter   | Yes 	|
		And check HTTP response is 303
		And check URI returned is valid
		And check contents of created URL contain
			| Note      | Check out my new Album http://donaldmcintosh.net/resource/ben-nevis		|
		And check sent 1 twitter messages 
		And check twitter message contains
			| Note      | Check out my new Album							|
#			| URL  		| http://localhost:8081/resource/note-new-ben-nevis-album    	|
		And stop triki
	
	Scenario: Send in a Note and syndicate Twitter with no short link
		Given add intercept routes
		And initialise triki with test.properties
		And create a new note with following parameters
			| Note      | Check out my new Album							|
			| Twitter   | Yes 	|
		And check HTTP response is 303
		And check URI returned is valid
		And check contents of created URL contain
			| Note      | Check out my new Album							|
		And check sent 1 twitter messages 
		And stop triki
	
	Scenario: Send in a long Note and syndicate Twitter with no short link
		Given add intercept routes
		And initialise triki with test.properties
		And create a new note with following parameters
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election we need to be very clear on what currency we will have afterwards	|
			| Twitter   | Yes 	|
		And check HTTP response is 303
		And check URI returned is valid
		And check contents of created URL contain
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election we need to be very clear on what currency we will have afterwards |
		And check sent 1 twitter messages 
		And check twitter message contains
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election	|
#			| URL       | http://localhost:8081/resource/note- |
		And stop triki	

	Scenario: Send in a long Note and syndicate Twitter with short link
		Given add intercept routes
		And initialise triki with test.properties
		And create a new note with following parameters
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election we need to be very clear on what currency we will have afterwards	|
			| Subject   | Which Scottish Currency   	|
			| Twitter   | Yes 	|
		And check HTTP response is 303
		And check URI returned is valid
		And check contents of created URL contain
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election we need to be very clear on what currency we will have afterwards |
		And check sent 1 twitter messages 
		And check twitter message contains
			| Note      | #EUreferendum #indyref when we have another Scottish Independence Election	|
#			| URL       | http://localhost:8081/resource/note-which-scottish-currency |
		And stop triki	
	
	Scenario: Send in a Note and syndicate to Email with short link
		Given add intercept routes
		Given initialise triki with test.properties
		And create a new email with following parameters
			| Note      | Check out my new Album http://donaldmcintosh.net/resource/ben-nevis	|
			| Subject   | New Ben Nevis Album   	|
			| Email1    | me |
			| Email2    | Jackie |
		And check HTTP response is 302
		And check URI returned is valid
		And check sent 2 email messages 
		And stop triki