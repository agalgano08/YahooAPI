package Main;

public class Company {
	public final String name;
	float currentPrice;
	float analystEstimate;
	String website;

	Company(String name) {
		this.name = name;
	}

	public float getAnalystEstimate() {
		return analystEstimate;
	}

	public float getCurrentPrice() {
		return currentPrice;
	}

}
