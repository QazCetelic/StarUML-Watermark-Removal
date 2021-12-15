import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import java.awt.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame


fun main() {
    val theme = LafManager.themeForPreferredStyle(getPreferredThemeStyle())
    LafManager.install(theme)

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

        /*
            FILE FILTERS
         */
        // Adds filters
        addChoosableFileFilter(svgFilter)
        addChoosableFileFilter(watermarkedSvgFilter)
        // Sets default file filter
        fileFilter = watermarkedSvgFilter


        /*
            ADJUST UI ELEMENTS
         */

        val buttonBox = ((components[3] as Container).components[3] as Container)
        val cancelButton = ((buttonBox.components[1] as Container) as JButton)
        val removeWatermarkButton = ((buttonBox.components[0] as Container) as JButton)

        approveButtonText                   = "Remove Watermark"
        removeWatermarkButton.toolTipText   = "Removes Watermark from selected file"

        // Removes the 'cancel' button…
        cancelButton.isVisible = false
        // …and then fixes the layout
        buttonBox.layout = FlowLayout().apply { alignment = FlowLayout.RIGHT }
    }

    JFrame("StarUML Watermark Remover").apply {
        size = Dimension(700, 500)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        add(filePicker)
        isVisible = true

        val classLoader: ClassLoader = {}::class.java.classLoader
        val resource = classLoader.getResourceAsStream("icon.png")
        iconImage = ImageIO.read(resource)
    }
}