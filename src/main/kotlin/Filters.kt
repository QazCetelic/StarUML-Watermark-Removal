import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import javax.swing.filechooser.FileFilter

val svgFilter = object: FileFilter() {
    override fun accept(file: File): Boolean {
        // Uses this instead of matching with file extensions because the extension is not always present
        return file.isDirectory || Files.probeContentType(file.toPath()) == "image/svg+xml"
    }
    override fun getDescription() = "SVG"
}

val pdfFilter = object: FileFilter() {
    override fun accept(file: File): Boolean {
        // Uses this instead of matching with file extensions because the extension is not always present
        return file.isDirectory || Files.probeContentType(file.toPath()) == "application/pdf"
    }
    override fun getDescription() = "PDF"
}


val watermarkedSvgFilter = object: FileFilter() {
    override fun accept(file: File): Boolean {
        if (file.isDirectory) return true
        // Uses this instead of matching with file extensions because the extension is not always present
        if (Files.probeContentType(file.toPath()) != "image/svg+xml") return false

        val stream = FileInputStream(file)
        if (128 > stream.available()) return false
        stream.skip(128)
        val charBuffer = buildString {
            // Reads 256 bytes or fewer depending on file size
            for (i in 0..(minOf(256, stream.available()))) {
                val b = stream.read().toByte()
                append(b.toChar())
            }
        }
        stream.close()

        return unregisteredTextRegex in charBuffer
    }
    override fun getDescription() = "Watermarked SVG"
}