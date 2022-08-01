package eu.darkbot.kaiserdj.hell.Deadpool;

import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DeadpoolTable {

    private final I18nAPI i18n;
    private final PluginInfo plugin;
    private DeadpoolData dData;
    private final String type;

    private DefaultTableModel model;
    private JTable table;
    private String id;

    public DeadpoolTable(I18nAPI i18n, PluginInfo plugin, DeadpoolData dData, String type) throws SQLException {
        this.i18n = i18n;
        this.plugin = plugin;
        this.dData = dData;
        this.type = type;

        this.model = new DefaultTableModel();
        this.table = new JTable(this.model);

        this.setColumn();
        setFormatTable();
        this.loadData();
    }

    public DeadpoolTable(I18nAPI i18n, PluginInfo plugin, DeadpoolData dData, String type, String id) throws SQLException {
        this.i18n = i18n;
        this.plugin = plugin;
        this.dData = dData;
        this.type = type;
        this.id = id;

        this.model = new DefaultTableModel();
        this.table = new JTable(this.model);

        this.setColumn();
        setFormatTable();
        this.loadData();
    }

    public void setColumn(){
        switch (this.type){
            case "general":
            case "session":
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.position"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.name"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.id"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.numTimes"));
                break;
            case "details_general":
            case "details_session":
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.id"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.name"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.map"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.location"));
                this.model.addColumn(this.i18n.get(this.plugin, "deadpool.time"));
                break;
        }
    }

    public void setFormatTable(){
        this.table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        this.table.setFillsViewportHeight(true);
        this.table.getTableHeader().setReorderingAllowed(false);
        this.table.removeEditor();
        this.table.setEnabled(false);
        this.table.setRowHeight(25);
        this.table.setIntercellSpacing(new Dimension(5, 5));
        this.table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public void loadData() throws SQLException {
        ArrayList<String[]> data = null;
        switch (this.type) {
            case "general":
            case "session":
                data = this.dData.getGeneral(this.type);
                break;
            case "details_general":
                data = this.dData.getDetails("general", this.id);
                break;
            case "details_session":
                data = this.dData.getDetails("session", this.id);
        }
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
