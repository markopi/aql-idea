package care.better.tools.aqlidea.plugin.toolWindow;

import javax.swing.*;
import javax.swing.table.TableModel;

public class AqlToolWindow extends JPanel {
    private JPanel rootPanel;
    private JTable responseTable;
    private JTabbedPane tabbedPane1;
    private JEditorPane rawRequestEditor;
    private JEditorPane rawResponseEditor;

    public void setRawRequest(String rawRequest) {
        rawRequestEditor.setText(rawRequest);
    }

    public void setRawResponse(String rawRespone) {
        rawResponseEditor.setText(rawRespone);
    }

    public void setTableModel(TableModel tableModel) {
        responseTable.setModel(tableModel);
    }

    public void showQueryTab(QueryTab tab) {
        tabbedPane1.setSelectedIndex(tab.index);
    }

    private void createUIComponents() {
        // workaround to allow setting the actual aql tool window to content
        rootPanel = this;
    }

    enum QueryTab {
        request(0), response(1), table(2);
        private int index;

        QueryTab(int index) {
            this.index = index;
        }
    }
}
