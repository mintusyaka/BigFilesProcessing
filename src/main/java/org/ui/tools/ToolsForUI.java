package org.ui.tools;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class ToolsForUI {
    public static void fillThreadsTableHeader(JTable threadsTable)
    {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        model.setColumnIdentifiers(new String[] { "Thread ID", "Status", "File", "Execution Time"});
    }

    public static void clearThreadTable(JTable threadsTable) {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        model.setRowCount(0);
    }

    public static void updateThreadTable(Thread thread, String status, String logName, String executionTime, JTable threadsTable)
    {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        int rows = threadsTable.getRowCount();

        boolean threadAlreadyAdded = false;
        int threadRow = model.getRowCount() - 1;

        for(int i = 0; i < rows; ++i)
        {
            String threadName = (String) threadsTable.getValueAt(i, 0);
            if(threadName.equals(thread.getName().substring(7))) {
                threadAlreadyAdded = true;
                threadRow = i;
                break;
            }
        }

        if(threadAlreadyAdded)
        {
            model.setValueAt(status, threadRow, 1);
            model.setValueAt(logName, threadRow, 2);
            model.setValueAt(executionTime, threadRow, 3);
        } else {
            Vector<String> row = new Vector<>();
            row.add(thread.getName().substring(7));
            row.add(status);
            row.add(logName);
            row.add(executionTime);

            model.addRow(row);
        }

    }
}
