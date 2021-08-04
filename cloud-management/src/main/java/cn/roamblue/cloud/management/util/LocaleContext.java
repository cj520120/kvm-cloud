package cn.roamblue.cloud.management.util;

import java.util.Locale;

/**
 * @ClassName: LocaleContext
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/4 下午2:00
 */
public class LocaleContext {
    static final ThreadLocal<Locale> context = new ThreadLocal();

    public static Locale getLocale() {
        Locale locale = context.get();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public static void setLocale(Locale locale) {
        context.set(locale);
    }
}
