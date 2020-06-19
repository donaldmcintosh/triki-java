package net.opentechnology.triki.core.renderer

import com.vladsch.flexmark.ast.Document
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.profiles.pegdown.Extensions
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter
import com.vladsch.flexmark.util.options.DataHolder
import com.vladsch.flexmark.util.options.MutableDataHolder
import com.vladsch.flexmark.util.options.MutableDataSet

class MarkdownRenderer {

    static final MutableDataHolder OPTIONS = new MutableDataSet()
            .set(HtmlRenderer.HARD_BREAK, "<br>\n")
            .set(Parser.EXTENSIONS, Arrays.asList(MediaTagsExtension.create(), TablesExtension.create()));

    static final DataHolder pegdownOptions = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL
    );

    Parser parser
    HtmlRenderer renderer

    public MarkdownRenderer(){
        parser = Parser.builder(OPTIONS).build();
        renderer = HtmlRenderer.builder(OPTIONS).build();
    }

    String render(String markdown){
        Document document = parser.parse(markdown);
        String html = renderer.render(document)
        // Need to do this due to https://github.com/vsch/flexmark-java/issues/166
        def nopara =  html.replaceAll(/(?s)^<p>(.+)<\/p>\n$/, '$1\n')
        return nopara;
    }
}
