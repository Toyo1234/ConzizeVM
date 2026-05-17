package consize;

import java.util.List;

public class Word {
    enum Type {
        NATIVE,
        QUOTE,
        LITERAL
    }

    public final Type type;
    public final String value;   // für literal
    public final List<String> code; // für quote

    public Word(Type type, String value, List<String> code) {
        this.type = type;
        this.value = value;
        this.code = code;
    }
}
