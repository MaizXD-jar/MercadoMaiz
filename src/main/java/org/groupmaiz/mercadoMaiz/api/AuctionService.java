package org.groupmaiz.mercadoMaiz.api;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * API de AuctionHouse para que otros plugins puedan consumirla.
 */
public interface AuctionService {

    /** Tipos de mercado según bando */
    enum MarketType {
        CAPITALISTA,
        SOCIALISTA
    }

    /**
     * Representa una subasta en curso.
     */
    interface Auction {
        /** @return ID único de la subasta */
        String getId();
        /** @return la key del item subastado (ej: "minecraft:diamond") */
        String getItemKey();
        /** @return precio actual de la subasta */
        double getCurrentBid();
        /** @return nombre del jugador propietario */
        String getOwner();
    }

    /**
     * Crea una nueva subasta.
     * @param ownerName nombre del jugador que subasta
     * @param itemKey key del item (Material.name())
     * @param startPrice precio inicial
     * @return instancia de la subasta creada
     */
    Auction createAuction(String ownerName, String itemKey, double startPrice);

    /**
     * Lista todas las subastas activas.
     * @return lista de subastas
     */
    List<Auction> listAuctions();

    /**
     * Realiza una puja en una subasta existente.
     * @param auctionId ID de la subasta
     * @param bidderName nombre del jugador que puja
     * @param amount cantidad de la puja
     * @return true si la puja supera la actual, false si es menor o subasta no existe
     */
    boolean placeBid(String auctionId, String bidderName, double amount);

    /**
     * Cierra una subasta y la elimina.
     * @param auctionId ID de la subasta
     * @return subasta cerrada si existía
     */
    Optional<Auction> closeAuction(String auctionId);

    /**
     * Construye un Inventario de Bukkit para mostrar el mercado según el tipo.
     * @param type tipo de mercado (bando)
     * @return inventario listo para abrir
     */
    Inventory buildMarketInventory(MarketType type);

    /**
     * Maneja eventos de click en el inventario del mercado.
     * @param event evento de click en inventario
     */
    void handleClick(InventoryClickEvent event);

    /**
     * Determina el bando/facción de un jugador.
     * @param playerName nombre del jugador
     * @return "Capitalista","Socialista" o "Neutral"
     */
    String getPlayerFaction(org.bukkit.entity.Player player);
}
