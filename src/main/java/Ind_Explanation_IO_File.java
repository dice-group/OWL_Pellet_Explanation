import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import io.manchester.ManchesterSyntaxExplanationRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Ind_Explanation_IO_File {

    // Define a default output directory
    private static final String DEFAULT_OUTPUT_DIR = "E:/Workspace_Dice/Pellet_Explanation_File_IO/src/main/java/explanation_output/";

    public void run(String ns, String localOntologyPath, String queryStr, String individualNames) throws OWLOntologyCreationException, OWLException, IOException {
        PelletExplanation.setup();
        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology = owlmanager.loadOntologyFromOntologyDocument(file);
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);
        PelletExplanation expGen = new PelletExplanation(reasoner);
        OWLClassExpression query = parseQueryString(ns, queryStr);
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(query, false);
        Set<OWLNamedIndividual> individuals = instances.getFlattened();

        // Split the individual names by comma
        String[] individualNamesArray = individualNames.split(",");

        for (String individualName : individualNamesArray) {
            individualName = individualName.trim(); // Trim whitespace
            OWLNamedIndividual selectedIndividual = null;

            // Find the individual matching the provided name
            for (OWLNamedIndividual individual : individuals) {
                if (individual.getIRI().getShortForm().equals(individualName)) {
                    selectedIndividual = individual;
                    break;
                }
            }

            // If the individual is not found, display available individuals
            if (selectedIndividual == null) {
                System.out.println("No individual found with the name: " + individualName);
                System.out.println("Below are Individuals found for the given query:");

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
                    continue; // Skip to the next individual in the input list
                } else {
                    selectedIndividual = individualList.get(selectedIndex - 1);
                }
            }
            // Explain the classification of the selected individual
            Set<Set<OWLAxiom>> explanation = expGen.getInstanceExplanations(selectedIndividual, query, 10);
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
        result.add(input.substring (lastIndex).trim());
        return result;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, OWLException, IOException {
        // Record the start time
        long startTime = System.currentTimeMillis();

        // Create a scanner for console input
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the input file path
        System.out.print("Enter the path to the input file: ");
        String inputPath = scanner.nextLine();

        // Read the input file
        String inputContent = new String(Files.readAllBytes(Paths.get(inputPath)));
        String[] lines = inputContent.split("\n");

        // Read ontology path, namespace, query, and individual names from the input
        String localOntologyPath = lines[0].split("=")[1].trim();
        String ns = lines[1].split("=")[1].trim();
        String queryStr = lines[2].split("=")[1].trim();
        String individualNames = lines[3].split("=")[1].trim();

        // Create an instance of the Ind_Explanation_IO_File class and run the explanation generation
        Ind_Explanation_IO_File app = new Ind_Explanation_IO_File();
        app.run(ns, localOntologyPath, queryStr, individualNames);
        System.out.println("HI I am good");

        // Record the end time
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " milliseconds");

        // Close the scanner
        scanner.close();
    }
}
