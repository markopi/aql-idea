package care.better.tools.aqlidea.plugin.toolWindow;

import javax.swing.*;
import javax.swing.table.TableModel;

public class AqlToolWindow {
    private JPanel rootPanel;
    private JTable responseTable;
    private JTabbedPane tabbedPane1;
    private JEditorPane rawRequestEditor;
    private JEditorPane rawResponseEditor;

    public JPanel getContent() {
        return rootPanel;
    }

    public JTable getTable1() {
        return responseTable;
    }

    public void setRawRequest(String rawRequest) {
        rawRequestEditor.setText(rawRequest);
    }

    public void setRawResponse(String rawRespone) {
        rawResponseEditor.setText(rawRespone);
    }

    public void setTableModel(TableModel tableModel) {
        responseTable.setModel(tableModel);
    }

    public void showTableTab() {
        tabbedPane1.setSelectedIndex(2);
    }

}
