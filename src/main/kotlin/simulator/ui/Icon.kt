package simulator.ui

import java.awt.Toolkit
import javax.swing.ImageIcon

object Icon {
  enum class Name(val path: String) {
    PLAY("/icons/iconic/raster/green/play_9x12.png"),
    STOP("/icons/iconic/raster/red/stop_12x12.png"),
    RESET("/icons/iconic/raster/gray_dark/reload_9x11.png"),
    STEP_FORWARD("/icons/bitcons/png/gray/16x16/forward.png"),
    INSTRUCTION_POINTER("/icons/dr.png"),
  }

  private val iconMap : MutableMap<Name, ImageIcon> = mutableMapOf()

  @JvmStatic fun <T> getIcon(tk: Toolkit, clazz: Class<T>, name: Name) : ImageIcon {
    if (iconMap.containsKey(name)) {
      return iconMap[name]!!
    }
    iconMap.put(name, ImageIcon(tk.getImage(clazz.getResource(name.path))))
    return iconMap[name]!!
  }
}


