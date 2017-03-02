package kilobyte.simulator.ui;

import kilobyte.common.annotations.CallOnEDT;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;

import static kilobyte.simulator.ui.SimulatorControlsToolbar.ToolbarDisplayState.*;

import kilobyte.simulator.ui.SimulatorControlsToolbar.ToolbarDisplayState.Action;

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
  static JButton play, step, reset, stop;
  
  static {
    play = createButton(Icon.PLAY,"Start the simulator");
    step = createButton(Icon.STEP_FORWARD, "Step forward the simulation one step");
    reset = createButton(Icon.RESET, "Reset the simulation to its initial state");
    stop = createButton(Icon.STOP, "Stops the simulation");
  }
  
  static JButton createButton(Icon icon, String tooltip) {
      JButton button = new JButton();
      button.setIcon(icon.getImageIcon());
      button.setToolTipText(tooltip);
      button.setBorderPainted(false);
      button.setBorder(null); // Removes image drop-shadow
      button.setContentAreaFilled(false);
      return button;
  }
  
  Thread backgroundThread = null;

  // Dictates what icons should/shouldn't be active in the given state
  public enum ToolbarDisplayState {
    INITIAL(play, false, step, false, reset, false, stop, false),
    RESET(play, true, step, true, reset, false, stop, false),
    RUNNING(play, false, step, false, reset, false, stop, true),
    STOPPED(play, true, step, true, reset, true, stop, false),
    STEP(play, true, step, true, reset, true, stop, false),
    FINISHED(play, false, step, false, reset, true, stop, false);

    @FunctionalInterface
    interface Action {
      void invoke();
    }

    Action callOnStateChange;

    ToolbarDisplayState(JButton play,
                        boolean b,
                        JButton step,
                        boolean b1,
                        JButton reset,
                        boolean b2,
                        JButton stop,
                        boolean b3) {
      callOnStateChange = () -> {
        play.setEnabled(b);
        step.setEnabled(b1);
        reset.setEnabled(b2);
        stop.setEnabled(b3);
      };
    }
  }

  SimulatorControlsToolbar(SimulatorApplication s) {
    super();
    // Moves all the Icons to the far left AND (important) adds sensible
    // margins between them.
    this.setLayout(new FlowLayout(FlowLayout.LEFT));
    transitionToDisplayState(ToolbarDisplayState.INITIAL);
    add(play);
    add(step);
    add(reset);
    add(stop);

    play.addActionListener(e -> {
      log.info("Running the simulation");
      transitionToDisplayState(RUNNING);
      changeCurrentBackgroundThread(new Thread(s::run));
    });

    reset.addActionListener(e -> {
      log.info("Resetting the simulation");
      transitionToDisplayState(RESET);
      changeCurrentBackgroundThread(new Thread(s::reloadProgram));
    });

    step.addActionListener(e -> {
      log.info("Executing the next instruction");
      transitionToDisplayState(STEP);
      changeCurrentBackgroundThread(new Thread(s::executeNextInstruction));
    });
    stop.addActionListener(e -> {
      log.info("Stopping the simulation");
      transitionToDisplayState(STOPPED);
      changeCurrentBackgroundThread(new Thread(s::stop));
    });
  }

  @CallOnEDT
  public void transitionToDisplayState(ToolbarDisplayState tbs) {
    tbs.callOnStateChange.invoke();
  }

  private synchronized void changeCurrentBackgroundThread(Thread thread) {
    if (backgroundThread != null) {
      backgroundThread.interrupt();
    }
    backgroundThread = thread;
    thread.start();
  }
}
