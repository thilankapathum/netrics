package dev.thilanka.netrics.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
//@Setter
@RequiredArgsConstructor
@Service
public class ConsoleProgress {

    private int basicKpiCompleted = 0;
    private int basicKpiTotal = 0;
    private int kpiTrendCompleted = 0;
    private int kpiTrendTotal = 0;
    private int worstCellCompleted = 0;
    private int worstCellTotal = 0;
    private String ratName = "";

    private Map<String, Integer> basicKpiCompletedMap = new HashMap<>();
    private Map<String, Integer> kpiTrendCompletedMap = new HashMap<>();
    private Map<String, Integer> worstCellCompletedMap = new HashMap<>();
    private Map<String, Integer> basicKpiTotalMap = new HashMap<>();
    private Map<String, Integer> kpiTrendTotalMap = new HashMap<>();
    private Map<String, Integer> worstCellTotalMap = new HashMap<>();

    public void setBasicKpiTotalMap(String ratName, Integer value) {
        this.basicKpiTotalMap.putIfAbsent(ratName, value);
        this.basicKpiTotalMap.replace(ratName, value);
    }

    public void setBasicKpiCompletedMap(String ratName, Integer value) {
        this.basicKpiCompletedMap.putIfAbsent(ratName, value);
        if (this.basicKpiCompletedMap.get(ratName) != value) {
            printLog(value, this.kpiTrendCompletedMap.get(ratName), this.worstCellCompletedMap.get(ratName), ratName);
            this.basicKpiCompletedMap.replace(ratName, value);
        }
    }

    public void setKpiTrendTotalMap(String ratName, Integer value) {
        this.kpiTrendTotalMap.putIfAbsent(ratName, value);
        this.kpiTrendTotalMap.replace(ratName, value);
    }

    public void setKpiTrendCompletedMap(String ratName, Integer value) {
        this.kpiTrendCompletedMap.putIfAbsent(ratName,value);
        if (this.kpiTrendCompletedMap.get(ratName) != value) {
            printLog(this.basicKpiCompletedMap.get(ratName), value, this.worstCellCompletedMap.get(ratName), ratName);
            this.kpiTrendCompletedMap.replace(ratName, value);
        }
    }

    public void setWorstCellTotalMap(String ratName, Integer value) {
        this.worstCellTotalMap.putIfAbsent(ratName, value);
        this.worstCellTotalMap.replace(ratName, value);
    }

    public void setWorstCellCompletedMap(String ratName, Integer value) {
        this.worstCellCompletedMap.putIfAbsent(ratName,value);
        if (this.worstCellCompletedMap.get(ratName) != value) {
            printLog(this.basicKpiCompletedMap.get(ratName), this.kpiTrendCompletedMap.get(ratName), value, ratName);
            this.worstCellCompletedMap.replace(ratName, value);
        }
    }

    // Setter with logging
    public void setBasicKpiCompleted(Integer value) {
        if (this.basicKpiCompleted != value) {
//            System.out.println("basicKpiCompleted changed from " + this.basicKpiCompleted + " to " + value);
            printLog(value, this.kpiTrendCompleted, this.worstCellCompleted, this.ratName);
            this.basicKpiCompleted = value;
        }
    }

    public void setBasicKpiTotal(int value) {
        this.basicKpiTotal = value;
    }

    public void setKpiTrendTotal(int value) {
        this.kpiTrendTotal = value;
    }

    public void setWorstCellTotal(int value) {
        this.worstCellTotal = value;
    }

    public void setRatName(String ratName) {
        this.ratName = ratName;
    }

    public void setKpiTrendCompleted(int value) {
        if (this.kpiTrendCompleted != value) {
//            System.out.println("kpiTrendCompleted changed from " + this.kpiTrendCompleted + " to " + value);
            printLog(this.basicKpiCompleted, value, this.worstCellCompleted, this.ratName);
            this.kpiTrendCompleted = value;
        }
    }


    public void setWorstCellCompleted(int value) {
        if (this.worstCellCompleted != value) {
//            System.out.println("worstCellCompleted changed from " + this.worstCellCompleted + " to " + value);
            printLog(this.basicKpiCompleted, this.kpiTrendCompleted, value, this.ratName);
            this.worstCellCompleted = value;
        }
    }

    public void printLog(Integer basicKpiValue, Integer kpiTrendValue, Integer worstCellValue, String ratName) {
        System.out.println(String.format("[%s] Basic KPI: %d/%d (%.1f%%) | KPI Trend: %d/%d (%.1f%%) | Worst Cells: %d/%d (%.1f%%)",
                ratName, basicKpiValue, this.basicKpiTotalMap.get(ratName), (basicKpiValue * 100.0 / this.basicKpiTotalMap.get(ratName)),
                kpiTrendValue, this.kpiTrendTotalMap.get(ratName), (kpiTrendValue * 100.0 / this.kpiTrendTotalMap.get(ratName)),
                worstCellValue, this.worstCellTotalMap.get(ratName), (worstCellValue * 100.0 / this.worstCellTotalMap.get(ratName))));

//        System.out.println(String.format("[%s] Basic KPI: %d/%d (%.1f%%)",
//                ratName, basicKpiValue, this.basicKpiTotal, (basicKpiValue * 100.0 / this.basicKpiTotal)
//                ));
    }
}
