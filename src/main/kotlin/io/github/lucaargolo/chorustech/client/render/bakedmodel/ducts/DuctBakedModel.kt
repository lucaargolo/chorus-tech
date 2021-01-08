package io.github.lucaargolo.chorustech.client.render.bakedmodel.ducts

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity
import io.github.lucaargolo.chorustech.common.item.ItemCompendium
import io.github.lucaargolo.chorustech.common.item.addons.DuctAddon
import io.github.lucaargolo.chorustech.utils.ModIdentifier
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.*
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class DuctBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val modelIdList = listOf(
            ModIdentifier("block/iron_item_duct"),
            ModIdentifier("block/iron_item_duct_cable_l"),
            ModIdentifier("block/iron_item_duct_cable_s"),
            ModIdentifier("block/iron_item_duct_connection")
    )
    private val modelList = mutableListOf<BakedModel?>()
    private val spriteIdList = mutableListOf(
            SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/iron_item_duct")),
            SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/iron_connection"))
    )
    private val spriteList = mutableListOf<Sprite>()

    override fun getModelDependencies(): Collection<Identifier> = modelIdList

    override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>) = spriteIdList

    override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel {
        spriteIdList.forEach { spriteIdentifier ->
            spriteList.add(textureGetter.apply(spriteIdentifier))
        }
        modelIdList.forEach { identifier ->
            val unbakedModel = loader.getOrLoadModel(identifier)
            if(identifier.path == "block/iron_item_duct") {
                modelList.add(unbakedModel.bake(loader, textureGetter, rotationContainer, modelId))
            }else{
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X90_Y0, modelId))
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId))
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId))
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X0_Y180, modelId))
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X0_Y270, modelId))
                modelList.add(unbakedModel.bake(loader, textureGetter, ModelRotation.X0_Y90, modelId))
            }
        }
        return this
    }

    override fun getSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitBlockQuads(world: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        val entity = world.getBlockEntity(pos) as? DuctBlockEntity ?: return
        entity.updateEntityConnections(world, pos)

        val ctmContext = DuctCTMContext.getOrCreate(entity)

        val emitter = context.emitter
        Direction.values().forEach {
            when(entity.connectionsMap[it]) {
                DuctBlockEntity.ConnectionType.OPEN -> renderConnection(DuctBlockEntity.ConnectionType.OPEN, entity.addonsMap[it], it, emitter)
                DuctBlockEntity.ConnectionType.CABLE -> renderConnection(DuctBlockEntity.ConnectionType.CABLE, entity.addonsMap[it], it, emitter)
                else -> renderCenter(ctmContext, it, emitter)
            }
        }

    }

    private fun renderCenter(ctmContext: DuctCTMContext, side: Direction, emitter: QuadEmitter) {
        val uv = ctmContext.getUV(side)
        var rot = ((ctmContext.getRotationAngle(side)/90) % 4).toInt()

        emitter.square(side,  0.3125f, 0.3125f, 0.6875f, 0.6875f, 0.3125f)

        emitter.sprite(rot++, 0, uv.minU, uv.minV)
        if(rot > 3) rot = 0
        emitter.sprite(rot++, 0, uv.minU, uv.maxV)
        if(rot > 3) rot = 0
        emitter.sprite(rot++, 0, uv.maxU, uv.maxV)
        if(rot > 3) rot = 0
        emitter.sprite(rot, 0, uv.maxU, uv.minV)

        emitter.spriteBake(0, sprite, QuadEmitter.BAKE_ROTATE_NONE)
        emitter.spriteColor(0, -1, -1, -1, -1)
        emitter.emit()
    }

    private fun renderConnection(connectionType: DuctBlockEntity.ConnectionType, addon: DuctAddon?, side: Direction, emitter: QuadEmitter) {
        when(connectionType) {
            DuctBlockEntity.ConnectionType.CABLE -> {
                modelList[1 + side.id]?.getQuads(null, null, Random())?.forEach { q ->
                    emitter.fromVanilla(q.vertexData, 0, true)
                    emitter.emit()
                }
            }
            DuctBlockEntity.ConnectionType.OPEN, DuctBlockEntity.ConnectionType.OPEN_FORCED -> {
                modelList[7 + side.id]?.getQuads(null, null, Random())?.forEach { q ->
                    emitter.fromVanilla(q.vertexData, 0, true)
                    emitter.emit()
                }
                modelList[13 + side.id]?.getQuads(null, null, Random())?.forEach { q ->
                    emitter.fromVanilla(q.vertexData, 0, true)
                    emitter.emit()
                }
                addon?.let { _ ->
                    MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(ItemCompendium.getId(addon), "")).getQuads(null, null, Random()).forEach { q ->
                        emitter.fromVanilla(q.vertexData, 0, true)
                        emitter.emit()
                    }
                }
            }
            else -> {}
        }
    }

    override fun emitItemQuads(p0: ItemStack?, p1: Supplier<Random>?, p2: RenderContext?) {}

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion() = false
    override fun hasDepth() = false
    override fun isSideLit() = false
    override fun isBuiltin() = false

    override fun getOverrides() = null
    override fun getTransformation() = null

}