package eurobank.soap.model;

import java.math.BigDecimal;

public class Fund {
	
	private String fundName;
	private long fundId;
	private BigDecimal value;
	private FundUnit fundUnit;
	
	public Fund(long fundId, String fundName, FundUnit fundUnit, BigDecimal value) {
		this.fundId = fundId;
		this.fundName = fundName;
		this.fundUnit = fundUnit;
		this.value = value;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(fundName);
		builder.append(" - ");
		builder.append(fundUnit.getName());
		builder.append(" ( ");
		builder.append(value);
		builder.append(" )");
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
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public void setFundUnit(FundUnit fundUnit) {
		this.fundUnit = fundUnit;
	}
	public FundUnit getFundUnit() {
		return fundUnit;
	}
}
