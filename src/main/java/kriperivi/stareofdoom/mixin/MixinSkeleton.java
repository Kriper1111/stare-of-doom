package kriperivi.stareofdoom.mixin;

import kriperivi.stareofdoom.common.Config;
import net.minecraft.client.Minecraft;
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

@Mixin(EntitySkeleton.class)
abstract class MixinSkeleton extends EntityLiving {
    private long ticksStared = 0;

    public MixinSkeleton(World p_i45324_1_) {
        super(p_i45324_1_);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
    private void onLivingUpdate(CallbackInfo info) {
        if (worldObj.isRemote) {
            return;
        }
        if (this.dead) {
            return;
        }

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }

        double diffX = this.posX - player.posX;
        double diffY = this.boundingBox.minY + (double)(this.height / 2.0F) - (player.posY + (double)player.getEyeHeight());
        double diffZ = this.posZ - player.posZ;

        double tetherDistance = (diffX * diffY * diffZ);

        if (tetherDistance > Config.maxDistance) {
            ticksStared = 0;
            return;
        }

        if (!player.canEntityBeSeen(this)) {
            ticksStared = 0;
            return;
        }

        LOGGER.info("[{}] Stare Timer = {} / {}", this.entityUniqueID, ticksStared, Config.stareThreshold);
        Vec3 tether = Vec3.createVectorHelper(diffX, diffY, diffZ);
        tetherDistance = Math.sqrt(tetherDistance);

        double dot = player.getLook(1.0F).normalize().dotProduct(tether.normalize());

        if (dot < (1.0 - Config.getSpreadCorrection() / tetherDistance)) {
            ticksStared = Math.max(ticksStared - Config.stareFalloff, 0);
            return;
        }

        ticksStared += 1;
        if (ticksStared >= Config.stareThreshold) {
            if (Config.strikeLightning) {
                this.worldObj.addWeatherEffect(new EntityLightningBolt(this.worldObj, this.posX, this.posY, this.posZ));
            } else {
                // Spawn instant damage particle cloud, and still play the thunder sounds.
                this.worldObj.playAuxSFX(2002, (int)Math.round(this.posX), (int)Math.round(this.posY), (int)Math.round(this.posZ), 16428);
                this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
                this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.explode", 1.0F, 0.5F + this.rand.nextFloat() * 0.2F);
            }
            this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
            ticksStared = 0;
        }
    }
}
