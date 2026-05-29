package roommate.distance;

public class SigmoidDistance implements DistanceFunction {
    private final double k;
    private final double threshold;
    private final double maxDiff;

    public SigmoidDistance(double k, double threshold, double maxDiff) {
        this.k = k;
        this.threshold = threshold;
        this.maxDiff = maxDiff;
    }

    @Override
    public double calculate(double a, double b) {
        double diff = Math.min(Math.abs(a - b), maxDiff);
        double min = raw(0.0);
        double max = raw(maxDiff);
        if (max == min) {
            return 0.0;
        }
        return clamp((raw(diff) - min) / (max - min));
    }

    private double raw(double diff) {
        return 1.0 / (1.0 + Math.exp(-k * (diff - threshold)));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
