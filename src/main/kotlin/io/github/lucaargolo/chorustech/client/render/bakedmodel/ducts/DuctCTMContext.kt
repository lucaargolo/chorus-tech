package io.github.lucaargolo.chorustech.client.render.bakedmodel.ducts

import io.github.lucaargolo.chorustech.ChorusTech
import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class DuctCTMContext {

    class UV(var minU: Float, var minV: Float, var maxU: Float, var maxV: Float)

    private val rotationAngleMap: LinkedHashMap<Direction, Float> = linkedMapOf()
    private val uvMap: LinkedHashMap<Direction, UV> = linkedMapOf()

    init {
        Direction.values().forEach {
            rotationAngleMap[it] = 0f
            uvMap[it] = UV(0f, 0f, 16f, 16f)
        }
    }

    fun getRotationAngle(face: Direction) = rotationAngleMap[face]!!
    fun getUV(face: Direction) = uvMap[face]!!

    companion object {

        private val map = linkedMapOf<BlockPos, DuctCTMContext>()

        private fun Boolean.toInt() = if(this) 1 else 0

        fun getOrCreate(entity: DuctBlockEntity): DuctCTMContext {
            if(map.contains(entity.pos))
                return map[entity.pos]!!

            val texture = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier(ChorusTech.MOD_ID, "block/iron_item_duct"))
            val cache = DuctCTMContext()

            Direction.values().forEach { face ->

                val bl1 = when (face) {
                    Direction.UP, Direction.DOWN -> entity.connectionsMap[Direction.NORTH] != null
                    Direction.EAST, Direction.WEST -> entity.connectionsMap[Direction.UP] != null
                    Direction.NORTH, Direction.SOUTH -> entity.connectionsMap[Direction.EAST] != null
                }
                val bl2 = when (face) {
                    Direction.UP, Direction.DOWN -> entity.connectionsMap[Direction.SOUTH] != null
                    Direction.EAST, Direction.WEST -> entity.connectionsMap[Direction.DOWN] != null
                    Direction.NORTH, Direction.SOUTH -> entity.connectionsMap[Direction.WEST] != null
                }
                val bl3 = when (face) {
                    Direction.UP, Direction.DOWN -> entity.connectionsMap[Direction.EAST] != null
                    Direction.EAST, Direction.WEST -> entity.connectionsMap[Direction.NORTH] != null
                    Direction.NORTH, Direction.SOUTH -> entity.connectionsMap[Direction.UP] != null
                }
                val bl4 = when (face) {
                    Direction.UP, Direction.DOWN -> entity.connectionsMap[Direction.WEST] != null
                    Direction.EAST, Direction.WEST -> entity.connectionsMap[Direction.SOUTH] != null
                    Direction.NORTH, Direction.SOUTH -> entity.connectionsMap[Direction.DOWN] != null
                }

                when (bl1.toInt() + bl2.toInt() + bl3.toInt() + bl4.toInt()) {
                    0 -> {
                        cache.uvMap[face]!!.maxU -= 10
                        cache.uvMap[face]!!.maxV -= 10
                    }
                    1 -> {
                        cache.uvMap[face]!!.minU += 5
                        cache.uvMap[face]!!.maxU -= 5
                        cache.uvMap[face]!!.maxV -= 10
                        cache.rotationAngleMap[face] = when (face) {
                            Direction.EAST -> bl1.toInt() * 90f + bl2.toInt() * 270f + bl3.toInt() * 0f + bl4.toInt() * 180f
                            Direction.WEST -> bl1.toInt() * 90f + bl2.toInt() * 270f + bl3.toInt() * 180f + bl4.toInt() * 0f
                            Direction.SOUTH -> bl1.toInt() * 0f + bl2.toInt() * 180f + bl3.toInt() * 90f + bl4.toInt() * 270f
                            Direction.NORTH -> bl1.toInt() * 180f + bl2.toInt() * 0f + bl3.toInt() * 90f + bl4.toInt() * 270f
                            Direction.UP -> bl1.toInt() * 90f + bl2.toInt() * 270f + bl3.toInt() * 0f + bl4.toInt() * 180f
                            Direction.DOWN -> bl1.toInt() * 270f + bl2.toInt() * 90f + bl3.toInt() * 0f + bl4.toInt() * 180f
                        }
                    }
                    2 -> {
                        if (bl1 && bl2 || bl3 && bl4) {
                            cache.uvMap[face]!!.minU += 10
                            cache.uvMap[face]!!.maxV -= 10
                            cache.rotationAngleMap[face] = when (face) {
                                Direction.EAST, Direction.WEST -> bl2.toInt() * 90f
                                Direction.NORTH, Direction.SOUTH -> bl4.toInt() * 90f
                                Direction.UP, Direction.DOWN -> bl2.toInt() * 90f
                            }
                        } else {
                            cache.uvMap[face]!!.minU += 5
                            cache.uvMap[face]!!.minV += 5
                            cache.uvMap[face]!!.maxU -= 5
                            cache.uvMap[face]!!.maxV -= 5
                            cache.rotationAngleMap[face] = when (face) {
                                Direction.EAST -> (bl1 && bl3).toInt() * 180f + (bl1 && bl4).toInt() * 270f + (bl2 && bl3).toInt() * 90f + (bl2 && bl4).toInt() * 0f
                                Direction.WEST -> (bl1 && bl3).toInt() * 270f + (bl1 && bl4).toInt() * 180f + (bl2 && bl3).toInt() * 0f + (bl2 && bl4).toInt() * 90f
                                Direction.SOUTH -> (bl1 && bl3).toInt() * 180f + (bl1 && bl4).toInt() * 90f + (bl2 && bl3).toInt() * 270f + (bl2 && bl4).toInt() * 0f
                                Direction.NORTH -> (bl1 && bl3).toInt() * 270f + (bl1 && bl4).toInt() * 0f + (bl2 && bl3).toInt() * 180f + (bl2 && bl4).toInt() * 90f
                                Direction.UP -> (bl1 && bl3).toInt() * 180f + (bl1 && bl4).toInt() * 270f + (bl2 && bl3).toInt() * 90f + (bl2 && bl4).toInt() * 0f
                                Direction.DOWN -> (bl1 && bl3).toInt() * 90f + (bl1 && bl4).toInt() * 0f + (bl2 && bl3).toInt() * 180f + (bl2 && bl4).toInt() * 270f
                            }
                        }
                    }
                    3 -> {
                        cache.uvMap[face]!!.minV += 5
                        cache.uvMap[face]!!.maxV -= 5
                        cache.uvMap[face]!!.maxU -= 10
                        cache.rotationAngleMap[face] = when (face) {
                            Direction.EAST -> (bl1 && bl2 && bl3).toInt() * 90f + (bl1 && bl2 && bl4).toInt() * 270f + (bl2 && bl3 && bl4).toInt() * 0f + (bl1 && bl4 && bl3).toInt() * 180f
                            Direction.WEST -> (bl1 && bl2 && bl3).toInt() * 270f + (bl1 && bl2 && bl4).toInt() * 90f + (bl2 && bl3 && bl4).toInt() * 0f + (bl1 && bl4 && bl3).toInt() * 180f
                            Direction.SOUTH -> (bl1 && bl2 && bl3).toInt() * 180f + (bl1 && bl2 && bl4).toInt() * 0f + (bl2 && bl3 && bl4).toInt() * 270f + (bl1 && bl4 && bl3).toInt() * 90f
                            Direction.NORTH -> (bl1 && bl2 && bl3).toInt() * 180f + (bl1 && bl2 && bl4).toInt() * 0f + (bl2 && bl3 && bl4).toInt() * 90f + (bl1 && bl4 && bl3).toInt() * 270f
                            Direction.UP -> (bl1 && bl2 && bl3).toInt() * 90f + (bl1 && bl2 && bl4).toInt() * 270f + (bl2 && bl3 && bl4).toInt() * 0f + (bl1 && bl4 && bl3).toInt() * 180f
                            Direction.DOWN -> (bl1 && bl2 && bl3).toInt() * 90f + (bl1 && bl2 && bl4).toInt() * 270f + (bl2 && bl3 && bl4).toInt() * 180f + (bl1 && bl4 && bl3).toInt() * 0f
                        }
                    }
                    4 -> {
                        cache.uvMap[face]!!.minV += 10
                        cache.uvMap[face]!!.maxU -= 10
                    }
                }
            }

            map[entity.pos] = cache
            return cache
        }

        fun delete(entity: DuctBlockEntity) = map.remove(entity.pos)
    }

}
