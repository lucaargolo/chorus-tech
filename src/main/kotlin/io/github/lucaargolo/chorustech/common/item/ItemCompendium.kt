package io.github.lucaargolo.chorustech.common.item

import io.github.lucaargolo.chorustech.common.block.BlockCompendium
import io.github.lucaargolo.chorustech.common.item.addons.DuctAddon
import io.github.lucaargolo.chorustech.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ItemCompendium: RegistryCompendium<Item>(Registry.ITEM) {

    private val addonList = mutableListOf<Identifier>()

    val IRON_EXTRACTOR = register("iron_extractor", DuctAddon(DuctAddon.Type.EXTRACTOR, 8, 4.0, Settings()))
    val IRON_FILTER = register("iron_filter", DuctAddon(DuctAddon.Type.EXTRACTOR, 8, 4.0, Settings()))
    val IRON_RETRIEVER = register("iron_retriever", DuctAddon(DuctAddon.Type.EXTRACTOR, 8, 4.0, Settings()))

    override fun register(identifier: Identifier, entry: Item): Item {
        if(entry is DuctAddon) {
            addonList.add(identifier)
        }
        return super.register(identifier, entry)
    }

    override fun initialize() {
        BlockCompendium.registerBlockItems(map)
        ModelLoadingRegistry.INSTANCE.registerAppender { _, out ->
            addonList.forEach { out.accept(ModelIdentifier(it, "")) }
        }
        super.initialize()
    }
}