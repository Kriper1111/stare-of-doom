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


import static kriperivi.stareofdoom.StareOfDoom.LOGGER;
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

        EntityPlayer player = worldObj.getClosestPlayer(this.posX, this.posY, this.posZ, Config.maxDistance);
        if (player == null) {
            return;
        }

        double diffX = this.posX - player.posX;
        double diffY = this.boundingBox.minY + (double)(this.height / 2.0F) - (player.posY + (double)player.getEyeHeight());
        double diffZ = this.posZ - player.posZ;

        long ticksStared = this.getEntityData().getLong(TAG_NAME);
        ticksStared = updateStaring(ticksStared, player, diffX, diffY, diffZ);
        if (ticksStared >= Config.stareThreshold) {
            eviscerate();
            ticksStared = 0;
        } else if (ticksStared < 0) {
            ticksStared = 0;
        }
        this.getEntityData().setLong(TAG_NAME, ticksStared);
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

    private long updateStaring(long ticksStared, EntityPlayer player, double diffX, double diffY, double diffZ) {
        double tetherDistance = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

        if (!player.canEntityBeSeen(this)) {
            return 0;
        }

        Vec3 tether = Vec3.createVectorHelper(diffX, diffY, diffZ);

        double dot = player.getLook(1.0F).normalize().dotProduct(tether.normalize());

        if (dot < (1.0 - 0.05 / tetherDistance)) {
            return ticksStared - Config.stareFalloff;
        }

        LOGGER.debug("[{}] Ticks Stared {}", getUniqueID(), ticksStared);

        return ticksStared + 1;
    }
}
