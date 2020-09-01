package xyz.acrylicstyle.seikatu

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import util.SneakyThrow
import xyz.acrylicstyle.paper.Paper
import xyz.acrylicstyle.seikatu.generators.stone.StoneWorldGenerator
import xyz.acrylicstyle.seikatu.utils.MenuGui
import xyz.acrylicstyle.tomeito_api.TomeitoAPI
import xyz.acrylicstyle.tomeito_api.command.PlayerCommandExecutor
import java.io.IOException

class SeikatuPlugin : JavaPlugin(), Listener {
    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        // Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")
        Bukkit.getPluginManager().registerEvents(this, this)
        TomeitoAPI.registerCommand("menu", object : PlayerCommandExecutor() {
            override fun onCommand(player: Player, args: Array<String>) {
                val c = player.inventory.contents
                for (i in c.indices) {
                    if (MenuGui.isMenuItem(c[i])) player.inventory.setItem(i, null)
                }
                val item = player.inventory.getItem(8)
                if (item == null || item.type == Material.AIR) {
                    player.inventory.setItem(8, MenuGui.menuItem)
                }
                player.openInventory(MenuGui.buildMenu())
            }
        })
    }

    override fun onDisable() {
        try {
            this.config.save("./plugins/SeikatuPlugin/config.yml")
        } catch (e: IOException) {
            SneakyThrow.sneaky<Any>(e)
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val c = e.player.inventory.contents
        for (i in c.indices) {
            if (MenuGui.isMenuItem(c[i])) e.player.inventory.setItem(i, null)
        }
        val item = e.player.inventory.getItem(8)
        if (item == null || item.type == Material.AIR) {
            e.player.inventory.setItem(8, MenuGui.menuItem)
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (e.player.gameMode == GameMode.CREATIVE) return
        // ??? (i forgot what to do)
    }

    @EventHandler
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        if (e.player.gameMode == GameMode.CREATIVE) return
        if (MenuGui.isMenuItem(e.itemDrop.itemStack)) e.itemDrop.remove()
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.whoClicked.gameMode == GameMode.CREATIVE) return
        if (MenuGui.isMenuItem(e.currentItem)) {
            e.isCancelled = true
            if (e.isRightClick) e.currentItem = null
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (MenuGui.isMenuItem(e.item)) {
            e.isCancelled = true
            if (e.action.name.startsWith("LEFT")) return
            e.player.openInventory(MenuGui.buildMenu())
        }
        if (e.item != null && Paper.itemStack(e.item!!).orCreateTag.getBoolean("vector")) {
            e.player.playSound(e.player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1F, 1F)
            e.player.velocity = Vector(e.player.location.direction.x*2, 1.5, e.player.location.direction.z*2)
        }
    }

    @EventHandler
    fun onPrepareItemCraft(e: PrepareItemCraftEvent) {
        for (`is` in e.inventory.matrix) {
            if (MenuGui.isMenuItem(`is`)) {
                e.inventory.result = null
            }
        }
    }

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            if (e.cause == EntityDamageEvent.DamageCause.VOID) {
                if (!e.entity.world.name.toLowerCase().contains("end")) e.isCancelled = true
            }
        }
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator = generator

    companion object {
        lateinit var instance: SeikatuPlugin
        private val generator = StoneWorldGenerator()
    }
}
