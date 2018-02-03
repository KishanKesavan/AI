import java.io.*;
import java.util.*;

public class FOLResolution {
    public static class Predicate {
        private boolean negated;
        private String name;
        private List<String> arguments;

        public Predicate() {
        }

        public Predicate(boolean negated, String name, List<String> arguments) {
            this.negated = negated;
            this.name = name;
            this.arguments = arguments;
        }

        public Predicate(Predicate predicate){
            this.negated = predicate.isNegated();
            this.setArguments(new ArrayList<>(predicate.getArguments()));
            this.name = predicate.getName();
        }

        public boolean isNegated() {
            return negated;
        }

        public void setNegated(boolean negated) {
            this.negated = negated;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public void setArguments(List<String> arguments) {
            this.arguments = arguments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Predicate predicate = (Predicate) o;

            if (negated != predicate.negated) return false;
            if (name != null ? !name.equals(predicate.name) : predicate.name != null) return false;
            return arguments != null ? arguments.equals(predicate.arguments) : predicate.arguments == null;
        }

        @Override
        public int hashCode() {
            int result = (negated ? 1 : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
            return result;
        }
    }

    public static class Rule {

        public Rule(List<Predicate> predicates) {
            this.predicates = predicates;
        }

        private List<Predicate> predicates;


        public List<Predicate> getPredicates() {
            return predicates;
        }

        public void setPredicates(List<Predicate> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Rule rule = (Rule) o;

            return predicates != null ? predicates.equals(rule.predicates) : rule.predicates == null;
        }

        @Override
        public int hashCode() {
            return predicates != null ? predicates.hashCode() : 0;
        }
    }

    private static List<Rule> rules;
    private static List<Predicate> queries;
    private static Map<String, Set<Rule>> predicateRuleMapping;
    private static Map<String, String> substitution;
    private static BufferedWriter bw;
    private static Date queryStartTime;
    private static void parseInput() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace(" ", "");
                lines.add(line);
            }
            queries = new ArrayList<>();
            rules = new ArrayList<>();
            predicateRuleMapping = new HashMap<>();
            int noOfQueries = Integer.parseInt(lines.get(0));
            lines.remove(0);
            for (int i = 0; i < noOfQueries; ++i) {
                line = lines.get(0);
                queries.add(getPredicate(line));
                lines.remove(0);
            }
            int noOfRules = Integer.parseInt(lines.get(0));
            lines.remove(0);
            for (int i = 0; i < noOfRules; ++i) {
                line = lines.get(0);
                Rule rule = getRule(line);
                for (Predicate predicate : rule.getPredicates()) {
                    String name = getName(predicate);
                    predicateRuleMapping.computeIfAbsent(name, k -> new HashSet<>());
                    predicateRuleMapping.get(name).add(rule);
                }
                rules.add(rule);
                lines.remove(0);
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String getName(Predicate predicate) {
        return (predicate.isNegated() ? "~" : "") + predicate.getName();
    }

    private static Rule getRule(String s) {
        String[] predicates = s.split("\\|");
        List<Predicate> predicateList = new ArrayList<>();
        for (String predicate : predicates) {
            predicateList.add(getPredicate(predicate));
        }
        return new Rule(standardizeVariables(predicateList));
    }

    private static Predicate getPredicate(String s) {
        boolean negated = s.charAt(0) == '~';
        s = s.replace("~", "");
        String name = s.split("\\(")[0];
        List<String> arguments = Arrays.asList(s.substring(s.indexOf('(') + 1, s.indexOf(')')).split(","));
        return new Predicate(negated, name, arguments);
    }

    private static void printPredicates(List<Predicate> predicates) {
        StringBuilder s = new StringBuilder();
        for (Predicate predicate : predicates) {
            if (predicate.isNegated()) {
                s.append("~");
            }
            s.append(predicate.getName() + "(");
            for (String argument : predicate.getArguments()) {
                s.append(argument + ",");
            }
            s.deleteCharAt(s.length() - 1);
            s.append(") | ");
        }
        s.delete(s.length() - 3, s.length() - 1);
        System.out.println(s);
    }

    private static String getPredicatesString(List<Predicate> predicates) {
        StringBuilder s = new StringBuilder();
        int count = 0;
        Collections.sort(predicates, Comparator.comparing(FOLResolution::getName));

        Map<String, String> variableMap = new HashMap<>();
        for (Predicate predicate : predicates) {
            if (predicate.isNegated()) {
                s.append("~");
            }
            s.append(predicate.getName() + "(");
            for (String argument : predicate.getArguments()) {
                if (isAVariable(argument)) {
                    if (variableMap.containsKey(argument)) {
                        s.append(variableMap.get(argument) + ",");
                    } else {
                        String temp = "v" + (++count);
                        s.append(temp + ",");
                        variableMap.put(argument, temp);
                    }
                } else {
                    s.append(argument + ",");
                }
            }
            s.deleteCharAt(s.length() - 1);
            s.append(") | ");
        }
        if (predicates.size() > 0)
            s.delete(s.length() - 3, s.length() - 1);

        return s.toString();
    }

    private static boolean isAVariable(String argument) {
        return Character.isLowerCase(argument.charAt(0));
    }

    private static List<Predicate> standardizeVariables(List<Predicate> predicates) {
        List<Predicate> result = new ArrayList<>();
        Map<String, String> variableMapping = new HashMap<>();
        for (Predicate predicate : predicates) {
            Predicate tempPredicate = new Predicate();
            tempPredicate.setName(predicate.getName());
            tempPredicate.setNegated(predicate.isNegated());
            List<String> tempArguments = new ArrayList<>();
            for (String argument : predicate.getArguments()) {
                if (isAVariable(argument)) {
                    if (!variableMapping.containsKey(argument)) {
                        variableMapping.put(argument, "v" + new Random().nextInt(Integer.MAX_VALUE));
                    }
                    tempArguments.add(variableMapping.get(argument));
                } else {
                    tempArguments.add(argument);
                }
            }
            tempPredicate.setArguments(tempArguments);
            result.add(tempPredicate);
        }
        return result;
    }

    private static List<Predicate> removeDuplicates(List<Predicate> predicates) {
        Set<String> variablePredicateSet = new HashSet<>();
        List<Predicate> newResolved = new ArrayList<>();
        for (Predicate predicate : predicates) {
            StringBuilder s = new StringBuilder();
            s.append(getName(predicate));
            int count = 0;
            for (String argument : predicate.getArguments()) {
                if (isAVariable(argument)) {
                    s.append("v" + (++count));
                } else {
                    s.append(argument);
                }
            }
            if (!variablePredicateSet.contains(s.toString())) {
                newResolved.add(predicate);
                variablePredicateSet.add(s.toString());
            }
        }
        return newResolved;
    }

    private static boolean resolve(List<Predicate> resolved, Rule rule, List<String> resolvedHistory) {
        if((new Date().getTime() - queryStartTime.getTime()) > 150 * 1000){
            return false;
        }
        printPredicates(resolved);
        printPredicates(rule.getPredicates());
        System.out.println( );
        for (Predicate predicate : resolved) {
            for (Predicate rulePredicate : rule.getPredicates()) {
                unify(predicate, rulePredicate);
                if (substitution == null) {
                    continue;
                }
                List<Predicate> ruleTemp = new ArrayList<>(rule.getPredicates());
                List<Predicate> resolvedTemp = new ArrayList<>(resolved);
                ruleTemp.remove(rulePredicate);
                resolvedTemp.remove(predicate);
                List<Predicate> newResolved = new ArrayList<>();
                newResolved.addAll(substitute(ruleTemp));
                newResolved.addAll(substitute(resolvedTemp));
                substitution = null;
                if (newResolved.isEmpty()) {
                    return true;
                }
                newResolved = removeDuplicates(newResolved);
                newResolved = standardizeVariables(newResolved);
                String newResolvedString = getPredicatesString(newResolved);
                if (resolvedHistory.contains(newResolvedString)) {
                    return false;
                }
                for (Predicate p : newResolved) {
                    Predicate temp = new Predicate(p);
                    temp.setNegated(!temp.isNegated());
                    String toSearch = getName(temp);
                    if (!predicateRuleMapping.containsKey(toSearch)) {
                        continue;
                    }
                    for (Rule r : predicateRuleMapping.get(toSearch)) {
                        resolvedHistory.add(newResolvedString);
                        if (resolve(newResolved, r, resolvedHistory)) {
                            return true;
                        }
                        resolvedHistory.remove(newResolvedString);
                    }
                }
            }
        }
        return false;
    }

    private static void resolution(Predicate query) {
        try {
            if(checkForDirectMatch(query)){
                System.out.println("TRUE");
                bw.write("TRUE\n");
                return;
            }
            queryStartTime = new Date();
            String toSearch = getName(query);
            query.setNegated(!query.isNegated());
            if (!predicateRuleMapping.containsKey(toSearch)) {
                System.out.println("FALSE");
                bw.write("FALSE\n");
                return;
            }
            boolean resolved = false;
            for (Rule rule : predicateRuleMapping.get(toSearch)) {
                List<Predicate> temp = new ArrayList<Predicate>() {{
                    add(query);
                }};
                if (resolve(temp, rule, new ArrayList<String>() {{
                    add(getPredicatesString(temp));
                }})) {
                    resolved = true;
                    break;
                }
            }
            System.out.println(resolved ? "TRUE" : "FALSE");
            bw.write(resolved ? "TRUE\n" : "FALSE\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean checkForDirectMatch(Predicate query){
        String toSearch = getName(query);
        if(!predicateRuleMapping.containsKey(toSearch)){
            return false;
        }
        for(Rule r : predicateRuleMapping.get(toSearch)){
            if(r.getPredicates().size() > 1){
                continue;
            }
            if(unifyInternal(query.getArguments(),r.getPredicates().get(0).getArguments(),new HashMap<>()) != null){
                return true;
            }
        }
        return false;
    }
    private static List<Predicate> substitute(List<Predicate> predicates) {
        List<Predicate> result = new ArrayList<>();
        for (Predicate predicate : predicates) {
            Predicate tempPredicate = new Predicate();
            tempPredicate.setName(predicate.getName());
            tempPredicate.setNegated(predicate.isNegated());
            List<String> tempArguments = new ArrayList<>();
            for (String argument : predicate.getArguments()) {
                if (isAVariable(argument)) {
                    String temp = argument;
                    while (substitution.containsKey(temp)) {
                        temp = substitution.get(temp);
                    }
                    tempArguments.add(temp);
                } else {
                    tempArguments.add(argument);
                }
            }
            tempPredicate.setArguments(tempArguments);
            result.add(tempPredicate);
        }
        return result;
    }

    static boolean unify(Predicate resolved, Predicate rule) {
        if (!resolved.getName().equals(rule.getName())) {
            return false;
        }
        if (resolved.isNegated() == rule.isNegated()) {
            return false;
        }
        if (resolved.getArguments().size() != rule.getArguments().size()) {
            return false;
        }
        substitution = unifyInternal(resolved.getArguments(), rule.getArguments(), new HashMap<>());
        return substitution != null;
    }

    static Map<String, String> unifyInternal(Object x, Object y, Map<String, String> substitution) {
        if (substitution == null) {
            return null;
        } else if (isAString(x)) {
            String xStr = (String) x;
            String yStr = (String) y;
            if (xStr.equals(yStr)) {
                return substitution;
            } else if (isAVariable(xStr)) {
                return unifyVariable(xStr, yStr, substitution);
            } else if (isAVariable(yStr)) {
                return unifyVariable(yStr, xStr, substitution);
            } else {
                return null;
            }
        } else {
            List<String> xArguments = (List) x;
            List<String> yArguments = (List) y;
            if (xArguments.size() == 1) {
                return unifyInternal(xArguments.get(0), yArguments.get(0), substitution);
            }
            return unifyInternal(xArguments.subList(1, xArguments.size()), yArguments.subList(1, yArguments.size()), unifyInternal(xArguments.get(0), yArguments.get(0), substitution));
        }
    }

    static Map<String, String> unifyVariable(String var, String x, Map<String, String> substitution) {
        if (substitution.containsKey(var)) {
            return unifyInternal(substitution.get(var), x, substitution);
        } else if (substitution.containsKey(x)) {
            return unifyInternal(var, substitution.get(x), substitution);
        } else {
            substitution.put(var, x);
            return substitution;
        }
    }

    static boolean isAString(Object a) {
        return a instanceof String;
    }

    public static void main(String[] args) {

        parseInput();
        try {
            bw = new BufferedWriter(new FileWriter("output.txt"));

            for (Predicate query : queries) {
                resolution(query);
            }
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
