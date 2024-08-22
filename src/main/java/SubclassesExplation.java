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

public class SubclassesExplation {

    private static final String NS = "http://cohse.semanticweb.org/ontologies/people#";


    // Path to the local ontology file
    String localOntologyPath = "E:/Workspace_Dice/DataSource/people+pets.owl"; // Update path if needed


    public void run() throws OWLOntologyCreationException, OWLException, IOException {

        PelletExplanation.setup();
        // The renderer is used to pretty print explanation

        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();


        // The writer used for the explanation rendered
        PrintWriter out = new PrintWriter(System.out);
        renderer.startRendering(out);


        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology1 = owlmanager.loadOntologyFromOntologyDocument(file);

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology1);


        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);

        // Create the 'pet owner' concept
        //OWLClass petowner = OWL.Class(NS + "pet+owner");
        OWLClass vehicle = OWL.Class(NS + "vehicle");

        // Get subclasses of 'vehicle'
        Set<OWLClass> subClasses = reasoner.getSubClasses(vehicle, true).getFlattened();
        

        // Print the subclasses in a numbered list format
        System.out.println("All subclasses of " + vehicle + " are:");
        int count = 1;
        for (OWLClass subClass : subClasses) {
            System.out.println(count + ". " + subClass);
            count++;
        }


        // Loop through the subclasses and generate explanations
        for (OWLClass subClass : subClasses) {
            out.println("\n Explation for this SubClass and its individuals for: " + subClass +"\n");
            Set<Set<OWLAxiom>> explanations = expGen.getSubClassExplanations(subClass, vehicle);
            out.println( "Why is " + subClass + " subclass of " + vehicle + "?" );
            renderer.render(explanations);

            // Get all individuals of the subclass
            Set<OWLNamedIndividual> individuals = reasoner.getInstances(subClass, false).getFlattened();
           

            // Loop through each individual and explain its classification
            for (OWLNamedIndividual individual : individuals) {
                out.println("Individual: " + individual);
                Set<OWLAxiom> indivExplanation = expGen.getInstanceExplanation(individual, subClass);
                renderer.renderSingleExplanation(indivExplanation);
            }

        }

        renderer.endRendering();

    }
    public static void main(String[] args) throws OWLOntologyCreationException, OWLException, IOException {
        SubclassesExplation app = new SubclassesExplation();
        app.run();
    }
}
