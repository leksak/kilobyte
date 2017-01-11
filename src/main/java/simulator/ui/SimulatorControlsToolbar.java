package simulator.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Presents all the required control operations required by the
 * assignment specification,
 *
 * The interface must afford the user (at least) the following
 * operations:
 * <ul>
 *   <li>Step: Execute the next instruction and then wait.</li>
 *   <li>Run: Run the program until it ends.</li>
 *   <li>Reset: Reset to the initial state when the program file
 *              was loaded. This should be possible even when the
 *              program is in (a possibly unending loop).</li>
 * </ul>
 */
public class SimulatorControlsToolbar extends JToolBar {
  SimulatorControlsToolbar() {
    super();
    // Moves all the Icons to the far left AND (important) adds sensible
    // margins between them.
    this.setLayout(new FlowLayout(FlowLayout.LEFT));

    addControl(Icon.Name.PLAY,"Start the simulator");
    addControl(Icon.Name.STEP_FORWARD, "Step forward the simulation one step");
    addControl(Icon.Name.RESET, "Reset the simulation to its initial state");
  }

  private void addControl(Icon.Name name, String tooltip) {
    Toolkit tk = Toolkit.getDefaultToolkit();

    JButton button = new JButton();
    button.setIcon(Icon.getIcon(tk, this.getClass(), name));
    button.setToolTipText(tooltip);
    button.setBorderPainted(false);
    button.setBorder(null); // Removes image drop-shadow
    button.setContentAreaFilled(false);

    add(button);
  }
}
