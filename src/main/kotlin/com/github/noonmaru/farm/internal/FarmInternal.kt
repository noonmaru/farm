package com.github.noonmaru.farm.internal

import com.github.noonmaru.farm.plugin.FarmPlugin
import org.bukkit.Bukkit
import org.bukkit.block.Block
import java.io.File

internal object FarmInternal {
    lateinit var io: FarmIO
    lateinit var task: CropTask
    lateinit var manager: FarmManagerImpl

    internal fun initialize(plugin: FarmPlugin) {
        io = FarmIOSQLite(plugin, File(plugin.dataFolder, "database/crops.db"))
        task = CropTask()
        manager = FarmManagerImpl()
        manager.apply {
            for (world in Bukkit.getWorlds()) {
                loadWorld(world)
            }
        }

        plugin.server.apply {
            scheduler.runTaskTimer(plugin, task, 0L, 1L)
            pluginManager.registerEvents(EventListener(manager), plugin)
        }
    }

    internal fun disable() {
        io.close()
        manager.destroy()
    }
}

internal val Block.crop: FarmCropImpl?
    get() {
        return FarmInternal.manager.getWorld(world).cropAt(x, y, z)
    }