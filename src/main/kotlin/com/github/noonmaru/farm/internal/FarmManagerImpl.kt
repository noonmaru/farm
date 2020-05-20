package com.github.noonmaru.farm.internal

import com.github.noonmaru.farm.FarmManager
import com.github.noonmaru.farm.internal.timer.CropTimeViewer
import com.google.common.collect.ImmutableList
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

class FarmManagerImpl : FarmManager {
    private val worldsByBukkitWorld = IdentityHashMap<World, FarmWorldImpl>()

    override val worlds: List<FarmWorldImpl>
        get() = ImmutableList.copyOf(worldsByBukkitWorld.values)

    internal val timeViewers = WeakHashMap<Player, CropTimeViewer>()

    override fun getWorld(bukkitWorld: World): FarmWorldImpl {
        return requireNotNull(worldsByBukkitWorld[bukkitWorld]) { "Unregistered world" }
    }

    internal fun loadWorld(world: World) {
        worldsByBukkitWorld.computeIfAbsent(world) {
            val farmWorld = FarmWorldImpl(world.name, it)
            FarmInternal.io.saveWorld(farmWorld)

            for (chunk in world.loadedChunks) {
                farmWorld.loadChunk(chunk)
            }

            farmWorld
        }
    }

    internal fun unloadWorld(world: World) {
        worldsByBukkitWorld.remove(world)?.destroy()
    }

    internal fun addTimer(player: Player, crop: FarmCropImpl) {
        timeViewers.put(player, CropTimeViewer(player, crop).apply { show() })?.remove()
    }

    internal fun removeTimer(player: Player) {
        timeViewers.remove(player)?.remove()
    }

    internal fun destroy() {
        for (world in worlds) {
            world.destroy()
        }
        worldsByBukkitWorld.clear()

        for (viewer in timeViewers.values) {
            viewer.remove()
        }
        timeViewers.clear()
    }
}