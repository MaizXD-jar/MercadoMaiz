package org.groupmaiz.mercadoMaiz;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Utilidad para crear cabezas personalizadas (URSS / OTAN).
 */
public class  {

    /**
     * Crea una cabeza con la textura de la bandera URSS.
     */
    public static ItemStack createHeadURSS() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ1NjFmODY4NzZmYzc3NjVkZGI3ZWRhYmY0ZGY4NjJiMDJkODdkZDdmNzRmNDVmN2FlZmY3OWYxN2JhOTJhIn19fQ==";
        return createCustomHead("§cSocialista", base64);
    }

    /**
     * Crea una cabeza con la textura de la bandera OTAN.
     */
    public static ItemStack createHeadOTAN() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjY2MDE0OTAwODkzY2ZlNWY4ZmU3ZGZhZjBiMzUwYTc3Y2Y2ZmM1NzI5Y2U4NWJhYWY0OWZmOTA1ZWZhYmM2YSJ9fX0=";
        return createCustomHead("§9Capitalista", base64);
    }

    /**
     * Crea una cabeza personalizada con un nombre visible y una textura base64.
     * @param displayName Nombre que se verá en el inventario.
     * @param base64 Textura en base64 (de Minecraft).
     * @return ItemStack de tipo cabeza.
     */
    private static ItemStack createCustomHead(String displayName, String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }
}
