package simulator.ui;

import simulator.Simulator;

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
  SimulatorApplication s;

  SimulatorControlsToolbar(SimulatorApplication s) {
    super();
    // Moves all the Icons to the far left AND (important) adds sensible
    // margins between them.
    this.setLayout(new FlowLayout(FlowLayout.LEFT));

    JButton play = addControl(Icon.Name.PLAY,"Start the simulator");
    JButton step = addControl(Icon.Name.STEP_FORWARD, "Step forward the simulation one step");
    JButton reset = addControl(Icon.Name.RESET, "Reset the simulation to its initial state");

    step.addActionListener(e -> {
      s.executeNextInstruction();
    });
  }

  private JButton addControl(Icon.Name name, String tooltip) {
    Toolkit tk = Toolkit.getDefaultToolkit();

    JButton button = new JButton();
    button.setIcon(Icon.getIcon(tk, this.getClass(), name));
    button.setToolTipText(tooltip);
    button.setBorderPainted(false);
    button.setBorder(null); // Removes image drop-shadow
    button.setContentAreaFilled(false);

    add(button);
    return button;
  }
}
