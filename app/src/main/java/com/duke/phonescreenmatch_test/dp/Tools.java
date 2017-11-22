package com.duke.phonescreenmatch_test.dp;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param sourceValue 原字符串 @dimen/dp_xxx 或 xxxdp
     * @param multiple    屏幕宽度dp基于360dp宽度的系数比值
     * @return 乘以系数后的缩放值字符串，且带单位
     */
    public static String countValue(boolean isFontMatch, String sourceValue, double multiple) {
        if (sourceValue == null || "".equals(sourceValue.trim())) {
            //无效值，不执行计算
            return "errorValue";
        }
        //去除值两端空格，包括引用值
        sourceValue = sourceValue.trim();
        // @dimen/dp_xxx
        // @dimen/sp_xxx
        if (sourceValue.startsWith("@dimen/")) {
            //引用值，不执行计算
            return sourceValue;
        }
        //替换非引用值的单位dip为dp
        if (sourceValue.endsWith("dip")) {
            //我只确保最后的dip替换成dp，你非要写成39dipdip这种恶心的值，我也管不了
            sourceValue = sourceValue.replaceAll("dip", "dp");
        }
        // xxpx
        // xxpt
        // ...
        if (!sourceValue.endsWith("dp") && !sourceValue.endsWith("sp")) {
            //非dp或sp数据，不执行计算
            return sourceValue;
        }
        if (sourceValue.endsWith("sp")) {
            if (!isFontMatch) {
                //如果为false，不执行计算
                return sourceValue;
            }
        }
        if (sourceValue.length() < 3) {
            //只剩下单位dp或sp，不执行计算
            return sourceValue;
        }
        int length = sourceValue.length();
        String endValue = sourceValue.substring(length - 2, length);//单位dp或sp
        String startValue = sourceValue.substring(0, length - 2);//数值
        endValue = endValue.trim();
        startValue = startValue.trim();
        if ("".equals(endValue) || "".equals(startValue)) {
            return sourceValue;
        }
        //乘以系数
        double temp = 0;
        try {
            temp = Double.parseDouble(startValue) * multiple;
        } catch (Exception e) {
            return sourceValue;
        }
        //数据格式化对象
        DecimalFormat df = new DecimalFormat("0.0000");
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

    /**
     * 递归删除目录
     *
     * @param file 待删除的文件或目录
     */
    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    // 递归删除
                    deleteFile(files[i]);
                }
            }
            try {
                // 删除当前空目录
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 是文件
            try {
                // 删除当前文件
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断当前文件目录名是否为 values-wXXXdp 格式，即以前的旧文件目录
     *
     * @param path           ..../res/values-wXXXdp
     * @param isUseNewFolder 是否使用新的目录格式 values-swXXXdp
     * @return 是否是指定格式的目录
     */
    public static boolean isOldFolder(String path, boolean isUseNewFolder) {
        if (path == null || path.trim().length() == 0) {
            return false;
        }

        String regEx = "";
        if (isUseNewFolder) {
            //即删除旧的目录 values-wXXXdp
            regEx = "^values-w[0-9]+dp$";
        } else {
            //删除新的目录格式 values-swXXXdp
            regEx = "^values-sw[0-9]+dp$";
        }
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find();
    }
}