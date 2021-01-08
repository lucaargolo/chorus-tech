package io.github.lucaargolo.chorustech.client.render.bakedmodel

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.chorustech.client.render.bakedmodel.ducts.DuctBakedModel
import io.github.lucaargolo.chorustech.utils.GenericCompendium
import io.github.lucaargolo.chorustech.utils.ModIdentifier
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.function.Function

object BakedModelCompendium: GenericCompendium<BakedModel>() {

    val DUCT_MODEL = register(ModelIdentifier(ModIdentifier("iron_item_duct"), ""), DuctBakedModel())

    override fun initialize() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelIdentifier, _ ->
                map.forEach { (identifier, model) ->
                    val equals = if(identifier is ModelIdentifier) {
                        identifier.namespace == modelIdentifier.namespace && identifier.path == modelIdentifier.path && identifier.variant == modelIdentifier.variant
                    } else {
                        identifier.namespace == modelIdentifier.namespace && identifier.path == modelIdentifier.path
                    }
                    if(equals) {
                        return@ModelVariantProvider (model as? UnbakedModel) ?: object : UnbakedModel {
                            override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                            override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationScreenHandler: ModelBakeSettings, modelId: Identifier) = model
                            override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>?, unresolvedTextureReferences: MutableSet<Pair<String, String>>?): MutableCollection<SpriteIdentifier> = mutableListOf()
                        }
                    }
                }
                return@ModelVariantProvider null
            }
        }
    }

}