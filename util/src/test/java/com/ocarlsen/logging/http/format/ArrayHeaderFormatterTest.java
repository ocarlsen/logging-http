package com.ocarlsen.logging.http.format;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class ArrayHeaderFormatterTest extends BaseHeaderFormatterTest<Header[]> {

    @Override
    protected Header[] buildHeaders() {
        final Header header1 = new BasicHeader("headerName1", "headerValue1.1, headerValue1.2");
        final Header header2 = new BasicHeader("headerName2", "headerValue2.1");
        return new Header[]{header1, header2};
    }

    @Override
    protected HeaderFormatter<Header[]> buildHeaderFormatter() {
        return ArrayHeaderFormatter.INSTANCE;
    }
}