package io.github.lucaargolo.chorustech.utils

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

fun areStacksCompatible(left: ItemStack, right: ItemStack): Boolean {
    return ItemStack.areItemsEqual(left, right) && ItemStack.areTagsEqual(left, right)
}

fun Inventory.tryToInsert(stack: ItemStack): ItemStack {
    (0 until size()).forEach {
        if(stack.isEmpty) return ItemStack.EMPTY
        val storedStack = getStack(it)
        if(storedStack.isEmpty) {
            setStack(it, stack)
            return ItemStack.EMPTY
        }else{
            if(areStacksCompatible(stack, storedStack)) {
                if(storedStack.count + stack.count <= stack.maxCount) {
                    storedStack.count += stack.count
                    return ItemStack.EMPTY
                }else{
                    val availableSpace = storedStack.maxCount-storedStack.count
                    storedStack.count += availableSpace
                    stack.count -= availableSpace
                }
            }
        }
    }
    return stack
}
