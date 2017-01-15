package simulator.ui;

import lombok.extern.java.Log;
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
@Log
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
    JButton stop = addControl(Icon.Name.STOP, "Stops the simulation");


    step.addActionListener(e -> {
      log.info("Executing the next instruction");
      s.executeNextInstruction();
    });
    play.addActionListener(e -> {
      log.info("Running the simulation");
      s.run();
    });
    reset.addActionListener(e -> {
      log.info("Resetting the simulation");
      //s.stop();
      s.reset();
    });
    stop.addActionListener(e -> {
      log.info("Stopping the simulation");
      s.stop();
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
