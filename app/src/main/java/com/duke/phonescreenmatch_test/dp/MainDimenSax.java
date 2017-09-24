package com.duke.phonescreenmatch_test.dp;

import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 16:16
 * @Description: dp适配文件生成工具类
 */
public class MainDimenSax {
    //基准dp，比喻：360dp
    public static final double DEFAULT_DP = 360.00;
    private static double baseDP = DEFAULT_DP;
    //默认支持的dp值
    private static final String[] defaultDPArr = new String[]{"384", "392", "400", "410", "411", "480", "533", "592", "600", "640", "662", "720", "768", "800", "811", "820", "960", "961", "1024", "1280", "1365"};
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
        start(tempBaseDP, needMatchs, ignoreMatchs, resFolderPath);
    }


    /**
     * 适配文件调用入口
     *
     * @param tempBaseDP    基准dp值
     * @param needMatchs    待适配宽度dp值
     * @param ignoreMatchs  待忽略宽度dp值
     * @param resFolderPath base dimens.xml 文件的res目录
     */
    public static void start(String tempBaseDP, String[] needMatchs, String[] ignoreMatchs, String resFolderPath) {
        try {
            baseDP = Double.parseDouble(tempBaseDP);
        } catch (NumberFormatException e) {
            baseDP = DEFAULT_DP;
            e.printStackTrace();
        }
        //添加默认的数据
        for (int i = 0; i < defaultDPArr.length; i++) {
            try {
                dataSet.add(Double.parseDouble(defaultDPArr[i]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (needMatchs != null) {
            for (int i = 0; i < needMatchs.length; i++) {
                try {
                    dataSet.add(Double.parseDouble(needMatchs[i]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (ignoreMatchs != null) {
            for (int i = 0; i < ignoreMatchs.length; i++) {
                try {
                    dataSet.remove(Double.parseDouble(ignoreMatchs[i]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("基准dp：" + baseDP + " dp");
        System.out.println("待适配的屏幕dp参数: " + dataSet.toString());
        //获取基准的dimens.xml文件
        String baseDimenFilePath = resFolderPath + File.separator + "values/dimens.xml";
        File testBaseDimenFile = new File(baseDimenFilePath);
        //判断基准文件是否存在
        if (!testBaseDimenFile.exists()) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 路径下的文件找不到!");
            return;
        }
        //解析源dimens.xml文件
        ArrayList<DimenBean> list = readBaseDimenFile(baseDimenFilePath);
        if (list == null || list.size() <= 0) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 文件无数据!");
            return;
        } else {
            System.out.println("OK \"./res/values/dimens.xml\" 基准dimens文件解析成功!");
        }
        //循环指定的dp参数，生成对应的dimens-swXXXdp.xml文件
        Iterator<Double> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            double item = iterator.next();
            //获取当前dp除以baseDP后的倍数
            double multiple = item / baseDP;
            //创建当前dp对应的dimens文件目录
            String outPutDir = resFolderPath + "/values-w" + (int) item + "dp/";
            new File(outPutDir).mkdirs();
            //生成的dimens文件里路径
            String outPutFile = outPutDir + "dimens.xml";
            //生成目标文件dimens.xml输出目录
            createDestinationDimens(list, multiple, outPutFile);
        }
        System.out.println("OK ALL OVER，全部生成完毕！");
    }

    /**
     * 解析基准dimens文件
     *
     * @param baseDimenFilePath 源dimens文件路径
     */
    private static ArrayList<DimenBean> readBaseDimenFile(String baseDimenFilePath) {
        ArrayList<DimenBean> list = null;
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxparser = saxParserFactory.newSAXParser();
            InputStream inputStream = new FileInputStream(baseDimenFilePath);
            SAXReadHandler saxReadHandler = new SAXReadHandler();
            saxparser.parse(inputStream, saxReadHandler);
            list = saxReadHandler.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 生成对应的dimens目标文件
     *
     * @param list       源dimens数据
     * @param multiple   对应新文件需要乘以的系数
     * @param outPutFile 目标文件输出目录
     */
    private static void createDestinationDimens(ArrayList<DimenBean> list, double multiple, String outPutFile) {
        try {
            File targetFile = new File(outPutFile);
            if (targetFile.exists()) {
                targetFile.delete();
                System.out.println("旧文件 \"" + outPutFile + "\" 文件删除成功!");
            }
            System.out.println("正在生成 " + outPutFile + " 文件...");
            //创建SAXTransformerFactory实例
            SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            //创建TransformerHandler实例
            TransformerHandler handler = saxTransformerFactory.newTransformerHandler();
            //创建Transformer实例
            Transformer transformer = handler.getTransformer();
            //是否自动添加额外的空白
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //设置字符编码
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //添加xml版本，默认也是1.0
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            //保存xml路径
            StreamResult result = new StreamResult(targetFile);
            handler.setResult(result);
            //创建属性Attribute对象
            AttributesImpl attributes = new AttributesImpl();
            attributes.clear();
            //开始xml
            handler.startDocument();
            //换行
            handler.characters("\n".toCharArray(), 0, "\n".length());
            //写入根节点resources
            handler.startElement("", "", SAXReadHandler.ELEMENT_RESOURCE, attributes);
            //集合大小
            int size = list.size();
            for (int i = 0; i < size; i++) {
                DimenBean dimenBean = list.get(i);
                //乘以系数，加上后缀
                String targetValue = countValue(dimenBean.value, multiple);
                attributes.clear();
                attributes.addAttribute("", "", SAXReadHandler.PROPERTY_NAME, "", dimenBean.name);

                //新dimen之前，换行、缩进
                handler.characters("\n".toCharArray(), 0, "\n".length());
                handler.characters("\t".toCharArray(), 0, "\t".length());

                //开始标签对输出
                handler.startElement("", "", SAXReadHandler.ELEMENT_DIMEN, attributes);
                handler.characters(targetValue.toCharArray(), 0, targetValue.length());
                handler.endElement("", "", SAXReadHandler.ELEMENT_DIMEN);
            }
            handler.endElement("", "", SAXReadHandler.ELEMENT_RESOURCE);
            handler.endDocument();
            System.out.println("新的 " + outPutFile + " 文件生成完成!");
        } catch (Exception e) {
            System.out.println("DK WARNING: " + outPutFile + " 文件生成失败!");
            e.printStackTrace();
        }
    }

    /**
     * 乘以系数
     *
     * @param oldValue 原字符串
     * @param multiple 乘以系数后，且带单位的字符串
     * @return
     */
    private static String countValue(String oldValue, double multiple) {
        if (oldValue == null) {
            return "";
        }
        oldValue = oldValue.trim();
        if ("".equals(oldValue)
                || oldValue.length() <= 2
                || oldValue.startsWith("@dimen/")
                || !"dp".equals(oldValue.substring(oldValue.length() - 2, oldValue.length()))) {
            return oldValue;
        }
        //乘以系数
        double temp = 0;
        try {
            temp = Double.parseDouble(oldValue.substring(0, oldValue.length() - 2).trim()) * multiple;
        } catch (Exception e) {
            return oldValue.trim();
        }
        //数据格式化对象
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(temp) + "dp";
    }
}