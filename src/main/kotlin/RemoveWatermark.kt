import org.apache.batik.dom.svg.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.svg2svg.SVGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.apache.pdfbox.contentstream.operator.Operator
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdfparser.PDFStreamParser
import org.apache.pdfbox.pdfwriter.ContentStreamWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDStream
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

val unregisteredTextRegex = Regex("<text([^>])+>UNREGISTERED</text>")

fun File.withoutWatermark(convertToImage: Boolean) {

    when (Files.probeContentType(toPath())) {
        "image/svg+xml" -> {
            // TODO use less hacky method to remove watermark
            stupidWatermarkRemoval()

            val svg = toSVGDocument().withoutWatermark()
            writeSvg(svg)
            if (convertToImage) {
                val bitmapFile = File(parentFile, "$nameWithoutExtension.png")
                bitmapFile.writeSvgAsBitmap(svg)
            }
        }
        "application/pdf" -> {
            val pdf = toPDFDocument().withoutWatermark()
            writePdf(pdf)
        }
    }

}

fun File.stupidWatermarkRemoval() {
    writeText(readText().replace(unregisteredTextRegex, ""))
}

fun PDDocument.withoutWatermark(): PDDocument {
    val searchString = "UNREGISTERED"
    val replacement = ""

    for (page: PDPage in getPages()){
        val parser = PDFStreamParser(page)
        parser.parse()
        val tokens = parser.tokens

        for (j in 0 until tokens.size) {
            val next = tokens[j]
            if(next !is Operator) {
                continue
            }

            val op = next

            var pstring = ""
            var prej = 0

            //Tj and TJ are the two operators that display strings in a PDF
            if (op.name == "Tj") {
                // Tj takes one operator and that is the string to display so lets update that operator
                val previous = tokens[j - 1] as COSString
                var string = previous.getString()
                string = string.replaceFirst(searchString.toRegex(), replacement)
                previous.setValue(string.toByteArray())
            } else if (op.name == "TJ") {
                val previous = tokens[j - 1] as COSArray
                for (k in 0 until previous.size()) {
                    val arrElement: Any = previous.getObject(k)
                    if (arrElement is COSString) {
                        val string = arrElement.getString()
                        if (j === prej) {
                            pstring += string
                        } else {
                            prej = j
                            pstring = string
                        }
                    }
                }
                if (searchString.equals(pstring.trim { it <= ' ' })) {
                    val cosString2 = previous.getObject(0) as COSString
                    cosString2.setValue(replacement.toByteArray())
                    val total = previous.size() - 1
                    for (k in total downTo 1) {
                        previous.remove(k)
                    }
                }
            }

        }
        // Now that the tokens are updated, replace the page content stream.
        val updatedStream = PDStream(document)
        val out = updatedStream.createOutputStream(COSName.FLATE_DECODE)
        val tokenWriter = ContentStreamWriter(out)
        tokenWriter.writeTokens(tokens)
        out.close()
        page.setContents(updatedStream)
    }

    return this
}

fun Document.withoutWatermark(): Document {
    val textElements: NodeList = getElementsByTagName("text")
    for (element in textElements.iterator()) {
        if ("UNREGISTERED" in element.firstChild.nodeValue) {
            element.parentNode.removeChild(element)
        }
        else {
            // Reduces the size of the font because it's too big when for some reason
            val fontSizeNode = element.attributes.getNamedItem("font-size")
            val fontSize = fontSizeNode.nodeValue.removeSuffix("px").toIntOrNull() ?: 0
            // Reduce the font size by 15%
            val newFontSize = (fontSize * 0.85).toInt()
            fontSizeNode.nodeValue = newFontSize.toString() + "px"
        }
    }

    return this
}

fun File.toSVGDocument(): Document {
    return SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createSVGDocument(toURI().toString())
}

fun File.toPDFDocument(): PDDocument {
    return PDDocument.load(this)
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

fun File.writePdf(pdf: PDDocument) {
    val outputPdf = File(path)
    pdf.save(outputPdf)
}

fun File.writeSvgAsBitmap(svg: Document) {
    try {
        if (!exists()) createNewFile()

        val inputTranscodeStream    = TranscoderInput(svg)
        val outputStream            = outputStream()
        val outputTranscodeStream   = TranscoderOutput(outputStream)

        // Create transcoder with transparant background
        val transcoder = PNGTranscoder()
        transcoder.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true)

        // Save the image.
        transcoder.transcode(inputTranscodeStream, outputTranscodeStream)
    } catch (ex: IOException) {
        println("Failed to read/write to files: $ex")
    } catch (ex: TranscoderException) {
        println("Failed to convert SVG to PNG: $ex")
    }
}