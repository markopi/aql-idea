package care.better.tools.aqlidea.plugin.toolWindow;

import com.intellij.openapi.vcs.history.VcsRevisionNumber.Int;

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

    public void showQueryTab(QueryTab tab) {
        tabbedPane1.setSelectedIndex(tab.index);
    }

    enum QueryTab {
        request(0),response(1),table(2);
        private int index;

        QueryTab(int index) {
            this.index = index;
        }
    }
}
