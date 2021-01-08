package io.github.lucaargolo.chorustech.common.item.addons

import net.minecraft.item.Item

class DuctAddon(type: Type, size: Int, speed: Double, settings: Settings): Item(settings) {

    enum class Type {
        FILTER,
        EXTRACTOR,
        RETRIEVER
    }

}