package kr.pe.kwonnam.research.java.properties;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesLoading {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

        // InputStream 사용시에는 UTF-8 이 작동하지 않는다.
//        properties.load(ClassLoader.getSystemResourceAsStream("hangul.properties"));

        // Reader 로 변환해서, UTF-8 인코딩을 지정해줘야 작동한다.
        properties.load(new InputStreamReader(ClassLoader.getSystemResourceAsStream("hangul.properties"), StandardCharsets.UTF_8));

        String hangulGreetings = properties.getProperty("greetings");
        String enGreetings = properties.getProperty("greetings-en");

        System.out.println("한글 인사 : " + hangulGreetings);
        System.out.println("영어 인사 : " + enGreetings);
    }
}
