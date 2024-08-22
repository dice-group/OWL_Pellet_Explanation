import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import java.util.Set;


public class OWLAPIExample {
    public final static void main(String[] args) throws Exception {
        String file = "http://owl.man.ac.uk/2005/07/sssw/people.owl";
        System.out.println("Data picked from this URL : "+ file);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create(file));
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);
        System.out.println("done.");
        //  reasoner.getKB().realize();
        // reasoner.getKB().printClassTree();


        // Get all classes in the ontology
        Set<OWLClass> classes = ontology.getClassesInSignature();
        for (OWLClass cls : classes) {
            System.out.println("Class: " + cls);


            // Fetch all individuals of this class
            NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(cls, false);
            for (Node<OWLNamedIndividual> sameInd : individuals) {
                OWLNamedIndividual ind = sameInd.getRepresentativeElement();
                System.out.println(" Individual: " + ind);

                // Fetch all data properties and their values for the individual
                Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature();
                System.out.println(" dataProperties: " + dataProperties);
                for (OWLDataProperty dataProp : dataProperties) {
                    System.out.println(" dataProp: " + dataProp);
                    Set<OWLLiteral> dataValues = reasoner.getDataPropertyValues(ind, dataProp);
                    for (OWLLiteral value : dataValues) {
                        System.out.println("  Data Property: " + dataProp + " Value: " + value.getLiteral());
                    }
                }
                // Fetch all data properties and their values for the individual
                Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();
                for (OWLObjectProperty objProp : objectProperties) {
                    NodeSet<OWLNamedIndividual> objectPropValues = reasoner.getObjectPropertyValues(ind, objProp);
                    // Print the object property and its values
                    if (!objectPropValues.isEmpty()) {
                        System.out.println("  Object Property: " + objProp);
                        for (Node value : objectPropValues) {
                            System.out.println("    Value: " + value);
                        }
                    }


                }
            }
        }
    }
}
