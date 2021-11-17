import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import com.github.weisj.darklaf.theme.DarculaTheme
import java.awt.*
import java.io.File
import java.nio.charset.Charset
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileFilter
import kotlin.system.exitProcess


fun main() {
    val theme = LafManager.themeForPreferredStyle(getPreferredThemeStyle())
    LafManager.install(theme)

    val frame = JFrame("StarUML Watermark Remover").apply {
        size = Dimension(700, 500)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    val downloads = File("${System.getProperty("user.home")}/Downloads")
    val filePicker = JFileChooser(downloads).apply {
        addActionListener {
            selectedFile.removeWatermark()
            for (file in selectedFiles) {
                file.removeWatermark()
            }
            // Re-Trigger watermarkSvgFilter
            if (fileFilter == watermarkedSvgFilter) {
                removeChoosableFileFilter(watermarkedSvgFilter)
                addChoosableFileFilter(watermarkedSvgFilter)
                fileFilter = watermarkedSvgFilter
            }
        }
        approveButtonText = "Remove Watermark"

        addChoosableFileFilter(svgFilter)
        addChoosableFileFilter(watermarkedSvgFilter)

        fileFilter = watermarkedSvgFilter

        val buttonBox = ((components[3] as Container).components[3] as Container)
        // Removes the 'cancel' button
        ((buttonBox.components[1] as Container) as JButton).isVisible = false
        // Fixes layout
        buttonBox.layout = FlowLayout().apply {
            alignment = FlowLayout.RIGHT
        }
        ((buttonBox.components[0] as Container) as JButton).toolTipText = "Removes Watermark from selected file"
    }

    frame.add(filePicker)
    frame.isVisible = true
}