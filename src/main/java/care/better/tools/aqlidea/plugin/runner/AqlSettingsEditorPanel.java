package care.better.tools.aqlidea.plugin.runner;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

public class AqlSettingsEditorPanel {
    private JLabel scriptFileLabel;
    private JTextField scriptFileField;
    private JPanel rootPanel;

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTextField getScriptFileField() {
        return scriptFileField;
    }
}
