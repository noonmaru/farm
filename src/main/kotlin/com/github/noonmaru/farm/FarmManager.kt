package com.github.noonmaru.farm

import org.bukkit.World

interface FarmManager {
    val worlds: List<FarmWorld>

    fun getWorld(bukkitWorld: World): FarmWorld
}