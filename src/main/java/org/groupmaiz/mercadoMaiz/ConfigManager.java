package org.groupmaiz.mercadoMaiz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.milkbowl.vault.economy.Economy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager implements AuctionService {

    private final MercadoMaiz plugin;
    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

    public ConfigManager(MercadoMaiz plugin) {
        this.plugin = plugin;
    }

    @Override
    public Auction createAuction(String ownerName, String itemKey, double startPrice) {
        String id = UUID.randomUUID().toString();
        AuctionImpl auction = new AuctionImpl(id, ownerName, itemKey, startPrice);
        auctions.put(id, auction);
        return auction;
    }

    @Override
    public List<Auction> listAuctions() {
        return new ArrayList<>(auctions.values());
    }

    @Override
    public boolean placeBid(String auctionId, String bidderName, double amount) {
        AuctionImpl auc = (AuctionImpl) auctions.get(auctionId);
        if (auc == null) return false;
        if (amount > auc.currentBid) {
            auc.currentBid = amount;
            auc.highestBidder = bidderName;
            return true;
        }
        return false;
    }

    @Override
    public Optional<Auction> closeAuction(String auctionId) {
        Auction auc = auctions.remove(auctionId);
        return Optional.ofNullable(auc);
    }

    @Override
    public Inventory buildMarketInventory(MarketType type) {
        String title = ChatColor.GOLD + "Mercado " + type.name();
        Inventory inv = Bukkit.createInventory(null, 54, title);
        int slot = 0;
        for (Auction auc : auctions.values()) {
            ItemStack item = createAuctionItem(auc);
            inv.setItem(slot++, item);
            if (slot >= inv.getSize()) break;
        }
        return inv;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        List<String> lore = meta.getLore();
        String idLine = ChatColor.stripColor(lore.get(0)); // "ID: <uuid>"
        String id = idLine.split(": ")[1];

        AuctionImpl auc = (AuctionImpl) auctions.get(id);
        if (auc == null) {
            player.sendMessage(ChatColor.RED + "Subasta no encontrada.");
            return;
        }

        double price = auc.currentBid;
        Economy econ = plugin.getEcon();
        if (econ.has(player, price)) {
            econ.withdrawPlayer(player, price);
            Player seller = Bukkit.getPlayerExact(auc.owner);
            if (seller != null) econ.depositPlayer(seller, price);
            player.getInventory().addItem(new ItemStack(
                    Material.matchMaterial(auc.itemKey), 1
            ));
            auctions.remove(id);
            player.sendMessage(ChatColor.GREEN + "¡Has comprado " + auc.itemKey + " por " + price + "$!");
        } else {
            player.sendMessage(ChatColor.RED + "No tienes suficiente dinero para esta compra.");
        }
    }

    @Override
    public String getPlayerFaction(Player player) {
        return "Neutral";
    }

    private ItemStack createAuctionItem(Auction auc) {
        ItemStack item = new ItemStack(
                Material.matchMaterial(auc.getItemKey()), 1
        );
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + auc.getItemKey());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "ID: " + auc.getId());
        lore.add(ChatColor.GREEN + "Precio: " + auc.getCurrentBid() + "$");
        lore.add(ChatColor.AQUA + "Owner: " + auc.getOwner());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static class AuctionImpl implements Auction {
        private final String id;
        private final String owner;
        private final String itemKey;
        private double currentBid;
        private String highestBidder;

        public AuctionImpl(String id, String owner, String itemKey, double startPrice) {
            this.id = id;
            this.owner = owner;
            this.itemKey = itemKey;
            this.currentBid = startPrice;
            this.highestBidder = owner;
        }

        @Override public String getId() { return id; }
        @Override public String getItemKey() { return itemKey; }
        @Override public double getCurrentBid() { return currentBid; }
        @Override public String getOwner() { return owner; }
    }

}
