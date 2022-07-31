package eu.darkbot.kaiserdj.hell.Deadpool;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.ExtraMenus;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.managers.*;
import eu.darkbot.util.Popups;

import javax.swing.*;
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

    private DeadpoolTable dTable;
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

        this.dTable = new DeadpoolTable(this.i18n, this.plugin, this.dData);

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
                    repairAPI.getLastDeathTime().toString()
            };

            try {
                this.dData.addData(dead);
                this.dTable.loadData();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
        final Collection<JComponent> components = new ArrayList<JComponent>();

        components.add(createSeparator(this.i18n.get(this.plugin, "deadpool.separador")));

        components.add(create("Deadpool", e -> this.popup()));

        return components;
    }

    protected void popup() {
        JButton reset = new JButton("reset");
        reset.addActionListener(e -> System.out.println("reset"));

        final JOptionPane options = new JOptionPane(this.content());
        options.setOptions(new Object[] { reset, "OK" });
        Popups.showMessageAsync("Deadpool", options);
    }

    protected JComponent content() {
        final JComponent panel = new JPanel();
        JScrollPane tablePanel = new JScrollPane(this.dTable.getTable());
        panel.add(tablePanel, BorderLayout.CENTER);

        /*final JComponent panel2 = new JPanel();
        JTable table2 = this.dTable.getTable();
        JScrollPane tablePanel2 = new JScrollPane(table2);
        panel2.add(tablePanel2, BorderLayout.CENTER);*/

        JTabbedPane tabbed = new JTabbedPane();
        tabbed.setTabPlacement(JTabbedPane.LEFT);

        tabbed.addTab("General", panel);
        //tabbed.addTab("Session", panel2);

        return tabbed;
    }
}

