package com.telepathicgrunt.the_bumblezone.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.telepathicgrunt.the_bumblezone.modinit.BzDimension;
import com.telepathicgrunt.the_bumblezone.modinit.BzLootConditionTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class NonBumblezoneDimensionCheck implements LootItemCondition {
    static final NonBumblezoneDimensionCheck INSTANCE = new NonBumblezoneDimensionCheck();
    public static final Codec<NonBumblezoneDimensionCheck> CODEC = Codec.unit(() -> INSTANCE);

    public NonBumblezoneDimensionCheck() {
        super();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        return entity == null || !entity.level().dimension().equals(BzDimension.BZ_WORLD_KEY);
    }

    @Override
    public LootItemConditionType getType() {
        return BzLootConditionTypes.NON_BUMBLEZONE_DIMENSION_CHECK.get();
    }
}