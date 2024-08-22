import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import io.manchester.ManchesterSyntaxExplanationRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class FamilyExplanation {

   // private static final String	NS	="http://cohse.semanticweb.org/ontologies/people#";
    private static final String NS = "http://www.benchmark.org/family#";


    // Path to the local ontology file
    String localOntologyPath = "E:/Workspace_Dice/DataSource/family.owl"; // Update path if needed
    public void run() throws OWLOntologyCreationException, OWLException, IOException {

        PelletExplanation.setup();
        // The renderer is used to pretty print explanation

        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();

        // The writer used for the explanation rendered
        PrintWriter out = new PrintWriter( System.out );
        renderer.startRendering( out );


        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology = owlmanager.loadOntologyFromOntologyDocument(file);

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);

        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);


        // Create some concepts
        OWLClass Person = OWL.Class( NS + "Person" );
        OWLClass Child = OWL.Class( NS + "Child" );

        Set<OWLClass> subclasses = reasoner.getSubClasses(Person,true).getFlattened();
        System.out.println("Subclasses of " + Person + ":");
        for (OWLClass subclass : subclasses) {
            System.out.println("\t" + subclass);
        }

        // Now explain why Child is a sub class of Person
        Set<Set<OWLAxiom>> exp = expGen.getSubClassExplanations( Child, Person );
        out.println( "Why is " + Child + " subclass of " + Person + "?" );
        renderer.render( exp );

        // Define classes and object properties
        OWLClass femaleClass = OWL.Class(NS + "Female");
        OWLClass maleClass = OWL.Class(NS + "Male");
        OWLObjectProperty hasChildProperty = OWL.ObjectProperty(NS + "hasChild");

        // Build the query: (Female and hasChild some Male)
        OWLClassExpression query = OWL.and(femaleClass, OWL.some(hasChildProperty, maleClass));

        // Execute the query to get instances
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(query, false);
        Set<OWLNamedIndividual> individuals = instances.getFlattened();


        // Check if there are any results
        if (!individuals.isEmpty()) {
            // Get the first individual
            OWLNamedIndividual firstIndividual = individuals.iterator().next();
            System.out.println("First individual: " + firstIndividual);

            // Explain the all classification of the first individual

            Set<Set<OWLAxiom>> explanation = expGen.getInstanceExplanations(firstIndividual, query,6);

            out.println("Explanation for individual: " + firstIndividual);

            // Use the new method to render all explanations
            renderer.renderAllExplanations(null, explanation);

        } else {
            System.out.println("No individuals found for the query.");
        }



        renderer.endRendering();

}
    public static void main(String[] args) throws OWLOntologyCreationException, OWLException,
            IOException {
        // Record the start time
        long startTime = System.currentTimeMillis();
        FamilyExplanation app = new FamilyExplanation();
        app.run();
        // Record the end time
        long endTime = System.currentTimeMillis();

        // Calculate the execution time
        long executionTime = endTime - startTime;

        // Print the execution time
        System.out.println("Execution time: " + executionTime + " milliseconds");
    }
}
