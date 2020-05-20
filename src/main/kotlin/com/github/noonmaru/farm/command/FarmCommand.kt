package com.github.noonmaru.farm.command

import com.github.noonmaru.farm.CropType
import com.github.noonmaru.kommand.KommandBuilder
import com.github.noonmaru.kommand.KommandSyntaxException
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

object FarmCommand {
    internal fun register(builder: KommandBuilder) {
        builder.then("animate") {
            require { this is Player }
            executes { context ->
                animate(context.sender as Player)
            }
        }
    }

    private fun animate(sender: Player) {
        val inv = sender.inventory
        val item = inv.itemInMainHand
        val itemType = item.type
        val cropType = CropType.getBySeedType(itemType) ?: throw KommandSyntaxException("씨앗 혹은 묘목을 들고 시도해주세요.")

        val meta = item.itemMeta
        if (meta.hasEnchant(Enchantment.DURABILITY)) return

        meta.addEnchant(Enchantment.DURABILITY, 1, true) //심을 수 있는 인챈트
        item.itemMeta = meta
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        inv.setItemInMainHand(item)
        sender.sendMessage("${cropType.name} 씨앗을 인챈트했습니다.")
    }
}