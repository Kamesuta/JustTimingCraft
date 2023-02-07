package com.kamesuta.justtimingcraft

import net.kunmc.lab.configlib.BaseConfig
import net.kunmc.lab.configlib.value.BooleanValue
import net.kunmc.lab.configlib.value.IntegerValue
import org.bukkit.plugin.Plugin

class Config(plugin: Plugin) : BaseConfig(plugin) {
    /** 締め切り時間 */
    val craftTimeLimit = IntegerValue(5)

    /** クラフト許可時間 */
    val craftAllowTime = IntegerValue(10)

    /** 戦犯を爆死させるかどうか */
    val trollToDeath = BooleanValue(false)
}