package io.github.lucaargolo.chorustech.common.block.ducts

import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity
import io.github.lucaargolo.chorustech.common.blockentity.ducts.DuctBlockEntity.ConnectionType
import io.github.lucaargolo.chorustech.common.item.ItemCompendium
import io.github.lucaargolo.chorustech.common.item.addons.DuctAddon
import io.github.lucaargolo.chorustech.utils.rotateShape
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class Duct(settings: Settings, val speed: Double, val capacity: Int): BlockWithEntity(settings){

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        (world.getBlockEntity(pos) as? DuctBlockEntity)?.addonsMap?.set(hit.side, ItemCompendium.IRON_FILTER as DuctAddon)
        (world.getBlockEntity(pos) as? DuctBlockEntity)?.markConnectionDirty()
        return ActionResult.SUCCESS
    }

    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, newState: BlockState, world: WorldAccess, pos: BlockPos, posFrom: BlockPos): BlockState {
        (world.getBlockEntity(pos) as? DuctBlockEntity)?.markConnectionDirty()
        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos) as? DuctBlockEntity
            blockEntity?.let{
                val stackList = DefaultedList.ofSize(it.movingStacks.size, ItemStack.EMPTY)
                it.movingStacks.forEachIndexed { idx, stk ->
                    stackList[idx] = stk.stack
                }
                ItemScatterer.spawn(world, pos, stackList)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun createBlockEntity(world: BlockView?) = DuctBlockEntity()

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext?) = getShapes(world, pos)

    override fun getCollisionShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext?) = getShapes(world, pos)

    fun getShapes(world: BlockView, pos: BlockPos): VoxelShape {
        var shape = createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0)
        val be = world.getBlockEntity(pos) as? DuctBlockEntity ?: return shape
        Direction.values().forEach {
            shape = VoxelShapes.union(shape, when(be.connectionsMap[it]) {
                ConnectionType.CABLE -> createCuboidShape(5.0, 5.0, 0.0, 11.0, 11.0, 5.0).rotateShape(it)
                ConnectionType.OPEN, ConnectionType.OPEN_FORCED -> VoxelShapes.union(
                    createCuboidShape(5.0, 5.0, 3.0, 11.0, 11.0, 5.0),
                    createCuboidShape(4.0, 4.0, 0.0, 12.0, 12.0, 3.0)
                ).rotateShape(it)
                else -> VoxelShapes.empty()
            })
        }
        return shape
    }


}