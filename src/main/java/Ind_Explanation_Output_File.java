/*
import com.clarkparsia.owlapiv3.OWL;
        import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
        import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
        import io.manchester.ManchesterSyntaxExplanationRenderer;
        import org.semanticweb.owlapi.model.*;
        import org.semanticweb.owlapi.reasoner.NodeSet;

        import java.io.File;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.io.PrintWriter;
        import java.io.StringWriter;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Scanner;
        import java.util.Set;

public class Ind_Explanation_Output_File {

    // Define a default output directory
    private static final String DEFAULT_OUTPUT_DIR = "E:/Workspace_Dice/Pellet_Explanation_File_IO/src/main/java/explanation_output/";

    public void run(String ns, String localOntologyPath, String queryStr, String individualName) throws OWLOntologyCreationException, OWLException, IOException {

        PelletExplanation.setup();

        // The renderer is used to pretty print explanation
        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();

        // Create the reasoner and load the ontology
        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology = owlmanager.loadOntologyFromOntologyDocument(file);

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);

        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);

        // Parse the query string to build the OWLClassExpression
        OWLClassExpression query = parseQueryString(ns, queryStr);

        // Execute the query to get instances
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(query, false);
        Set<OWLNamedIndividual> individuals = instances.getFlattened();

        // Check if there are any results
        if (!individuals.isEmpty()) {
            // Find the individual matching the provided name
            OWLNamedIndividual selectedIndividual = null;
            for (OWLNamedIndividual individual : individuals) {
                if (individual.getIRI().getShortForm().equals(individualName)) {
                    selectedIndividual = individual;
                    break;
                }
            }

            if (selectedIndividual == null) {
                System.out.println("No individual found with the name: " + individualName);
            } else {
                // Explain the classification of the selected individual
                Set<Set<OWLAxiom>> explanation = expGen.getInstanceExplanations(selectedIndividual, query, 10);

                // Render the explanation to a string
                StringWriter stringWriter = new StringWriter();
                PrintWriter out = new PrintWriter(stringWriter);
                renderer.startRendering(out);
                out.println("Explanation for individual: " + selectedIndividual);
                renderer.renderAllExplanations(null, explanation);
                renderer.endRendering();
                out.close();

                // Generate a unique file name based on the individual name and timestamp
                String outputFilePath = DEFAULT_OUTPUT_DIR + individualName + "_explanation_" + System.currentTimeMillis() + ".txt";

                // Write the explanation to the specified file
                try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
                    fileWriter.write(stringWriter.toString());
                }

                System.out.println("Explanation saved to: " + outputFilePath);
            }

        } else {
            System.out.println("No individuals found for the query.");
        }
    }

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

        // Take the individual name input from user
        System.out.print("Enter the individual name (e.g., F8M132): ");
        String individualName = scanner.nextLine();

        // Run the explanation with user inputs
        Ind_Explanation_Output_File app = new Ind_Explanation_Output_File();
        app.run(ns, localOntologyPath, queryStr, individualName);

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

*/



import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import io.manchester.ManchesterSyntaxExplanationRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Ind_Explanation_Output_File {

    // Define a default output directory
    private static final String DEFAULT_OUTPUT_DIR = "E:/Workspace_Dice/Pellet_Explanation_File_IO/src/main/java/explanation_output/";

    public void run(String ns, String localOntologyPath, String queryStr, String individualName) throws OWLOntologyCreationException, OWLException, IOException {

        PelletExplanation.setup();

        // The renderer is used to pretty print explanation
        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();

        // Create the reasoner and load the ontology
        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology = owlmanager.loadOntologyFromOntologyDocument(file);

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);

        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);

        // Parse the query string to build the OWLClassExpression
        OWLClassExpression query = parseQueryString(ns, queryStr);

        // Execute the query to get instances
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(query, false);
        Set<OWLNamedIndividual> individuals = instances.getFlattened();

        // Check if there are any results
        if (!individuals.isEmpty()) {
            // Find the individual matching the provided name
            OWLNamedIndividual selectedIndividual = null;
            for (OWLNamedIndividual individual : individuals) {
                if (individual.getIRI().getShortForm().equals(individualName)) {
                    selectedIndividual = individual;
                    break;
                }
            }

            // If the individual is not found, display available individuals
            if (selectedIndividual == null) {
                System.out.println("No individual found with the name: " + individualName);
                System.out.println("Below are Individuals found for given query:");

                List<OWLNamedIndividual> individualList = new ArrayList<>(individuals);
                for (int i = 0; i < individualList.size(); i++) {
                    System.out.println((i + 1) + ": " + individualList.get(i).getIRI().getShortForm());
                }

                // Prompt user to select an individual
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the number corresponding to the individual you want an explanation for: ");
                int selectedIndex = scanner.nextInt();

                // Validate the selection
                if (selectedIndex < 1 || selectedIndex > individualList.size()) {
                    System.out.println("Invalid selection.");
                    return;
                } else {
                    selectedIndividual = individualList.get(selectedIndex - 1);
                }
            }

            // Explain the classification of the selected individual
            Set<Set<OWLAxiom>> explanation = expGen.getInstanceExplanations(selectedIndividual, query, 10);

            // Render the explanation to a string
            StringWriter stringWriter = new StringWriter();
            PrintWriter out = new PrintWriter(stringWriter);
            renderer.startRendering(out);
            out.println("Explanation for individual: " + selectedIndividual);
            renderer.renderAllExplanations(null, explanation);
            renderer.endRendering();
            out.close();

            // Generate a unique file name based on the individual name and timestamp
            String outputFilePath = DEFAULT_OUTPUT_DIR + selectedIndividual.getIRI().getShortForm() + "_explanation_" + System.currentTimeMillis() + ".txt";

            // Write the explanation to the specified file
            try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
                fileWriter.write(stringWriter.toString());
            }

            System.out.println("Explanation saved to: " + outputFilePath);
        } else {
            System.out.println("No individuals found for the query.");
        }
    }

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

        // Take the individual name input from user
        System.out.print("Enter the individual name (e.g., F8M132): ");
        String individualName = scanner.nextLine();

        // Run the explanation with user inputs
        Ind_Explanation_Output_File app = new Ind_Explanation_Output_File();
        app.run(ns, localOntologyPath, queryStr, individualName);

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








