package com.ocarlsen.logging.http.client.apache;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GzipContentEnablingEntityTest {

    @Test
    public void getContent() throws IOException {
        final String body = "hello";
        final GzipCompressingEntity gzipCompressingEntity = (GzipCompressingEntity)
                EntityBuilder.create()
                             .setText(body)
                             .gzipCompress()
                             .build();
        final GzipContentEnablingEntity entityIn = new GzipContentEnablingEntity(gzipCompressingEntity);
        final GzipDecompressingEntity entityOut = new GzipDecompressingEntity(entityIn);
        final InputStream inputStream = entityOut.getContent();
        final String content = IOUtils.toString(inputStream, UTF_8);
        assertThat(content, is(body));

    }
}