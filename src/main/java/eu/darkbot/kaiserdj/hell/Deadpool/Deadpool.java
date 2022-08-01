package eu.darkbot.kaiserdj.hell.Deadpool;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.ExtraMenus;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.util.Popups;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

@Feature(name = "Deadpool", description = "Module that does nothing, just to show how to create a module")
public class Deadpool implements Task, ExtraMenus{
    protected final PluginAPI api;
    protected final HeroAPI heroAPI;
    protected final EntitiesAPI entitiesAPI;
    private final RepairAPI repairAPI;
    private final I18nAPI i18n;
    private final ExtensionsAPI extensionsAPI;
    private final PluginInfo plugin;

    private DeadpoolData dData;

    private DeadpoolWindow panelGeneral;
    private DeadpoolWindow panelSession;
    private Instant lastDead;

    public Deadpool(PluginAPI api, HeroAPI hero, EntitiesAPI entities, RepairAPI repair) throws SQLException {
        this.api = api;
        this.heroAPI = hero;
        this.entitiesAPI = entities;
        this.repairAPI = repair;
        this.i18n = this.api.requireAPI(I18nAPI.class);
        this.extensionsAPI = this.api.requireAPI(ExtensionsAPI.class);
        this.plugin = this.extensionsAPI.getFeatureInfo((Class)this.getClass()).getPluginInfo();

        try {
            Path hellPath = Paths.get("hell");
            if (!Files.exists(hellPath, new LinkOption[0])) {
                Files.createDirectory(hellPath);
            }
        } catch (IOException error) {
            error.printStackTrace();
        }

        this.dData = new DeadpoolData();

        this.lastDead = null;
    }

    @Override
    public void onTickTask() {
        if(repairAPI.isDestroyed()) {
            if (this.lastDead != null && this.lastDead.equals(repairAPI.getLastDeathTime())) {
                return;
            }
            this.lastDead = repairAPI.getLastDeathTime();

            AtomicReference<String> id = new AtomicReference<>("");
            String DestroyerName;
            if (repairAPI.getLastDestroyerName().equals("")) {
                DestroyerName = "Unknown";
                id.set("Unknown");
            } else {
                DestroyerName = repairAPI.getLastDestroyerName();

                this.entitiesAPI.getShips().stream()
                        .filter(s -> s.getEntityInfo().getUsername().equals(DestroyerName))
                        .findFirst()
                        .ifPresent(killer -> id.set("" + killer.getId() + ""));

                if(id.get().equals("")) {
                    this.entitiesAPI.getNpcs().stream()
                        .filter(s -> s.getEntityInfo().getUsername().equals(DestroyerName))
                        .findFirst()
                        .ifPresent(killer -> id.set("npc_" + killer.getNpcId() + ""));
                    }
                    if(id.get().equals("")) {
                        id.set("Unknown");
                    }
                }

            String[] dead = {
                    DestroyerName,
                    id.get(),
                    this.heroAPI.getMap().getName(),
                    repairAPI.getLastDeathLocation().toString(),
                    String.valueOf(repairAPI.getLastDeathTime().toEpochMilli())
            };

            try {
                this.dData.addData(dead);
                this.panelGeneral.getTable().loadData();
                this.panelSession.getTable().loadData();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
        final Collection<JComponent> components = new ArrayList<JComponent>();

        components.add(createSeparator(this.i18n.get(this.plugin, "deadpool.separador")));

        components.add(create("Deadpool", e -> {
            try {
                this.mainWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));

        return components;
    }

    protected void mainWindow() throws SQLException {
        JFrame frame = new JFrame("Deadpool");


        // REVISAR
        JButton reset = new JButton(this.i18n.get(this.plugin, "deadpool.reset"));
        reset.addActionListener(e -> {
            int check = JOptionPane.showConfirmDialog(null,
                    "Is the contact server up", "Please select",
                    JOptionPane.YES_NO_OPTION);

            if (check == JOptionPane.YES_NO_OPTION) {
                try {
                    this.dData.resetData();
                    this.panelGeneral.getTable().loadData();
                    this.panelSession.getTable().loadData();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        JButton close = new JButton("close");
        reset.addActionListener(e -> frame.dispose());


        final JComponent tabGeneral = new JPanel();
        this.panelGeneral = new DeadpoolWindow(this.i18n, this.plugin, this.dData, "general");
        tabGeneral.add(this.panelGeneral.getPanel(), BorderLayout.CENTER);

        final JComponent tabSession = new JPanel();
        this.panelSession = new DeadpoolWindow(this.i18n, this.plugin, this.dData, "session");
        tabSession.add(this.panelSession.getPanel(), BorderLayout.CENTER);

        JTabbedPane tabbed = new JTabbedPane();
        tabbed.setTabPlacement(JTabbedPane.LEFT);

        tabbed.addTab("General", tabGeneral);
        tabbed.addTab("Session", tabSession);

        final JOptionPane options = new JOptionPane(tabbed);
        options.setOptions(new Object[] { reset, "close" });

        frame.getContentPane().add(BorderLayout.CENTER, options);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}

