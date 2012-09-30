/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eurobank.soap.model;

import java.math.BigDecimal;

/**
 *
 * @author Tomasz Jankowski
 */
public enum FundUnit {

    AFundUnit("Jednostka A", 1.02, 1),
    BFundUnit("Jednostka B", 1, 0.98);

    private String name;
    private BigDecimal buyProvision;
    private BigDecimal sellProvision;

    private FundUnit(String name, double buyProvision, double sellProvision) {
        this.name = name;
        this.buyProvision = BigDecimal.valueOf(buyProvision);
        this.sellProvision = BigDecimal.valueOf(sellProvision);
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBuyProvision() {
        return buyProvision;
    }

    public BigDecimal getSellProvision() {
        return sellProvision;
    }

}
