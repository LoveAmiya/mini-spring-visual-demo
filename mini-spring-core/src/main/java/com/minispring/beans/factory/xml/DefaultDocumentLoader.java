package com.minispring.beans.factory.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.InputStream;
import javax.xml.XMLConstants;

/**
 * 默认的XML文档加载器实现
 * 使用DOM4J的SAXReader解析XML文档
 */
public class DefaultDocumentLoader implements DocumentLoader {

    /**
     * 从输入流加载XML文档
     * 
     * @param inputStream XML输入流
     * @return 解析后的Document对象
     * @throws DocumentException 如果解析过程中发生错误
     */
    @Override
    public Document loadDocument(InputStream inputStream) throws DocumentException {
        SAXReader reader = new SAXReader();
        configureSecureParsing(reader);
        return reader.read(inputStream);
    }

    private void configureSecureParsing(SAXReader reader) throws DocumentException {
        try {
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External XML entities are disabled");
            });
        } catch (SAXException exception) {
            throw new DocumentException("Unable to configure secure XML parsing", exception);
        }
    }
}
