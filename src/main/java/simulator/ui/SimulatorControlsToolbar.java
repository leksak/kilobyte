package simulator.ui;

import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;

import static simulator.ui.SimulatorControlsToolbar.ToolbarState.*;

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
  JButton play, step, reset, stop;
  ToolbarState tbs;
  Thread backgroundThread = null;


  public enum ToolbarState{
    INIT, RESET, RUN, STOP, STEP;
  }

  SimulatorControlsToolbar(SimulatorApplication s) {
    super();
    // Moves all the Icons to the far left AND (important) adds sensible
    // margins between them.
    this.setLayout(new FlowLayout(FlowLayout.LEFT));

    play = addControl(Icon.Name.PLAY,"Start the simulator");
    step = addControl(Icon.Name.STEP_FORWARD, "Step forward the simulation one step");
    reset = addControl(Icon.Name.RESET, "Reset the simulation to its initial state");
    stop = addControl(Icon.Name.STOP, "Stops the simulation");


    play.addActionListener(e -> {
      log.info("Running the simulation");
      stateSwitcher(RUN);
      changeCurrentBackgroundThread(new Thread(() -> s.run()));
    });

    reset.addActionListener(e -> {
      log.info("Resetting the simulation");
      stateSwitcher(RESET);
      changeCurrentBackgroundThread(new Thread(() -> s.reset()));
    });

    step.addActionListener(e -> {
      log.info("Executing the next instruction");
      stateSwitcher(STEP);
      changeCurrentBackgroundThread(new Thread(() -> s.executeNextInstruction()));
    });
    stop.addActionListener(e -> {
      log.info("Stopping the simulation");
      stateSwitcher(STOP);
      changeCurrentBackgroundThread(new Thread(() -> s.stop()));
    });
    stateSwitcher(ToolbarState.INIT);

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

  public void stateSwitcher(ToolbarState tbs) {
    switch(tbs){
      case INIT:
        play.setEnabled(false);
        stop.setEnabled(false);
        step.setEnabled(false);
        reset.setEnabled(false);
        break;
      case RUN:
        play.setEnabled(false);
        stop.setEnabled(true);
        play.setEnabled(false);
        step.setEnabled(false);
        reset.setEnabled(false);
        break;
      case STOP:
        play.setEnabled(false);
        stop.setEnabled(false);
        step.setEnabled(false);
        reset.setEnabled(true);
        break;
      case RESET:
        play.setEnabled(true);
        stop.setEnabled(false);
        step.setEnabled(true);
        reset.setEnabled(false);
      break;
      case STEP:
        play.setEnabled(true);
        stop.setEnabled(false);
        step.setEnabled(true);
        reset.setEnabled(true);
      break;
    }
  }


  private synchronized void changeCurrentBackgroundThread(Thread thread) {
    if (backgroundThread != null) {
      backgroundThread.interrupt();
    }
    backgroundThread = thread;
    thread.start();
  }
}
