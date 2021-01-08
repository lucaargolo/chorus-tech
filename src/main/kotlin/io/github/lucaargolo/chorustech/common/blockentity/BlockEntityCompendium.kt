package io.github.lucaargolo.chorustech.common.blockentity

import io.github.lucaargolo.chorustech.common.block.BlockCompendium
import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity
import io.github.lucaargolo.chorustech.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val DUCT_TYPE = register("duct", BlockEntityType.Builder.create( { DuctBlockEntity() }, BlockCompendium.IRON_DUCT ).build(null))

}