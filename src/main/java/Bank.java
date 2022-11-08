import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    private BigDecimal wallet;
    private List<Person> personList = new ArrayList<>();

    public BigDecimal getWallet() {
        return wallet;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public void setWallet(BigDecimal wallet) {
        this.wallet = wallet;
    }
}
