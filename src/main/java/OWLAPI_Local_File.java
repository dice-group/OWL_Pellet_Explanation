import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.util.Set;

public class OWLAPI_Local_File {
    public final static void main(String[] args) throws Exception {


        // Path to the local ontology file
        //String localOntologyPath = "E:\\Workspace_Dice\\DataSource\\People.owl"; // Update path if needed
        String localOntologyPath = "E:\\Workspace_Dice\\DataSource\\CEX-Ontology.owl"; // Update path if needed
        System.out.println("Data picked from this file : "+ localOntologyPath);
        // Create an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        // Load the ontology from the local file
        File file = new File(localOntologyPath);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

        // Use a reasoner
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        System.out.println("Reasoner and Ontology setup done.");


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
                // System.out.println(" objectProperties: " + objectProperties);

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
