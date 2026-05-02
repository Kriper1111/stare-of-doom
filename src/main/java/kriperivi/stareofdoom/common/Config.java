package kriperivi.stareofdoom.common;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;


import static kriperivi.stareofdoom.StareOfDoom.LOGGER;

public class Config {
    public static double maxDistance;
    public static int stareThreshold;
    public static int stareFalloff;
    public static boolean strikeLightning;

    public static void init(FMLPreInitializationEvent evt) {
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();

        float distance = config.getFloat("maxDistance", "doom", 32.0f, 0, 64.0f, "Maximum distance between you and the skeleton after which it's no longer doomed.");
        float stareTime = config.getFloat("stareTime", "doom", 5.0f, 0.2f, 60.0f, "How long do you have to stare at the skeleton until it's eviscerated.");
        float stareCooldown = config.getFloat("stareCooldown", "doom", 0.2f, -1.0f, 60.0f, "How fast should the doom counter tick down if you're not looking at it.");
        strikeLightning = config.getBoolean("castLightning", "doom", true, "If the skeleton is eviscerated, should it get struck by lightning or disappear in smoke?");

        maxDistance = Math.pow(distance, 2);
        stareThreshold = (int) (stareTime * 20);
        if (stareCooldown < 0.0) {
            stareFalloff = stareThreshold;
        } else {
            stareFalloff = (int) (stareCooldown * 20);
        }

        config.save();
        LOGGER.debug("Successfully read and reloaded Stare of DOOM's config.");
    }

    public static float getSpreadCorrection() {
        return 0.025f;
    }

    public static int getAUXSfxIdentifier() {
        return 2002;
    }

    public static int getAUXSfxValue() {
        return 16428;
    }
}
