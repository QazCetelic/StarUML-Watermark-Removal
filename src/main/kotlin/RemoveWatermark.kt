import java.io.File

val unregisteredTextRegex = Regex("<text([^>])+>UNREGISTERED</text>")
fun removeWatermarkFromSVG(svgString: String): String {
    return svgString.replace(unregisteredTextRegex, "")
}

fun File.removeWatermark() {
    val svgString = readText()
    writeText(removeWatermarkFromSVG(svgString))
}