package com.isroot.stash.plugin.errors;

import com.atlassian.stash.setting.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Sean Ford
 * @since 2015-04-26
 */
public class YaccErrorBuilder {
    private static final Logger log = LoggerFactory.getLogger(YaccErrorBuilder.class);

    public static final String ERROR_BEARS = "\n" +
            "  (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c) \n" +
            "   / ._. \\      / ._. \\      / ._. \\      / ._. \\      / ._. \\ \n" +
            " __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__\n" +
            "(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)\n" +
            "   || E ||      || R ||      || R ||      || O ||      || R ||\n" +
            " _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '.\n" +
            "(.-./`-'\\.-.)(.-./`-`\\.-.)(.-./`-`\\.-.)(.-./`-'\\.-.)(.-./`-`\\.-.)\n" +
            " `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-' \n" +
            "\n" +
            "\n" +
            "Push rejected.\n";

    private final Settings settings;

    public YaccErrorBuilder(Settings settings) {
        this.settings = settings;
    }

    public String getErrorMessage(List<YaccError> errors) {
        StringBuilder sb = new StringBuilder();

        sb.append(getHeader(settings));

        sb.append(getErrors(errors));

        sb.append(getFooter(settings));

        return sb.toString();
    }

    private String getErrors(List<YaccError> errors) {
        StringBuilder sb = new StringBuilder();

        for (YaccError error : errors) {
            log.debug("error type={} message={}", error.getType(), error.getMessage());

            sb.append(error.getMessage())
                    .append("\n");

            String custom = settings.getString("errorMessage." + error.getType());
            if(custom != null && !custom.isEmpty()) {
                sb.append("\n")
                        .append("    ")
                        .append(custom)
                        .append("\n\n");
            }
        }

        sb.append("\n");

        return sb.toString();
    }

    private String getHeader(Settings settings) {
        String header = settings.getString("errorMessageHeader");

        if(header == null || header.isEmpty()) {
            // sford: long live the error bears
            header = ERROR_BEARS;
        }

        return header + "\n\n";
    }

    private String getFooter(Settings settings) {
        String footer = settings.getString("errorMessageFooter");
        if(footer != null && !footer.isEmpty()) {
            return footer + "\n\n";
        }

        return "";
    }

}
