package eurobank.soap.model;

public class WalletItem {
	
	private String fundName;
	private long fundId;
	private int quantity;
	private FundUnit fundUnit;
	
	public WalletItem(long fundId, String fundName, FundUnit fundUnit, int quantity) {
		this.fundId = fundId;
		this.fundName = fundName;
		this.fundUnit = fundUnit;
		this.quantity = quantity;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(fundName);
		builder.append(" - ");
		builder.append(fundUnit.getName());
		builder.append(" x ");
		builder.append(quantity);
		return builder.toString();
	}
	
	public void setFundName(String fundName) {
		this.fundName = fundName;
	}
	public String getFundName() {
		return fundName;
	}
	public void setFundId(long fundId) {
		this.fundId = fundId;
	}
	public long getFundId() {
		return fundId;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setFundUnit(FundUnit fundUnit) {
		this.fundUnit = fundUnit;
	}
	public FundUnit getFundUnit() {
		return fundUnit;
	}

}
