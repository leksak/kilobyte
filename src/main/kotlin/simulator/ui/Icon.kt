package simulator.ui

import java.awt.Toolkit
import javax.swing.ImageIcon

object Icon {
  private const val DIRECTORY_PATH = "/icons/iconic/raster"
  private const val PLAY_PATH = "$DIRECTORY_PATH/green/play_18x24.png"

  private var playIcon : ImageIcon? = null

  @JvmStatic fun <T> play(tk: Toolkit, clazz: Class<T>) : ImageIcon {
    if (playIcon == null) {
      playIcon = ImageIcon(tk.getImage(clazz.getResource(PLAY_PATH)))
    }
    return playIcon!!
  }
}


