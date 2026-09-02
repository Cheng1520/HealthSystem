package com.ncu.csh.util;

/**
 * 结果分析工具 —— 依据参考范围判断检查数值为 正常/偏高/偏低。
 * 抽离为纯静态逻辑，便于单元测试与复用。
 */
public class ResultAnalyzer {

    /**
     * 分析检测值。
     *
     * @param value  检测数值
     * @param refMin 参考下限（可为 null，表示不设下限）
     * @param refMax 参考上限（可为 null，表示不设上限）
     * @return "偏低" / "偏高" / "正常"
     */
    public static String analyze(double value, Double refMin, Double refMax) {
        if (refMin != null && value < refMin) {
            return "偏低";
        }
        if (refMax != null && value > refMax) {
            return "偏高";
        }
        return "正常";
    }
}
