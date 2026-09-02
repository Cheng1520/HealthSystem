package com.ncu.csh.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ResultAnalyzer 单元测试 —— 覆盖 偏低/偏高/正常/边界值/无参考范围 五种情况。
 */
public class ResultAnalyzerTest {

    @Test
    public void valueBelowMinShouldBeLow() {
        assertEquals("偏低", ResultAnalyzer.analyze(100, 115.0, 150.0));
    }

    @Test
    public void valueAboveMaxShouldBeHigh() {
        assertEquals("偏高", ResultAnalyzer.analyze(160, 115.0, 150.0));
    }

    @Test
    public void valueInRangeShouldBeNormal() {
        assertEquals("正常", ResultAnalyzer.analyze(120, 115.0, 150.0));
    }

    @Test
    public void valueAtLowerBoundShouldBeNormal() {
        assertEquals("正常", ResultAnalyzer.analyze(115, 115.0, 150.0));
    }

    @Test
    public void valueAtUpperBoundShouldBeNormal() {
        assertEquals("正常", ResultAnalyzer.analyze(150, 115.0, 150.0));
    }

    @Test
    public void valueWithoutRangeShouldBeNormal() {
        assertEquals("正常", ResultAnalyzer.analyze(120, null, null));
    }
}
