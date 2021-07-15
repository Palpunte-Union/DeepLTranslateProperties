package com.github.eighty88.deepl;

import com.machinepublishers.jbrowserdriver.Timezone;
import de.linus.deepltranslator.DeepLConfiguration;
import de.linus.deepltranslator.DeepLTranslator;
import de.linus.deepltranslator.Language;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {

    File input;

    File output;

    public static void main(String[] args) {
        try {
            new Main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Main() throws Exception {
        File f = new File(System.getProperty("java.class.path"));
        File jarDir = f.getAbsoluteFile().getParentFile();
        input = new File(jarDir, "input");
        output = new File(jarDir, "output");

        output.mkdir();
        input.mkdir();

        File[] filesList = input.listFiles();

        DeepLConfiguration deepLConfiguration = new DeepLConfiguration.Builder()
                .setTimeout(Duration.ofSeconds(10))
                .setRepetitions(3)
                .setRepetitionsDelay(retryNumber -> Duration.ofMillis(3000 + 5000L * retryNumber))
                .setTimezone(Timezone.ASIA_TOKYO)
                .build();

        DeepLTranslator deepLTranslator = new DeepLTranslator(deepLConfiguration);

        for(File file : Objects.requireNonNull(filesList)) {
            System.out.println(file.getName());
            if(file.getName().contains("properties")) {

                List<String> lines = Files.lines(Paths.get(file.getPath()), StandardCharsets.UTF_8).collect(Collectors.toList());
                List<String> result = new ArrayList<>();
                for(String str:lines) {
                    String[] temp = str.split("=", 2);
                    String string;
                    temp[1] = temp[1].replace("[", "BAA ").replace("]", " BAB ").replace("\\n", " NEWLINE ").replace("%", " PER ").replace(" /", " SLASH ");
                    if(temp[0].contains("param")) {
                        string = str;
                    } else {
                        string = temp[0] + "=" + sync(deepLTranslator, temp[1]);
                    }

                    string = string.replace("BAA ", "[").replace("BAA", "[").replace(" BAB", "]").replace("BAB", "]").replace(" NEWLINE", "\\n").replace("NEWLINE ", "\\n").replace("NEWLINE", "\\n").replace("PER", "%").replace("SLASH", "/");

                    System.out.println(string);
                    result.add(string);
                }
                File fileO = new File(output, file.getName());
                Files.write(fileO.toPath(), result, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
        }
    }

    private String sync(DeepLTranslator deepLTranslator, String text) {
        try {
            return deepLTranslator.translate(text, Language.ENGLISH, Language.JAPANESE);
        } catch (Exception e) {
            return text;
        }
    }
}
