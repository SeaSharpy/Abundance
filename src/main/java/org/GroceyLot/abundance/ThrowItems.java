package org.GroceyLot.abundance;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.Vec3d;

public final class ThrowItems {
    public static void spawnFromPlayer(ServerPlayerEntity player, ItemStack item) {
        if (!(item.getItem() instanceof BlockItem bi)) return;

        item.decrementUnlessCreative(1, player);
        ServerWorld world = player.getEntityWorld();

        Vec3d camNow = player.getCameraPosVec(0.0f).subtract(0, 0, 0);
        Vec3d camNext = player.getCameraPosVec(1.0f).subtract(0, 0, 0);
        Vec3d dir = player.getRotationVec(1.0f);
        Vec3d spawnPos = camNow.add(dir.multiply(0.5));
        Vec3d velocity = dir.normalize().add(camNext.subtract(camNow));

        BlockState state = bi.getBlock().getDefaultState();

        BlockPos bp = BlockPos.ofFloored(spawnPos);
        FallingBlockEntity falling = FallingBlockEntity.spawnFromBlock(world, bp, state);

        falling.setVelocity(velocity.x, velocity.y, velocity.z);
    }
}
