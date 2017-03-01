/************************************************************************************
*
*   This file is part of triki
*
*   Written by Donald McIntosh (dbm@opentechnology.net) 
*
*   triki is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   triki is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with triki.  If not, see <http://www.gnu.org/licenses/>.
*
************************************************************************************/

package net.opentechnology.triki.logo;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.XMLOutputter
import org.jdom2.output.Format
import org.apache.commons.io.FileUtils

class LogoGenerator {

	private static final int STROKE_WIDTH = 6
	private static final int CIRCLE_RADIUS = 6
	private static final int CHAR_HEIGHT = 40
	private static final int LETTER_SPACE = 38

	public static void main(String[] args) {
		LogoGenerator gen = new LogoGenerator()
		gen.createIcons()
	}

	private void createIcons(){
		String outDir = "/tmp/images"
		def heightPixels = 20;
	   
		def sizes = [15, 20, 25, 40, 50, 100]
	   
		cleanOutput(outDir)
		sizes.each() { size ->
			createLogo(outDir, "trikiLogo", size)
			createPower(outDir, "trikiPower", size)
			createNewIcon(outDir, "new", size)
			createMinusIcon(outDir, "minus", size)
			createConfiguration(outDir, "configuration", size)
			createOutgoingIcon(outDir, "outTree", size)
			createIncomingIcon(outDir, "inTree", size)
			createInOutIcon(outDir, "inOutTree", size)
			createUploadArrow(outDir, "uploadArrow", size)
			createTextIcon(outDir, "text", size)
			createTemplateIcon(outDir, "template", size)
			createStylesheetIcon(outDir, "stylesheet", size)
			createButton(outDir, "edit", size, "Edit", 200)
			createButton(outDir, "save", size, "Save", 240)
			createButton(outDir, "view", size, "View", 240)
			createButton(outDir, "delete", size, "Delete", 280)
			createButton(outDir, "clone", size, "Clone", 240)
			createButton(outDir, "apply", size, "Apply", 240)
			createButton(outDir, "login", size, "Login", 240)
			createButton(outDir, "validate", size, "Validate", 320)
			createButton(outDir, "update", size, "Update", 280)
			createButton(outDir, "render", size, "Render", 280)
			createButton(outDir, "upload", size, "Upload", 280)
			createButton(outDir, "content", size, "Content", 320)
			createButton(outDir, "blank", size, "", 480)
		}			
	}
   
	def cleanOutput(String outDir) {
		def dir = new File(outDir)
		FileUtils.deleteDirectory(dir)
		if(!dir.exists()){
			dir.mkdir()
		}
	}
	
	private createButton(String outDir, String outfileRoot, def desiredHeight, def label, def width) {
		Document doc = new Document()
		def actualWidth = width
		def actualHeight = 100
		int baseX = 20
		int baseY = 20

		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"

		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)

		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)

		// Background
		addBackground(actualWidth, actualHeight, everything);

		// Border
		addBorder(actualWidth, actualHeight, everything)

		Element text = new Element("text", "http://www.w3.org/2000/svg");
		text.setAttribute("x", "40");
		text.setAttribute("y", "70");
		text.setAttribute("font-family", "Verdana");
		text.setAttribute("font-size", "60");
		text.setAttribute("fill", "#000000")
		text.setText(label)
		everything.addContent(text)

		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}

   
	private createNewIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 20
		int baseY = 20
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		buildRectangle(everything, baseX + 25, baseY + 5, STROKE_WIDTH, CHAR_HEIGHT + 10, CIRCLE_RADIUS, 0, "black");
		buildRectangle(everything, baseX, baseY + 30, STROKE_WIDTH, CHAR_HEIGHT + 10 , CIRCLE_RADIUS, -90, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
   
	private createMinusIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 20
		int baseY = 20
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		buildRectangle(everything, baseX, baseY + 30, STROKE_WIDTH, CHAR_HEIGHT + 10 , CIRCLE_RADIUS, -90, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
	
	private createConfiguration(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 40
		int baseY = 50
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		// Handle
		buildRectangle(everything, baseX, baseY, STROKE_WIDTH, (CHAR_HEIGHT/1.5).intValue(), CIRCLE_RADIUS, 45, "black");
		buildRectangle(everything, baseX, baseY, STROKE_WIDTH, (CHAR_HEIGHT/2).intValue(), CIRCLE_RADIUS, 160, "black");
		buildRectangle(everything, baseX, baseY, STROKE_WIDTH, (CHAR_HEIGHT/2).intValue(), CIRCLE_RADIUS, -70, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
   
	private createOutgoingIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 25
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5, CIRCLE_RADIUS, -55, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -4 , CIRCLE_RADIUS, -90, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5, CIRCLE_RADIUS, -125, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
   
	private createIncomingIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 70
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5, CIRCLE_RADIUS, 125, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -4, CIRCLE_RADIUS, 90, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5, CIRCLE_RADIUS, 55, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
   
	private createInOutIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 46
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		// Out
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +0, CIRCLE_RADIUS, -55, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -9, CIRCLE_RADIUS, -90, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +0, CIRCLE_RADIUS, -125, "black");
	   
		// In
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +0, CIRCLE_RADIUS, 125, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -9, CIRCLE_RADIUS, 90, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +0, CIRCLE_RADIUS, 55, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
	
	private createTextIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 25
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		// Line 1
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
	   
		// Line 2
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
	   
		// Line 3
		buildRectangle(everything, baseX, baseY + 70, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
	
   
	private createTemplateIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 25
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		// Line 1
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT -8 , CIRCLE_RADIUS, -90, "black");
	   
		// Line 2
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -15 , CIRCLE_RADIUS, -90, "black");
	   
		// Line 3
		buildRectangle(everything, baseX, baseY + 70, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "black");
		buildRectangle(everything, baseX, baseY + 70, STROKE_WIDTH, CHAR_HEIGHT -20 , CIRCLE_RADIUS, -90, "black");
	   
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
   
	private createStylesheetIcon(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 100
		def actualHeight = 100
		int baseX = 25
		int baseY = 5
   
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	   
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	   
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	   
		// Background
		addBackground(actualWidth, actualHeight, everything);
	   
		// Border
		addBorder(actualWidth, actualHeight, everything)
	   
		// Line 1
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "#103061");
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT -8 , CIRCLE_RADIUS, -90, "#103061");
	   
		// Line 2
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "#205081");
		buildRectangle(everything, baseX, baseY + 45, STROKE_WIDTH, CHAR_HEIGHT -15 , CIRCLE_RADIUS, -90, "#205081");
	   
		// Line 3
		buildRectangle(everything, baseX, baseY + 70, STROKE_WIDTH, CHAR_HEIGHT +5 , CIRCLE_RADIUS, -90, "#000000");
		buildRectangle(everything, baseX, baseY + 70, STROKE_WIDTH, CHAR_HEIGHT -20 , CIRCLE_RADIUS, -90, "#000000");
	   
	   
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}
	
	private createUploadArrow(String outDir, String outfileRoot, def desiredHeight) { 
		Document doc = new Document() 
		def actualWidth = 100 
		def actualHeight = 100 
		int baseX = 50 
		int baseY = 5
		
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
	
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")
		File outDirFile = new File(outDir)
		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)
	
		// Add scaler to everything
		addDefs(svgRoot)
		Element everything = addScaler(scale, svgRoot)
	
		// Background
		addBackground(actualWidth, actualHeight, everything);
	
		// Border
		addBorder(actualWidth, actualHeight, everything)
	
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT + 8, CIRCLE_RADIUS, 0, "black");
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT - 6, CIRCLE_RADIUS, 55, "black");
		buildRectangle(everything, baseX, baseY + 20, STROKE_WIDTH, CHAR_HEIGHT - 6, CIRCLE_RADIUS, -55, "black");
	
		// output DOM XML to console
		writeImages(doc, outDirFile, outfileRoot, suffix)
	}

	private createLogo(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 180
		def actualHeight = 100
		int baseX = 20
		int baseY = 26
		File outDirFile = new File(outDir)
   
		addLogo(desiredHeight, actualHeight, actualWidth, doc, baseX, baseY) { String suffix, Element everything ->
			// output DOM XML to console
			writeImages(doc, outDirFile, outfileRoot, suffix)
		}
	}
	
	private createPower(String outDir, String outfileRoot, def desiredHeight) {
		Document doc = new Document()
		def actualWidth = 470
		def actualHeight = 100
		int baseX = 310
		int baseY = 26
		File outDirFile = new File(outDir)
   
		addLogo(desiredHeight, actualHeight, actualWidth, doc, baseX, baseY) { String suffix, Element everything ->
			
			Element text = new Element("text", "http://www.w3.org/2000/svg");
			text.setAttribute("x", "20");
			text.setAttribute("y", "65");
			text.setAttribute("font-family", "Verdana");
			text.setAttribute("font-size", "45");
			text.setAttribute("fill", "#000000")
			text.setText("Powered by")
			everything.addContent(text)
			// output DOM XML to console
			writeImages(doc, outDirFile, outfileRoot, suffix)
		}
	}

	private addLogo(desiredHeight, int actualHeight, int actualWidth, Document doc, int baseX, int baseY, Closure action) {
		def scale = desiredHeight/actualHeight;
		def desiredWidth = (actualWidth * scale).intValue();
		def suffix = "_${desiredWidth}x${desiredHeight}"
		Element svgRoot = new Element("svg", "http://www.w3.org/2000/svg")

		svgRoot.setAttribute("width", "${desiredWidth}");
		svgRoot.setAttribute("height", "${desiredHeight}");
		doc.addContent(svgRoot)

		Element everything = addScaler(scale, svgRoot)

		// Background
		addDefs(svgRoot)
		addBackground(actualWidth, actualHeight, everything);

		//border
		addBorder(actualWidth, actualHeight, everything)

		//t
		buildRectangle(everything, baseX, baseY - 5, STROKE_WIDTH, CHAR_HEIGHT + 15, CIRCLE_RADIUS, 0, "black");
		buildRectangle(everything, baseX, baseY + 10, STROKE_WIDTH, CHAR_HEIGHT - 20 , CIRCLE_RADIUS, -90, "black");

		//r
		buildRectangle(everything, baseX + LETTER_SPACE, baseY + 10, STROKE_WIDTH, CHAR_HEIGHT, CIRCLE_RADIUS, 0, "black");
		buildRectangle(everything, baseX + LETTER_SPACE, baseY + 30, STROKE_WIDTH, CHAR_HEIGHT - 12 , CIRCLE_RADIUS, -135, "black");

		//i
		buildRectangle(everything, baseX + LETTER_SPACE * 2, baseY + 10, STROKE_WIDTH, CHAR_HEIGHT, CIRCLE_RADIUS, 0, "black");

		//k
		buildRectangle(everything, (baseX + LETTER_SPACE * 2.5).intValue(), baseY - 10, STROKE_WIDTH, CHAR_HEIGHT + 20, CIRCLE_RADIUS, 0, "black");
		buildRectangle(everything, (baseX + LETTER_SPACE * 2.5).intValue(), baseY + 30, STROKE_WIDTH, CHAR_HEIGHT - 12 , CIRCLE_RADIUS, -135, "#205081");
		buildRectangle(everything, (baseX + LETTER_SPACE * 2.5).intValue(), baseY + 30, STROKE_WIDTH, CHAR_HEIGHT - 12 , CIRCLE_RADIUS, -45, "#205081");

		//i
		buildRectangle(everything, (baseX + LETTER_SPACE * 3.5).intValue(), baseY + 10, STROKE_WIDTH, CHAR_HEIGHT, CIRCLE_RADIUS, 0, "black");
		
		action(suffix, everything)
	}

	private Element addScaler(float scale, Element svgRoot) {
		Element everything = new Element("g", "http://www.w3.org/2000/svg");
		everything.setAttribute("transform", "scale(${scale}, ${scale})");
		svgRoot.addContent(everything)
		return everything
	}

	private addBorder(int actualWidth, int actualHeight, Element everything) {
		def borderWidth = STROKE_WIDTH
		Element border = new Element("rect", "http://www.w3.org/2000/svg");
		border.setAttribute("x", "${borderWidth / 2}");
		border.setAttribute("y", "${borderWidth / 2}");
		border.setAttribute("width", "${actualWidth - borderWidth}");
		border.setAttribute("height", "${actualHeight - borderWidth}");
		border.setAttribute("fill", "url(#ButtonGradient)")
		border.setAttribute("rx", "2")
		border.setAttribute("ry", "2")
		everything.addContent(border)
	}

	private addBackground(int actualWidth, int actualHeight, Element everything) {
		Element background = new Element("rect", "http://www.w3.org/2000/svg");
		background.setAttribute("x", "0");
		background.setAttribute("y", "0");
		background.setAttribute("width", "${actualWidth}");
		background.setAttribute("height", "${actualHeight}");
		background.setAttribute("fill", "#6090B1")
		background.setAttribute("rx", "0")
		background.setAttribute("ry", "0")
		everything.addContent(background)
	}

	private writeImages(Document doc, File outDirFile, String outfileRoot, String suffix) {
		XMLOutputter outter=new XMLOutputter();
		outter.setFormat(Format.getPrettyFormat());
		outter.output(doc, new FileWriter(new File(outDirFile, outfileRoot + "${suffix}.svg")));
		createImageFile(outDirFile, outfileRoot + "${suffix}")
	}
   
	def createImageFile(File outDirFile, def filename)
	{
		PNGTranscoder t = new PNGTranscoder();
		def infile = new FileInputStream(new File(outDirFile, filename + ".svg"))
		TranscoderInput input = new TranscoderInput(infile);
		def outfile = new File(outDirFile, filename + ".png")
		OutputStream ostream = new FileOutputStream(outfile);
		TranscoderOutput output = new TranscoderOutput(ostream);
		t.transcode(input, output);
		System.out.println("Generated " + outfile.getCanonicalFile())
	   
		ostream.flush();
		ostream.close();
	}

	def buildRectangle(Element svg, int x, int y, int width, int height, int radius, int angle, String colour) {
		int xRotateCentre = x + (width/2)
		int yRotateCentre = y
		String transform = "rotate(${angle} ${xRotateCentre} ${yRotateCentre})"
		Element g = new Element("g", "http://www.w3.org/2000/svg");
		Element t = new Element("rect", "http://www.w3.org/2000/svg");
		t.setAttribute("x", x.toString());
		t.setAttribute("y", y.toString());
		t.setAttribute("width", width.toString());
		t.setAttribute("height", height.toString());
		t.setAttribute("fill", colour)
	   
		Element cStart = buildCircle(svg, (x + width/2).intValue(), (y).intValue(), radius, colour)
		Element cEnd = buildCircle(svg, (x + width/2).intValue(), (y + height).intValue(), radius, colour)
		g.addContent(t)
		g.addContent(cStart)
		g.addContent(cEnd)
		g.setAttribute("transform", transform);
	   
		svg.addContent(g)
	}
   
	Element buildCircle(Element svg, int x, int y, int radius, String colour) {
		Element curcle = new Element("circle", "http://www.w3.org/2000/svg");
		curcle.setAttribute("cx", x.toString());
		curcle.setAttribute("cy", y.toString());
		curcle.setAttribute("r", radius.toString());
		curcle.setAttribute("fill", colour)
		return curcle;
	}

	private addDefs(Element everything) {
		Element defs = new Element("defs", "http://www.w3.org/2000/svg");
		Element startOffset = new Element("stop", "http://www.w3.org/2000/svg");
		startOffset.setAttribute("offset", "0%");
		startOffset.setAttribute("stop-color", "white");
		Element stopOffset = new Element("stop", "http://www.w3.org/2000/svg");
		stopOffset.setAttribute("offset", "100%");
		stopOffset.setAttribute("stop-color", "#80C0E1");
		Element buttonGradient = new Element("linearGradient", "http://www.w3.org/2000/svg");
		buttonGradient.setAttribute("id", "ButtonGradient");
		buttonGradient.setAttribute("x1", "0");
		buttonGradient.setAttribute("y1", "0");
		buttonGradient.setAttribute("x2", "0");
		buttonGradient.setAttribute("y2", "100%");
		buttonGradient.addContent(startOffset)
		buttonGradient.addContent(stopOffset)
		defs.addContent(buttonGradient);

		everything.addContent(defs)
	}
}

