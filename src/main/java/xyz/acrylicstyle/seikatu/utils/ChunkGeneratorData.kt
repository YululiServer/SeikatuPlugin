package xyz.acrylicstyle.seikatu.utils

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator.BiomeGrid
import util.Validate

class ChunkGeneratorData(world: World, val chunkX: Int, val chunkZ: Int, biome: BiomeGrid) {
    val world: World = Validate.notNull(world, "world cannot be null")
    val biome: BiomeGrid = Validate.notNull(biome, "biome cannot be null")
}
