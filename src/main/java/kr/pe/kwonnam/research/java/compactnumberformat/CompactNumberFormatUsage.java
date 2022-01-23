package kr.pe.kwonnam.research.java.compactnumberformat;

import java.text.CompactNumberFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * {@link CompactNumberFormat} 은 LONG, SHORT 로 숫자를 사람이 읽기 편한 형태로 간편하게 표시한다.
 *
 * @see CompactNumberFormat
 * @see <a href="https://dzone.com/articles/features-of-java-17">Java 17 for the Impatient</a>
 */
public class CompactNumberFormatUsage {
    public static void main(String[] args) {
        // 한국어(KR)에서는 Long,Short 에 차이가 없었음.

        NumberFormat numberFormatLong = NumberFormat.getCompactNumberInstance(Locale.forLanguageTag("NL"), NumberFormat.Style.LONG);
        System.out.println(numberFormatLong.format(2000));
        System.out.println(numberFormatLong.format(20000));
        System.out.println(numberFormatLong.format(200000));

        NumberFormat numberFormatShort = NumberFormat.getCompactNumberInstance(Locale.forLanguageTag("NL"), NumberFormat.Style.SHORT);
        System.out.println(numberFormatShort.format(2000));
        System.out.println(numberFormatShort.format(20000));
        System.out.println(numberFormatShort.format(200000));

    }

}
