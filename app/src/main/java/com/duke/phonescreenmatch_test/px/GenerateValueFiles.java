package com.duke.phonescreenmatch_test.px;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 14:31
 * @Description: hongyang的px适配，1280x720为基准
 */
public class GenerateValueFiles {
    private int baseW = 720;
    private int baseH = 1280;
    private String dirStr = "./res";
    private final static String XDIMEN = "<dimen name=\"x{0}\">{1}px</dimen>\n";
    private final static String YDIMEN = "<dimen name=\"y{0}\">{1}px</dimen>\n";
    /**
     * {0}-HEIGHT
     * {1}-WIDTH
     */
    private final static String FOLDER_NAME = "values-{0}x{1}";
    private HashSet<String> dataSet;//装的是"width,height"字符串
    private static final String SUPPORT_DIMESION = "320,480;480,800;480,854;540,888;600,1024;720,1184;720,1196;720,1280;768,1024;768,1280;800,1280;1080,1812;1080,1920;1440,2560;";
    private String supportStr = SUPPORT_DIMESION;

    public static void main(String[] args) {
        System.out.println("开始运行...");
        //基准宽高
        int baseW = -1;
        int baseH = -1;
        String addition = "";
        try {
            if (args.length == 1) {
                addition = args[0];
            } else if (args.length == 2) {
                baseW = Integer.parseInt(args[0]);
                baseH = Integer.parseInt(args[1]);
            } else if (args.length >= 3) {
                baseW = Integer.parseInt(args[0]);
                baseH = Integer.parseInt(args[1]);
                addition = args[2];
            } else {
                System.out.println("没有发现输入参数...");
            }
        } catch (NumberFormatException e) {
            System.err.println("right input params : java -jar xxx.jar baseW baseH w,h;w,h;...;w,h;");
            e.printStackTrace();
            System.exit(-1);
        }
        new GenerateValueFiles(baseW, baseH, addition).generate();
    }

    /**
     * 构造函数
     *
     * @param baseX      基准width
     * @param baseY      基准height
     * @param supportStr 需要适配的屏幕px尺寸
     */
    public GenerateValueFiles(int baseX, int baseY, String supportStr) {
        if (baseX > 0)
            this.baseW = baseX;
        if (baseY > 0)
            this.baseH = baseY;
        String test = this.baseW + "," + this.baseH + ";";
        System.out.println("基准尺寸：" + test);
        if (!this.supportStr.contains(test)) {
            this.supportStr += test;
        }
        //拼接和去重屏幕px尺寸参数
        validateInput(supportStr);
        File dir = new File(dirStr);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * @param temp w,h;...;w,h;
     * @return
     */
    private void validateInput(String temp) {
        if (temp != null && temp.trim().length() > 0)
            this.supportStr += temp;
        if (dataSet == null)
            dataSet = new HashSet<>();
        String[] tempArr = supportStr.split(";");
        for (int i = 0; i < tempArr.length; i++) {
            if (tempArr[i] != null && tempArr[i].trim().length() > 0) {
                dataSet.add(tempArr[i]);
            }
        }
        System.out.println("待适配的屏幕px参数：" + dataSet.toString());
    }

    public void generate() {
        Iterator<String> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            String whStr = iterator.next();
            String[] wh = whStr.split(",");
            generateXmlFile(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        }
        System.out.println("OK ALL OVER,全部生成完毕!");
    }

    private void generateXmlFile(int w, int h) {
        System.out.println("开始生成 " + String.valueOf(w + "x" + h) + "对应的dimens.xml文件");
        //文件部分dimens_x.xml
        StringBuffer sbForWidth = new StringBuffer();
        sbForWidth.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sbForWidth.append("<resources>\n");
        float coefficientW = w * 1.0f / baseW;
        for (int i = 1; i < baseW; i++) {
            sbForWidth.append("\t");
            sbForWidth.append(XDIMEN.replace("{0}", String.valueOf(i)).replace("{1}", String.valueOf(change(coefficientW * i))));
        }
        //最后一个不参与计算，保证为整数
        sbForWidth.append("\t");
        sbForWidth.append(XDIMEN.replace("{0}", String.valueOf(baseW)).replace("{1}", String.valueOf(w)));
        sbForWidth.append("</resources>");

        //文件部分dimens_y.xml
        StringBuffer sbForHeight = new StringBuffer();
        sbForHeight.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sbForHeight.append("<resources>\n");
        float coefficientH = h * 1.0f / baseH;
        for (int i = 1; i < baseH; i++) {
            sbForHeight.append("\t");
            sbForHeight.append(YDIMEN.replace("{0}", String.valueOf(i)).replace("{1}", String.valueOf(change(coefficientH * i))));
        }
        //最后一个不参与计算，保证为整数
        sbForHeight.append("\t");
        sbForHeight.append(YDIMEN.replace("{0}", String.valueOf(baseH)).replace("{1}", String.valueOf(h)));
        sbForHeight.append("</resources>");

        //生成文件路径  Height x Width
        File fileDir = new File(dirStr + File.separator + FOLDER_NAME.replace("{0}", String.valueOf(h)).replace("{1}", String.valueOf(w)));
        fileDir.mkdir();
        File dimensXFile = new File(fileDir.getAbsolutePath(), "dimens_x.xml");
        if (dimensXFile.exists()) {
            dimensXFile.delete();
            System.out.println("旧文件 \"dimens_x.xml\" 删除成功!");
        }
        File dimensYFile = new File(fileDir.getAbsolutePath(), "dimens_y.xml");
        if (dimensYFile.exists()) {
            dimensYFile.delete();
            System.out.println("旧文件 \"dimens_y.xml\" 删除成功!");
        }
        try {
            //写dimens_x.xml
            PrintWriter pw = new PrintWriter(new FileOutputStream(dimensXFile));
            pw.print(sbForWidth.toString());
            pw.close();
            //写dimens_y.xml
            pw = new PrintWriter(new FileOutputStream(dimensYFile));
            pw.print(sbForHeight.toString());
            pw.close();
            System.out.println("文件 dimens_m.xml 生成完毕!");
        } catch (FileNotFoundException e) {
            System.out.println("文件 dimens_m.xml 生成失败!");
            e.printStackTrace();
        }
    }

    //保留2位小数
    public static float change(float a) {
        int temp = (int) (a * 100);
        return temp / 100f;
    }
}