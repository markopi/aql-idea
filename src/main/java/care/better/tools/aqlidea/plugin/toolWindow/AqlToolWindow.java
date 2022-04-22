package care.better.tools.aqlidea.plugin.toolWindow;

import care.better.tools.aqlidea.ui.treetable.JTreeTable;
import care.better.tools.aqlidea.ui.treetable.TreeTableData;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class AqlToolWindow extends JPanel {
    private JPanel rootPanel;
    private JTabbedPane responseTable;
    private JEditorPane rawRequestEditor;
    private JEditorPane rawResponseEditor;
    private JPanel queryResponseTab;

    public void setRawRequest(String rawRequest) {
        rawRequestEditor.setText(rawRequest);
    }

    public void setRawResponse(String rawRespone) {
        rawResponseEditor.setText(rawRespone);
    }

    public void setResultTableData(@Nullable TreeTableData treeTableData) {
        if (treeTableData==null) treeTableData = new TreeTableData(Collections.emptyList(), Collections.emptyList());

        queryResponseTab.removeAll();

        JTreeTable table = JTreeTable.Companion.of(treeTableData);
        JScrollPane scrollPane = new JScrollPane(table);
        GridConstraints gc = new GridConstraints();
        gc.setFill(GridConstraints.FILL_BOTH);
        queryResponseTab.add(scrollPane, gc);

    }

    public void showQueryTab(QueryTab tab) {
        responseTable.setSelectedIndex(tab.index);
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
