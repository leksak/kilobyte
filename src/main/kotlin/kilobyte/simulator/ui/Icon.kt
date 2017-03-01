package kilobyte.simulator.ui

import java.awt.Toolkit
import javax.swing.ImageIcon

enum class Icon(path : String) {
  PLAY("/icons/iconic/raster/green/play_9x12.png"),
  STOP("/icons/iconic/raster/red/stop_12x12.png"),
  RESET("/icons/iconic/raster/gray_dark/reload_9x11.png"),
  STEP_FORWARD("/icons/bitcons/png/gray/16x16/forward.png"),
  INSTRUCTION_POINTER("/icons/dr.png");

  val imageIcon: ImageIcon

  init {
    val tk = Toolkit.getDefaultToolkit()
    imageIcon = ImageIcon(tk.getImage(this.javaClass.getResource(path)))
  }
}


