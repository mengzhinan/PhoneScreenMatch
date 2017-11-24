package com.duke.phonescreenmatch_test.dp;

import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * dimens处理
 *
 * @UpdateTime: 2017-09-29 09:55
 */
public class XmlIO {

    /**
     * 解析dimens文件
     *
     * @param baseDimenFilePath 源dimens文件路径
     */
    public static ArrayList<DimenItem> readDimenFile(String baseDimenFilePath) {
        ArrayList<DimenItem> list = null;
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
     * 生成dimens文件
     *
     * @param isFontMatch 字体是否也适配(是否与dp尺寸一样等比缩放)
     * @param list        源dimens数据
     * @param multiple    对应新文件需要乘以的系数
     * @param outPutFile  目标文件输出目录
     */
    public static void createDestinationDimens(boolean isFontMatch, ArrayList<DimenItem> list, double multiple, String outPutFile) {
        try {
            File targetFile = new File(outPutFile);
            if (targetFile.exists()) {
                try {
                    targetFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
                DimenItem dimenBean = list.get(i);
                //乘以系数，加上后缀
                String targetValue = Tools.countValue(isFontMatch, dimenBean.value, multiple);
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
            System.out.println(">>>>> " + outPutFile + " 文件生成完成!");
        } catch (Exception e) {
            System.out.println("DK WARNING: " + outPutFile + " 文件生成失败!");
            e.printStackTrace();
        }
    }
}
