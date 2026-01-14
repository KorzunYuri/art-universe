package yurykorzun.art.universe.common.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Converter(autoApply = false)
public class GzipBase64StringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        try (ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(byteArrayOS)) {

            gzipOut.write(attribute.getBytes(StandardCharsets.UTF_8));
            gzipOut.finish();
            return Base64.getEncoder().encodeToString(byteArrayOS.toByteArray());

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to compress string", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        byte[] compressed = Base64.getDecoder().decode(dbData);
        try (ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(compressed);
             GZIPInputStream gzipIn = new GZIPInputStream(byteArrayIS);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            return baos.toString(StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to decompress string", e);
        }
    }
}
