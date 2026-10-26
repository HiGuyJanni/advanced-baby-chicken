package com.advanced.baby.chicken.mixin;

import com.advanced.baby.chicken.AdvancedBabyChicken;
import com.advanced.baby.chicken.DirtyBabyChicken;
import com.advanced.baby.chicken.SleepingBabyChicken;
import com.advanced.baby.chicken.goal.BabyChickenSleepOnBedGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to add custom AI goals, sleeping state, and dirty state tracking to Chicken entities.
 * Implements SleepingBabyChicken and DirtyBabyChicken interfaces.
 */
@Mixin(Chicken.class)
public abstract class ChickenMixin extends Animal implements SleepingBabyChicken, DirtyBabyChicken {

    @Unique
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING_ON_BED =
        SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> DATA_DIRTY =
        SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private int advancedBabyChicken$dirtyTicks = 0;

    // Dirty thresholds (in ticks, 20 ticks = 1 second)
    @Unique
    private static final int DIRTY_THRESHOLD_ON_DIRTY_BLOCK = 20 * 90;      // ~1.5 minutes on dirty blocks
    @Unique
    private static final int DIRTY_THRESHOLD_IN_RAIN = 20 * 25;              // ~25 seconds in rain on dirty blocks
    @Unique
    private static final int DIRTY_THRESHOLD_IDLE = 20 * 60 * 12;            // ~12 minutes idle/natural

    // Tick accumulation rates
    @Unique
    private static final int TICKS_PER_DIRTY_BLOCK = 1;                      // Normal rate on dirty blocks
    @Unique
    private static final int TICKS_PER_RAIN_ON_DIRTY = 4;                    // 4x faster in rain on dirty blocks
    @Unique
    private static final int IDLE_TICK_CHANCE = 200;                         // 1/200 chance per tick to add idle dirt

    // Speed modifier for dirty chickens
    @Unique
    private static final AttributeModifier DIRTY_SPEED_MODIFIER = new AttributeModifier(
        AdvancedBabyChicken.id("dirty_slowdown"),
        -0.3,  // 30% speed reduction
        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    protected ChickenMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Helper method to check if an item is special baby chicken food.
     */
    @Unique
    private static boolean advancedBabyChicken$isBabyChickenFood(ItemStack itemStack) {
        return itemStack.is(Items.EGG) ||
               itemStack.is(Items.CHICKEN) ||
               itemStack.is(Items.COOKED_CHICKEN) ||
               itemStack.is(Items.MELON_SLICE) ||
               itemStack.is(Items.POTATO) ||
               itemStack.is(Items.BAKED_POTATO);
    }

    /**
     * Inject into defineSynchedData to register our custom sleeping and dirty data.
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void addCustomData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_SLEEPING_ON_BED, false);
        builder.define(DATA_DIRTY, false);
    }

    /**
     * Inject into the registerGoals method to add our custom bed-seeking goal and baby food tempt goal.
     */
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addBabyChickenGoals(CallbackInfo ci) {
        Chicken chicken = (Chicken) (Object) this;

        // Add the bed-seeking goal with priority 3
        // Lower numbers = higher priority
        // Vanilla chicken priorities:
        // 0 - FloatGoal (swimming)
        // 1 - PanicGoal
        // 2 - BreedGoal
        // 3 - TemptGoal (seeds)
        // 4 - FollowParentGoal
        // 5 - WaterAvoidingRandomStrollGoal
        // 6 - LookAtPlayerGoal
        // 7 - RandomLookAroundGoal
        //
        // We use priority 3 so it's more important than following parent at night,
        // but less important than panic or breeding
        this.goalSelector.addGoal(3, new BabyChickenSleepOnBedGoal(chicken, 1.0D));

        // Add tempt goal for baby chicken special foods (eggs, chicken, watermelon, potatoes)
        // Priority 2 makes it slightly higher priority than the vanilla seed tempt goal
        // This way baby chickens will follow players holding their special foods
        // The predicate checks if the chicken is a baby and if the food is valid
        // When dirty, only baked potatoes attract them
        this.goalSelector.addGoal(2, new TemptGoal(
            (PathfinderMob) chicken,
            1.0D,
            (itemStack) -> {
                if (!chicken.isBaby()) return false;
                DirtyBabyChicken dirtyChicken = (DirtyBabyChicken) chicken;
                if (dirtyChicken.advancedBabyChicken$isDirty()) {
                    // When dirty, only attracted to baked potatoes
                    return itemStack.is(Items.BAKED_POTATO);
                }
                return advancedBabyChicken$isBabyChickenFood(itemStack);
            },
            false
        ));

        AdvancedBabyChicken.LOGGER.debug("Added BabyChickenSleepOnBedGoal and baby food TemptGoal to chicken");
    }

    @Override
    public boolean advancedBabyChicken$isSleepingOnBed() {
        return this.entityData.get(DATA_SLEEPING_ON_BED);
    }

    @Override
    public void advancedBabyChicken$setSleepingOnBed(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING_ON_BED, sleeping);
    }

    // ===== DirtyBabyChicken interface implementation =====

    @Override
    public boolean advancedBabyChicken$isDirty() {
        return this.entityData.get(DATA_DIRTY);
    }

    @Override
    public void advancedBabyChicken$setDirty(boolean dirty) {
        boolean wasDirty = this.advancedBabyChicken$isDirty();
        this.entityData.set(DATA_DIRTY, dirty);

        // Apply or remove speed modifier when dirty state changes
        if (dirty != wasDirty) {
            AttributeInstance speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                if (dirty) {
                    if (!speedAttribute.hasModifier(DIRTY_SPEED_MODIFIER.id())) {
                        speedAttribute.addPermanentModifier(DIRTY_SPEED_MODIFIER);
                    }
                } else {
                    speedAttribute.removeModifier(DIRTY_SPEED_MODIFIER.id());
                }
            }
        }
    }

    @Override
    public int advancedBabyChicken$getDirtyTicks() {
        return this.advancedBabyChicken$dirtyTicks;
    }

    @Override
    public void advancedBabyChicken$setDirtyTicks(int ticks) {
        this.advancedBabyChicken$dirtyTicks = ticks;
    }

    /**
     * Check if the block below the chicken is a "dirty" block that can make it dirty.
     */
    @Unique
    private boolean advancedBabyChicken$isOnDirtyBlock() {
        BlockPos below = this.blockPosition().below();
        Block block = this.level().getBlockState(below).getBlock();
        return block == Blocks.DIRT ||
               block == Blocks.COARSE_DIRT ||
               block == Blocks.ROOTED_DIRT ||
               block == Blocks.MUD ||
               block == Blocks.MUDDY_MANGROVE_ROOTS ||
               block == Blocks.SAND ||
               block == Blocks.RED_SAND ||
               block == Blocks.GRAVEL ||
               block == Blocks.SOUL_SAND ||
               block == Blocks.SOUL_SOIL ||
               block == Blocks.FARMLAND;
    }

    /**
     * Check if the chicken is in water (for cleaning).
     */
    @Unique
    private boolean advancedBabyChicken$isTouchingWater() {
        return this.isInWater();
    }

    /**
     * Clean the baby chicken - removes dirt and plays splash effect.
     */
    @Unique
    private void advancedBabyChicken$clean() {
        if (this.advancedBabyChicken$isDirty()) {
            this.advancedBabyChicken$setDirty(false);
            this.advancedBabyChicken$setDirtyTicks(0);

            // Play splash sound and particles
            this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_SPLASH,
                SoundSource.NEUTRAL,
                0.5F,
                1.2F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );

            // Spawn water splash particles
            if (this.level().isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(
                        ParticleTypes.SPLASH,
                        this.getRandomX(0.5),
                        this.getY() + 0.2,
                        this.getRandomZ(0.5),
                        0, 0.1, 0
                    );
                }
            }

            AdvancedBabyChicken.LOGGER.debug("Baby chicken was cleaned!");
        }
    }

    /**
     * Inject into aiStep to handle dirty tick logic and visual effects.
     */
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void handleDirtyTick(CallbackInfo ci) {
        // Handle client-side particle effects for dirty chickens
        if (this.level().isClientSide() && this.isBaby() && this.advancedBabyChicken$isDirty()) {
            // Spawn occasional dirt particles floating above dirty baby chickens
            if (this.random.nextInt(15) == 0) {
                // Spawn particles above and around the chicken
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = 0.3 + this.random.nextDouble() * 0.4; // Between 0.3 and 0.7 above ground

                this.level().addParticle(
                    ParticleTypes.MYCELIUM,  // Brown/dirt-like particles
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    (this.random.nextDouble() - 0.5) * 0.02,  // Slight horizontal drift
                    0.01 + this.random.nextDouble() * 0.02,   // Upward float
                    (this.random.nextDouble() - 0.5) * 0.02   // Slight horizontal drift
                );
            }
        }

        // Server-side dirty state processing
        if (this.level().isClientSide() || !this.isBaby()) {
            return;
        }

        // Check if chicken is in water - clean it!
        if (this.advancedBabyChicken$isTouchingWater()) {
            if (this.advancedBabyChicken$isDirty()) {
                this.advancedBabyChicken$clean();
            }
            // Reset dirty ticks when in water
            this.advancedBabyChicken$dirtyTicks = 0;
            return;
        }

        // If already dirty, nothing more to do
        if (this.advancedBabyChicken$isDirty()) {
            return;
        }

        // Accumulate dirty ticks based on conditions
        boolean onDirtyBlock = this.advancedBabyChicken$isOnDirtyBlock();
        boolean inRain = this.level().isRainingAt(this.blockPosition());

        int ticksToAdd = 0;

        if (onDirtyBlock && inRain) {
            // Rain on dirty block = fastest accumulation
            ticksToAdd = TICKS_PER_RAIN_ON_DIRTY;
        } else if (onDirtyBlock) {
            // Just on dirty block
            ticksToAdd = TICKS_PER_DIRTY_BLOCK;
        } else {
            // Idle accumulation (rare random chance)
            if (this.random.nextInt(IDLE_TICK_CHANCE) == 0) {
                ticksToAdd = 1;
            }
        }

        if (ticksToAdd > 0) {
            this.advancedBabyChicken$dirtyTicks += ticksToAdd;

            // Check thresholds
            int threshold;
            if (onDirtyBlock && inRain) {
                threshold = DIRTY_THRESHOLD_IN_RAIN;
            } else if (onDirtyBlock) {
                threshold = DIRTY_THRESHOLD_ON_DIRTY_BLOCK;
            } else {
                threshold = DIRTY_THRESHOLD_IDLE;
            }

            if (this.advancedBabyChicken$dirtyTicks >= threshold) {
                this.advancedBabyChicken$setDirty(true);
                AdvancedBabyChicken.LOGGER.debug("Baby chicken got dirty!");
            }
        }
    }

    /**
     * Override isPushable to make sleeping baby chickens immovable.
     * When a baby chicken is sleeping on a bed, it cannot be pushed by players or other entities.
     */
    @Override
    public boolean isPushable() {
        // If this is a baby chicken sleeping on a bed, it cannot be pushed
        if (this.isBaby() && this.advancedBabyChicken$isSleepingOnBed()) {
            return false;
        }
        // Otherwise, use the default behavior
        return super.isPushable();
    }

    /**
     * Inject into isFood to allow baby chickens to be fed special items.
     * Baby chickens can be fed: eggs, chicken (raw/cooked), watermelon slices, potatoes (raw/baked).
     * When dirty, only baked potatoes are accepted as food.
     * This allows players to speed up the growth of baby chickens with these items.
     */
    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void allowBabyChickenFood(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        // Only apply special food rules for baby chickens
        if (this.isBaby()) {
            if (this.advancedBabyChicken$isDirty()) {
                // When dirty, only baked potatoes are food
                if (itemStack.is(Items.BAKED_POTATO)) {
                    cir.setReturnValue(true);
                }
            } else {
                // When clean, all baby chicken foods work
                if (advancedBabyChicken$isBabyChickenFood(itemStack)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    /**
     * Override mobInteract to handle baby chicken interactions:
     * - Water bucket cleans dirty chickens
     * - Feeding with special foods (only baked potato when dirty)
     * We override instead of inject because Chicken doesn't have its own mobInteract method.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Handle water bucket cleaning for dirty baby chickens
        if (this.isBaby() && this.advancedBabyChicken$isDirty() && itemStack.is(Items.WATER_BUCKET)) {
            if (!this.level().isClientSide()) {
                // Clean the chicken
                this.advancedBabyChicken$clean();

                // Replace water bucket with empty bucket (unless creative mode)
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Handle feeding for baby chickens
        if (this.isBaby()) {
            boolean isDirty = this.advancedBabyChicken$isDirty();

            // When dirty, only accept baked potatoes
            if (isDirty) {
                if (itemStack.is(Items.BAKED_POTATO)) {
                    if (!this.level().isClientSide()) {
                        // Consume the item
                        this.usePlayerItem(player, hand, itemStack);

                        // Speed up growth
                        int age = this.getAge();
                        this.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-age), true);

                        // Heal the baby chicken
                        this.heal(4.0F);

                        // Play squeak sound
                        this.level().playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.CHICKEN_AMBIENT_BABY.value(),
                            SoundSource.NEUTRAL,
                            1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                        );

                        AdvancedBabyChicken.LOGGER.debug("Dirty baby chicken was fed baked potato!");
                    }
                    return InteractionResult.SUCCESS;
                }
                // Dirty chickens refuse other baby foods - just ignore and let parent handle
            } else {
                // When clean, accept all baby chicken foods
                if (advancedBabyChicken$isBabyChickenFood(itemStack)) {
                    if (!this.level().isClientSide()) {
                        // Consume the item
                        this.usePlayerItem(player, hand, itemStack);

                        // Speed up growth
                        int age = this.getAge();
                        this.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-age), true);

                        // Heal the baby chicken
                        this.heal(4.0F);

                        // Play squeak sound
                        this.level().playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.CHICKEN_AMBIENT_BABY.value(),
                            SoundSource.NEUTRAL,
                            1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                        );

                        AdvancedBabyChicken.LOGGER.debug("Baby chicken was fed and healed!");
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // Call the parent implementation for normal behavior
        return super.mobInteract(player, hand);
    }
}
