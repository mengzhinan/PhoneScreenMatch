package com.duke.phonescreenmatch_test.dp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 16:16
 * @UpdateTime: 2017-09-29 09:55
 * @Description: 入口
 */
public class Main {
    //基准dp，比喻：360dp
    private static final double DEFAULT_DP = 360;
    private static double baseDP = DEFAULT_DP;
    //默认支持的dp值
    private static final String[] defaultDPArr = new String[]{"384", "392", "400", "410", "411", "480", "533", "592", "600", "640", "662", "720", "768", "800", "811", "820", "960", "961", "1024", "1280", "1365"};

    //生成的values目录格式(代码中替换XXX字符串)
    public static String LETTER_REPLACE = "XXX";
    private static String VALUES_OLD_FOLDER = "values-wXXXdp";//values-w410dp，这个目录需要删除
    private static String VALUES_NEW_FOLDER = "values-swXXXdp";//values-sw410dp

    //是否删除旧的目录格式
    private static final boolean isDeleteAnotherFolder = true;

    //去重复的数据集合
    private static HashSet<Double> dataSet = new HashSet<>();

    /**
     * 命令行入口
     *
     * @param args 命令行参数[注意，命令行是以空格分割的]
     */
    public static void main(String[] args) {
        //获取当前目录的绝对路径
        String resFolderPath = new File("./res/").getAbsolutePath();
        String tempBaseDP = null;
        String[] needMatchs = null;
        String[] ignoreMatchs = null;
        if (args != null && args.length > 0) {
            /**
             * 调用Main函数，默认数组第一个为基准适配dp值
             */
            tempBaseDP = args[0];
            ignoreMatchs = new String[]{tempBaseDP};
            if (args.length > 1) {
                needMatchs = Arrays.copyOfRange(args, 1, args.length);
            }
        }
        start(true, tempBaseDP, needMatchs, ignoreMatchs, resFolderPath, true);
    }


    /**
     * 适配文件调用入口
     *
     * @param isFontMatch    字体是否也适配(是否与dp尺寸一样等比缩放)
     * @param tempBaseDP     基准dp值
     * @param needMatchs     待适配宽度dp值
     * @param ignoreMatchs   待忽略宽度dp值
     * @param resFolderPath  base dimens.xml 文件的res目录
     * @param isUseNewFolder 是否创建 values-swXXXdp 新格式的目录
     * @return 返回消息
     */
    public static String start(boolean isFontMatch, String tempBaseDP, String[] needMatchs, String[] ignoreMatchs, String resFolderPath, boolean isUseNewFolder) {
        if (tempBaseDP != null && !"".equals(tempBaseDP.trim())) {
            try {
                baseDP = Double.parseDouble(tempBaseDP.trim());
                if (baseDP <= 0) {
                    baseDP = DEFAULT_DP;
                }
            } catch (NumberFormatException e) {
                baseDP = DEFAULT_DP;
                e.printStackTrace();
            }
        } else {
            baseDP = DEFAULT_DP;
        }

        //添加默认的数据
        for (String aDefaultDPArr : defaultDPArr) {
            if (aDefaultDPArr == null || "".equals(aDefaultDPArr.trim())) {
                continue;
            }
            try {
                dataSet.add(Double.parseDouble(aDefaultDPArr.trim()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (needMatchs != null) {
            for (String needMatch : needMatchs) {
                if (needMatch == null || "".equals(needMatch.trim())) {
                    continue;
                }
                try {
                    double needMatchDouble = Double.parseDouble(needMatch.trim());
                    if (needMatchDouble > 0) {
                        dataSet.add(needMatchDouble);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (ignoreMatchs != null) {
            for (String ignoreMatch : ignoreMatchs) {
                if (ignoreMatch == null || "".equals(ignoreMatch.trim())) {
                    continue;
                }
                try {
                    dataSet.remove(Double.parseDouble(ignoreMatch.trim()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("基准宽度dp值：[ " + Tools.cutLastZero(baseDP) + " dp ]");
        System.out.println("本次待适配的宽度dp值: [ " + Tools.getOrderedString(dataSet) + " ]");
        //获取基准的dimens.xml文件
        String baseDimenFilePath = resFolderPath + File.separator + "values" + File.separator + "dimens.xml";
        File testBaseDimenFile = new File(baseDimenFilePath);
        //判断基准文件是否存在
        if (!testBaseDimenFile.exists()) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 路径下的文件找不到!");
            return "对应Module \"./res/values/dimens.xml\" 路径下的文件找不到!";
        }
        //解析源dimens.xml文件
        ArrayList<DimenItem> list = XmlIO.readDimenFile(baseDimenFilePath);
        if (list == null || list.size() <= 0) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 文件无数据!");
            return "\"./res/values/dimens.xml\" 文件无数据!";
        } else {
            System.out.println("OK \"./res/values/dimens.xml\" 基准dimens文件解析成功!");
        }
        try {
            //循环指定的dp参数，生成对应的dimens-swXXXdp.xml文件
            Iterator<Double> iterator = dataSet.iterator();
            while (iterator.hasNext()) {
                double item = iterator.next();
                //获取当前dp除以baseDP后的倍数
                double multiple = item / baseDP;

                //待输出的目录
                String outFolderPath = "";
                //待删除的目录
                String delFolderPath = "";
                //values目录上带的dp整数值
                String folderDP = String.valueOf((int) item);

                if (isUseNewFolder) {
                    outFolderPath = VALUES_NEW_FOLDER.replace(LETTER_REPLACE, folderDP);
                    delFolderPath = VALUES_OLD_FOLDER.replace(LETTER_REPLACE, folderDP);
                } else {
                    outFolderPath = VALUES_OLD_FOLDER.replace(LETTER_REPLACE, folderDP);
                    delFolderPath = VALUES_NEW_FOLDER.replace(LETTER_REPLACE, folderDP);
                }
                outFolderPath = resFolderPath + File.separator + outFolderPath + File.separator;
                delFolderPath = resFolderPath + File.separator + delFolderPath + File.separator;


                if (isDeleteAnotherFolder) {
                    /**
                     * 删除以前适配方式的目录values-wXXXdp
                     */
                    File oldFile = new File(delFolderPath);
                    if (oldFile.exists() && oldFile.isDirectory() && Tools.isOldFolder(oldFile.getName(), isUseNewFolder)) {
                        //找出res目录下符合要求的values目录，然后递归删除values目录
                        Tools.deleteFile(oldFile);
                    }
                }


                /**
                 * 生成新的目录values-swXXXdp
                 */
                //创建当前dp对应的dimens文件目录
                new File(outFolderPath).mkdirs();


                //生成的dimens文件的路径
                String outPutFile = outFolderPath + "dimens.xml";
                //生成目标文件dimens.xml输出目录
                XmlIO.createDestinationDimens(isFontMatch, list, multiple, outPutFile);
            }
            System.out.println("OK ALL OVER，全部生成完毕！");
            //适配完成
            return "Over, adapt successful";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}