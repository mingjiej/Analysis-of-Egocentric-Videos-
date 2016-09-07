

public class Trajectory {
	private double x;
	private double y;
	private double a;
	public Trajectory() {
		
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getA() {
		return a;
	}

	public Trajectory(double x, double y, double a) {
		this.x = x;
		this.y = y;

		this.a = a;
	}
	public static Trajectory add(Trajectory a, Trajectory b) {
		return new Trajectory(a.x+b.x, a.y+b.y, a.a + b.a);
	}
	public static Trajectory vinus(Trajectory a, Trajectory b) {
		return new Trajectory(a.x-b.x, a.y-b.y, a.a - b.a);
	}
	public static Trajectory multiply(Trajectory a, Trajectory b) {
		return new Trajectory(a.x*b.x, a.y*b.y, a.a * b.a);
	}
	public static Trajectory divide(Trajectory a, Trajectory b) {
		return new Trajectory(a.x/b.x, a.y/b.y, a.a/b.a);
	}
}
