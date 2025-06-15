package com.kamesuta.justtimingcraft

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class Config(private val plugin: Plugin) {
    private var config: FileConfiguration = plugin.config

    /** 締め切り時間 */
    val craftTimeLimit: Int
        get() = config.getInt("craft-time-limit", 5)

    /** クラフト許可時間 */
    val craftAllowTime: Int
        get() = config.getInt("craft-allow-time", 10)

    /** 戦犯を爆死させるかどうか */
    val trollToDeath: Boolean
        get() = config.getBoolean("troll-to-death", false)

    init {
        plugin.saveDefaultConfig()
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config
    }
}