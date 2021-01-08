package io.github.lucaargolo.chorustech.common.blockentity.ducts

import io.github.lucaargolo.chorustech.client.render.bakedmodel.ducts.DuctCTMContext
import io.github.lucaargolo.chorustech.common.block.BlockCompendium
import io.github.lucaargolo.chorustech.common.block.ducts.Duct
import io.github.lucaargolo.chorustech.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.chorustech.common.item.ItemCompendium
import io.github.lucaargolo.chorustech.common.item.addons.DuctAddon
import io.github.lucaargolo.chorustech.utils.MovingStack
import io.github.lucaargolo.chorustech.utils.tryToInsert
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView
import net.minecraft.world.World

class DuctBlockEntity: BlockEntity(BlockEntityCompendium.DUCT_TYPE), Tickable, BlockEntityClientSerializable {

    private val duct: Duct by lazy { (world?.getBlockState(pos)?.block as? Duct) ?: BlockCompendium.IRON_DUCT as Duct }

    enum class ConnectionType {
        CABLE,
        OPEN,
        OPEN_FORCED,
        NONE_FORCED
    }

    var connectionsMap = linkedMapOf<Direction, ConnectionType>()
    var addonsMap = linkedMapOf<Direction, DuctAddon>()
    var connectionsDirty = true

    var movingStacks = mutableListOf<MovingStack>()

    fun addMovingStack(stack: ItemStack, from: Direction, progress: Double = 0.0) {
        val to = if(from.horizontal != -1) when {
            connectionsMap[Direction.DOWN] != null -> Direction.DOWN
            connectionsMap[from.opposite] != null -> from.opposite
            connectionsMap[from.rotateYClockwise()] != null -> from.rotateYClockwise()
            connectionsMap[from.rotateYCounterclockwise()] != null -> from.rotateYCounterclockwise()
            connectionsMap[Direction.UP] != null -> Direction.UP
            else -> from
        } else when {
            connectionsMap[from.opposite] != null -> from.opposite
            connectionsMap[Direction.NORTH] != null -> Direction.NORTH
            connectionsMap[Direction.EAST] != null -> Direction.EAST
            connectionsMap[Direction.SOUTH] != null -> Direction.SOUTH
            connectionsMap[Direction.WEST] != null -> Direction.WEST
            else -> from
        }
        addMovingStack(stack, from, to, progress)
    }

    fun addMovingStack(stack: ItemStack, from: Direction, to: Direction, progress: Double = 0.0) {
        movingStacks.add(MovingStack(stack, from, to, progress))
        markDirtyAndSync()
        if(movingStacks.size > duct.capacity) {
            world?.breakBlock(pos, false)
        }
    }

    private fun markDirtyAndSync() {
        markDirty()
        if(world?.isClient == false)
            sync()
    }

    override fun tick() {
        if(connectionsDirty) {
            connectionsDirty = false
            world?.let { updateEntityConnections(it, pos) }
        }
        val iterator = movingStacks.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            it.progress += duct.speed
            if (world?.isClient == false) {
                when (it.progress) {
                    50.0 -> {
                        if (connectionsMap[it.to] == null) {
                            ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it.stack)
                            iterator.remove()
                            markDirtyAndSync()
                        }
                    }
                    100.0 -> {
                        when (connectionsMap[it.to]) {
                            ConnectionType.CABLE -> {
                                val toDuct = world?.getBlockEntity(pos.add(it.to.vector)) as? DuctBlockEntity
                                if (toDuct != null) {
                                    toDuct.addMovingStack(it.stack, it.to.opposite)
                                } else {
                                    ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it.stack)
                                }
                            }
                            ConnectionType.OPEN, ConnectionType.OPEN_FORCED -> {
                                val toInv = world?.let { wld -> getAttachedInventory(wld, pos.add(it.to.vector)) }
                                if (toInv != null) {
                                    val s = toInv.tryToInsert(it.stack)
                                    if (!s.isEmpty)
                                        addMovingStack(s, it.to)
                                } else {
                                    ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it.stack)
                                }
                            }
                            else -> {}
                        }
                        iterator.remove()
                        markDirtyAndSync()
                    }
                }
            }
        }
    }

    fun markConnectionDirty() {
        connectionsDirty = true
    }

    fun updateEntityConnections(world: BlockView, pos: BlockPos) {
        connectionsMap = linkedMapOf()
        Direction.values().forEach { dir ->
            when {
                isTwin(world, pos.add(dir.vector)) -> ConnectionType.CABLE
                isInventory(world, pos.add(dir.vector)) -> ConnectionType.OPEN
                else -> null
            }?.let{
                connectionsMap[dir] = it
            }
        }
        DuctCTMContext.delete(this)
    }

    private fun updateEntityConnections(world: World, pos: BlockPos) {
        updateEntityConnections(world as BlockView, pos)
        if(!world.isClient) sync()
    }

    private fun isTwin(world: BlockView, blockPos: BlockPos): Boolean {
        return world.getBlockState(blockPos).block == duct
    }

    private fun isInventory(world: BlockView, blockPos: BlockPos): Boolean {
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        return block is InventoryProvider || world.getBlockEntity(blockPos) is Inventory
    }

    private fun getAttachedInventory(world: World, blockPos: BlockPos): Inventory? {
        var inventory: Inventory? = null
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        if (block is InventoryProvider) {
            inventory = (block as InventoryProvider).getInventory(blockState, world, blockPos)
        } else if (block.hasBlockEntity()) {
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is Inventory) {
                inventory = blockEntity
                if (inventory is ChestBlockEntity && block is ChestBlock) {
                    inventory = ChestBlock.getInventory(block, blockState, world, blockPos, true)
                }
            }
        }
        return inventory
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        val connectionsTag = CompoundTag()
        connectionsMap.forEach { (direction, type) ->
            connectionsTag.putString(direction.name, type.name)
        }
        tag.put("connections", connectionsTag)
        val addonsTag = CompoundTag()
        addonsMap.forEach { (addon, type) ->
            addonsTag.putString(addon.name, ItemCompendium.getId(type).toString())
        }
        tag.put("addons", addonsTag)
        val movingStacksTag = ListTag()
        movingStacks.forEach {
            movingStacksTag.add(it.toTag(CompoundTag()))
        }
        tag.put("movingStacks", movingStacksTag)
        return tag
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        connectionsMap = linkedMapOf()
        val connectionsTag = tag.getCompound("connections")
        connectionsTag.keys.forEach {
            val direction = Direction.valueOf(it)
            connectionsMap[direction] = ConnectionType.valueOf(connectionsTag.getString(it))
        }
        addonsMap = linkedMapOf()
        val addonsTag = tag.getCompound("addons")
        addonsTag.keys.forEach {
            val direction = Direction.valueOf(it)
            (Registry.ITEM.get(Identifier(addonsTag.getString(it))) as? DuctAddon)?.let { a -> addonsMap[direction] = a }
        }
        movingStacks = mutableListOf()
        val movingStacksTag = tag.get("movingStacks") as ListTag
        movingStacksTag.forEach {
            movingStacks.add(MovingStack.fromTag(it as CompoundTag))
        }
    }

    override fun toClientTag(tag: CompoundTag) = toTag(tag)
    override fun fromClientTag(tag: CompoundTag) = fromTag(BlockCompendium.IRON_DUCT.defaultState, tag)

}