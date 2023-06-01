package com.telepathicgrunt.the_bumblezone.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.items.essence.KnowingEssence;
import com.telepathicgrunt.the_bumblezone.mixin.RandomizableContainerBlockEntityAccessor;
import com.telepathicgrunt.the_bumblezone.mixin.client.LevelRendererAccessor;
import com.telepathicgrunt.the_bumblezone.modinit.BzBlocks;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class KnowingEssenceLootBlockOutlining {
    public static void outlineLootBlocks(PoseStack poseStack, Camera camera, LevelRenderer levelRenderer) {
        Player player = Minecraft.getInstance().player;
        if (KnowingEssence.IsKnowingEssenceActive(player)) {
            Level level = player.level();

            float drawRadius = 0.45f;
            float minCorner = 0.5f - drawRadius;
            float maxCorner = 0.5f + drawRadius;
            Vector4f vector4fMin = new Vector4f(minCorner, minCorner, minCorner, 1.0F);
            Vector4f vector4fMax = new Vector4f(maxCorner, maxCorner, maxCorner, 1.0F);

            Vec3 cameraPos = camera.getPosition();
            BlockPos worldSpot = BlockPos.containing(cameraPos);

            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            Tesselator tesselator = Tesselator.getInstance();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            int chunkRadius = 4;
            ChunkPos centerChunkPos = new ChunkPos(worldSpot);
            for (int x = -chunkRadius; x <= chunkRadius; x++) {
                for (int z = -chunkRadius; z <= chunkRadius; z++) {
                    LevelChunk chunk = level.getChunk(x + centerChunkPos.x, z + centerChunkPos.z);
                    for (Map.Entry<BlockPos, BlockEntity> blockEntityEntry : chunk.getBlockEntities().entrySet()) {
                        BlockEntity blockEntity = blockEntityEntry.getValue();
                        Block block = blockEntity.getBlockState().getBlock();
                        if (blockEntityEntry.getValue() instanceof RandomizableContainerBlockEntity ||
                            blockEntityEntry.getValue() instanceof EnderChestBlockEntity ||
                            block instanceof EnderChestBlock)
                        {
                             BlockPos lootBlockPos = blockEntityEntry.getKey();

                            if (!((LevelRendererAccessor)levelRenderer).getCullingFrustum().isVisible(new AABB(
                                    lootBlockPos.getX() + minCorner,
                                    lootBlockPos.getY() + minCorner,
                                    lootBlockPos.getZ() + minCorner,
                                    lootBlockPos.getX() + maxCorner,
                                    lootBlockPos.getY() + maxCorner,
                                    lootBlockPos.getZ() + maxCorner)))
                            {
                                continue;
                            }

                            int red = 255;
                            int green = 255;
                            int blue = 255;
                            int alpha = 255;

                            // TODO: Add tags for this
                            if (blockEntity instanceof ShulkerBoxBlockEntity || block instanceof ShulkerBoxBlock) {
                                //purple
                                green = 0;
                            }
                            else if (blockEntity instanceof ChestBlockEntity || block instanceof ChestBlock) {
                                //yellow
                                blue = 0;
                            }
                            else if (blockEntity instanceof BarrelBlockEntity || block instanceof BarrelBlock) {
                                //orange
                                green = 155;
                                blue = 0;
                            }
                            else if (blockEntity instanceof EnderChestBlockEntity || block instanceof EnderChestBlock) {
                                //dark green
                                red = 0;
                                green = 150;
                                blue = 100;
                            }
                            else {
                                //translucent white
                                alpha /= 2;
                            }

                            renderLineBox(
                                    bufferbuilder,
                                    poseStack.last().pose(),
                                    vector4fMin.x() + lootBlockPos.getX(),
                                    vector4fMin.y() + lootBlockPos.getY(),
                                    vector4fMin.z() + lootBlockPos.getZ(),
                                    vector4fMax.x() + lootBlockPos.getX(),
                                    vector4fMax.y() + lootBlockPos.getY(),
                                    vector4fMax.z() + lootBlockPos.getZ(),
                                    red,
                                    green,
                                    blue,
                                    alpha);
                        }
                    }
                }
            }
            tesselator.end();
            poseStack.popPose();
            RenderType.cutout().clearRenderState();
        }
    }

    private static void renderLineBox(BufferBuilder builder, Matrix4f pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int red, int green, int blue, int alpha) {
        builder.vertex(pose, minX, minY, minZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, minY, minZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, minY, minZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, maxY, minZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, minY, minZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(pose, minX, minY, maxZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(pose, maxX, minY, minZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(red, green, blue, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, maxY, minZ).color(red, green, blue, alpha).normal(-1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, maxY, minZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, minY, maxZ).color(red, green, blue, alpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        builder.vertex(pose, minX, minY, maxZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
        builder.vertex(pose, maxX, minY, minZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, -1.0F).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0.0F, 0.0F, 1.0F).endVertex();
    }
}