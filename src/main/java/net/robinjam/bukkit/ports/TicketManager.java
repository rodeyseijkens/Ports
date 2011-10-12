package net.robinjam.bukkit.ports;

import java.util.HashMap;
import java.util.Map;
import net.robinjam.bukkit.ports.persistence.Port;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author robinjam
 */
public class TicketManager implements Runnable {
    
    Ports plugin;
    Map<Player, Port> tickets = new HashMap();
    Map<World, Long> previousTimes = new HashMap();
    Map<World, Long> portsTimes = new HashMap();
    
    public TicketManager(Ports plugin) {
        this.plugin = plugin;
    }
    
    public void addTicket(Player player, Port port) {
        tickets.put(player, port);
    }
    
    public void removeTicket(Player player) {
        tickets.remove(player);
    }

    public void run() {
        for (Map.Entry<Player, Port> ticket : tickets.entrySet()) {
            Player player = ticket.getKey();
            Port port = ticket.getValue();
            
            World world = Bukkit.getWorld(port.getWorld());
            long time = world.getTime();
            
            if (portsTimes.get(world) == null) {
                portsTimes.put(world, time);
            }
            
            if (previousTimes.get(world) == null) {
                previousTimes.put(world, time);
            }
            
            long previousTime = portsTimes.get(world);
            long newTime = previousTime + time - previousTimes.get(world);
            
            // If time went backwards (thanks to /time set 0 for example)
            // then move forwards 24h
            if (newTime < previousTime)
                newTime += 24000;
            
            portsTimes.put(world, newTime);
            previousTimes.put(world, time);
            
            boolean teleported = false;
            for (long x = previousTime; x <= newTime; x++) {
                if (x % port.getDepartureSchedule() == 0) {
                    plugin.teleportPlayer(player, port);
                    teleported = true;
                }
            }
            
            if (!teleported) {
                long t = (port.getDepartureSchedule() - newTime % port.getDepartureSchedule());
                long d = t / 24000;
                long h = (t % 24000) / 1000;
                long m = ((t % 24000) % 1000) / 16;
                String nextDeparture = String.format("[%dd %dh %dm]", d, h, m);
                player.sendMessage(ChatColor.AQUA + "This " + port.getDescription() + " will depart in " + nextDeparture);
            }
        }
    }
    
}
