package com.advanced.baby.chicken.goal;

import com.advanced.baby.chicken.AdvancedBabyChicken;
import com.advanced.baby.chicken.SleepingBabyChicken;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AI Goal that makes baby chickens search for beds at nighttime and sit on top of them.
 * Similar to villager behavior but adapted for baby chickens.
 */
public class BabyChickenSleepOnBedGoal extends Goal {
    private final Chicken chicken;
    private final Level level;
    private final PathNavigation navigation;
    private final double speedModifier;
    private final int searchRange;

    private BlockPos targetBedPos;
    private boolean isOnBed;
    private int ticksOnBed;
    private int pathfindCooldown;

    private static final int MAX_SEARCH_RANGE = 16;
    private static final int PATHFIND_COOLDOWN_TICKS = 20;

    public BabyChickenSleepOnBedGoal(Chicken chicken, double speedModifier) {
        this.chicken = chicken;
        this.level = chicken.level();
        this.navigation = chicken.getNavigation();
        this.speedModifier = speedModifier;
        this.searchRange = MAX_SEARCH_RANGE;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only baby chickens use this goal
        if (!chicken.isBaby()) {
            return false;
        }

        // Only at night time - use isDarkOutside() which checks sky brightness
        if (!isNightTime()) {
            return false;
        }

        // If already on bed, continue
        if (isOnBed && targetBedPos != null) {
            return true;
        }

        // Search for a nearby bed
        targetBedPos = findNearbyBed();
        return targetBedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if chicken grows up
        if (!chicken.isBaby()) {
            return false;
        }

        // Stop during daytime (but give a little grace period)
        if (!isNightTime() && ticksOnBed > 100) {
            return false;
        }

        // Stop if bed is destroyed
        if (targetBedPos != null && !isBedAt(targetBedPos)) {
            return false;
        }

        return targetBedPos != null;
    }

    @Override
    public void start() {
        isOnBed = false;
        ticksOnBed = 0;
        pathfindCooldown = 0;

        if (targetBedPos != null) {
            AdvancedBabyChicken.LOGGER.debug("Baby chicken starting to seek bed at {}", targetBedPos);
        }
    }

    @Override
    public void stop() {
        navigation.stop();
        targetBedPos = null;
        isOnBed = false;
        ticksOnBed = 0;
        pathfindCooldown = 0;

        // Update sleeping state
        if (chicken instanceof SleepingBabyChicken sleepingChicken) {
            sleepingChicken.advancedBabyChicken$setSleepingOnBed(false);
        }
    }

    @Override
    public void tick() {
        if (targetBedPos == null) {
            return;
        }

        // Check if we're close enough to the bed
        double distanceSq = chicken.blockPosition().distSqr(targetBedPos);

        if (distanceSq < 2.5) {
            // We're at the bed
            if (!isOnBed) {
                // Move on top of the bed
                moveOnTopOfBed();
            } else {
                // Stay on the bed, look around occasionally
                ticksOnBed++;
                navigation.stop();

                // Small random movements to look cozy
                if (ticksOnBed % 100 == 0) {
                    chicken.setYRot(chicken.getRandom().nextFloat() * 360.0F);
                }
            }
        } else {
            // Navigate to the bed
            isOnBed = false;
            ticksOnBed = 0;

            if (pathfindCooldown <= 0) {
                boolean success = navigation.moveTo(
                    targetBedPos.getX() + 0.5,
                    targetBedPos.getY() + 0.5625, // Bed height
                    targetBedPos.getZ() + 0.5,
                    speedModifier
                );

                if (!success) {
                    // Try to find a different bed if pathfinding fails
                    pathfindCooldown = PATHFIND_COOLDOWN_TICKS * 3;
                    targetBedPos = findNearbyBed();
                } else {
                    pathfindCooldown = PATHFIND_COOLDOWN_TICKS;
                }
            } else {
                pathfindCooldown--;
            }
        }
    }

    private void moveOnTopOfBed() {
        if (targetBedPos == null) return;

        // Position the chicken on top of the bed
        double bedX = targetBedPos.getX() + 0.5;
        double bedY = targetBedPos.getY() + 0.5625; // Bed is 9/16 blocks tall
        double bedZ = targetBedPos.getZ() + 0.5;

        chicken.setPos(bedX, bedY, bedZ);
        chicken.setDeltaMovement(Vec3.ZERO);
        navigation.stop();

        isOnBed = true;
        ticksOnBed = 0;

        // Update sleeping state for rendering
        if (chicken instanceof SleepingBabyChicken sleepingChicken) {
            sleepingChicken.advancedBabyChicken$setSleepingOnBed(true);
        }

        AdvancedBabyChicken.LOGGER.debug("Baby chicken settled on bed at {}", targetBedPos);
    }

    private boolean isNightTime() {
        // Use the level's isDarkOutside() method which checks sky brightness
        // This works across dimensions and respects the dimension's time settings
        return level.isDarkOutside();
    }

    private BlockPos findNearbyBed() {
        BlockPos chickenPos = chicken.blockPosition();
        BlockPos closestBed = null;
        double closestDistSq = Double.MAX_VALUE;

        // Search in a cube around the chicken
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos checkPos = chickenPos.offset(x, y, z);

                    if (isBedHeadAt(checkPos)) {
                        double distSq = chickenPos.distSqr(checkPos);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closestBed = checkPos;
                        }
                    }
                }
            }
        }

        return closestBed;
    }

    private boolean isBedAt(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.BEDS);
    }

    private boolean isBedHeadAt(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.BEDS)) {
            return false;
        }

        // Prefer the head part of the bed for positioning
        if (state.getBlock() instanceof BedBlock) {
            return state.getValue(BedBlock.PART) == BedPart.HEAD;
        }

        return true;
    }
}
