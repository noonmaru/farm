package com.github.noonmaru.farm.plugin

import com.github.noonmaru.farm.CropType
import com.github.noonmaru.farm.Farm
import com.github.noonmaru.farm.command.FarmCommand
import com.github.noonmaru.farm.internal.FarmInternal
import com.github.noonmaru.kommand.kommand
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * @author Nemo
 */
class FarmPlugin : JavaPlugin() {
    override fun onEnable() {
        CropType.load(File(dataFolder, "crops-config.yml"))

        for (type in CropType.types) {
            println("${type.name} = ${type.duration}")
        }


        FarmInternal.initialize(this)
        Farm.manager = FarmInternal.manager

        kommand {
            register("farm") {
                FarmCommand.register(this)
            }
        }
    }

    override fun onDisable() {
        FarmInternal.disable()
    }
}