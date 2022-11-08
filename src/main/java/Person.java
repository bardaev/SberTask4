import java.math.BigDecimal;

public class Person {
    private String name;
    private BigDecimal wallet;
    private BigDecimal sumWallet;
    private BigDecimal appendFromBank;

    public Person(String name, BigDecimal wallet) {
        this.name = name;
        this.wallet = wallet;
        this.appendFromBank = BigDecimal.ZERO;
        setSumWallet(wallet);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWallet() {
        return wallet;
    }

    public void setWallet(BigDecimal wallet) {
        this.wallet = wallet;
    }

    public BigDecimal getSumWallet() {
        return sumWallet;
    }

    private void setSumWallet(BigDecimal sumWallet) {
        this.sumWallet = sumWallet;
    }

    public BigDecimal getAppendFromBank() {
        return appendFromBank;
    }

    public void setAppendFromBank(BigDecimal appendFromBank) {
        setSumWallet(getSumWallet().add(appendFromBank));
        this.appendFromBank = this.appendFromBank.add(appendFromBank);
    }
}
