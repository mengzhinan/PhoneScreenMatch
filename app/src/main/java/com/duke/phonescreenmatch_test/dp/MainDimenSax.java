package com.duke.phonescreenmatch_test.dp;

import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    //源dimens文件的dimen集合
    private static ArrayList<DimenBean> list;
    //当前根目录：D:/android/
    private static String baseDirPath;
    //基准dp，比喻：360dp
    private static float baseDP = 360.0f;
    //默认支持的dp值
    private static final String[] defaultDPArr = new String[]{"384", "392", "400", "410", "411", "480", "533", "592", "600", "640", "662", "720", "768", "800", "811", "820", "960", "961", "1024", "1280", "1365"};
    //去重复的数据集合
    private static HashSet<String> dataSet = new HashSet<>();
    //基准dimens.xml文件路径，比喻：D:/android/res/values/dimens.xml
    private static String baseDimenFilePath;

    /**
     * 入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        //数组第一项为基准宽度dp，后面为需要生成的对应宽度dp
        if (args != null && args.length > 0) {
            baseDP = Float.parseFloat(args[0]);
            for (int i = 1; i < args.length; i++) {
                dataSet.add(args[i]);
            }
        } else {
            System.out.println("没有发现输入参数...");
        }
        //添加默认的数据
        for (int i = 0; i < defaultDPArr.length; i++) {
            dataSet.add(defaultDPArr[i]);
        }
        System.out.println("基准dp：" + baseDP + " dp");
        System.out.println("待适配的屏幕dp参数: " + dataSet.toString());
        //获取当前目录的结对路径
        baseDirPath = new File("./res/").getAbsolutePath();
        //获取基准的dimens.xml文件
        baseDimenFilePath = baseDirPath + File.separator + "values/dimens.xml";
        File testBaseDimenFile = new File(baseDimenFilePath);
        //判断基准文件是否存在
        if (!testBaseDimenFile.exists()) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 路径下的文件找不到!");
            return;
        }
        //解析源dimens.xml文件
        list = readBaseDimenFile(baseDimenFilePath);
        if (list == null || list.size() <= 0) {
            System.out.println("DK WARNING:  \"../res/values/dimens.xml\" 文件无数据!");
            return;
        } else {
            System.out.println("OK \"../res/values/dimens.xml\" 基准dimens文件解析成功!");
        }
        //循环指定的dp参数，生成对应的dimens-swXXXdp.xml文件
        Iterator<String> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            //获取当前dp除以baseDP后的倍数
            float multiple = Float.parseFloat(item) / baseDP;
            //创建当前dp对应的dimens文件目录
            String outPutDir = baseDirPath + "/values-w" + item + "dp/";
            new File(outPutDir).mkdirs();
            //待生成的dimens文件里路径
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
    private static void createDestinationDimens(ArrayList<DimenBean> list, float multiple, String outPutFile) {
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
    private static String countValue(String oldValue, float multiple) {
        //数据格式化对象
        DecimalFormat df = new DecimalFormat("0.00");
        String suffix = oldValue.substring(oldValue.length() - 2, oldValue.length());
        //乘以系数
        double temp = Double.parseDouble(oldValue.substring(0, oldValue.length() - 2)) * multiple;
        return df.format(temp) + suffix;
    }
}