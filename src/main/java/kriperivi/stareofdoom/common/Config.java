package kriperivi.stareofdoom.common;

public class Config {
    private final double maxDistanceSquared;
    private final int stareThreshold;
    private final int stareFalloff;
    private final boolean strikeLightning;

    public double getMaxDistanceSquared() {
        return maxDistanceSquared;
    }

    public int getStareThreshold() {
        return stareThreshold;
    }

    public int getStareFalloff() {
        return stareFalloff;
    }

    public boolean doStrikeLightning() {
        return strikeLightning;
    }

    public Config(double maxDistanceSquared, int stareThreshold, int stareFalloff, boolean strikeLightning) {
        this.maxDistanceSquared = maxDistanceSquared;
        this.stareThreshold = stareThreshold;
        this.stareFalloff = stareFalloff;
        this.strikeLightning = strikeLightning;
    }
}
