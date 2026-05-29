package roommate.distance;

public class AbsoluteDistance implements DistanceFunction {
    private final double maxDiff;

    public AbsoluteDistance(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public double calculate(double a, double b) {
        if (maxDiff <= 0.0) {
            return 0.0;
        }
        return Math.min(Math.abs(a - b) / maxDiff, 1.0);
    }
}
