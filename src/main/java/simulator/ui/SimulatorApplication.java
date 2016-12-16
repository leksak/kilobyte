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

@InstantiateOnEDT
@Value
public class SimulatorApplication implements Observer<FileMenu> {
  JFrame applicationFrame = new JFrame();
  ProgramView programView = new ProgramView();
  Simulator simulator = new Simulator();
  FileMenu fileMenu = FileMenu.withCloseAction(applicationFrame,
        // Clicking on exit in the file-menu closes the application
        () -> dispatchEvent(WINDOW_CLOSING)
  );

  SimulatorApplication() {
    applicationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    fileMenu.addObserver(this);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    applicationFrame.setJMenuBar(menuBar);

    JPanel applicationPanel = new JPanel(new BorderLayout(5, 5));
    //applicationPanel.add(programView);
    applicationPanel.add(programView, BorderLayout.CENTER);

    applicationFrame.add(applicationPanel);

    applicationFrame.setMinimumSize(applicationFrame.getSize());
    /* Disallow shrinking beyond a certain point */
    applicationFrame.setMinimumSize(applicationFrame.getPreferredSize());

    /* Orient the window */
    applicationFrame.setLocationRelativeTo(null);

    /*
    // ADD the register view here later
    JSplitPane splitPane = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          applicationPanel,
          new JScrollPane(registerView));
    add(splitPane, BorderLayout.CENTER);*/


    applicationFrame.pack();

    /* Center the GUI, has to be called after pack() */
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
      JMenu simulatorMenu = new JMenu("Simulator");

      // Allows toggling between bases
      JMenu registersMenu = new JMenu("Registers");

      app.setVisible();
    });
  }

  @Override
  public void notify(Observable<FileMenu> o) {
    System.out.println(SwingUtilities.isEventDispatchThread());
    File selectedFile = o.reify().getCurrentlySelectedFile();

    try {
      programView.display(Program.from(selectedFile));
    } catch (IOException e) {
      // TODO: Catch exception sensibly
      e.printStackTrace();
    }
  }
}
