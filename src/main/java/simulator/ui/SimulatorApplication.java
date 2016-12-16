package simulator.ui;

import common.annotations.InstantiateOnEDT;
import lombok.EqualsAndHashCode;
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

@InstantiateOnEDT
@Value
public class SimulatorApplication implements Observer<FileMenu> {
  JFrame applicationFrame = new JFrame("Kilobyte");
  ProgramView programView = new ProgramView();
  Simulator simulator = new Simulator();
  FileMenu fileMenu = FileMenu.withCloseAction(applicationFrame,
        // Clicking on exit in the file-menu closes the application
        () -> dispatchEvent(WINDOW_CLOSING)
  );
  RegisterMenu registerMenu = new RegisterMenu();

  SimulatorApplication() {
    applicationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    fileMenu.addObserver(this);
    //SimulatorMenuBar menuBar = new SimulatorMenuBar(fileMenu);
    SimulatorMenuBar menuBar = new SimulatorMenuBar(fileMenu, registerMenu);
    applicationFrame.setJMenuBar(menuBar);

    applicationFrame.pack();

    /* Center the GUI on the screen, has to be called after pack() */
    applicationFrame.setLocationRelativeTo(null);

    JPanel applicationPanel = new JPanel(new BorderLayout(5, 5));
    applicationPanel.add(programView, BorderLayout.CENTER);

    applicationFrame.add(applicationPanel);

    applicationFrame.setMinimumSize(applicationFrame.getSize());

    /*
    // ADD the register view here later
    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          applicationPanel,
          new JScrollPane(registerView));
    add(splitPane, BorderLayout.CENTER);*/

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

  @Override
  public void notify(Observable<FileMenu> o) {
    // We are on the EDT
    File selectedFile = o.reify().getCurrentlySelectedFile();

    try {
      programView.display(Program.from(selectedFile));
    } catch (IOException e) {
      // TODO: Catch exception sensibly
      e.printStackTrace();
    }
  }
}
