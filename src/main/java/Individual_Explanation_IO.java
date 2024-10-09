import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import io.manchester.ManchesterSyntaxExplanationRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Individual_Explanation_IO {


    
    private OWLClassExpression parseQueryString(String ns, String queryStr) {
        // Remove outer parentheses if present
        queryStr = queryStr.trim();
        if (queryStr.startsWith("(") && queryStr.endsWith(")")) {
            queryStr = queryStr.substring(1, queryStr.length() - 1).trim();
        }

        // Handle OR at the top level
        List<String> orParts = splitAtTopLevel(queryStr, " or ");
        if (orParts.size() > 1) {
            List<OWLClassExpression> orExpressions = new ArrayList<>();
            for (String orPart : orParts) {
                orExpressions.add(parseQueryString(ns, orPart));
            }
            return OWL.or(orExpressions.toArray(new OWLClassExpression[0]));
        }

        // Handle AND at the top level
        List<String> andParts = splitAtTopLevel(queryStr, " and ");
        if (andParts.size() > 1) {
            List<OWLClassExpression> andExpressions = new ArrayList<>();
            for (String andPart : andParts) {
                andExpressions.add(parseQueryString(ns, andPart));
            }
            return OWL.and(andExpressions.toArray(new OWLClassExpression[0]));
        }

        // Handle NOT
        if (queryStr.startsWith("not ")) {
            String subQuery = queryStr.substring(4).trim();
            return OWL.not(parseQueryString(ns, subQuery));
        }

        // Handle existential quantification (some)
        if (queryStr.contains(" some ")) {
            List<String> subPartsList = splitAtTopLevel(queryStr, " some ");
            if (subPartsList.size() == 2) {
                OWLObjectProperty property = OWL.ObjectProperty(ns + subPartsList.get(0).trim());
                OWLClassExpression cls = parseQueryString(ns, subPartsList.get(1).trim());
                return OWL.some(property, cls);
            }
        }

        // Handle universal quantification (only)
        if (queryStr.contains(" only ")) {
            List<String> subPartsList = splitAtTopLevel(queryStr, " only ");
            if (subPartsList.size() == 2) {
                OWLObjectProperty property = OWL.ObjectProperty(ns + subPartsList.get(0).trim());
                OWLClassExpression cls = parseQueryString(ns, subPartsList.get(1).trim());
                return OWL.only(property, cls);
            }
        }

        // Handle cardinality restrictions (exactly, min, max)
        if (queryStr.contains(" exactly ")) {
            List<String> subPartsList = splitAtTopLevel(queryStr, " exactly ");
            if (subPartsList.size() == 2) {
                OWLObjectProperty property = OWL.ObjectProperty(ns + subPartsList.get(0).trim());
                int cardinality = Integer.parseInt(subPartsList.get(1).trim());
                return OWL.exactly(property, cardinality);
            }
        } else if (queryStr.contains(" min ")) {
            List<String> subPartsList = splitAtTopLevel(queryStr, " min ");
            if (subPartsList.size() == 2) {
                OWLObjectProperty property = OWL.ObjectProperty(ns + subPartsList.get(0).trim());
                int cardinality = Integer.parseInt(subPartsList.get(1).trim());
                return OWL.min(property, cardinality);
            }
        } else if (queryStr.contains(" max ")) {
            List<String> subPartsList = splitAtTopLevel(queryStr, " max ");
            if (subPartsList.size() == 2) {
                OWLObjectProperty property = OWL.ObjectProperty(ns + subPartsList.get(0).trim());
                int cardinality = Integer.parseInt(subPartsList.get(1).trim());
                return OWL.max(property, cardinality);
            }
        }

        // Handle simple class names
        return OWL.Class(ns + queryStr.trim());
    }



    // Helper method to split by a keyword at the top level, ignoring nested parentheses
    private List<String> splitAtTopLevel(String input, String delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int lastIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && input.startsWith(delimiter, i)) {
                result.add(input.substring(lastIndex, i).trim());
                lastIndex = i + delimiter.length();
                i += delimiter.length() - 1; // skip the delimiter
            }
        }
        result.add(input.substring(lastIndex).trim());
        return result;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, OWLException, IOException {
        // Record the start time
        long startTime = System.currentTimeMillis();

        // Create a Scanner object for user input
        Scanner scanner = new Scanner(System.in);

        // Take namespace input from user
        System.out.print("Enter the namespace without quotes (e.g., http://www.benchmark.org/family#): ");
        String ns = scanner.nextLine();

        // Take local ontology path input from user
        System.out.print("Enter the local ontology file path without quotes and replace / to (e.g., E:/Workspace_Dice/DataSource/family.owl): ");
        String localOntologyPath = scanner.nextLine();

        // Take the query string input from user
        System.out.print("Enter the query (e.g., Female and hasChild some Male): ");
        String queryStr = scanner.nextLine();

        // Run the explanation with user inputs
        Individual_Explanation_IO app = new Individual_Explanation_IO();
        app.run(ns, localOntologyPath, queryStr);

        // Record the end time
        long endTime = System.currentTimeMillis();

        // Calculate the execution time
        long executionTime = endTime - startTime;

        // Print the execution time
        System.out.println("Execution time: " + executionTime + " milliseconds");

        // Close the scanner
        scanner.close();
    }
}
