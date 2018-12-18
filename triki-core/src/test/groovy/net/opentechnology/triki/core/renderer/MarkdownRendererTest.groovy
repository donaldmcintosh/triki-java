package net.opentechnology.triki.core.renderer

import spock.lang.Specification

class MarkdownRendererTest extends Specification {

    void "check href rendered correctly"() {
        given:

        MarkdownRenderer markdownRenderer = new MarkdownRenderer()

        when: "render some markdown"

        def markdown = "[hello](http://www.blah.com/hello)"

        then: "generate expected HTML"

        markdownRenderer.render(markdown) == "<a href=\"http://www.blah.com/hello\">hello</a>\n"
    }

    void "check paragraph wrap working rendered correctly"() {
        given:

        MarkdownRenderer markdownRenderer = new MarkdownRenderer()

        when: "render some markdown"

        def markdown = "yeah! <p> [hello](http://www.blah.com/hello) <p>"

        then: "generate expected HTML"

        markdownRenderer.render(markdown) == "yeah! <p> <a href=\"http://www.blah.com/hello\">hello</a> <p>\n"
    }

    void "dont break new lines"() {
        given:

        MarkdownRenderer markdownRenderer = new MarkdownRenderer()

        when: "render some markdown"

        def markdown = '''hello
world
'''

        then: "generate expected HTML"

        markdownRenderer.render(markdown) == "hello\nworld\n"
    }

}
