package eu.darkbot.kaiserdj.hell.Deadpool;

import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeadpoolTable {

    private final I18nAPI i18n;
    private final PluginInfo plugin;
    private DeadpoolData dData;

    private DefaultTableModel model;
    private JTable table;

    public DeadpoolTable(I18nAPI i18n, PluginInfo plugin, DeadpoolData dData) throws SQLException {
        this.i18n = i18n;
        this.plugin = plugin;
        this.dData = dData;

        this.model = new DefaultTableModel();
        this.table = new JTable(this.model);

        this.model.addColumn(this.i18n.get(this.plugin, "deadpool.position"));
        this.model.addColumn(this.i18n.get(this.plugin, "deadpool.name"));
        this.model.addColumn(this.i18n.get(this.plugin, "deadpool.id"));
        this.model.addColumn(this.i18n.get(this.plugin, "deadpool.numTimes"));
        this.model.addColumn(this.i18n.get(this.plugin, "deadpool.details"));
        this.loadData();
    }

    public void loadData() throws SQLException {
        ArrayList<String[]> data = this.dData.getGeneral();
        this.model.setNumRows(data.size());

        for (int i = 0; i < data.size(); i++) {
            String[] columns = data.get(i);
            for (int j = 0; j < this.model.getColumnCount(); j++) {
                this.model.setValueAt(columns[j], i, j);
            }
        }
    }

    public JTable getTable() {
        return this.table;
    }
}
