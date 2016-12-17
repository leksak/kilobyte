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

    applicationFrame.pack();

    /* Center the GUI on the screen, has to be called after pack() */
    applicationFrame.setLocationRelativeTo(null);

    JPanel applicationPanel = new JPanel(new BorderLayout(5, 5));
    //applicationPanel.add(programView, BorderLayout.CENTER);
    //applicationPanel.add(registersPanel, BorderLayout.WEST);

    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          registersPanel,
          programView);
    applicationPanel.add(splitPane, BorderLayout.CENTER);
    applicationFrame.add(applicationPanel);
    applicationFrame.setMinimumSize(applicationFrame.getSize());

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
