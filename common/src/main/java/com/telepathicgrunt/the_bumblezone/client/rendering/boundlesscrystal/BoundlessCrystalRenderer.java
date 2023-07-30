package com.telepathicgrunt.the_bumblezone.client.rendering.boundlesscrystal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.entities.living.BoundlessCrystalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;

public class BoundlessCrystalRenderer extends LivingEntityRenderer<BoundlessCrystalEntity, BoundlessCrystalModel<BoundlessCrystalEntity>> {
    private static final ResourceLocation SKIN = new ResourceLocation(Bumblezone.MODID, "textures/entity/boundless_crystal.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation(Bumblezone.MODID, "textures/entity/boundless_crystal_laser.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

    public BoundlessCrystalRenderer(EntityRendererProvider.Context context) {
        super(context, new BoundlessCrystalModel<>(context.bakeLayer(BoundlessCrystalModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public void render(BoundlessCrystalEntity boundlessCrystalEntity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        renderLiving(boundlessCrystalEntity, entityYaw, partialTicks, stack, buffer, LightTexture.FULL_BRIGHT);

        renderHealth(
                boundlessCrystalEntity,
                Component.literal("Health: " + boundlessCrystalEntity.getHealth()),
                stack,
                buffer,
                LightTexture.FULL_BRIGHT);

        renderLaser(
                boundlessCrystalEntity,
                entityYaw,
                partialTicks,
                stack,
                buffer,
                LightTexture.FULL_BRIGHT);
    }

    public void renderLiving(BoundlessCrystalEntity boundlessCrystalEntity, float f, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(boundlessCrystalEntity, partialTick);
        this.model.riding = boundlessCrystalEntity.isPassenger();
        this.model.young = boundlessCrystalEntity.isBaby();
        float h = Mth.rotLerp(partialTick, boundlessCrystalEntity.yBodyRotO, boundlessCrystalEntity.yBodyRot);
        float j = Mth.rotLerp(partialTick, boundlessCrystalEntity.yHeadRotO, boundlessCrystalEntity.yHeadRot);
        float k = j - h;

        float l = this.getBob(boundlessCrystalEntity, partialTick);
        float xRot = Mth.lerp(partialTick, boundlessCrystalEntity.xRotO, boundlessCrystalEntity.getXRot());
        this.setupRotations(boundlessCrystalEntity, poseStack, l, h, partialTick);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scale(boundlessCrystalEntity, poseStack, partialTick);
        poseStack.translate(0.0f, -1.501f, 0.0f);

        float n = 0.0f;
        float o = 0.0f;
        if (!boundlessCrystalEntity.isPassenger() && boundlessCrystalEntity.isAlive()) {
            n = boundlessCrystalEntity.walkAnimation.speed(partialTick);
            o = boundlessCrystalEntity.walkAnimation.position(partialTick);
            if (n > 1.0f) {
                n = 1.0f;
            }
        }

        this.model.prepareMobModel(boundlessCrystalEntity, o, n, partialTick);
        this.model.setupAnim(boundlessCrystalEntity, o, n, l, k, xRot);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = this.isBodyVisible(boundlessCrystalEntity);
        boolean bl2 = !bl && !boundlessCrystalEntity.isInvisibleTo(minecraft.player);
        boolean bl3 = minecraft.shouldEntityAppearGlowing(boundlessCrystalEntity);

        RenderType renderType = this.getRenderType(boundlessCrystalEntity, bl, bl2, bl3);
        if (renderType != null) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
            int overlayCoords = LivingEntityRenderer.getOverlayCoords(boundlessCrystalEntity, this.getWhiteOverlayProgress(boundlessCrystalEntity, partialTick));

            float currentHealthState = Math.min(1, (Math.min(1, boundlessCrystalEntity.getHealth() / boundlessCrystalEntity.getMaxHealth()) * 0.35f) + 0.7f);

            float red = currentHealthState;
            float green = currentHealthState;
            float blue = currentHealthState;

            if (boundlessCrystalEntity.getTicksFrozen() > 0) {
                red *= 0.75f;
                green *= 0.75f;
            }
            if (boundlessCrystalEntity.isOnFire()) {
                green *= 0.75f;
                blue *= 0.75f;
            }
            for(MobEffect mobEffect : boundlessCrystalEntity.getActiveEffectsMap().keySet()) {
                if (mobEffect == MobEffects.POISON) {
                    red *= 0.75f;
                    blue *= 0.75f;
                }
                else if (mobEffect == MobEffects.WITHER) {
                    red *= 0.5f;
                    green *= 0.5f;
                    blue *= 0.5f;
                }
                else if (!mobEffect.isInstantenous() && !mobEffect.isBeneficial()) {
                    String namespace = BuiltInRegistries.MOB_EFFECT.getKey(mobEffect).getNamespace();
                    if (!namespace.equals("minecraft") && !namespace.equals(Bumblezone.MODID)) {
                        Color color = new Color(mobEffect.getColor());
                        red = (red + (color.getRed() / 255f)) / 2f;
                        green = (green + (color.getGreen() / 255f)) / 2f;
                        blue = (blue + (color.getBlue() / 255f)) / 2f;
                    }
                }
            }

            ((Model)this.model).renderToBuffer(poseStack, vertexConsumer, packedLight, overlayCoords, red, green, blue, bl2 ? 0.15f : 1.0f);
        }

        if (!boundlessCrystalEntity.isSpectator()) {
            for (RenderLayer<BoundlessCrystalEntity, ?> renderLayer : this.layers) {
                renderLayer.render(poseStack, multiBufferSource, packedLight, boundlessCrystalEntity, o, n, partialTick, l, k, xRot);
            }
        }

        poseStack.popPose();
    }

    protected void renderHealth(Entity entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (this.entityRenderDispatcher.distanceToSqr(entity) > 100.0) {
            return;
        }
        float f = entity.getNameTagOffsetY() + 0.5F;
        poseStack.pushPose();
        poseStack.translate(0.0f, f, 0.0f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = poseStack.last().pose();
        float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int k = (int)(g * 255.0f) << 24;
        Font font = this.getFont();
        float h = -font.width(component) / 2F;
        font.drawInBatch(component, h, 0, 0x20FFFFFF, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, k, packedLight);
        font.drawInBatch(component, h, 0, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    @Override
    protected void setupRotations(BoundlessCrystalEntity boundlessCrystalEntity, PoseStack poseStack, float f, float g, float partialTick) {
        if (this.isShaking(boundlessCrystalEntity)) {
            g += (float)(Math.cos((double)boundlessCrystalEntity.tickCount * 3.25) * Math.PI * 0.4000000059604645);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - g));

        float newXRot = Mth.lerp(partialTick, boundlessCrystalEntity.prevVisualXRot, boundlessCrystalEntity.visualXRot);

        poseStack.translate(0, 1, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(newXRot));
        poseStack.translate(0, -1, 0);
    }


    public void renderLaser(BoundlessCrystalEntity boundlessCrystalEntity, float f, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (boundlessCrystalEntity.isLaserFiring()) {
            float totalTickTime = boundlessCrystalEntity.tickCount + partialTick;

            float colorSpeed = 2;
            float redSin = Mth.sin(((totalTickTime * colorSpeed) % 360) * Mth.DEG_TO_RAD);
            float greenSin = Mth.sin(((totalTickTime * colorSpeed) % 360) * Mth.DEG_TO_RAD + 30);
            float blueSin = Mth.sin(((totalTickTime * colorSpeed) % 360) * Mth.DEG_TO_RAD + 60);

            int red = 200 + (int) (redSin * 56);
            int green = 200 + (int) (greenSin * 56);
            int blue = 200 + (int) (blueSin * 56);

            float k = totalTickTime * 0.5f % 1.0f;
            float eyeY = boundlessCrystalEntity.getEyeHeight();
            poseStack.pushPose();
            poseStack.translate(0.0f, eyeY, 0.0f);

            Vec3 startPos = this.getPosition(boundlessCrystalEntity, eyeY, partialTick);
            Vec3 endPos = boundlessCrystalEntity.getLookAngle().scale(50).add(startPos);

            HitResult hitResult = boundlessCrystalEntity.level()
                    .clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boundlessCrystalEntity));

            if (hitResult.getType() != HitResult.Type.MISS) {
                endPos = hitResult.getLocation();
            }

            Vec3 vectToTarget = endPos.subtract(startPos);

            float laserLength = (float) vectToTarget.length() - 0.01f;
            vectToTarget = vectToTarget.normalize();
            float n = (float)Math.acos(vectToTarget.y);
            float o = (float)Math.atan2(vectToTarget.z, vectToTarget.x);
            poseStack.mulPose(Axis.YP.rotationDegrees((1.5707964f - o) * 57.295776f));
            poseStack.mulPose(Axis.XP.rotationDegrees(n * 57.295776f));
            float q = totalTickTime * 0.05f * -1.5f;
            float v = 0.2f;
            float w2 = 0.5f;
            float z5 = Mth.sin(q + 2.3561945f) * w2;
            float x7 = Mth.cos(q + 2.3561945f) * w2;
            float z9 = Mth.cos(q + 0.7853982f) * w2;
            float z6 = Mth.sin(q + 0.7853982f) * w2;
            float x6 = Mth.cos(q + 3.926991f) * w2;
            float z8 = Mth.sin(q + 3.926991f) * w2;
            float x5 = Mth.cos(q + 5.4977875f) * w2;
            float z7 = Mth.sin(q + 5.4977875f) * w2;
            float x1 = Mth.cos(q + (float)Math.PI) * v;
            float z1 = Mth.sin(q + (float)Math.PI) * v;
            float x2 = Mth.cos(q + 0.0f) * v;
            float z2 = Mth.sin(q + 0.0f) * v;
            float x3 = Mth.cos(q + 1.5707964f) * v;
            float z3 = Mth.sin(q + 1.5707964f) * v;
            float x4 = Mth.cos(q + 4.712389f) * v;
            float z4 = Mth.sin(q + 4.712389f) * v;
            float y1 = laserLength;
            float y2 = 0.0f;
            float ux1 = 0.4999f;
            float ux2 = 0.0f;
            float uv2 = -1.0f + k;
            float uv1 = laserLength * 2.5f + uv2;
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BEAM_RENDER_TYPE);
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            vertex(vertexConsumer, matrix4f, matrix3f, x1, y1, z1, red, green, blue, ux1, uv1);
            vertex(vertexConsumer, matrix4f, matrix3f, x1, y2, z1, red, green, blue, ux1, uv2);
            vertex(vertexConsumer, matrix4f, matrix3f, x2, y2, z2, red, green, blue, ux2, uv2);
            vertex(vertexConsumer, matrix4f, matrix3f, x2, y1, z2, red, green, blue, ux2, uv1);
            vertex(vertexConsumer, matrix4f, matrix3f, x3, y1, z3, red, green, blue, ux1, uv1);
            vertex(vertexConsumer, matrix4f, matrix3f, x3, y2, z3, red, green, blue, ux1, uv2);
            vertex(vertexConsumer, matrix4f, matrix3f, x4, y2, z4, red, green, blue, ux2, uv2);
            vertex(vertexConsumer, matrix4f, matrix3f, x4, y1, z4, red, green, blue, ux2, uv1);
            float as = 0.0f;
            if (boundlessCrystalEntity.tickCount % 4 < 2) {
                as = 0.5f;
            }
            vertex(vertexConsumer, matrix4f, matrix3f, x7, y1, z5, red, green, blue, 0.5f, as + 0.5f);
            vertex(vertexConsumer, matrix4f, matrix3f, z9, y1, z6, red, green, blue, 1.0f, as + 0.5f);
            vertex(vertexConsumer, matrix4f, matrix3f, x5, y1, z7, red, green, blue, 1.0f, as);
            vertex(vertexConsumer, matrix4f, matrix3f, x6, y1, z8, red, green, blue, 0.5f, as);
            poseStack.popPose();
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float x, float y, float z, int red, int green, int blue, float ux, float uz) {
        vertexConsumer
                .vertex(matrix4f, x, y, z)
                .color(red, green, blue, 255)
                .uv(ux, uz)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
        double e = Mth.lerp(f, livingEntity.xOld, livingEntity.getX());
        double g = Mth.lerp(f, livingEntity.yOld, livingEntity.getY()) + d;
        double h = Mth.lerp(f, livingEntity.zOld, livingEntity.getZ());
        return new Vec3(e, g, h);
    }

    @Override
    public ResourceLocation getTextureLocation(BoundlessCrystalEntity boundlessCrystalEntity) {
        return SKIN;
    }

    @Override
    protected boolean shouldShowName(BoundlessCrystalEntity entity) {
        return false;
    }
}