import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import javax.swing.filechooser.FileFilter

val svgFilter = object: FileFilter() {
    override fun accept(file: File): Boolean = file.isDirectory || file.extension.equals("SVG", ignoreCase = true)
    override fun getDescription() = "SVG"
}

val watermarkedSvgFilter = object: FileFilter() {
    override fun accept(file: File): Boolean {
        if (file.isDirectory) return true
        if (!file.extension.equals("SVG", ignoreCase = true)) return false

        val stream = FileInputStream(file)
        if (128 > stream.available()) return false
        stream.skip(128)
        val charBuffer = buildString {
            for (i in 0..(minOf(256, stream.available()))) {
                val b = stream.read().toByte()
                append(b.toChar())
            }
        }

        return unregisteredTextRegex in charBuffer
    }
    override fun getDescription() = "Watermarked SVG"
}