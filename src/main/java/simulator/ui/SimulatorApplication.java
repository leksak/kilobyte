package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.Value;
import simulator.Observable;
import simulator.Observer;
import simulator.Simulator;
import simulator.program.Program;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import static java.awt.event.WindowEvent.WINDOW_CLOSING;

/**
 * The simulator has to be capable of displaying its operation on
 * a per-instruction basis. The interface may be either graphical
 * (which this GUI satisfies) or textual (ignored), and has to
 * meet the following requirements:
 *
 * <ol>
 *    <li>
 *      Each instruction of the program, with a pointer or
 *      highlighting indicating the instruction that is currently
 *      being executed.
 *    </li>
 *    <li>
 *      The numerical constituent fields of each instruction.
 *    </li>
 *    <li>
 *      The current value of each register which has been changed
 *      during the execution of the program has to be shown.
 *    </li>
 *    <li>
 *      The current value of the program counter (PC) has to be
 *      shown.
 *    </li>
 *    <li>
 *      The current value of each memory location which has been
 *      changed during the execution of the program has to be shown.
 *    </li>
 * </ol>
 *
 * There must be a choice of whether values are displayed in
 * decimal or hexadecimal. Minimally, this may be implemented
 * as an option at startup time, but it would be preferable to
 * allow the form of display to be changed during the execution
 * of the simulator.
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
@InstantiateOnEDT // Important!
@Value
public class SimulatorApplication implements Observer<RegisterMenu> {
  Simulator s = new Simulator();
  JFrame applicationFrame = new JFrame("Kilobyte");
  ProgramView programView = new ProgramView();
  FileMenu fileMenu = FileMenu.withCloseAction(applicationFrame,
        // Clicking on exit in the file-menu closes the application
        () -> dispatchEvent(WINDOW_CLOSING),
        this::loadProgram
  );
  RegisterMenu registerMenu = new RegisterMenu();
  RegistersPanel registersPanel = new RegistersPanel(s.getRegisterFile());

  SimulatorApplication() {
    applicationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    SimulatorMenuBar menuBar = new SimulatorMenuBar(fileMenu, registerMenu);
    applicationFrame.setJMenuBar(menuBar);

    JPanel applicationPanel = new JPanel();
    JPanel pcAndRegistersPanel = new JPanel();
    pcAndRegistersPanel.setLayout(new BoxLayout(pcAndRegistersPanel, BoxLayout.PAGE_AXIS));

    pcAndRegistersPanel.add(registersPanel);

    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          pcAndRegistersPanel,
          programView);
    applicationPanel.add(splitPane, BorderLayout.WEST);
    applicationFrame.add(applicationPanel);
    //applicationFrame.setMinimumSize(applicationFrame.getSize());

    applicationFrame.pack();

    /* Center the GUI on the screen, has to be called after pack() */
    applicationFrame.setLocationRelativeTo(null);
  }

  private void dispatchEvent(int i) {
    applicationFrame.dispatchEvent(new WindowEvent(applicationFrame, i));
  }

  private void setVisible() {
    applicationFrame.setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      // The application itself is conglomeration of Swing components
      // hence it has to be instantiated on the EDT
      SimulatorApplication app = new SimulatorApplication();

      // Contains Run/Pause/Step/Reset
      //JMenu simulatorMenu = new JMenu("Simulator");


      app.setVisible();
    });
  }

  public void loadProgram(File f) {
    try {
      programView.display(Program.from(f));
    } catch (IOException e) {
      // TODO: Catch sensibly
      e.printStackTrace();
    }
  }

  @Override
  public void notify(Observable<RegisterMenu> o) {
    // Get the currently selected base and forward that information to
    // the RegistersPanel
  }
}
