package io.github.lucaargolo.chorustech.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction

class MovingStack(val stack: ItemStack, val from: Direction, val to: Direction, var progress: Double = 0.0) {

    var lastRenderProgress = 0.0

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.put("stack", stack.toTag(CompoundTag()))
        tag.putString("from", from.name)
        tag.putString("to", to.name)
        tag.putDouble("progress", progress)
        return tag
    }

    companion object {

        fun fromTag(tag: CompoundTag): MovingStack {
            val stack = ItemStack.fromTag(tag.getCompound("stack"))
            val from = Direction.valueOf(tag.getString("from"))
            val to = Direction.valueOf(tag.getString("to"))
            val movingStack = MovingStack(stack, from, to)
            movingStack.progress = tag.getDouble("progress")
            return movingStack
        }

    }
}

