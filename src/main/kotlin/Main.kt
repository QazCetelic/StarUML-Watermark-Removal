import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*


fun main() {
    val systemTheme = LafManager.themeForPreferredStyle(getPreferredThemeStyle())
    LafManager.install(systemTheme)

    System.setProperty("awt.useSystemAAFontSettings","on");
    System.setProperty("swing.aatext", "true");

    val downloads = File("${System.getProperty("user.home")}/Downloads")
    val filePicker = JFileChooser(downloads).apply {
        val convertCheckBox = JCheckBox()

        addActionListener {
            selectedFile.withoutWatermark(convertCheckBox.isSelected)
            for (file in selectedFiles) {
                file.withoutWatermark(convertCheckBox.isSelected)
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
        addChoosableFileFilter(pdfFilter)
        // Sets default file filter
        fileFilter = watermarkedSvgFilter


        /*
            ADJUST UI ELEMENTS
         */

        val buttonBox = ((components[3] as Container).components[3] as Container)
        val cancelButton = ((buttonBox.components[1] as Container) as JButton)
        val openFileButton = ((buttonBox.components[0] as Container) as JButton)

        // Removes the 'cancel' button…
        cancelButton.isVisible = false

        openFileButton.addPropertyChangeListener {
            if (it.propertyName == "text") {
                openFileButton.text = "Remove Watermark"
            }
        }
        openFileButton.text = "Remove Watermark"
        openFileButton.toolTipText = ""

        buttonBox.apply {
            add(JLabel("Convert to PNG"), 0)
            add(convertCheckBox, 0)
        }

        // …and then fixes the layout
        buttonBox.layout = FlowLayout().apply { alignment = FlowLayout.RIGHT }
    }

    JFrame("StarUML Watermark Remover").apply {
        size = Dimension(700, 500)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        add(filePicker)
        isVisible = true

        val iconStream = ClassLoader.getSystemClassLoader().getResourceAsStream("icon.png")
        iconImage = ImageIO.read(iconStream)
    }
}