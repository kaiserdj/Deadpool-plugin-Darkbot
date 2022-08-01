package eu.darkbot.kaiserdj.hell.Deadpool;

import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.api.managers.I18nAPI;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class DeadpoolWindow {

    private final I18nAPI i18n;
    private final PluginInfo plugin;
    private final DeadpoolData dData;
    private final JComponent panel;
    private final String type;
    private String id;

    private DeadpoolTable table;

    public DeadpoolWindow(I18nAPI i18n, PluginInfo plugin, DeadpoolData dData, String type) throws SQLException {
        this.i18n = i18n;
        this.plugin = plugin;
        this.dData = dData;
        this.panel = new JPanel();
        this.type = type;

        this.generate();
    }

    public DeadpoolWindow(I18nAPI i18n, PluginInfo plugin, DeadpoolData dData, String type, String id) throws SQLException {
        this.i18n = i18n;
        this.plugin = plugin;
        this.dData = dData;
        this.panel = new JPanel();
        this.type = type;
        this.id = id;

        this.generate();
    }

    private void generate() throws SQLException {
        this.table = new DeadpoolTable(this.i18n, this.plugin, this.dData, this.type);
        this.panel.add(new JScrollPane(this.table.getTable()), BorderLayout.CENTER);

        JTable table = this.table.getTable();
        switch (this.type) {
            case "general":
                table.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        int row = table.rowAtPoint(evt.getPoint());
                        int col = table.columnAtPoint(evt.getPoint());
                        if (row >= 0 && col >= 0) {
                            JFrame frame = new JFrame((String) table.getValueAt(row, 1));
                            try {
                                frame.add(generateDetails("general", (String) table.getValueAt(row, 2)));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            frame.pack();
                            frame.setLocationByPlatform(true);
                            frame.setVisible(true);
                        }
                    }
                });
                break;
            case "session":
                table.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        int row = table.rowAtPoint(evt.getPoint());
                        int col = table.columnAtPoint(evt.getPoint());
                        if (row >= 0 && col >= 0) {
                            JFrame frame = new JFrame((String) table.getValueAt(row, 1));
                            try {
                                frame.add(generateDetails("session", (String) table.getValueAt(row, 2)));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            frame.pack();
                            frame.setLocationByPlatform(true);
                            frame.setVisible(true);
                        }
                    }
                });
                break;
        }
    }

    public JOptionPane generateDetails(String type, String id) throws SQLException {
        DeadpoolTable tableD = new DeadpoolTable(this.i18n, this.plugin, this.dData, "details_" + type , id);

        JButton reset = new JButton("reset");
        reset.addActionListener(e -> {

            int check = JOptionPane.showConfirmDialog(null,
                    "Is the contact server up", "Please select",
                    JOptionPane.YES_NO_OPTION);

            if (check == JOptionPane.YES_NO_OPTION) {
                try {
                    this.dData.resetData(id);
                    table.loadData();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        final JOptionPane options = new JOptionPane(new JScrollPane(tableD.getTable()));
        options.setOptions(new Object[] { reset, "OK" });

        return options;
    }

    public JComponent getPanel() {
        return this.panel;
    }

    public DeadpoolTable getTable(){
        return this.table;
    }

}
