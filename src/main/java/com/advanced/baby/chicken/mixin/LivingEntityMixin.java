package com.advanced.baby.chicken.mixin;

import com.advanced.baby.chicken.AdvancedBabyChicken;
import com.advanced.baby.chicken.TotemBabyChicken;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for LivingEntity to handle baby chicken totem death protection.
 * This is needed because we can't inject into parent class methods from the Chicken mixin.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean isBaby();

    /**
     * Check if this entity is a baby chicken with a totem that should provide death protection.
     */
    @Unique
    private boolean advancedBabyChicken$checkTotemDeathProtection(DamageSource damageSource) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Only applies to baby chickens
        if (!(self instanceof Chicken chicken) || !chicken.isBaby()) {
            return false;
        }

        // Check if it has a totem
        if (!(chicken instanceof TotemBabyChicken totemChicken) || !totemChicken.advancedBabyChicken$hasTotem()) {
            return false;
        }

        // Void damage and similar bypasses totem protection
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        ItemStack totem = totemChicken.advancedBabyChicken$getTotem().copy();
        DeathProtection protection = totem.get(DataComponents.DEATH_PROTECTION);

        if (protection == null) {
            return false;
        }

        // Consume the totem
        totemChicken.advancedBabyChicken$setTotem(ItemStack.EMPTY);

        // Set health to 1
        this.setHealth(1.0F);

        // Apply totem effects (regeneration, absorption, fire resistance)
        protection.applyEffects(totem, self);

        // Broadcast the totem animation event (event 35 = totem of undying activation)
        self.level().broadcastEntityEvent(self, (byte) 35);

        // Cause game event
        totem.causeUseVibration(self, GameEvent.ITEM_INTERACT_FINISH);

        AdvancedBabyChicken.LOGGER.debug("Baby chicken's totem of undying activated!");

        return true;
    }

    /**
     * Inject into hurtServer to check for totem death protection before die() is called.
     */
    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            cancellable = true)
    private void checkBabyChickenTotemProtection(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (this.advancedBabyChicken$checkTotemDeathProtection(source)) {
            // Death was prevented, don't call die()
            cir.setReturnValue(true);
        }
    }

    /**
     * Drop the totem when the baby chicken dies.
     */
    @Inject(method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("TAIL"))
    private void dropBabyChickenTotemOnDeath(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof Chicken chicken && chicken.isBaby()) {
            if (chicken instanceof TotemBabyChicken totemChicken && totemChicken.advancedBabyChicken$hasTotem()) {
                ItemStack totem = totemChicken.advancedBabyChicken$getTotem();
                chicken.spawnAtLocation(level, totem);
                totemChicken.advancedBabyChicken$setTotem(ItemStack.EMPTY);
            }
        }
    }
}
