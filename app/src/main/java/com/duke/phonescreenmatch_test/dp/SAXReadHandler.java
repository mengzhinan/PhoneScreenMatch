package com.duke.phonescreenmatch_test.dp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 17:27
 * @UpdateTime: 2017-09-29 09:55
 * @Description: 解析xml工具类
 */
public class SAXReadHandler extends DefaultHandler {
    private ArrayList<DimenItem> list = null;
    private DimenItem dimenBean;
    private String tempElement;
    static final String ELEMENT_RESOURCE = "resources";
    static final String ELEMENT_DIMEN = "dimen";
    static final String PROPERTY_NAME = "name";

    public ArrayList<DimenItem> getData() {
        return list;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempElement = qName;
        if (qName != null && qName.trim().length() > 0) {
            if (qName.equals(ELEMENT_RESOURCE)) {
                //创建集合
                list = new ArrayList<>();
            } else if (qName.equals(ELEMENT_DIMEN)) {
                //创建对象
                dimenBean = new DimenItem();
                if (attributes != null && attributes.getLength() > 0) {
                    dimenBean.name = attributes.getValue(PROPERTY_NAME);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName != null && qName.trim().length() > 0) {
            if (qName.equals(ELEMENT_DIMEN)) {
                //dimen结束标签，添加对象到集合
                if (list != null) {
                    list.add(dimenBean);
                    dimenBean = null;
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tempElement != null && tempElement.trim().equals(ELEMENT_DIMEN)) {
            if (dimenBean != null) {
                String temp = new String(ch, start, length);
                if (temp.trim().length() > 0) {
                    temp = temp.trim();
                    /**
                     * 感谢网友提醒，发现偶现的bug，同一处的文本会回调多次
                     */
                    if (dimenBean.value == null || dimenBean.value.trim().length() == 0) {
                        dimenBean.value = temp;
                    } else {
                        //内容累加
                        dimenBean.value += temp;
                    }
                }
            }
        }
    }
}