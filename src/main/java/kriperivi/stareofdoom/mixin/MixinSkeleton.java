package kriperivi.stareofdoom.mixin;

import kriperivi.stareofdoom.common.Config;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.List;

import static kriperivi.stareofdoom.StareOfDoom.TAG_NAME;

@Mixin(EntitySkeleton.class)
abstract class MixinSkeleton extends EntityLiving {
    public MixinSkeleton(World p_i45324_1_) {
        super(p_i45324_1_);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void onLivingUpdate(CallbackInfo info) {
        if (this.dead) {
            return;
        }

        if (this.worldObj == null || this.worldObj.isRemote) {
            return;
        }

        // We know this is EntityPlayer at least
        //noinspection unchecked
        List<EntityPlayer> playerList = worldObj.playerEntities;
        long ticksStaredIn = this.getEntityData().getLong(TAG_NAME);
        long ticksStaredOut = ticksStaredIn;
        boolean isSpared = true;
        for (final EntityPlayer player : playerList) {
            if (player.dimension != this.dimension) {
                continue;
            }

            double diffX = this.posX - player.posX;
            double diffY = this.boundingBox.minY + (double)(this.height / 2.0F) - (player.posY + (double)player.getEyeHeight());
            double diffZ = this.posZ - player.posZ;

            if (isBeingStaredAt(player, diffX, diffY, diffZ)) {
                ticksStaredOut = Math.min(ticksStaredOut + 1, Config.stareThreshold);
                isSpared = false;
            }
            if (ticksStaredOut >= Config.stareThreshold) {
                eviscerate();
                ticksStaredOut = 0;
                break;
            }
        }

        if (isSpared) {
            ticksStaredOut = Math.max(ticksStaredOut - Config.stareFalloff, 0);
        }

        if (ticksStaredIn != ticksStaredOut) {
            this.getEntityData().setLong(TAG_NAME, ticksStaredOut);
        }
    }

    private void eviscerate() {
        if (Config.strikeLightning) {
            this.worldObj.addWeatherEffect(new EntityLightningBolt(this.worldObj, this.posX, this.posY, this.posZ));
        } else {
            // Spawn instant damage particle cloud, and still play the thunder sounds.
            this.worldObj.playAuxSFX(2002, (int)Math.round(this.posX), (int)Math.round(this.posY), (int)Math.round(this.posZ), 16428);
            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.explode", 1.0F, 0.5F + this.rand.nextFloat() * 0.2F);
        }
        this.kill();
        this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
    }

    private boolean isBeingStaredAt(EntityPlayer player, double diffX, double diffY, double diffZ) {
        double tetherDistance = diffX * diffX + diffY * diffY + diffZ * diffZ;

        if (tetherDistance > Config.maxDistanceSquared) {
            return false;
        }

        if (!player.canEntityBeSeen(this)) {
            return false;
        }

        Vec3 lookVector = player.getLookVec();
        tetherDistance = Math.sqrt(tetherDistance);
        double dot = (lookVector.xCoord * diffX + lookVector.yCoord * diffY + lookVector.zCoord * diffZ) / tetherDistance;
        return (dot > (1.0 - 0.05 / tetherDistance));
    }
}
