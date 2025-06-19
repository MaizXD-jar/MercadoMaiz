package org.groupmaiz.mercadoMaiz;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.groupmaiz.mercadoMaiz.api.AuctionService;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public final class MercadoMaiz extends JavaPlugin implements Listener {

    private Economy econ;
    private AuctionManager auctionManager;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault no encontrado. Desactivando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.auctionManager = new AuctionManager(this);

        getServer().getServicesManager().register(
                AuctionService.class,
                auctionManager,
                this,
                org.bukkit.plugin.ServicePriority.Normal
        );
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("MercadoMaiz habilitado.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores pueden usar este comando.");
            return true;
        }
        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("mercado")) {
            if (args.length == 0) {
                openMarketSelector(player);
            } else if (args[0].equalsIgnoreCase("vender")) {
                openSellDialog(player);
            } else {
                player.sendMessage(ChatColor.RED + "Uso: /mercado [vender]");
            }
            return true;
        }
        return false;
    }

    public Economy getEcon() {
        return econ;
    }

    private void openMarketSelector(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Selecciona tu Mercado");
        gui.setItem(3, HeadUtils.createHeadURSS());
        gui.setItem(5, HeadUtils.createHeadOTAN());
        player.openInventory(gui);
    }

    private void openSellDialog(Player player) {
        String bando = determinePlayerBando(player);
        if (bando.equals("Neutral")) {
            openMarketSelector(player);
        } else if (bando.equals("Capitalista")) {
            openSpecificMarket(player, AuctionService.MarketType.CAPITALISTA);
        } else if (bando.equals("Socialista")) {
            openSpecificMarket(player, AuctionService.MarketType.SOCIALISTA);
        }
    }

    private void openSpecificMarket(Player player, AuctionService.MarketType type) {
        Inventory gui = auctionManager.buildMarketInventory(type);
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Selecciona tu Mercado")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (name.contains("Socialista")) {
                openSpecificMarket(player, AuctionService.MarketType.SOCIALISTA);
            } else if (name.contains("Capitalista")) {
                openSpecificMarket(player, AuctionService.MarketType.CAPITALISTA);
            }
        } else if (title.contains("Mercado ")) {
            event.setCancelled(true);
            auctionManager.handleClick(event);
        }
    }

    private String determinePlayerBando(Player player) {
        return Optional.ofNullable(
                getServer().getServicesManager().getRegistration(AuctionService.class)
        ).map(rsp -> auctionManager.getPlayerFaction(player)).orElse("Neutral");
    }

    static class HeadUtils {
        private static final String TEXTURE_URSS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ1NjFmODY4NzZmYzc3NjVkZGI3ZWRhYmY0ZGY4NjJiMDJkODdkZDdmNzRmNDVmN2FlZmY3OWYxN2JhOTJhIn19fQ==";
        private static final String TEXTURE_OTAN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjY2MDE0OTAwODkzY2ZlNWY4ZmU3ZGZhZjBiMzUwYTc3Y2Y2ZmM1NzI5Y2U4NWJhYWY0OWZmOTA1ZWZhYmM2YSJ9fX0=";

        public static ItemStack createCustomHead(String base64, String name) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            meta.setDisplayName(name);
            head.setItemMeta(meta);
            return head;
        }

        public static ItemStack createHeadURSS() {
            return createCustomHead(TEXTURE_URSS, ChatColor.RED + "Mercado Socialista");
        }

        public static ItemStack createHeadOTAN() {
            return createCustomHead(TEXTURE_OTAN, ChatColor.BLUE + "Mercado Capitalista");
        }
    }
}
