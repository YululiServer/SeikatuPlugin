package xyz.acrylicstyle.seikatu.generators.stone

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.ChunkGenerator
import xyz.acrylicstyle.seikatu.utils.ChunkGeneratorData
import java.util.*
import java.util.function.Function

class StoneWorldGenerator : ChunkGenerator() {
    override fun isParallelCapable(): Boolean {
        return true
    }

    companion object {
        private val function: Function<ChunkGeneratorData, ChunkData>

        init {
            function =
                Function { data: ChunkGeneratorData ->
                    val chunk =
                        Bukkit.createChunkData(data.world)
                    val biome: BiomeGrid = data.biome
                    val cond =
                        data.chunkX == 0 && data.chunkZ == 0
                    for (x in 0..15) {
                        for (y in 0..254) {
                            for (z in 0..15) {
                                biome.setBiome(x, y, z, Biome.DESERT)
                                if (cond) {
                                    if (x <= 4 && z <= 4 && y >= 70 && y < 80) {
                                        if (y == 70) {
                                            chunk.setBlock(
                                                x,
                                                y,
                                                z,
                                                Material.BEDROCK
                                            )
                                        } else {
                                            if ((x == 4 || z == 4 || x == 0 || z == 0) && y == 71) {
                                                chunk.setBlock(
                                                    x,
                                                    y,
                                                    z,
                                                    Material.TORCH
                                                )
                                            } else {
                                                chunk.setBlock(
                                                    x,
                                                    y,
                                                    z,
                                                    Material.AIR
                                                )
                                            }
                                        }
                                    } else {
                                        chunk.setBlock(
                                            x,
                                            y,
                                            z,
                                            Material.STONE
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!cond) chunk.setRegion(
                        0,
                        0,
                        0,
                        16,
                        255,
                        16,
                        Material.STONE
                    )
                    chunk
                }
        }
    }

    override fun generateChunkData(
        world: World,
        random: Random,
        cx: Int,
        cz: Int,
        biome: BiomeGrid
    ): ChunkData {
        return function.apply(ChunkGeneratorData(world, cx, cz, biome))
    }

    override fun getFixedSpawnLocation(world: World, random: Random): Location? {
        return Location(world, 2.5, 70.0, 2.5)
    }
}
