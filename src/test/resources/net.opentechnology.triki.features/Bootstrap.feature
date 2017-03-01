@mand
Feature: triki startup

    On startup without a root dir, triki should:
    - Return immediately with a helpful error

    On startup with an empty root dir, triki should:
    - Recognise this
    - Layout the default expected file structure
    - Generate an initial site.ttl to the ttl directory and load it
    - Output the URL to browse to the standard output

    On startup with a root dir, triki should:
    - Recognise this
    - Expect to find default directories (actively check)
    - Load the site file from ttl/site.ttl
    - Load default modules with ttl configuration

    Startup should error if any of the following happens:
    - Cannot create the default site file then exit with error
    - If cannot create the content directories, then exit with error
    - Cannot bind to default port, then exit with error
    - The site file passed is corrupt

	Scenario: triki startup without no directory specified
		Given initialise triki
		And start triki with no content path
		And expect error message "Must specify a content directory, with -Dcontent_dir=<dir>"

	Scenario: triki startup without an empty content directory
		Given initialise triki
		And create empty directory /tmp/content
		And start triki with content path /tmp/content and port 8080