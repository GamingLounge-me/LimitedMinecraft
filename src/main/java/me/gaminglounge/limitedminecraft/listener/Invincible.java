package me.gaminglounge.limitedminecraft.listener; 
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;

import me.gaminglounge.limitedminecraft.LimitedMinecraft;

public class Invincible implements Listener{

    @EventHandler
    public void onPVP(EntityDamageEvent e) {
        if (!LimitedMinecraft.INSTANCE.aktive) {e.setCancelled(true);}
    }

    @EventHandler
    public void onExhausting(EntityExhaustionEvent e) {
        if (!LimitedMinecraft.INSTANCE.aktive) {e.setCancelled(true);}
    }

}
