package com.duke.phonescreenmatch_test.dp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 17:27
 * @Description: 解析xml
 */
public class SAXReadHandler extends DefaultHandler {
    private ArrayList<DimenBean> list = null;
    private DimenBean dimenBean;
    private String tempElement;
    public static final String ELEMENT_RESOURCE = "resources";
    public static final String ELEMENT_DIMEN = "dimen";
    public static final String PROPERTY_NAME = "name";

    public ArrayList<DimenBean> getData() {
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
                dimenBean = new DimenBean();
                if (attributes != null && attributes.getLength() > 0) {
                    String property = attributes.getValue(PROPERTY_NAME);
                    dimenBean.name = property;
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
                dimenBean.value = new String(ch, start, length);
            }
        }
    }
}