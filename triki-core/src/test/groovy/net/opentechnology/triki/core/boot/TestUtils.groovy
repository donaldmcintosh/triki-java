package net.opentechnology.triki.core.boot

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileUtils

class TestUtils {

    Model loadModel() throws FileNotFoundException {
        Model model = ModelFactory.createDefaultModel();
        InputStream instream = FileUtils.openResourceFileAsStream("test.ttl")
        model.read(instream, null, "TTL");

        model
    }
}
