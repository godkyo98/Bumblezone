package com.telepathicgrunt.the_bumblezone.entities.nonliving;

import com.telepathicgrunt.the_bumblezone.configs.BzGeneralConfigs;
import com.telepathicgrunt.the_bumblezone.modinit.BzCriterias;
import com.telepathicgrunt.the_bumblezone.modinit.BzEffects;
import com.telepathicgrunt.the_bumblezone.modinit.BzEntities;
import com.telepathicgrunt.the_bumblezone.modinit.BzItems;
import com.telepathicgrunt.the_bumblezone.modinit.BzSounds;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BeeStingerEntity extends AbstractArrow {
    public BeeStingerEntity(EntityType<? extends BeeStingerEntity> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(0.5d);
    }

    public BeeStingerEntity(Level level, LivingEntity livingEntity, ItemStack ammo, @Nullable ItemStack weapon) {
        super(BzEntities.BEE_STINGER_ENTITY.get(), livingEntity, level, ammo, weapon);
        this.setBaseDamage(0.5d);
        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (!this.inGround) {
                this.makeParticle(4);
            }
        }
        else if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 600) {
            this.level().broadcastEntityEvent(this, (byte)0);
        }
    }

    private void makeParticle(int particlesToSpawn) {
        if (particlesToSpawn > 0) {
            float red = 0.3F;
            float green = 0.3F;
            float blue = 0.3F;

            for(int i = 0; i < particlesToSpawn; ++i) {
                this.level().addParticle(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, red, green, blue),
                        this.getRandomX(0.5D),
                        this.getRandomY(),
                        this.getRandomZ(0.5D),
                        0.0,
                        0.0,
                        0.0);
            }
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity livingEntity) {
        if (!livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
            boolean isPoisoned = livingEntity.hasEffect(MobEffects.POISON);
            boolean isSlowed = livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
            boolean isWeakened = livingEntity.hasEffect(MobEffects.WEAKNESS);
            boolean isParalyzed = livingEntity.hasEffect(BzEffects.PARALYZED.holder());

            livingEntity.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    120,
                    0,
                    false,
                    true,
                    true));

            if (!isParalyzed && isPoisoned && livingEntity.getRandom().nextFloat() < 0.35f) {
                livingEntity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        180,
                        0,
                        true,
                        true,
                        true));
            }

            if (!isParalyzed && isPoisoned && isSlowed && livingEntity.getRandom().nextFloat() < 0.3f) {
                livingEntity.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        200,
                        0,
                        true,
                        true,
                        true));
            }

            if (isPoisoned && isSlowed && isWeakened && livingEntity.getRandom().nextFloat() < 0.25f) {
                livingEntity.addEffect(new MobEffectInstance(
                        BzEffects.PARALYZED.holder(),
                        Math.min(BzGeneralConfigs.paralyzedMaxTickDuration, 100),
                        0,
                        false,
                        true,
                        true));

                livingEntity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                livingEntity.removeEffect(MobEffects.WEAKNESS);

                if(!livingEntity.isDeadOrDying() && this.getOwner() instanceof ServerPlayer serverPlayer) {
                    BzCriterias.BEE_STINGER_PARALYZE_TRIGGER.get().trigger(serverPlayer);
                }
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return BzItems.BEE_STINGER.get().getDefaultInstance();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return BzSounds.BEE_STINGER_HIT.get();
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}