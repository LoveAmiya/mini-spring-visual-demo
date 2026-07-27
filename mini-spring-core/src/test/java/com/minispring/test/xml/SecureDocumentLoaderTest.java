package com.minispring.test.xml;

import com.minispring.beans.factory.xml.DefaultDocumentLoader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SecureDocumentLoaderTest {

    @Test
    void rejectsDocumentsContainingDoctypeDeclarations() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE beans [<!ENTITY injected "untrusted">]>
                <beans><bean id="value" class="&injected;"/></beans>
                """;

        DefaultDocumentLoader loader = new DefaultDocumentLoader();

        assertThrows(DocumentException.class, () -> loader.loadDocument(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    }
}
