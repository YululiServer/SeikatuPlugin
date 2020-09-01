package xyz.acrylicstyle.seikatu.utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import util.reflect.Ref
import xyz.acrylicstyle.paper.Paper
import xyz.acrylicstyle.seikatu.SeikatuPlugin
import xyz.acrylicstyle.tomeito_api.gui.ClickableItem
import xyz.acrylicstyle.tomeito_api.gui.GuiBuilder
import xyz.acrylicstyle.tomeito_api.gui.impl.SimpleGuiBuilder
import java.util.*
import java.util.function.Consumer

object MenuGui {
    private val black = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    val menuItem: ItemStack
        get() {
            val item = ItemStack(Material.NETHER_STAR)
            val meta = item.itemMeta
            meta.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + "メニュー")
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
            meta.lore = listOf(
                ChatColor.LIGHT_PURPLE.toString() + "アイテムを持った状態で右クリックでメニューを開けます。",
                "",
                ChatColor.LIGHT_PURPLE.toString() + "アイテムを持った状態で左クリックで",
                ChatColor.LIGHT_PURPLE.toString() + "資源ワールドの選択画面を開けます。",
                "",
                ChatColor.GOLD.toString() + "インベントリでメニューのアイテムにカーソルを合わせて",
                ChatColor.YELLOW.toString() + "右クリック" + ChatColor.LIGHT_PURPLE + "すると、アイテムを削除できます。",
                "",
                ChatColor.GOLD.toString() + "削除されても" + ChatColor.YELLOW + "/menu",
                ChatColor.GOLD.toString() + "を使用してメニューを開くことができます。"
            )
            item.itemMeta = meta
            val util = Paper.itemStack(item)
            val tag =
                (if (util.hasTag()) util.tag else util.orCreateTag) ?: throw NullPointerException("tag is null")
            tag.setBoolean("menu", true)
            util.tag = tag
            return util.itemStack
        }

    fun isMenuItem(itemStack: ItemStack?): Boolean {
        if (itemStack == null || itemStack.type != Material.NETHER_STAR) return false
        val util = Paper.itemStack(itemStack)
        val tag =
            (if (util.hasTag()) util.tag else util.orCreateTag) ?: throw NullPointerException("tag is null")
        return tag.hasKey("menu") && tag.getBoolean("menu")
    }

    private var inventoryCache: Inventory? = null
    fun buildMenu(): Inventory {
        if (inventoryCache != null) return inventoryCache as Inventory
        val builder = GuiBuilder.newInstance("メニュー", 54).register(SeikatuPlugin.instance)
        @Suppress("UNCHECKED_CAST") val clickEvents =
            Ref.getDeclaredField(
                SimpleGuiBuilder::class.java, "clickEvents"
            ).accessible(true)[builder as SimpleGuiBuilder] as MutableMap<Int, Consumer<InventoryClickEvent>>
        for (i in 0..53) clickEvents[i] = Consumer { e: InventoryClickEvent -> e.isCancelled = true }
        builder.setItem(
                11,
                ClickableItem.of(
                        Material.COMPASS,
                        1,
                        ChatColor.AQUA.toString() + "サーバー選択",
                        ArrayList()
                ) { e ->
                    e.isCancelled = true
                    (e.whoClicked as Player).chat("/servers")
                }
        )
        builder.setItem(
            12,
            ClickableItem.of(
                Material.GRASS_BLOCK,
                1,
                ChatColor.DARK_GREEN.toString() + "資源ワールド",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/sigen")
            }
        )
        builder.setItem(
            13,
            ClickableItem.of(
                Material.NETHER_STAR,
                1,
                ChatColor.GREEN.toString() + "実績一覧",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/a")
            }
        )
        builder.setItem(
            14,
            ClickableItem.of(
                Material.ENCHANTED_GOLDEN_APPLE,
                1,
                ChatColor.LIGHT_PURPLE.toString() + "報酬一覧",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/rewards")
            }
        )
        builder.setItem(
            21,
            ClickableItem.of(
                Material.CHEST,
                1,
                ChatColor.GREEN.toString() + "ポイントショップ",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/shop")
            }
        )
        builder.setItem(
            22,
            ClickableItem.of(
                Material.BLAZE_POWDER,
                1,
                ChatColor.GOLD.toString() + "パーティクル選択",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/pp")
            }
        )
        builder.setItem(
            23,
            ClickableItem.of(
                Material.PAINTING,
                1,
                ChatColor.GREEN.toString() + "絵画ショップ",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/photoshop")
            }
        )
        builder.setItem(
            30,
            ClickableItem.of(
                Material.OAK_PLANKS,
                1,
                ChatColor.GREEN.toString() + "Homeに戻る",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/home")
                close(e.whoClicked)
            }
        )
        builder.setItem(
            31,
            ClickableItem.of(
                Material.BEDROCK,
                1,
                ChatColor.GREEN.toString() + "スポーン地点に戻る",
                listOf("左クリック: メイン拠点", "右クリック: 第2拠点")
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                if (e.isRightClick) {
                    if (!e.whoClicked.hasPermission("simplecommands.spawn") && !e.whoClicked.location
                            .world.name.equals("world", ignoreCase = true)
                    ) {
                        e.whoClicked.sendMessage(ChatColor.RED.toString() + "このワールドでは使用できません。")
                        return@of
                    }
                    e.whoClicked
                        .teleport(Location(Bukkit.getWorld("world")!!, -499.5, 67.1, -498.5))
                } else {
                    (e.whoClicked as Player).chat("/spawn")
                }
                close(e.whoClicked)
            }
        )
        builder.setItem(
            32,
            ClickableItem.of(
                Material.OAK_LOG,
                1,
                ChatColor.GREEN.toString() + "Homeを設定する",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                (e.whoClicked as Player).chat("/sethome")
                close(e.whoClicked)
            }
        )
        //builder.setItem(14, ClickableItem.of(Material.GRASS_BLOCK, 1, ChatColor.GREEN + "Storage Box", new ArrayList<>(), e -> ((Player) e.getWhoClicked()).chat("/storage gui"))); // gui isn't implemented
        builder.setItem(
            49,
            ClickableItem.of(
                Material.BARRIER,
                1,
                ChatColor.RED.toString() + "閉じる",
                ArrayList()
            ) { e: InventoryClickEvent ->
                e.isCancelled = true
                close(e.whoClicked)
            }
        )
        val inventory = builder.getInventory()
        @Suppress("UNCHECKED_CAST") val contents: Array<ItemStack?> = inventory.contents as Array<ItemStack?>
        for (i in contents.indices) if (contents[i] == null || contents[i]!!
                .type == Material.AIR
        ) contents[i] = black
        @Suppress("UNCHECKED_CAST")
        inventory.contents = contents as Array<ItemStack>
        inventoryCache = inventory
        return inventory
    }

    private fun close(entity: HumanEntity) {
        object : BukkitRunnable() {
            override fun run() {
                entity.closeInventory()
            }
        }.runTask(SeikatuPlugin.instance)
    }

    init {
        val meta = black.itemMeta
        meta.setDisplayName(" ")
        black.itemMeta = meta
    }
}
