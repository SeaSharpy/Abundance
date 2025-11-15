package org.GroceyLot.abundance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.intellij.lang.annotations.Flow;

public class Abundance implements ModInitializer {

    @Override
    public void onInitialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();

                if (isThrowableBlock(block)) {
                    ThrowItems.spawnFromPlayer((ServerPlayerEntity) player, stack);
                    return ActionResult.SUCCESS;
                }
            } else if (isItem(stack, Items.STICK, "leafblower_marker")) {
                blowLeavesInFront((ServerPlayerEntity) player, (ServerWorld) world);
                return ActionResult.SUCCESS;
            } else if (isItem(stack, Items.STICK, "rocket_marker")) {
                // find a firework rocket in the player's inventory
                var inv = player.getInventory();
                int rocketSlot = -1;
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack s = inv.getStack(i);
                    if (!s.isEmpty() && s.isOf(Items.FIREWORK_ROCKET)) {
                        rocketSlot = i;
                        break;
                    }
                }
                if (rocketSlot != -1) {
                    ItemStack rocketStack = inv.getStack(rocketSlot);
                    ItemStack oneRocket = rocketStack.copy();
                    oneRocket.setCount(1);

                    var eyePos = player.getCameraPosVec(1.0F);
                    var look = player.getRotationVec(1.0F);

                    var rocket = new net.minecraft.entity.projectile.FireworkRocketEntity(
                            world, oneRocket, player, eyePos.x, eyePos.y, eyePos.z, true
                    );
                    rocket.setVelocity(look.x, look.y, look.z, 1.5F, 0.0F);

                    world.spawnEntity(rocket);
                    rocketStack.decrementUnlessCreative(1, player);
                }
            }
            return ActionResult.PASS;
        });
    }


    public static boolean isItem(ItemStack stack, Item item, String marker) {
        // must be a stick
        if (!stack.isOf(item)) return false;
        // bail out if there's no custom_data component at all
        if (!stack.contains(DataComponentTypes.CUSTOM_DATA)) return false;
        // fetch the CustomData component
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return false;
        // check for marker key
        return data.toString().contains(marker);
    }


    private void blowLeavesInFront(ServerPlayerEntity player, ServerWorld world) {
        BlockPos center = player.getBlockPos();
        world.playSound(player, center, SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.PLAYERS);
        int radius = 2, radiusSq = radius * radius;

        // iterate a cube of side length 2*radius+1
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // squared-distance check for a spherical radius
                    if (dx*dx + dy*dy + dz*dz > radiusSq) continue;

                    BlockPos pos = center.add(dx, dy, dz);
                    BlockState state = world.getBlockState(pos);

                    if (state.isOf(Blocks.PINK_PETALS) || state.isOf(Blocks.LEAF_LITTER) || state.isOf(Blocks.WILDFLOWERS) || state.isOf(Blocks.SHORT_DRY_GRASS) || state.isOf(Blocks.TALL_DRY_GRASS) || state.isOf(Blocks.SHORT_GRASS) || state.isOf(Blocks.TALL_GRASS) || state.isOf(Blocks.BUSH) || state.isOf(Blocks.DEAD_BUSH) || state.isOf(Blocks.FERN) || state.isOf(Blocks.LARGE_FERN) || state.getBlock() instanceof FlowerBlock) {
                        boolean drop = false;
                        Item item = state.getBlock().asItem();
                        if (item != Item.fromBlock(Blocks.AIR)) {
                            ItemStack stack = new ItemStack(item, 1);

                            boolean inserted = player.getInventory().insertStack(stack);

                            if (!inserted) drop = true;
                        }
                        world.breakBlock(pos, drop);
                    }
                }
            }
        }
    }
    private boolean isThrowableBlock(Block block) {
        return block == Blocks.DRAGON_EGG
                || block == Blocks.COBWEB
                || block == Blocks.SLIME_BLOCK
                || block == Blocks.HONEY_BLOCK
                || block == Blocks.TRIPWIRE
                || block == Blocks.REDSTONE_WIRE
                || block == Blocks.IRON_CHAIN
                || Blocks.COPPER_CHAINS.getAll().contains(block)
                || block == Blocks.LANTERN
                || block == Blocks.SOUL_LANTERN
                || block == Blocks.TORCH
                || block == Blocks.SOUL_TORCH
                || block == Blocks.END_ROD
                || block == Blocks.LIGHTNING_ROD
                || block instanceof FenceBlock
                || block instanceof PressurePlateBlock
                || block instanceof WallBlock
                || block instanceof PaneBlock
                || block instanceof CarpetBlock
                || block instanceof CandleBlock
                || block instanceof PointedDripstoneBlock
                || block instanceof PlantBlock
                || block instanceof MushroomBlock
                || block instanceof AbstractCoralBlock
                || block instanceof FallingBlock
                || block instanceof SignBlock
                || block instanceof AbstractRailBlock;
    }
}
