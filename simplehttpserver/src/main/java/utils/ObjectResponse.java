package utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.ByteArrayOutputStream;

@AllArgsConstructor
@Getter
public class ObjectResponse {
    private ByteArrayOutputStream byteArrayOutputStream;
    private String fileExtension;
}
