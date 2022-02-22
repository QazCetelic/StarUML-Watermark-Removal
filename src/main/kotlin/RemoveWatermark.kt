import org.apache.batik.dom.svg.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.svg2svg.SVGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

val unregisteredTextRegex = Regex("<text([^>])+>UNREGISTERED</text>")

fun File.removeWatermark(convertToImage: Boolean) {
    // TODO use less hacky method to remove watermark
    stupidWatermarkRemoval()
    val svg = toSVGDocument().removeWatermark()
    writeSvg(svg)
    if (convertToImage) {
        val bitmapFile = File(parentFile, "$nameWithoutExtension.png")
        bitmapFile.writeSvgAsBitmap(svg)
    }
}

fun File.stupidWatermarkRemoval() {
    writeText(readText().replace(unregisteredTextRegex, ""))
}

fun Document.removeWatermark(): Document {
    val textElements: NodeList = getElementsByTagName("text")
    for (element in textElements.iterator()) {
        if ("UNREGISTERED" in element.firstChild.nodeValue) {
            element.parentNode.removeChild(element)
        }
        else {
            // Reduces the size of the font because it's too big when for some reason
            val fontSizeNode = element.attributes.getNamedItem("font-size")
            val fontSize = fontSizeNode.nodeValue.removeSuffix("px").toIntOrNull() ?: 0
            // Reduce the font size by 8%
            val newFontSize = (fontSize * 0.92).toInt()
            fontSizeNode.nodeValue = newFontSize.toString() + "px"
        }
    }

    return this
}

fun File.toSVGDocument(): Document {
    return SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createSVGDocument(toURI().toString())
}

fun File.writeSvg(svg: Document) {
    val outputSvg = File(path)
    if (!outputSvg.exists()) outputSvg.createNewFile()
    FileOutputStream(outputSvg).use { os ->
        val writer = OutputStreamWriter(os, StandardCharsets.UTF_8)
        val output = TranscoderOutput(writer)
        SVGTranscoder().transcode(TranscoderInput(svg), output)
    }
}

fun File.writeSvgAsBitmap(svg: Document) {
    try {
        if (!exists()) createNewFile()

        val inputTranscodeStream    = TranscoderInput(svg)
        val outputStream            = outputStream()
        val outputTranscodeStream   = TranscoderOutput(outputStream)

        // Save the image.
        PNGTranscoder().transcode(inputTranscodeStream, outputTranscodeStream)
    } catch (ex: IOException) {
        println("Failed to read/write to files: $ex")
    } catch (ex: TranscoderException) {
        println("Failed to convert SVG to PNG: $ex")
    }
}