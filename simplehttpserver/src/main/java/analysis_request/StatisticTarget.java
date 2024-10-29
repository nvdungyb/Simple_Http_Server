package analysis_request;

import lombok.Data;

@Data
public class StatisticTarget implements Comparable<StatisticTarget> {
    public String target;
    public int numberRequests;

    public StatisticTarget(String element, int score) {
        this.target = element;
        this.numberRequests = score;
    }

    @Override
    public int compareTo(StatisticTarget o) {
        return Integer.compare(this.numberRequests, o.numberRequests);
    }

    @Override
    public String toString() {
        return "AnalysisTarget{" + "numberRequests=" + numberRequests + ", target='" + target + '\'' + '}';
    }
}
