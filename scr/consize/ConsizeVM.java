package consize;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConsizeVM {

    public static final Deque<Object> ds = new ArrayDeque<>();
    public static final Deque<Object> cs = new ArrayDeque<>();
    public static final Map<Object, Object> dict = new HashMap<>();

    public static final Scanner scanner = new Scanner(System.in);
    public static final boolean PRINT = false;

    public static final Object NIL = new Object() {
        @Override
        public String toString() {
            return "nil";
        }
    };

    public interface Word {
        void run(
                Deque<Object> ds,
                Deque<Object> cs,
                Map<Object, Object> dict
        );
    }

    public static void word(String name, DSWord fn) {
        dict.put(name, (Word) (ds, cs, dict) -> fn.run(ds));
    }

    public static void word(String name, CSWord fn) {
        dict.put(name, (Word) (ds, cs, dict) -> fn.run(cs));
    }

    public static void word(String name, DSCSWord fn) {
        dict.put(name, (Word) (ds, cs, dict) -> fn.run(ds, cs));
    }

    public static void word(String name, FullWord fn) {
        dict.put(name, (Word) fn::run);
    }

    public static void word(String name, Object... words) {
        Deque<Object> quote = new ArrayDeque<>();

        for (int i = words.length - 1; i >= 0; i--) {
            quote.push(words[i]);
        }

        dict.put(name, quote);
    }

    public static void word(String name, String... words) {
        Deque<Object> quote = new ArrayDeque<>();

        for (int i = words.length - 1; i >= 0; i--) {
            quote.push(words[i]);
        }

        dict.put(name, quote);
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
                Map<Object, Object> dict
        );
    }
    @FunctionalInterface
    public interface VMFunction {
        Deque<Object> apply(Deque<Object> ds);
    }

    public static void pushQuoteToCS(Object quote, Deque<Object> cs) {
        List<Object> items;

        if (quote instanceof Deque<?> deque) {
            items = new ArrayList<>(deque);
        } else if (quote instanceof List<?> list) {
            items = new ArrayList<>(list);
        } else {
            throw new IllegalArgumentException("Quote erwartet Stack/List, bekam: " + quote);
        }

        for (int i = items.size() - 1; i >= 0; i--) {
            cs.push(items.get(i));
        }
    }
    public static boolean consizeEquals(Object x, Object y) {
        if (x == y) {
            return true;
        }

        if (x instanceof Deque<?> dx && y instanceof Deque<?> dy) {
            return sequenceEquals(dx, dy);
        }

        if (x instanceof List<?> lx && y instanceof List<?> ly) {
            return sequenceEquals(lx, ly);
        }

        if (x instanceof Deque<?> dx && y instanceof List<?> ly) {
            return sequenceEquals(dx, ly);
        }

        if (x instanceof List<?> lx && y instanceof Deque<?> dy) {
            return sequenceEquals(lx, dy);
        }

        return Objects.equals(x, y);
    }

    public static boolean sequenceEquals(Iterable<?> xs, Iterable<?> ys) {
        Iterator<?> xi = xs.iterator();
        Iterator<?> yi = ys.iterator();

        while (xi.hasNext() && yi.hasNext()) {
            if (!consizeEquals(xi.next(), yi.next())) {
                return false;
            }
        }

        return !xi.hasNext() && !yi.hasNext();
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
            } else if (itm == null || itm == NIL) {
                ds.push("nil");
            } else {
                ds.push("_|_");
            }
        });

        word("equal?", (DSWord) ds -> {
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(consizeEquals(x, y) ? "t" : "f");
        });

        word("identical?", (DSWord) ds -> {
            Object y = ds.pop();
            Object x = ds.pop();

            ds.push(x == y ? "t" : "f");
        });
        word("emptystack", (DSWord) ds -> {
            ds.push(new ArrayDeque<>());
        });

        word("push", (DSWord) ds -> {
            Object x = ds.pop();
            Object s = ds.pop();

            if (!(s instanceof Deque<?> deque)) {
                throw new IllegalArgumentException("push erwartet Stack unter Wert, bekam: " + s);
            }

            Deque<Object> stack = new ArrayDeque<>((Deque<Object>) deque);

            stack.push(x);
            ds.push(stack);
        });
        word("top", (DSWord) ds -> {
            Deque<Object> stack = (Deque<Object>) ds.pop();

            Object value = stack.peek();

            ds.push(value == null ? NIL : value);
        });

        word("pop", (DSWord) ds -> {
            Deque<Object> stack = new ArrayDeque<>((Deque<Object>) ds.pop());

            if (!stack.isEmpty()) {
                stack.pop();
            }

            ds.push(stack);
        });

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
        word("mapping", (DSWord) ds -> {
            Deque<Object> s = new ArrayDeque<>((Deque<Object>) ds.pop());

            Map<Object, Object> map = new HashMap<>();

            while (!s.isEmpty()) {
                Object key = s.pop();
                Object value = s.pop();

                map.put(key, value);
            }

            ds.push(map);
        });

        word("unmap", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            List<Object> flat = new ArrayList<>();

            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                flat.add(entry.getKey());
                flat.add(entry.getValue());
            }

            for (int i = flat.size() - 1; i >= 0; i--) {
                result.push(flat.get(i));
            }

            ds.push(result);
        });

        word("keys", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            List<Object> keys = new ArrayList<>(m.keySet());

            for (int i = keys.size() - 1; i >= 0; i--) {
                result.push(keys.get(i));
            }

            ds.push(result);
        });

        word("assoc", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();
            Object k = ds.pop();
            Object v = ds.pop();

            Map<Object, Object> result = new HashMap<>(m);
            result.put(k, v);

            ds.push(result);
        });


        word("dissoc", (DSWord) ds -> {
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();
            Object k = ds.pop();

            Map<Object, Object> result = new HashMap<>(m);
            result.remove(k);

            ds.push(result);
        });
        word("get", (DSWord) ds -> {
            Object d = ds.pop();
            Map<Object, Object> m = (Map<Object, Object>) ds.pop();
            Object k = ds.pop();

            ds.push(m.getOrDefault(k, d));
        });

        word("merge", (DSWord) ds -> {
            Map<Object, Object> m1 = (Map<Object, Object>) ds.pop();
            Map<Object, Object> m2 = (Map<Object, Object>) ds.pop();

            Map<Object, Object> result = new HashMap<>(m2);
            result.putAll(m1);

            ds.push(result);
        });
        word("word", (DSWord) ds -> {
            Deque<Object> s = (Deque<Object>) ds.pop();

            StringBuilder result = new StringBuilder();

            for (Object item : s) {
                result.append(item);
            }

            ds.push(result.toString());
        });

        word("unword", (DSWord) ds -> {
            String w = (String) ds.pop();

            Deque<Object> result = new ArrayDeque<>();

            for (int i = w.length() - 1; i >= 0; i--) {
                result.push(String.valueOf(w.charAt(i)));
            }

            ds.push(result);
        });

        word("char", (DSWord) ds -> {
            String w = (String) ds.pop();

            String result = switch (w) {
                case "\\space" -> " ";
                case "\\newline" -> "\n";
                default -> {
                    if (w.startsWith("\\") && w.length() == 2) {
                        yield w.substring(1);
                    }

                    if (w.length() == 1) {
                        yield w;
                    }

                    throw new IllegalArgumentException("char erwartet Zeichenliteral, bekam: " + w);
                }
            };

            ds.push(result);
        });
        word("print", (DSWord) ds -> {
            Object w = ds.pop();

            System.out.print(String.valueOf(w));
            System.out.flush();
        });

        word("flush", (DSWord) ds -> {
            System.out.flush();
        });
        word("read-line", (DSWord) ds -> {
            ds.push(scanner.nextLine());
        });
        word("slurp", (DSWord) ds -> {
            String file = (String) ds.pop();

            try (var in = ConsizeVM.class.getResourceAsStream(file)) {

                if (in == null) {
                    throw new RuntimeException("Resource nicht gefunden: " + file);
                }

                String content = new String(
                        in.readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8
                );

                ds.push(content);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

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
        // uncomment: entfernt %-Kommentare
        word("uncomment", (DSWord) ds -> {
            String w = (String) ds.pop();

            String result = w.replaceAll("(?m)\\s*%.*$", "");

            ds.push(result);
        });

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

        // undocument: extrahiert nur Dokumentationszeilen, die mit >> beginnen
        word("undocument", (DSWord) ds -> {
            String w = (String) ds.pop();

            StringBuilder result = new StringBuilder();

            var pattern = java.util.regex.Pattern.compile("(?m)^%?>> (.*?)(\\r\\n?|\\n)");
            var matcher = pattern.matcher(w);

            while (matcher.find()) {
                if (!result.isEmpty()) {
                    result.append("\r\n");
                }

                result.append(matcher.group(1));
            }

            ds.push(result.toString());
        });
        word("current-time-millis", (DSWord) ds -> {
            ds.push(String.valueOf(System.currentTimeMillis()));
        });

        word("operating-system", (DSWord) ds -> {
            ds.push(System.getProperty("os.name"));
        });

        word("call", (DSCSWord) (ds, cs) -> {
            Deque<Object> quote = (Deque<Object>) ds.pop();

            List<Object> items = new ArrayList<>(quote);
            Collections.reverse(items);

            for (Object item : items) {
                cs.push(item);
            }
        });
        word("call/cc", (DSCSWord) (ds, cs) -> {
            Deque<Object> quote = (Deque<Object>) ds.pop();

            Deque<Object> oldDs = new ArrayDeque<>(ds);
            Deque<Object> oldCs = new ArrayDeque<>(cs);

            Deque<Object> continuation = new ArrayDeque<>();

            // continuation = [oldCs, oldDs]
            continuation.push(oldDs);
            continuation.push(oldCs);

            ds.clear();
            ds.addAll(continuation);

            cs.clear();
            cs.addAll(quote);
        });
        word("continue", (DSCSWord) (ds, cs) -> {
            Deque<Object> newCs = (Deque<Object>) ds.pop();
            Deque<Object> newDs = (Deque<Object>) ds.pop();

            ds.clear();
            ds.addAll(newDs);

            cs.clear();
            cs.addAll(newCs);
        });
        word("get-dict", (FullWord) (ds, cs, dict) -> {
            ds.push(new HashMap<>(dict));
        });
        word("set-dict", (FullWord) (ds, cs, dict) -> {
            Object obj = ds.pop();

            if (!(obj instanceof Map<?, ?> newDict)) {
                throw new IllegalArgumentException("set-dict erwartet Map, bekam: " + obj);
            }

            dict.clear();

            for (Map.Entry<?, ?> entry : newDict.entrySet()) {
                dict.put(entry.getKey(), entry.getValue());
            }
        });
        word("stepcc", (FullWord) (ds, cs, dict) -> {
            if (cs.isEmpty()) {
                throw new IllegalStateException("stepcc erwartet nicht-leeren Call Stack");
            }

            Object itm = cs.pop();

            try {
                if (itm instanceof String name) {
                    Object res = dict.get(name);

                    if (res instanceof Word word) {
                        word.run(ds, cs, dict);

                    } else if (res instanceof Deque<?> || res instanceof List<?>) {
                        pushQuoteToCS(res, cs);

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
        word("apply", (DSWord) ds -> {
            VMFunction f = (VMFunction) ds.pop();
            Deque<Object> s = (Deque<Object>) ds.pop();

            ds.push(f.apply(s));
        });

        word("compose", (DSWord) ds -> {
            VMFunction f2 = (VMFunction) ds.pop();
            VMFunction f1 = (VMFunction) ds.pop();

            ds.push((VMFunction) input -> f2.apply(f1.apply(input)));
        });

        word("func", (DSWord) ds -> {
            Map<Object, Object> capturedDict = (Map<Object, Object>) ds.pop();
            Deque<Object> quote = (Deque<Object>) ds.pop();

            VMFunction fn = input -> {
                Deque<Object> localDs = new ArrayDeque<>(input);
                Deque<Object> localCs = new ArrayDeque<>(quote);
                Map<Object, Object> localDict = new HashMap<>(capturedDict);

                while (!localCs.isEmpty()) {
                    Object itm = localCs.pop();

                    if (itm instanceof String name) {
                        Object res = localDict.get(name);

                        if (res instanceof Word word) {
                            word.run(localDs, localCs, localDict);

                        } else if (res instanceof Deque<?> || res instanceof List<?>) {
                            pushQuoteToCS(res, localCs);

                        } else {
                            localDs.push(name);
                        }

                    } else {
                        localDs.push(itm);
                    }
                }

                return localDs;
            };

            ds.push(fn);
        });
        word("integer?", (DSWord) ds -> {
            Object w = ds.pop();

            if (!(w instanceof String s)) {
                ds.push("f");
                return;
            }

            try {
                Long.parseLong(s);
                ds.push("t");
            } catch (NumberFormatException e) {
                ds.push("f");
            }
        });

        word("+", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(String.valueOf(x + y));
        });

        word("-", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(String.valueOf(x - y));
        });

        word("*", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(String.valueOf(x * y));
        });

        word("div", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(String.valueOf(x / y));
        });

        word("mod", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(String.valueOf(x % y));
        });

        word("<", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(x < y ? "t" : "f");
        });

        word(">", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(x > y ? "t" : "f");
        });

        word("==", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(x == y ? "t" : "f");
        });

        word("<=", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(x <= y ? "t" : "f");
        });

        word(">=", (DSWord) ds -> {
            long y = Long.parseLong((String) ds.pop());
            long x = Long.parseLong((String) ds.pop());

            ds.push(x >= y ? "t" : "f");
        });
        word("\\",
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
                    "\\",
                    "/consize/prelude-plain.txt",
                    "run",
                    "say-hi"
            };
        }

        for (int i = args.length - 1; i >= 0; i--) {
            cs.push(args[i]);
        }

        Scanner scanner = new Scanner(System.in);

        while (!cs.isEmpty()) {
            Object next = cs.pop();

            if (next instanceof String name) {
                Object res = dict.get(name);

                if (res instanceof Word word) {
                    word.run(ds, cs, dict);

                } else if (res instanceof Deque<?> || res instanceof List<?>) {
                    pushQuoteToCS(res, cs);

                } else {
                    ds.push(name);
                }

            } else {
                ds.push(next);
            }
            if(PRINT){
                System.out.println(
                        "Words: " + dict.keySet().stream()
                                .map(String::valueOf)
                                .sorted()
                                .collect(java.util.stream.Collectors.joining(" "))
                );

                System.out.println("DS: " + ds);
                System.out.println("CS: " + cs);
            }

        }

        System.out.println("Programm beendet.");
        System.out.println("DS: " + ds);
    }
    public static String shortStack(Deque<?> stack) {
        List<?> list = new ArrayList<>(stack);

        // Einzelne Elemente kürzen
        List<String> formatted = new ArrayList<>();
        for (Object item : list) {
            formatted.add(shortItem(item));
        }

        // Falls Stack kurz genug ist, alles anzeigen
        if (formatted.size() <= 6) {
            return formatted.toString();
        }

        // Sonst erste 3 + ... + letzte 3
        List<String> shortened = new ArrayList<>();
        shortened.addAll(formatted.subList(0, 3));
        shortened.add("...");
        shortened.addAll(formatted.subList(formatted.size() - 3, formatted.size()));

        return shortened.toString();
    }


    public static @NotNull String shortItem(Object item) {
        String s = String.valueOf(item);

        // Einzelnes Element auf max. 40 Zeichen begrenzen
        int maxLength = 40;

        if (s.length() > maxLength) {
            return s.substring(0, maxLength - 3) + "...";
        }

        return s;
    }
}