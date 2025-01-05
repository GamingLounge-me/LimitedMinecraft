package me.gaminglounge.limitedminecraft; 

import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager; 
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import me.gaminglounge.limitedminecraft.listener.Invincible;
import me.gaminglounge.limitedminecraft.listener.PlayerJoin;
import net.kyori.adventure.text.minimessage.MiniMessage;
 
public final class LimitedMinecraft extends JavaPlugin {
 
    public static LimitedMinecraft INSTANCE; 
    public static FileConfiguration CONFIG;
    public boolean aktive, finished;
    public LocalDateTime now, start, end;
    public BukkitTask doStart, doEnd; 
 
    @Override
    public void onLoad() {
        INSTANCE = this;
        this.saveDefaultConfig();
        CONFIG = this.getConfig();

        // if (!CommandAPI.isLoaded()) CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        // new ReloadCommand();

    }

    @Override
    public void onEnable() {
        this.listener();

        setScheduler();
    }

    @Override
    public void onDisable() {
        
    }

    public void listener() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new Invincible(), this);
        pm.registerEvents(new PlayerJoin(), this);
    } 

    public void setScheduler() {
        if (doStart != null) doStart.cancel();
        if (doEnd != null) doEnd.cancel();

        now = java.time.LocalDateTime.now();
        start = java.time.LocalDateTime.of(
            CONFIG.getInt("Start.year"),
            CONFIG.getInt("Start.month"),
            CONFIG.getInt("Start.day"),
            CONFIG.getInt("Start.hour"),
            CONFIG.getInt("Start.minute"),
            CONFIG.getInt("Start.second")
        );
        end = start;
        if (CONFIG.getInt("Duration.years") != 0) {
            end = end.plusYears(CONFIG.getInt("Duration.years"));
        }
        if (CONFIG.getInt("Duration.months") != 0) {
            end = end.plusMonths(CONFIG.getInt("Duration.months"));
        }
        if (CONFIG.getInt("Duration.days") != 0) {
            end = end.plusDays(CONFIG.getInt("Duration.days"));
        }
        if (CONFIG.getInt("Duration.hours") != 0) {
            end = end.plusHours(CONFIG.getInt("Duration.hours"));
        }
        if (CONFIG.getInt("Duration.minutes") != 0) {
            end = end.plusMinutes(CONFIG.getInt("Duration.minutes"));
        }
        if (CONFIG.getInt("Duration.seconds") != 0) {
            end = end.plusSeconds(CONFIG.getInt("Duration.seconds"));
        }
        if (CONFIG.getInt("Duration.weeks") != 0) {
            end = end.plusWeeks(CONFIG.getInt("Duration.weeks"));
        }

        if (now.isBefore(start)) {
            Bukkit.getWorlds().forEach(world -> {
                world.getWorldBorder().setSize(LimitedMinecraft.CONFIG.getInt("WorldBorder.Inaktive"));
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setTime(1000); // Set the time to day
                world.setStorm(false);
                world.setThundering(false);
            });
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
            this.aktive = false;
            this.finished = false;
        }
        
        if (now.isAfter(start) && now.isBefore(end)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
            Bukkit.getWorlds().forEach(world -> {
                world.getWorldBorder().setSize(LimitedMinecraft.CONFIG.getInt("WorldBorder.Aktive"));
                world.setGameRuleValue("doDaylightCycle", "true");
                world.setGameRuleValue("doWeatherCycle", "true");
            });
            aktive = true;
            this.finished = false;
        } else if (java.time.Duration.between(now, start).isPositive()) {
            doStart = Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
                Bukkit.getWorlds().forEach(world -> {
                    world.getWorldBorder().setSize(LimitedMinecraft.CONFIG.getInt("WorldBorder.Aktive"));
                    world.setGameRuleValue("doDaylightCycle", "true");
                    world.setGameRuleValue("doWeatherCycle", "true");
                });
                Bukkit.getOnlinePlayers().forEach(action -> {
                    action.setGameMode(org.bukkit.GameMode.SURVIVAL);
                });
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("World Boarder auf " + LimitedMinecraft.CONFIG.getInt("WorldBorder.Aktive") +  " erhöht"));
                this.aktive = true;
                this.finished = false;
            }, java.time.Duration.between(now, start).toSeconds() * 20);
        }

        if (now.isAfter(end)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setTime(1000); // Set the time to day
                world.setStorm(false);
                world.setThundering(false);
            });
            this.aktive = false;
            this.finished = true;
        } else if (java.time.Duration.between(now, end).isPositive()) {
            doEnd = Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
                Bukkit.getOnlinePlayers().forEach(action -> {
                    action.setGameMode(org.bukkit.GameMode.SPECTATOR);
                });
                Bukkit.getWorlds().forEach(world -> {
                    world.setGameRuleValue("doDaylightCycle", "false");
                    world.setGameRuleValue("doWeatherCycle", "false");
                    world.setTime(1000); // Set the time to day
                    world.setStorm(false);
                    world.setThundering(false);
                });
                this.aktive = false;
                this.finished = true;
            }, java.time.Duration.between(now, end).toSeconds() * 20);
        }

        BossBar bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        Bukkit.getScheduler().runTaskTimer(LimitedMinecraft.INSTANCE, () -> {
            java.time.Duration duration;
            if (finished) {
                duration = java.time.Duration.between(java.time.LocalDateTime.now(), end);
            } else if (aktive) {
                duration = java.time.Duration.between(java.time.LocalDateTime.now(), end);
            } else {
                duration = java.time.Duration.between(java.time.LocalDateTime.now(), start);
            }

            long days = duration.toDays();
            duration = duration.minusDays(days);

            long hours = duration.toHours();
            duration = duration.minusHours(hours);

            long minutes = duration.toMinutes();
            duration = duration.minusMinutes(minutes);

            long seconds = duration.getSeconds();

            String timeLeft = "§b" + days + " : " +
                              "§e" + hours + " : " +
                              "§a" + minutes + " : " +
                              "§9" + seconds + "";

            bar.setTitle(timeLeft);

            Bukkit.getOnlinePlayers().forEach(action -> {
                bar.addPlayer(action);
            });
        }, 0, 20);

    }
} 
