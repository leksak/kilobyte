package kilobyte.simulator.ui;

import kilobyte.common.annotations.InstantiateOnEDT;
import kilobyte.simulator.Simulator;
import kilobyte.simulator.program.Program;
import kilobyte.simulator.ui.memory.DataMemoryPanel;
import kilobyte.simulator.ui.memory.InstructionMemoryPanel;
import kilobyte.simulator.ui.memory.TabbedMemoryPane;
import kilobyte.simulator.ui.menu.FileMenu;
import kilobyte.simulator.ui.menu.HelpMenu;
import kilobyte.simulator.ui.menu.SimulatorMenuBar;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_V;
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
@Log
@Value
public class SimulatorApplication {
  @NonFinal
  Simulator simulator = new Simulator();

  JFrame applicationFrame = new JFrame("Kilobyte");
  ProgramView programView = new ProgramView();
  FileMenu fileMenu = FileMenu.withCloseAction(
        applicationFrame,
        // Clicking on exit in the file-menu closes the application
        () -> dispatchEvent(WINDOW_CLOSING),
        this::loadProgram);

  RegistersPanel registersPanel = new RegistersPanel(simulator.getRegisterFile());
  ProgramCounterView programCounterView = new ProgramCounterView(simulator.getProgramCounter());
  InstructionMemoryPanel instructionMemoryPanel = new InstructionMemoryPanel(simulator.getInstructionMemory(), "Instruction");
  DataMemoryPanel dataMemoryPanel = new DataMemoryPanel(simulator.getDataMemory());
  TabbedMemoryPane tabbedMemoriesView = new TabbedMemoryPane(instructionMemoryPanel, dataMemoryPanel);
  DisplaySettings displaySettings = new DisplaySettings(registersPanel, instructionMemoryPanel, dataMemoryPanel);
  ControlLinesPanel controlLines = new ControlLinesPanel(simulator.getControl());
  SimulatorMenuBar menuBar;
  Object interruptLock = new Object();
  SimulatorControlsToolbar controls;

  @NonFinal
  Program currentlyOpenProgram = null;

  @NonFinal
  AtomicBoolean wasInterrupted = new AtomicBoolean(false);

  @NonFinal
  boolean hasReadExitStatement = false;

  public void run() {
    wasInterrupted.set(false);
    controls.transitionToDisplayState(SimulatorControlsToolbar.ToolbarDisplayState.RUNNING);
    while(!(hasReadExitStatement || wasInterrupted.get() || Thread.interrupted())) {
      log.info("Executing the next instruction: " + simulator.getCurrentInstruction());
      executeNextInstruction();
      try {
        Thread.sleep(400);
      } catch (InterruptedException ignored) {
      }
    }
    if (hasReadExitStatement) {
      controls.transitionToDisplayState(SimulatorControlsToolbar.ToolbarDisplayState.FINISHED);
    }
  }

  public void stop() {
    log.info("Interrupting the simulation");
    wasInterrupted.set(true);
  }

  SimulatorApplication() {
    // DISPOSE_ON_CLOSE is cleaner than EXIT_ON_CLOSE
    applicationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    fileMenu.setMnemonic(VK_F);
    displaySettings.setMnemonic(VK_V);
    menuBar = new SimulatorMenuBar(fileMenu, displaySettings);

    HelpMenu helpMenu = new HelpMenu(applicationFrame);
    menuBar.add(helpMenu);

    applicationFrame.setJMenuBar(menuBar);
    controls = new SimulatorControlsToolbar(this);
    applicationFrame.add(controls, BorderLayout.NORTH);
    applicationFrame.add(controlLines, BorderLayout.SOUTH);
    JPanel applicationPanel = new JPanel(new BorderLayout());

    // Contains the program-counter and the registers in a stacked fashion
    JPanel pcAndRegistersPanel = new JPanel();

    // BoxLayout let's us stack our components
    pcAndRegistersPanel.setLayout(new BoxLayout(pcAndRegistersPanel, BoxLayout.PAGE_AXIS));
    pcAndRegistersPanel.add(programCounterView);
    pcAndRegistersPanel.add(registersPanel);
    programCounterView.setBorder(BorderFactory.createTitledBorder("Program Counter"));
    registersPanel.setBorder(BorderFactory.createTitledBorder("Registers"));

    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          pcAndRegistersPanel,
          programView);
    applicationPanel.add(splitPane, BorderLayout.CENTER);

    applicationPanel.add(tabbedMemoriesView, BorderLayout.EAST);
    applicationFrame.add(applicationPanel);

    applicationFrame.setMinimumSize(applicationFrame.getSize());

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

  static Options options = new Options()
        .addOption("h", "help", false, "print this message")
        .addOption("D", "debug", false, "activates logging")
        .addOption(null, "supported", false, "prints all supported instructions");
  static CommandLineParser parser = new DefaultParser();
  static HelpFormatter formatter = new HelpFormatter();

  private static CommandLine parse(String... args) throws ParseException {
    return parser.parse(options, args);
  }
  public static void main(String[] args) {
    CommandLine line;
    try {
      line = parse(args);
    } catch (ParseException e) {
      return;
    }

    if (line.hasOption("supported")) {
      Simulator.getSupportedInstructions().forEach(System.out::println);
      return;
    }

    if (!(line.hasOption("debug") || line.hasOption("D"))) {
      LogManager.getLogManager().reset();
    } else {
      log.info("Logging is activated");
    }

    SwingUtilities.invokeLater(() -> {
      // The application itself is conglomeration of Swing components
      // hence it has to be instantiated on the EDT
      SimulatorApplication app = new SimulatorApplication();

      app.setVisible();
    });
  }

  public void loadProgram(File f) {
    try {
      loadProgram(Program.from(f));
    } catch (IOException e) {
      // TODO: Catch sensibly
      e.printStackTrace();
    }
  }

  public void loadProgram(Program p) {
    currentlyOpenProgram = p;

    // All the values will be display
    simulator = Simulator.executingProgram(currentlyOpenProgram);
    programCounterView.display(simulator.getProgramCounter());
    registersPanel.display(simulator.getRegisterFile());
    instructionMemoryPanel.display(simulator.getInstructionMemory());
    dataMemoryPanel.display(simulator.getDataMemory());
    controlLines.display(simulator.getControl());
    controls.transitionToDisplayState(SimulatorControlsToolbar.ToolbarDisplayState.RESET);

    programView.display(currentlyOpenProgram);
  }

  public boolean executeNextInstruction() {
    hasReadExitStatement = simulator.executeNextInstruction();
    registersPanel.update();
    instructionMemoryPanel.update();
    dataMemoryPanel.update();
    programView.highlightLine(simulator.getProgramCounter().currentInstructionIndex());
    programCounterView.update();
    controlLines.update();
    if (hasReadExitStatement) {
      controls.transitionToDisplayState(SimulatorControlsToolbar.ToolbarDisplayState.FINISHED);
    }
    return hasReadExitStatement;
  }

  public void reloadProgram() {
    loadProgram(currentlyOpenProgram);
  }
}
