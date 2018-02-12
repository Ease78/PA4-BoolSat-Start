import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import bool_exp.ASTNode;
import bool_exp.BoolSatParser;

/*
 * Author: Ali Alshehri. This class accepts arugments from text files that have
 * boolean expressions expressed in the following format:
 * (a || b || !c) && (!a || b || c)
 * and parses it into valid tokens for the java compiler and then enumerating a
 * map<String, BOolean> at which point the class exahustively search the
 * possibilities of having a true statement as a whole from all possible
 * combinations of variables. If at least one whole expression is true, then the
 * whole expression is SAT. Otherwise, it's UNSAT
 * 
 * Cmd-line args[0] is the filename, args{1} is an optional debug option that
 * evalutates the whole statement.
 */

public class PA4BoolSat {

    public static void main(String[] args) {

        System.out.println("testing, attention please to bool sat");
        // Check that some input was provided
        if (args.length < 1 || args.length > 2) {
            System.err.println("USAGE: java PA4Main <inputFile.txt>");
            System.exit(1);
        }

        // Get the expression from the file
        String expression = null;
        // checks for debug command
        boolean debug;
        try (Scanner s = new Scanner(new File(args[0]));) {
            expression = s.nextLine();
            // checks for debug command
            debug = args[1].equals("DEBUG");
        } catch (FileNotFoundException e) {
            debug = false;
            System.err.printf("'%s' is not a valid file path.", args[0]);
            System.exit(1);
        }

        // evaluation of SAT or UNSAT
        String isSAT = null;

        Map<String, Boolean> booleanMap = new HashMap<String, Boolean>();

        System.out.println(expression);

        // Call the parser to generate the AST for the expression
        ASTNode root = BoolSatParser.parse(expression);

        // Traverse the AST and generate a dot representation
        String dotOutput = BoolSatParser.dotify(root);

        // Output the dot represenation to stdout
        System.out.println(dotOutput);
        List<String> nodesArr = inorderTraversal(root);

        List<String> results = exhaustiveSearch(-1, root, debug);
        Collections.sort(results);
        for (String result : results) {
            if (!result.equals(""))
                System.out.println(result);
        }

        // for (String aNode : nodesArr) {
        // System.out.println(aNode);
        // }

    }

    public static List<String> inorderTraversal(ASTNode root) {
        Stack<ASTNode> stack = new Stack<ASTNode>();
        List<String> result = new ArrayList<String>();
        ASTNode p = root;
        while (p != null) {
            stack.push(p);
            p = p.child1;
        }
        while (!stack.isEmpty()) {
            ASTNode t = stack.pop();
            result.add(t.getId());
            if (t.child2 != null) {
                t = t.child2;
                while (t != null) {
                    stack.push(t);
                    t = t.child1;
                }
            }
        }
        return result;
    }

    private static List<String> exhaustiveSearch(int values, ASTNode ast,
            boolean debug) {

        List<String> evaluations = new ArrayList<String>();
        TreeSet<String> ids = getIds(ast);
        if (values == -1) // initial case
            values = (int) Math.pow(2, ids.size()) - 1; // should be 1111...1 in
                                                        // binary
        // use values to create map
        Map<String, Boolean> idValues = new TreeMap<String, Boolean>();
        int bitmask = 1;
        for (String id : ids) {
            idValues.put(id, (values & bitmask) != 0);
            bitmask *= 2; // move mask to next digit
        }

        evaluations.add(getResultString(ast, idValues, debug));
        if (values != 0) // last possibility
            evaluations.addAll(exhaustiveSearch(values - 1, ast, debug));
        return evaluations;

    }

    private static String getResultString(ASTNode root,
            Map<String, Boolean> idValues, boolean debug) {
        boolean result;
        String resultStr = "";

        if ((result = evaluate(root, idValues)) || debug) {
            for (String id : idValues.keySet()) {
                resultStr += id + ": " + idValues.get(id) + ", ";
            }
        }
        if (debug) { // add extra debug info
            resultStr += result;
        } else if (resultStr.length() >= 2) {
            // get rid of added ", " from end of resultStr
            resultStr = resultStr.substring(0, resultStr.length() - 2);
        }
        return resultStr;
    }

    private static boolean evaluate(ASTNode node,
            Map<String, Boolean> idValues) {

        if (node.child1 == null && node.child2 == null)// leaf node (id)
            return idValues.get(node.getId());
        else if (node.child1 == null || node.child2 == null)// NOT node
            return evaluate(node.child1 != null ? node.child1 : node.child2,
                    idValues);
        else { // AND or OR
            if (node.isAnd())
                return evaluate(node.child1, idValues)
                        && evaluate(node.child2, idValues);
            else
                return evaluate(node.child1, idValues)
                        || evaluate(node.child2, idValues);
        }
    }

    private static TreeSet<String> getIds(ASTNode node) {
        TreeSet<String> ids = new TreeSet<String>();
        if (node.child1 == null && node.child2 == null) {// leaf node (id)
            ids.add(node.getId());
        } else {
            if (node.child1 != null)
                ids.addAll(getIds(node.child1));
            if (node.child2 != null)
                ids.addAll(getIds(node.child2));
        }
        return ids;
    }

}
