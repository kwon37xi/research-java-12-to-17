package kr.pe.kwonnam.research.java.properties;

import java.util.ResourceBundle;

public class ResourceBundleLoading {
    public static void main(String[] args) {
        // charset 을 지정하지 않아도 기본 UTF-8 로 읽는다.
        // from java 9
        ResourceBundle resourceBundle = ResourceBundle.getBundle("hangul");

        String hangulGreetings = resourceBundle.getString("greetings");
        String enGreetings = resourceBundle.getString("greetings-en");

        System.out.println("리소스 번들 한글 인사 : " + hangulGreetings);
        System.out.println("리소스 번들 영어 인사 : " + enGreetings);
    }
}
