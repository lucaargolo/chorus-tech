package io.github.lucaargolo.chorustech.utils

import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

fun VoxelShape.rotateShape(to: Direction) = rotateShape(Direction.NORTH, to)

fun VoxelShape.rotateShape(from: Direction, to: Direction): VoxelShape {
    var oldShape: VoxelShape = this
    var newShape: VoxelShape = VoxelShapes.empty()
    if(to.horizontal != -1) {
        val times: Int = (to.horizontal - from.horizontal + 4) % 4
        repeat(times) {
            oldShape.forEachBox { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
                newShape = VoxelShapes.union(newShape, VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
            }
            oldShape = newShape
            newShape = VoxelShapes.empty()
        }
    }else{
        repeat(if(to == Direction.UP) 1 else 3) {
            oldShape.forEachBox { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
                newShape = VoxelShapes.union(newShape, VoxelShapes.cuboid(minX, 1 - maxZ, minY, maxX, 1-minZ, maxY))
            }
            oldShape = newShape
            newShape = VoxelShapes.empty()
        }
    }
    return oldShape
}
