package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import simulator.Simulator;
import simulator.program.Program;
import simulator.ui.memory.DataMemoryPanel;
import simulator.ui.memory.InstructionMemoryPanel;
import simulator.ui.memory.TabbedMemoryPane;
import simulator.ui.menu.FileMenu;
import simulator.ui.menu.HelpMenu;
import simulator.ui.menu.SimulatorMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

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
  Simulator s = new Simulator();
  JFrame applicationFrame = new JFrame("Kilobyte");
  ProgramView programView = new ProgramView();
  FileMenu fileMenu = FileMenu.withCloseAction(
        applicationFrame,
        // Clicking on exit in the file-menu closes the application
        () -> dispatchEvent(WINDOW_CLOSING),
        this::loadProgram);

  RegistersPanel registersPanel = new RegistersPanel(s.getRegisterFile());
  ProgramCounterView pc = new ProgramCounterView(s.getProgramCounter());
  InstructionMemoryPanel instructionMemory = new InstructionMemoryPanel(s.getInstructionMemory(), "Instruction");
  DataMemoryPanel dataMemory = new DataMemoryPanel(s.getDataMemory());
  TabbedMemoryPane tabbedMemories = new TabbedMemoryPane(instructionMemory, dataMemory);
  ViewMenu displaySettings = new ViewMenu(registersPanel, instructionMemory, dataMemory);
  ControlLinesPanel controlLines = new ControlLinesPanel(s.getControl());

  Object interruptLock = new Object();

  @NonFinal
  AtomicBoolean wasInterrupted = new AtomicBoolean(false);

  @NonFinal
  boolean hasReadExitStatement = false;

  public void run() {
    wasInterrupted.set(false);
    while(!(hasReadExitStatement || wasInterrupted.get())) {
      log.info("Executing the next instruction: " + s.getCurrentInstruction());
      executeNextInstruction();
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
      }
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
    SimulatorMenuBar menuBar = new SimulatorMenuBar(fileMenu, displaySettings);

    HelpMenu helpMenu = new HelpMenu(applicationFrame);
    menuBar.add(helpMenu);

    applicationFrame.setJMenuBar(menuBar);

    JToolBar controls = new SimulatorControlsToolbar(this);
    applicationFrame.add(controls, BorderLayout.NORTH);
    applicationFrame.add(controlLines, BorderLayout.SOUTH);
    JPanel applicationPanel = new JPanel(new BorderLayout());

    // Contains the program-counter and the registers in a stacked fashion
    JPanel pcAndRegistersPanel = new JPanel();

    // BoxLayout let's us stack our components
    pcAndRegistersPanel.setLayout(new BoxLayout(pcAndRegistersPanel, BoxLayout.PAGE_AXIS));
    pcAndRegistersPanel.add(pc);
    pcAndRegistersPanel.add(registersPanel);
    pc.setBorder(BorderFactory.createTitledBorder("Program Counter"));
    registersPanel.setBorder(BorderFactory.createTitledBorder("Registers"));

    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          pcAndRegistersPanel,
          programView);
    applicationPanel.add(splitPane, BorderLayout.CENTER);

    applicationPanel.add(tabbedMemories, BorderLayout.EAST);
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

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      // The application itself is conglomeration of Swing components
      // hence it has to be instantiated on the EDT
      SimulatorApplication app = new SimulatorApplication();

      app.setVisible();
    });
  }

  public void loadProgram(File f) {
    try {
      Program p = Program.from(f);
      s.loadProgram(p);
      s.reset();
      programView.display(p);
      // All the values will be reset
      registersPanel.reset();
      instructionMemory.update();
      dataMemory.reset();
      controlLines.reset();
    } catch (IOException e) {
      // TODO: Catch sensibly
      e.printStackTrace();
    }
  }

  public boolean executeNextInstruction() {
    //wasInterrupted.set(false);
    hasReadExitStatement = s.executeNextInstruction();
    registersPanel.update();
    instructionMemory.update();
    dataMemory.update();
    programView.highlightLine(s.getProgramCounter().currentInstructionIndex());
    pc.update();
    controlLines.update();
    return hasReadExitStatement;
  }

  public void reset() {
    // TODO: Maybe we should just reload the program entirely?
    s.reset();
    registersPanel.reset();
    instructionMemory.update();
    dataMemory.update();
    controlLines.update();
    programView.reset();
    pc.update();
  }
}
