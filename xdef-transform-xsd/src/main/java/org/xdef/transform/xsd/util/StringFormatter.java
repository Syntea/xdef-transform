package org.xdef.transform.xsd.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author smid
 * @since 2021-05-13
 */
public class StringFormatter {

    public static final String HEADER_LINE = StringUtils.repeat('=', 80);

    public static String format(String format, Object... arguments) {
        return MessageFormatter.arrayFormat(format, arguments).getMessage();
    }

}
