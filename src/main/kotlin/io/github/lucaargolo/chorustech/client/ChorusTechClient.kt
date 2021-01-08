package io.github.lucaargolo.chorustech.client

import io.github.lucaargolo.chorustech.client.render.bakedmodel.BakedModelCompendium
import io.github.lucaargolo.chorustech.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.chorustech.common.block.BlockCompendium
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.render.RenderLayer

class ChorusTechClient: ClientModInitializer {

    override fun onInitializeClient() {
        BakedModelCompendium.initialize()
        BlockEntityRendererCompendium.initialize()
        BlockRenderLayerMap.INSTANCE.putBlock(BlockCompendium.IRON_DUCT, RenderLayer.getTranslucent())
    }

}