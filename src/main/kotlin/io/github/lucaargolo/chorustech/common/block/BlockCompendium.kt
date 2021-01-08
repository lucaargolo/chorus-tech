package io.github.lucaargolo.chorustech.common.block

import io.github.lucaargolo.chorustech.common.block.ducts.Duct
import io.github.lucaargolo.chorustech.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val IRON_DUCT = register("iron_item_duct", Duct(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), 4.0, 8))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = BlockItem(block, Item.Settings())
        }
    }

}