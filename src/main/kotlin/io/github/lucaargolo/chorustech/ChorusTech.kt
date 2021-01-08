package io.github.lucaargolo.chorustech

import io.github.lucaargolo.chorustech.common.block.BlockCompendium
import io.github.lucaargolo.chorustech.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.chorustech.common.item.ItemCompendium
import net.fabricmc.api.ModInitializer

class ChorusTech: ModInitializer {

    override fun onInitialize() {
        BlockCompendium.initialize()
        BlockEntityCompendium.initialize()
        ItemCompendium.initialize()
    }

    companion object {
        const val MOD_ID = "chorustech"
    }

}