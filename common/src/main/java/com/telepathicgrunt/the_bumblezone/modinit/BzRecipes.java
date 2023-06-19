package com.telepathicgrunt.the_bumblezone.modinit;

import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.configs.BzGeneralConfigs;
import com.telepathicgrunt.the_bumblezone.items.recipes.ContainerCraftingRecipe;
import com.telepathicgrunt.the_bumblezone.items.recipes.IncenseCandleRecipe;
import com.telepathicgrunt.the_bumblezone.items.recipes.NbtKeepingShapelessRecipe;
import com.telepathicgrunt.the_bumblezone.items.recipes.RecipeDiscoveredHookedShapedRecipe;
import com.telepathicgrunt.the_bumblezone.items.recipes.RecipeDiscoveredHookedShapelessRecipe;
import com.telepathicgrunt.the_bumblezone.mixin.containers.PotionBrewingAccessor;
import com.telepathicgrunt.the_bumblezone.modinit.registry.RegistryEntry;
import com.telepathicgrunt.the_bumblezone.modinit.registry.ResourcefulRegistries;
import com.telepathicgrunt.the_bumblezone.modinit.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class BzRecipes {
    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPES = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, Bumblezone.MODID);

    //Recipe
    public static final RegistryEntry<RecipeSerializer<ContainerCraftingRecipe>> CONTAINER_CRAFTING_RECIPE = RECIPES.register("container_shapeless_recipe_bz", ContainerCraftingRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<IncenseCandleRecipe>> INCENSE_CANDLE_RECIPE = RECIPES.register("incense_candle_recipe", IncenseCandleRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<RecipeDiscoveredHookedShapedRecipe>> RECIPE_DISCOVERED_HOOKED_RECIPE = RECIPES.register("recipe_discovered_hooked_recipe", RecipeDiscoveredHookedShapedRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<RecipeDiscoveredHookedShapelessRecipe>> RECIPE_DISCOVERED_HOOKED_SHAPELESS_RECIPE = RECIPES.register("recipe_discovered_hooked_shapeless_recipe", RecipeDiscoveredHookedShapelessRecipe.Serializer::new);
    public static final RegistryEntry<RecipeSerializer<NbtKeepingShapelessRecipe>> NBT_KEEPING_SHAPELESS_RECIPE = RECIPES.register("nbt_keeping_shapeless_recipe", NbtKeepingShapelessRecipe.Serializer::new);

    public static void registerBrewingStandRecipes() {
        if (BzGeneralConfigs.glisteringHoneyBrewingRecipe) {
            PotionBrewingAccessor.callAddMix(Potions.AWKWARD, BzItems.GLISTERING_HONEY_CRYSTAL.get(), Potions.LUCK);
        }
        if (BzGeneralConfigs.beeStingerBrewingRecipe) {
            PotionBrewingAccessor.callAddMix(Potions.AWKWARD, BzItems.BEE_STINGER.get(), Potions.LONG_POISON);
        }
    }
}
