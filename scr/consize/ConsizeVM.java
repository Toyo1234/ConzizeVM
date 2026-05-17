package consize;

import java.util.*;

public class ConsizeVM {

    public static final Deque<Object> ds = new ArrayDeque<>();
    public static final Deque<Object> cs = new ArrayDeque<>();
    public static final Map<String, Word> dict = new HashMap<>();

    public static final Scanner scanner = new Scanner(System.in);

    public interface Word {
        List<String> words();

        void run(
                Deque<Object> ds,
                Deque<Object> cs,
                Map<String, Word> dict
        );
    }

    @FunctionalInterface
    public interface DSWord {
        void run(Deque<Object> ds);
    }

    @FunctionalInterface
    public interface CSWord {
        void run(Deque<Object> cs);
    }

    @FunctionalInterface
    public interface DSCSWord {
        void run(Deque<Object> ds, Deque<Object> cs);
    }

    @FunctionalInterface
    public interface FullWord {
        void run(
                Deque<Object> ds,
                Deque<Object> cs,
                Map<String, Word> dict
        );
    }
    @FunctionalInterface
    public interface VMFunction {
        Deque<Object> apply(Deque<Object> ds);
    }

    public static void word(String name, DSWord fn) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of();
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                fn.run(ds);
            }
        });
    }
    public static void word(String name, Object... words) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of();
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                for (int i = words.length - 1; i >= 0; i--) {
                    cs.push(words[i]);
                }
            }
        });
    }

    public static void word(String name, CSWord fn) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of();
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                fn.run(cs);
            }
        });
    }

    public static void word(String name, DSCSWord fn) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of();
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                fn.run(ds, cs);
            }
        });
    }

    public static void word(String name, FullWord fn) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of();
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                fn.run(ds, cs, dict);
            }
        });
    }

    public static void word(String name, String... words) {
        dict.put(name, new Word() {
            public List<String> words() {
                return List.of(words);
            }

            public void run(
                    Deque<Object> ds,
                    Deque<Object> cs,
                    Map<String, Word> dict
            ) {
                // keine native Funktion
            }
        });
    }

    public static void defineBasicWords() {
        word("swap", (DSWord) ds -> {
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(y);
            ds.push(x);
        });

        word("dup", (DSWord) ds -> {
            ds.push(ds.peek());
        });

        word("drop", (DSWord) ds -> {
            ds.pop();
        });

        word("rot", (DSWord) ds -> {
            Object z = ds.pop();
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(y);
            ds.push(z);
            ds.push(x);
        });

        word("type", (DSWord) ds -> {
            Object itm = ds.pop();

            if (itm instanceof String) {
                ds.push("wrd");
            } else if (itm instanceof Deque<?> || itm instanceof List<?>) {
                ds.push("stk");
            } else if (itm instanceof Map<?, ?>) {
                ds.push("map");
            } else if (itm instanceof Word) {
                ds.push("fct");
            } else if (itm == null) {
                ds.push("nil");
            } else {
                ds.push("_|_");
            }
        });

        // equal?: [x y] -> ["t" | "f"]
        word("equal?", (DSWord) ds -> {
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(Objects.equals(x, y) ? "t" : "f");
        });

        // identical?: [x y] -> ["t" | "f"]
        word("identical?", (DSWord) ds -> {
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(x == y ? "t" : "f");
        });
        // emptystack: [] -> [[]]
        word("emptystack", (DSWord) ds -> {
            ds.push(new ArrayDeque<>());
        });

        // push: [x s] -> [s']   (x oben auf Stack s)
        word("push", (DSWord) ds -> {
            Object s = ds.pop();
            Object x = ds.pop();

            Deque<Object> stack = new ArrayDeque<>((Deque<Object>) s);

            stack.push(x);
            ds.push(stack);
        });

        // top: [s] -> [x]
        word("top", (DSWord) ds -> {
            Object s = ds.pop();

            Deque<Object> stack = (Deque<Object>) s;

            ds.push(stack.peek());
        });

        // pop: [s] -> [rest]
        word("pop", (DSWord) ds -> {
            Object s = ds.pop();

            Deque<Object> stack = new ArrayDeque<>((Deque<Object>) s);

            stack.pop();
            ds.push(stack);
        });

        // concat: [s1 s2] -> [s1+s2]
        word("concat", (DSWord) ds -> {
            Object s2 = ds.pop();
            Object s1 = ds.pop();

            Deque<Object> stack1 = (Deque<Object>) s1;

            Deque<Object> stack2 = (Deque<Object>) s2;

            Deque<Object> result = new ArrayDeque<>(stack2);

            List<Object> temp = new ArrayList<>(stack1);
            Collections.reverse(temp);

            for (Object obj : temp) {
                result.push(obj);
            }

            ds.push(result);
        });

        // reverse: [s] -> [reversed]
        word("reverse", (DSWord) ds -> {
            Object s = ds.pop();

            Deque<Object> stack = (Deque<Object>) s;

            List<Object> temp = new ArrayList<>(stack);

            Deque<Object> result = new ArrayDeque<>();

            for (Object obj : temp) {
                result.push(obj);
            }

            ds.push(result);
        });
        // mapping: [s] -> [map]
        word("mapping", (DSWord) ds -> {
            Deque<Object> s = (Deque<Object>) ds.pop();

            Map<Object, Object> map = new HashMap<>();

            while (!s.isEmpty()) {
                Object value = s.pop();
                Object key = s.pop();

                map.put(key, value);
            }

            ds.push(map);
        });

        // unmap: [map] -> [stack]
        word("unmap", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                result.push(entry.getKey());
                result.push(entry.getValue());
            }

            ds.push(result);
        });

        // keys: [map] -> [stack]
        word("keys", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            for (Object key : m.keySet()) {
                result.push(key);
            }

            ds.push(result);
        });

        // assoc: [m k v] -> [m']
        word("assoc", (DSWord) ds -> {
            Object v = ds.pop();
            Object k = ds.pop();

            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Map<Object, Object> result = new HashMap<>(m);
            result.put(k, v);

            ds.push(result);
        });

        // dissoc: [m k] -> [m']
        word("dissoc", (DSWord) ds -> {
            Object k = ds.pop();

            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Map<Object, Object> result = new HashMap<>(m);
            result.remove(k);

            ds.push(result);
        });

        // get: [d m k] -> [value-or-default]
        word("get", (DSWord) ds -> {
            Object k = ds.pop();

            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Object d = ds.pop();

            ds.push(m.getOrDefault(k, d));
        });

        // merge: [m1 m2] -> [merged]
        word("merge", (DSWord) ds -> {

            Map<Object, Object> m2 = (Map<Object, Object>) ds.pop();
            Map<Object, Object> m1 = (Map<Object, Object>) ds.pop();

            Map<Object, Object> result = new HashMap<>(m2);
            result.putAll(m1);

            ds.push(result);
        });

        // word: [stack] -> [string]
        word("word", (DSWord) ds -> {
            Deque<Object> s = (Deque<Object>) ds.pop();

            StringBuilder result = new StringBuilder();

            for (Object item : s) {
                result.append(item);
            }

            ds.push(result.toString());
        });

        // unword: [string] -> [stack]
        word("unword", (DSWord) ds -> {
            String w = (String) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            for (int i = w.length() - 1; i >= 0; i--) {
                result.push(String.valueOf(w.charAt(i)));
            }

            ds.push(result);
        });

        // char: [string] -> [string]
        word("char", (DSWord) ds -> {
            String w = (String) ds.pop();

            if (w.length() != 1) {
                throw new IllegalArgumentException("char erwartet genau ein Zeichen");
            }

            ds.push(w);
        });
        // print: [string] -> []
        word("print", (DSWord) ds -> {
            String w = (String) ds.pop();
            System.out.print(w);
        });

        // flush: [] -> []
        word("flush", (DSWord) ds -> {
            System.out.flush();
        });
        word("read-line", (DSWord) ds -> {
            ds.push(scanner.nextLine());
        });
        // slurp: [file] -> [content]
        word("slurp", (DSWord) ds -> {
            String file = (String) ds.pop();

            try {
                String content = java.nio.file.Files.readString(
                        java.nio.file.Path.of(file)
                );
                ds.push(content);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });

        // spit: [file data] -> []
        word("spit", (DSWord) ds -> {
            String data = (String) ds.pop();
            String file = (String) ds.pop();

            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of(file),
                        data
                );
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });

        // spit-on: [file data] -> []
        word("spit-on", (DSWord) ds -> {
            String data = (String) ds.pop();
            String file = (String) ds.pop();

            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of(file),
                        data,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND
                );
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
        // uncomment: [string] -> [string]
        word("uncomment", (DSWord) ds -> {
            String w = (String) ds.pop();

            String result = w.replaceAll("(?m)\\s*%.*$", "");

            ds.push(result);
        });

        // tokenize: [string] -> [stack]
        word("tokenize", (DSWord) ds -> {
            String w = ((String) ds.pop()).trim();

            Deque<Object> result = new ArrayDeque<>();

            if (!w.isEmpty()) {
                String[] tokens = w.split("\\s+");

                for (int i = tokens.length - 1; i >= 0; i--) {
                    result.push(tokens[i]);
                }
            }

            ds.push(result);
        });

        // undocument: [string] -> [string]
        word("undocument", (DSWord) ds -> {
            String w = (String) ds.pop();

            StringBuilder result = new StringBuilder();
            java.util.regex.Pattern pattern =
                    java.util.regex.Pattern.compile("(?m)^%?>> (.*?)(\\r\\n?|\\n)");

            java.util.regex.Matcher matcher = pattern.matcher(w);

            while (matcher.find()) {
                if (!result.isEmpty()) {
                    result.append("\r\n");
                }

                result.append(matcher.group(1));
            }

            ds.push(result.toString());
        });
        // current-time-millis: [] -> [string]
        word("current-time-millis", (DSWord) ds -> {
            ds.push(String.valueOf(System.currentTimeMillis()));
        });

        // operating-system: [] -> [string]
        word("operating-system", (DSWord) ds -> {
            ds.push(System.getProperty("os.name"));
        });

        // call: [quoteStack] -> []
        word("call", (DSCSWord) (ds, cs) -> {
            Deque<Object> quote = (Deque<Object>) ds.pop();

            List<Object> items = new ArrayList<>(quote);
            Collections.reverse(items);

            for (Object item : items) {
                cs.push(item);
            }
        });
        // call/cc: [quoteStack] -> [continuation], quoteStack wird auf cs gelegt
        word("call/cc", (DSCSWord) (ds, cs) -> {
            Deque<Object> quote = (Deque<Object>) ds.pop();

            Deque<Object> continuation = new ArrayDeque<>();
            continuation.push(new ArrayDeque<>(cs));
            continuation.push(new ArrayDeque<>(ds));

            ds.push(continuation);

            List<Object> items = new ArrayList<>(quote);
            Collections.reverse(items);

            for (Object item : items) {
                cs.push(item);
            }
        });
        // continue: [dsState csState] -> setzt ds und cs zurück
        word("continue", (DSCSWord) (ds, cs) -> {
            Deque<Object> csState = (Deque<Object>) ds.pop();
            Deque<Object> dsState = (Deque<Object>) ds.pop();

            ds.clear();
            ds.addAll(dsState);

            cs.clear();
            cs.addAll(csState);
        });
        // get-dict: [] -> [dict]
        word("get-dict", (FullWord) (ds, cs, dict) -> {
            ds.push(new HashMap<>(dict));
        });
        // set-dict: [dict] -> []
        word("set-dict", (FullWord) (ds, cs, dict) -> {
            Map<String, Word> newDict = (Map<String, Word>) ds.pop();

            dict.clear();
            dict.putAll(newDict);
        });
        // stepcc: führt genau einen VM-Schritt aus
        word("stepcc", (FullWord) (ds, cs, dict) -> {
            if (cs.isEmpty()) {
                throw new IllegalStateException("stepcc erwartet nicht-leeren Call Stack");
            }

            Object itm = cs.pop();

            try {
                if (itm instanceof String name) {
                    Word res = dict.get(name);

                    if (res != null) {
                        for (int i = res.words().size() - 1; i >= 0; i--) {
                            cs.push(res.words().get(i));
                        }

                        res.run(ds, cs, dict);
                    } else {
                        ds.push(name);
                        cs.push("read-word");
                    }

                } else if (itm instanceof Map<?, ?>) {
                    ds.push(itm);
                    cs.push("read-mapping");

                } else {
                    ds.push(itm);
                }

            } catch (Throwable e) {
                cs.push("error");
            }
        });
        // apply: [f s] -> [resultStack]
        word("apply", (DSWord) ds -> {
            Deque<Object> s = (Deque<Object>) ds.pop();
            VMFunction f = (VMFunction) ds.pop();

            ds.push(f.apply(s));
        });

        // compose: [f2 f1] -> [f]
        word("compose", (DSWord) ds -> {
            VMFunction f1 = (VMFunction) ds.pop();
            VMFunction f2 = (VMFunction) ds.pop();

            ds.push((VMFunction) input -> f2.apply(f1.apply(input)));
        });

        // func: [dict quote] -> [function]
        word("func", (DSWord) ds -> {
            Deque<Object> quote = (Deque<Object>) ds.pop();
            Map<String, Word> capturedDict = (Map<String, Word>) ds.pop();

            VMFunction fn = input -> {
                Deque<Object> localDs = new ArrayDeque<>(input);
                Deque<Object> localCs = new ArrayDeque<>(quote);
                Map<String, Word> localDict = new HashMap<>(capturedDict);

                while (!localCs.isEmpty()) {
                    Object itm = localCs.pop();

                    if (itm instanceof String name && localDict.containsKey(name)) {
                        Word w = localDict.get(name);

                        for (int i = w.words().size() - 1; i >= 0; i--) {
                            localCs.push(w.words().get(i));
                        }

                        w.run(localDs, localCs, localDict);
                    } else {
                        localDs.push(itm);
                    }
                }

                return localDs;
            };

            ds.push(fn);
        });
        // integer?: [w] -> ["t" | "f"]
        word("integer?", (DSWord) ds -> {
            Object w = ds.pop();

            if (!(w instanceof String s)) {
                ds.push("f");
                return;
            }

            try {
                Integer.parseInt(s);
                ds.push("t");
            } catch (NumberFormatException e) {
                ds.push("f");
            }
        });

        // +: [x y] -> [x+y]
        word("+", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(String.valueOf(x + y));
        });

        word("-", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(String.valueOf(x - y));
        });

        word("*", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(String.valueOf(x * y));
        });

        word("div", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(String.valueOf(x / y));
        });

        word("mod", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(String.valueOf(x % y));
        });

        word("<", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(x < y ? "t" : "f");
        });

        word(">", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(x > y ? "t" : "f");
        });

        word("==", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(x == y ? "t" : "f");
        });

        word("<=", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(x <= y ? "t" : "f");
        });

        word(">=", (DSWord) ds -> {
            int y = Integer.parseInt((String) ds.pop());
            int x = Integer.parseInt((String) ds.pop());

            ds.push(x >= y ? "t" : "f");
        });
        word("\\\\",
                new ArrayDeque<>(List.of(
                        "dup", "top", "rot", "swap",
                        "push", "swap", "pop", "continue"
                )),
                "call/cc"
        );

        word("load",
                "slurp", "uncomment", "tokenize"
        );

        word("run",
                "load", "call"
        );

        word("start",
                "slurp", "uncomment", "tokenize", "get-dict", "func", "emptystack", "swap", "apply"
        );
    }
    public static void main(String[] args) {

        defineBasicWords();

        if (args.length == 0) {
            args = new String[] {
                    "prelude-plain.txt",
                    "run",
                    "say-hi"
            };
        }

        // Programm auf den Call Stack legen
        for (int i = args.length - 1; i >= 0; i--) {
            cs.push(args[i]);
        }

        Scanner scanner = new Scanner(System.in);

        while (!cs.isEmpty()) {

            // Einen VM-Schritt ausführen
            dict.get("stepcc").run(ds, cs, dict);

            // Definierte Wörter in einer Zeile
            System.out.println(
                    "Words: " +
                            String.join(
                                    " ",
                                    new TreeSet<>(dict.keySet()) // alphabetisch sortiert
                            )
            );

            // Data Stack und Call Stack
            System.out.println("Data Stack:" + ds + "\t" + cs + "<<<Call Stack" );

            // Auf Enter warten
            System.out.print("Weiter mit Enter...");
            scanner.nextLine();
            System.out.println();
        }

        // Endergebnis
        System.out.println("Programm beendet.");
        System.out.println("DS: " + ds);
    }
}