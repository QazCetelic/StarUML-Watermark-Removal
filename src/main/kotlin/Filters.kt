import java.io.File
import java.nio.charset.Charset
import javax.swing.filechooser.FileFilter

val svgFilter = object: FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension.equals("SVG", ignoreCase = true)
    override fun getDescription(): String = "SVG"
}
val watermarkedSvgFilter = object: FileFilter() {
    override fun accept(f: File): Boolean {
        if (f.isDirectory) return true
        if (!f.extension.equals("SVG", ignoreCase = true)) return false

        // Reads a certain region of bytes in a file and checks if it matches the regex
        val skippedBytes = 128
        val readBytes = 256
        val totalBytes = skippedBytes + readBytes
        if (f.length() < totalBytes) return false
        val bReader = f.bufferedReader(Charset.defaultCharset(), totalBytes)
        bReader.skip(skippedBytes.toLong())
        val charBuffer = buildString {
            for (i in 0..readBytes) {
                append(bReader.read().toChar())
            }
        }
        return unregisteredTextRegex in charBuffer
    }
    override fun getDescription(): String = "Watermarked SVG"
}