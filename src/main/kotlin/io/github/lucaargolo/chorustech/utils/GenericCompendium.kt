package io.github.lucaargolo.chorustech.utils

import net.minecraft.util.Identifier

abstract class GenericCompendium<T: Any> {

    protected val map = mutableMapOf<Identifier, T>()

    open protected fun register(string: String, entry: T): T {
        return register(ModIdentifier(string), entry)
    }

    open protected fun register(identifier: Identifier, entry: T): T {
        map[identifier] = entry
        return entry
    }

    abstract fun initialize()

}