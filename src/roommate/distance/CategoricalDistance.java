package roommate.distance;

public class CategoricalDistance implements DistanceFunction {
    @Override
    public double calculate(double a, double b) {
        return Double.compare(a, b) == 0 ? 0.0 : 1.0;
    }
}
