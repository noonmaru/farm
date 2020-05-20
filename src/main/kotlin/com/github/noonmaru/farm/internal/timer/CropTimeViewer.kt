package com.github.noonmaru.farm.internal.timer

import com.github.noonmaru.farm.internal.FarmCropImpl
import com.github.noonmaru.tap.fake.setPosition
import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.math.max

internal class CropTimeViewer(
    private val player: Player,
    private val crop: FarmCropImpl
) {
    companion object {
        private val armorStand_constructor: Constructor<*>
        private val entity_getBukkitEntity: Method
        private val world_getHandle: Method

        private var worldRef: WeakReference<Any>? = null
        private val overworld: Any
            get() {
                var world = worldRef?.get()

                if (world == null) {
                    world = world_getHandle.invoke(Bukkit.getWorlds().first())
                    worldRef = WeakReference(world)
                }

                return world!!
            }

        init {
            val nms = "net.minecraft.server.${getBukkitVersion()}"
            val craft = "org.bukkit.craftbukkit.${getBukkitVersion()}"

            val worldClass = Class.forName("$nms.World")
            val entityClass = Class.forName("$nms.Entity")
            val armorStandClass = Class.forName("$nms.EntityArmorStand")
            val craftWorldClass = Class.forName("$craft.CraftWorld")

            armorStand_constructor =
                armorStandClass.getConstructor(worldClass, Double::class.java, Double::class.java, Double::class.java)
            entity_getBukkitEntity = entityClass.getMethod("getBukkitEntity")
            world_getHandle = craftWorldClass.getMethod("getHandle")
        }

        private fun createNewArmorStand(): ArmorStand {
            val nms = armorStand_constructor.newInstance(overworld, 0.0, 0.0, 0.0)

            return entity_getBukkitEntity.invoke(nms) as ArmorStand
        }
    }

    private val armorStand = createNewArmorStand()
        .apply {
            isVisible = false
            isMarker = true
        }

    private var ticks = 0

    init {
        update(System.currentTimeMillis())
        updatePosition()
    }

    fun show() {
        val spawnMob = armorStand.run {
            EntityPacket.spawnMob(
                entityId,
                uniqueId,
                1, //ArmorStand network ID
                location,
                0.0F,
                velocity
            )
        }
        val metadata = EntityPacket.metadata(armorStand)

        player.sendServerPacket(spawnMob)
        player.sendServerPacket(metadata)
    }

    private fun updatePosition(teleport: Boolean = false) {
        val loc = crop.block.location.apply {
            add(0.5, 0.75, 0.5)
        }
        armorStand.setPosition(loc)

        if (teleport) {
            val packet = EntityPacket.teleport(
                armorStand,
                loc,
                false
            )

            player.sendServerPacket(packet)
        }
    }

    private fun isWatching(): Boolean {
        val block = player.getTargetBlockExact(5)

        return (block != null && block.x == crop.x && block.y == crop.y && block.z == crop.z)
    }

    fun update(currentTime: Long, sendPacket: Boolean = false): Boolean {
        val crop = crop
        if (!crop.isQueued || !crop.chunk.isLoaded) return false
        if (!isWatching()) return false

        val type = crop.type ?: return false
        val resultTime = crop.plantedTime + type.duration
        val remainTime = max(0L, resultTime - currentTime)
        val display = TimeFormat.format(remainTime + 999)

        if (++ticks == 2)
            armorStand.isCustomNameVisible = true

        armorStand.customName = display

        if (sendPacket) {
            player.sendServerPacket(EntityPacket.metadata(armorStand))
        }
        return true
    }

    fun remove() {
        player.sendServerPacket(EntityPacket.destroy(arrayOf(armorStand)))
    }
}

private fun getBukkitVersion(): String {
    val matcher =
        Pattern.compile("v\\d+_\\d+_R\\d+").matcher(Bukkit.getServer().javaClass.getPackage().name)

    require(matcher.find()) { "Not found bukkit version" }

    return matcher.group()
}

enum class TimeFormat(
    val displayName: String,
    val millis: Long,
    var child: TimeFormat? = null
) {
    SECOND("초", 1000L),
    MINUTE("분", SECOND.millis * 60L, SECOND),
    HOUR("시간", MINUTE.millis * 60L, MINUTE),
    DAY("일", HOUR.millis * 60L, HOUR);

    companion object {
        fun format(time: Long): String {
            val unit = getUnitByTime(time)
            val builder = StringBuilder()

            builder.append(time / unit.millis).append(unit.displayName)

            unit.child?.let { child ->
                val childTime = (time % unit.millis) / child.millis

                if (childTime > 0) {
                    builder.append(' ').append(childTime).append(child.displayName)
                }
            }

            return builder.toString()
        }

        private fun getUnitByTime(time: Long): TimeFormat {
            return when {
                time >= DAY.millis -> DAY
                time >= HOUR.millis -> HOUR
                time >= MINUTE.millis -> MINUTE
                else -> SECOND
            }
        }
    }
}



