package elit.express.exchangerates;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

public class Rates7Days {
    @SerializedName("base")
    @Expose
    private String base;
    @SerializedName("start_at")
    @Expose
    private String start_at;
    @SerializedName("end_at")
    @Expose
    private String end_at;
    @SerializedName("rates")
    @Expose
    private Map<String,Map<String,String>> rates;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getStart_at() {
        return start_at;
    }

    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }

    public String getEnd_at() {
        return end_at;
    }

    public void setEnd_at(String end_at) {
        this.end_at = end_at;
    }

    public Map<String, Map<String, String>> getRates() {
        return rates;
    }

    public void setRates(Map<String, Map<String, String>> rates) {
        this.rates = rates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rates7Days that = (Rates7Days) o;
        return base.equals(that.base) &&
                start_at.equals(that.start_at) &&
                end_at.equals(that.end_at) &&
                Objects.equals(rates, that.rates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, start_at, end_at, rates);
    }
}
