package top.syshub.k_Explosion;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class K_Explosion extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private final Map<UUID, List<UUID>> affectedBlocklikeEntities = new HashMap<>();

    private boolean isBlocklike(Entity e) {
        return e instanceof Item ||
                e instanceof ArmorStand ||
                e instanceof Hanging;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity source = event.getEntity();
        float radius = 2 * event.getRadius();
        if (source.isInWater()) {
            List<UUID> entities = source.getNearbyEntities(radius, radius, radius).stream()
                    .filter(this::isBlocklike)
                    .map(Entity::getUniqueId)
                    .toList();
            affectedBlocklikeEntities.put(source.getUniqueId(), entities);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        EntityDamageEvent.DamageCause c = e.getCause();
        @SuppressWarnings("UnstableApiUsage")
        Entity s = e.getDamageSource().getDirectEntity();
        if (s == null) return;
        UUID uuid = s.getUniqueId();
        if ((c == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                c == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) &&
                affectedBlocklikeEntities.containsKey(uuid) &&
                affectedBlocklikeEntities.get(uuid).contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            List<UUID> entities = new ArrayList<>(affectedBlocklikeEntities.get(uuid).stream().toList());
            entities.remove(e.getEntity().getUniqueId());
            if (entities.isEmpty())
                affectedBlocklikeEntities.remove(uuid);
            else affectedBlocklikeEntities.put(uuid, entities);
        }
    }
}