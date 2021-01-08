package io.github.lucaargolo.chorustech.client.render.blockentity.ducts

import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper


class DuctBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<DuctBlockEntity>(dispatcher) {

    override fun render(entity: DuctBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        entity.movingStacks.forEach {
            val o = it.from.vector
            val a = it.from.opposite.vector
            val b = it.to.vector
            val h = it.to.opposite.vector

            matrices.push()

            val lerpProgress = MathHelper.lerp(tickDelta.toDouble(), it.lastRenderProgress, it.progress)

            when(lerpProgress) {
                in 0.0..50.0 -> {
                    val p = lerpProgress/100
                    matrices.translate(0.5+(o.x/2.0)+(a.x*p), 0.5+(o.y/2.0)+(a.y*p), 0.5+(o.z/2.0)+(a.z*p))
                }
                in 50.0..100.0 -> {
                    val p = lerpProgress/100
                    matrices.translate(0.5+(h.x/2.0)+(b.x*p), 0.5+(h.y/2.0)+(b.y*p), 0.5+(h.z/2.0)+(b.z*p))
                }
            }
            matrices.scale(0.35f, 0.35f, 0.35f)

            it.lastRenderProgress = lerpProgress

            MinecraftClient.getInstance().itemRenderer.renderItem(it.stack, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers)
            matrices.pop()
        }

    }


}