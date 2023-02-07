package com.kamesuta.justtimingcraft

import net.kunmc.lab.commandlib.Command
import net.kunmc.lab.commandlib.CommandLib
import net.kunmc.lab.configlib.ConfigCommandBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.util.*


class JustTimingCraft : JavaPlugin(), Listener {
    private var craftingItem: ItemStack? = null
    private val craftingPlayerList = mutableSetOf<UUID>()
    private var craftAllowed = false
    private var craftingTask: BukkitTask? = null

    private lateinit var config: Config

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        config = Config(this)
        // コンフィグ
        val configCommand = ConfigCommandBuilder(config).build()
        CommandLib.register(this, object : Command("justtiming") {
            init {
                addChildren(configCommand)
            }
        })
        // イベントリスナー
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    fun onCraft(event: CraftItemEvent) {
        // プレイヤーを取得
        val player = event.whoClicked
        if (player !is Player) return

        // クラフト許可タイム中はクラフトを許可
        if (craftAllowed) return

        if (player.hasPermission("justtimingcraft.craft")) {
            // 既にクラフト中のアイテムがある場合は何もしない
            if (craftingItem != null) {
                event.isCancelled = true
                return
            }
            // クラフト中のアイテムをセット
            craftingItem = event.recipe.result
            // リーダーがクラフトしようとしているとタイトルで全員にアナウンス
            sendTitle(
                Component.text("50人クラフト！").color(NamedTextColor.LIGHT_PURPLE),
                Component.text()
                    .append(Component.text("みんなで"))
                    .append(event.recipe.result.displayName().color(NamedTextColor.GREEN))
                    .append(Component.text("をクラフトしましょう！"))
                    .build(),
            )
            sendChat(
                Component.text()
                    .append(Component.text("50人クラフトタイム！ みんなで"))
                    .append(event.recipe.result.displayName().color(NamedTextColor.GREEN))
                    .append(Component.text("をクラフトしよう！"))
                    .build()
            )
            // 全員のチェックマークを☓にする
            server.onlinePlayers.forEach {
                it.setCheckmark(false)
            }
            // リーダーのチェックマーク
            player.setCheckmark(true)
            // タイマーをセット
            runLater(config.craftTimeLimit.longValue() * 20) {
                craftAllowed = false
                // タイマーが終わったらタイトルで全員にアナウンス
                sendTitle(
                    Component.text("50人クラフト失敗").color(NamedTextColor.RED),
                    Component.text()
                        .append(Component.text("残念、みんなの息が合いませんでした。"))
                        .build()
                )
                sendChat(
                    Component.text()
                        .append(event.recipe.result.displayName().color(NamedTextColor.GREEN))
                        .append(Component.text("の50人クラフト失敗！ 戦犯はTABで確認できます"))
                        .build()
                )
                reset()
            }
            event.isCancelled = true
        } else {
            // リーダーじゃない場合
            if (craftingItem != event.recipe.result) {
                if (craftingItem == null) {
                    player.sendChat(Component.text("まだクラフトが始まっていません"))
                } else {
                    player.sendChat(Component.text("みんながクラフトしているアイテムと違います"))
                }
                event.isCancelled = true
                return
            }

            // クラフトできない状態の時
            if (!craftingPlayerList.contains(player.uniqueId)) {
                // クラフト中のアイテムをクラフトしたらクラフトリストに追加
                craftingPlayerList.add(player.uniqueId)
                // クラフト連打！
                player.sendChat(Component.text("50人クラフトに参加した！息が揃うまでクラフト連打しよう！"))
            }
            // チェックマークをつける
            player.setCheckmark(true)

            // 全員クラフトした
            val totalPlayerCount = server.onlinePlayers.count { it.gameMode == GameMode.SURVIVAL }
            if (craftingPlayerList.size >= totalPlayerCount) {
                // タイトルで全員にアナウンス
                sendTitle(
                    Component.text("50人クラフト成功").color(NamedTextColor.GREEN),
                    Component.text()
                        .append(Component.text("50人の息が揃った！"))
                        .build()
                )
                sendChat(
                    Component.text()
                        .append(Component.text("50人クラフト成功！今から${config.craftAllowTime.longValue()}秒間だけ"))
                        .append(event.recipe.result.displayName().color(NamedTextColor.GREEN))
                        .append(Component.text("を好きなだけクラフトできます"))
                        .build()
                )
                // タイマーをセット
                runLater(config.craftAllowTime.longValue() * 20) {
                    // ステートをリセット
                    reset()
                    // チェックマークを消す
                    server.onlinePlayers.forEach {
                        it.setCheckmark(null)
                    }
                }
                craftAllowed = true
                return
            }
            event.isCancelled = true
        }
    }

    /**
     * 全体タイトルを送る
     */
    private fun sendTitle(title: Component, subtitle: Component) {
        server.onlinePlayers.forEach {
            it.showTitle(
                Title.title(
                    title,
                    subtitle,
                    Title.Times.of(Duration.ZERO, Duration.ofMillis(3000), Duration.ofMillis(2000))
                )
            )
        }
    }

    /**
     * チャットに送る
     */
    private fun Player.sendChat(text: Component) {
        sendMessage(
            Component.text("")
                .append(Component.text("◆ ").color(NamedTextColor.GRAY))
                .append(text)
        )
    }

    /**
     * 全体チャットに送る
     */
    private fun sendChat(text: Component) {
        server.broadcast(
            Component.text("")
                .append(Component.text("◆ ").color(NamedTextColor.GRAY))
                .append(text)
        )
    }

    /**
     * プレイヤーのチェックマークを設定する
     */
    private fun Player.setCheckmark(isDone: Boolean?) {
        val status = when (isDone) {
            true -> Component.text("✓").color(NamedTextColor.GOLD)
            false -> Component.text("✗").color(NamedTextColor.GRAY)
            else -> {
                playerListName(null)
                return
            }
        }
        val name = customName() ?: Component.text(name)
        playerListName(Component.text().append(status).append(name).build())
    }

    /**
     * 遅延実行
     */
    private fun runLater(delay: Long, f: () -> Unit) {
        craftingTask?.cancel()
        craftingTask = server.scheduler.runTaskLater(this, f, delay)
    }

    /**
     * クラフト状態をリセットする
     */
    private fun reset() {
        craftingTask?.cancel()
        craftingTask = null
        craftingItem = null
        craftingPlayerList.clear()
        craftAllowed = false
    }

    companion object {
        /** インスタンス */
        lateinit var instance: JustTimingCraft
    }
}