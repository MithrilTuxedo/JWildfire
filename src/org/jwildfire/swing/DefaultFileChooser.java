package org.jwildfire.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DefaultFileChooser extends JFileChooser {
  private static final long serialVersionUID = 1L;

  @Override
  public void approveSelection() {
    File f = getSelectedFile();
    if (f.exists() && getDialogType() == SAVE_DIALOG) {
      int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
      switch (result) {
        case JOptionPane.YES_OPTION:
          super.approveSelection();
          return;
        case JOptionPane.NO_OPTION:
          return;
        case JOptionPane.CANCEL_OPTION:
          cancelSelection();
          return;
      }
    }
    super.approveSelection();
  }

}
