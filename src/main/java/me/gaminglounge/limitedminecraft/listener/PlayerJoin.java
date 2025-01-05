package me.gaminglounge.limitedminecraft.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.jonas.stuff.Stuff;
import me.gaminglounge.limitedminecraft.LimitedMinecraft;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (LimitedMinecraft.INSTANCE.finished) {
            e.getPlayer().setGameMode(org.bukkit.GameMode.SPECTATOR);
        } else if (!LimitedMinecraft.INSTANCE.aktive) {
            e.getPlayer().teleport(Stuff.INSTANCE.getSpawn());
            e.getPlayer().setGameMode(org.bukkit.GameMode.ADVENTURE);
        } else {
            e.getPlayer().setGameMode(org.bukkit.GameMode.SURVIVAL);
        }

    }
    
}
