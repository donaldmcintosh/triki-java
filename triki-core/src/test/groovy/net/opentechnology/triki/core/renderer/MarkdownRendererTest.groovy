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

    void "support tables"() {
        given:

        MarkdownRenderer markdownRenderer = new MarkdownRenderer()

        when: "render some markdown"

        def markdown = '''| Day |  Start | End | Distance (miles) | Asc (ft) | 
| ----- | ----- | ----- | ----- | ----- |  
| 1 | Bray Dunes | Le Quesnoy | 150.6 | 4199 |
| 2 | Le Quesnoy | Epernay | 132.6 | 6304 |
'''

        then: "generate expected HTML"

        markdownRenderer.render(markdown) == '''<table>
<thead>
<tr><th>Day</th><th>Start</th><th>End</th><th>Distance (miles)</th><th>Asc (ft)</th></tr>
</thead>
<tbody>
<tr><td>1</td><td>Bray Dunes</td><td>Le Quesnoy</td><td>150.6</td><td>4199</td></tr>
<tr><td>2</td><td>Le Quesnoy</td><td>Epernay</td><td>132.6</td><td>6304</td></tr>
</tbody>
</table>
0!8'''
    }

}
