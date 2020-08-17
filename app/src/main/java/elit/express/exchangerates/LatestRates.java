package elit.express.exchangerates;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LatestRates {
    @SerializedName("base")
    @Expose
    private String base;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("rates")
    @Expose
    private Map<String,String> rates;

    private long timestamp=0;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String,String> getRates() {
        return rates;
    }

    public void setRates(Map<String,String> rates) {
        this.rates = rates;
    }

//    public HashMap<String,String> getRates() {
//        return rates;
//    }
//
//    public void setRates(HashMap<String,String> rates) {
//        this.rates = rates;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatestRates that = (LatestRates) o;
        return base.equals(that.base) &&
                date.equals(that.date) &&
                rates.equals(that.rates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, date, rates);
    }
}
