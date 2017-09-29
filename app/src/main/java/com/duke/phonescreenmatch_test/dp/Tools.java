package com.duke.phonescreenmatch_test.dp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * 算法工具类
 *
 * @UpdateTime: 2017-09-29 09:55
 */
public class Tools {

    /**
     * 乘以系数
     *
     * @param isFontMatch 字体是否也适配(是否与dp尺寸一样等比缩放)
     * @param sourceValue 原字符串
     * @param multiple    乘以系数后，且带单位的字符串
     * @return
     */
    public static String countValue(boolean isFontMatch, String sourceValue, double multiple) {
        if (sourceValue == null) {
            return "";
        }
        sourceValue = sourceValue.trim();
        if ("".equals(sourceValue)
                || sourceValue.startsWith("@dimen/")
                || sourceValue.length() < 3
                || "dip".equals(sourceValue)) {
            return sourceValue;
        }
        if (!sourceValue.endsWith("dp") && !sourceValue.endsWith("dip") && !sourceValue.endsWith("sp")) {
            return sourceValue;
        }
        String endValue = null;
        String startValue = null;
        if (sourceValue.endsWith("dip")) {
            endValue = "dip";
            startValue = sourceValue.substring(0, sourceValue.length() - 3);
        }
        if (sourceValue.endsWith("dp")) {
            endValue = "dp";
            startValue = sourceValue.substring(0, sourceValue.length() - 2);
        }
        if (sourceValue.endsWith("sp")) {
            if (!isFontMatch) {
                //如果为false，则字体不适配缩放
                return sourceValue;
            }
            endValue = "sp";
            startValue = sourceValue.substring(0, sourceValue.length() - 2);
        }
        if (endValue == null
                || "".equals(endValue.trim())
                || "".equals(startValue.trim())) {
            return sourceValue;
        }
        //乘以系数
        double temp = 0;
        try {
            temp = Double.parseDouble(startValue.trim()) * multiple;
        } catch (Exception e) {
            return sourceValue;
        }
        //数据格式化对象
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(temp) + endValue;
    }

    /**
     * 把set集合数据转成字符串，并有序的返回
     *
     * @param set
     * @return
     */
    public static String getOrderedString(HashSet<Double> set) {
        if (set == null || set.size() <= 0) {
            return "";
        }
        ArrayList<Double> list = new ArrayList<>();
        list.addAll(set);
        Object[] arr = list.toArray();
        Arrays.sort(arr);
        StringBuilder stringBuffer = new StringBuilder();
        for (Object anArr : arr) {
            stringBuffer.append(cutLastZero(Double.parseDouble(anArr.toString()))).append(", ");
        }
        String result = stringBuffer.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    /**
     * 去除浮点型后面多余的0
     *
     * @param value
     * @return
     */
    public static String cutLastZero(double value) {
        if (value <= 0) {
            return "0";
        }
        String sourceValue = String.valueOf(value);
        String result = "";
        if (sourceValue.contains(".")) {//带小数
            // 去除后面的0
            while (sourceValue.charAt(sourceValue.length() - 1) == '0') {
                sourceValue = sourceValue.substring(0, sourceValue.length() - 1);
            }
            //删除最后的点
            if (sourceValue.endsWith(".")) {
                sourceValue = sourceValue.substring(0, sourceValue.length() - 1);
            }
            result = sourceValue;
        }
        return result;
    }
}