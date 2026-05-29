package roommate.distance;

public class SquaredDistance implements DistanceFunction {
    private final double maxDiff;

    public SquaredDistance(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public double calculate(double a, double b) {
        if (maxDiff <= 0.0) {
            return 0.0;
        }
        double normalized = Math.abs(a - b) / maxDiff;
        return Math.min(normalized * normalized, 1.0);
    }
}
