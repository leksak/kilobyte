package simulator.ui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import common.annotations.InstantiateOnEDT;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.jcip.annotations.NotThreadSafe;
import simulator.Observable;
import simulator.Observer;

import javax.swing.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_Q;
import static javax.swing.JFileChooser.APPROVE_OPTION;

/**
 * Has to be instantiated on the EDT
 */
@InstantiateOnEDT
@NotThreadSafe // Doesn't store listeners in a thread-safe manner b/c overhead
@Value
@EqualsAndHashCode(callSuper = true)
class FileMenu extends JMenu implements Observable<FileMenu> {
  List<Observer<FileMenu>> observers = new ArrayList<>();

  @NonFinal
  File currentlySelectedFile = null;

  // Contains Load and Exit
  JMenuItem exit = new JMenuItem("Exit");
  JMenuItem load = new JMenuItem("Load");
  JFileChooser fileChooser = new JFileChooser();

  private FileMenu(JFrame frame, Runnable closeOperation) {
    super("File");

    exit.setMnemonic(VK_Q);
    exit.setAccelerator(KeyStroke.getKeyStroke(VK_Q, CTRL_MASK));
    exit.setToolTipText("Exit Application");
    exit.addActionListener(event -> SwingUtilities.invokeLater(closeOperation));

    load.setMnemonic(VK_L);
    load.setAccelerator(KeyStroke.getKeyStroke(VK_L, CTRL_MASK));
    load.setToolTipText("Load File");

    load.addActionListener(event -> {
      // This event happens on the event dispatch thread.

      int ret = fileChooser.showOpenDialog(frame);

      if (ret == APPROVE_OPTION) {
        currentlySelectedFile = fileChooser.getSelectedFile();
        notifyObservers(); // Still on the EDT!
      }
    });

    this.add(load);
    this.add(exit);
  }

  /**
   * Accepts an operation that closes the application. The passed Runnable
   * does not have to be called on the EDT by the caller - this class
   * manages that.
   *
   * @param frame required to center the file open dialog on top of the frame.
   * @param closeOperation the runnable that closes the application
   * @return the FileMenu for use by the application
   */
  static FileMenu withCloseAction(JFrame frame, Runnable closeOperation) {
    return new FileMenu(frame, closeOperation);
  }

  @Override
  public FileMenu reify() {
    return this;
  }

  @Override
  public void addObserver(Observer<FileMenu> o) {
    observers.add(o);
  }

  @Override
  public ImmutableCollection<Observer<FileMenu>> observers() {
    return ImmutableList.copyOf(observers);
  }
}
