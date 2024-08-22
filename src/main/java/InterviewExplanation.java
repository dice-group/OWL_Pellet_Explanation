// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com


import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import io.manchester.ManchesterSyntaxExplanationRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * Title: ExplanationExample
 * </p>
 * <p>
 * Description: This program shows how to use Pellet's explanation service
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 * @author Evren Sirin
 */


public class InterviewExplanation {

    //private static final String	NS	="http://cohse.semanticweb.org/ontologies/people#";
    private static final String	NS	= "http://www.semanticweb.org/CEX-Paper#";


    // Path to the local ontology file
    String localOntologyPath = "E:/Workspace_Dice/DataSource/CEX-Ontology.owl"; // Update path if needed



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

        // Get all classes in the ontology
       /* Set<OWLClass> classes = ontology.getClassesInSignature();
        for (OWLClass cls : classes) {
            System.out.println("Class: " + cls);
        }*/

        // Create some concepts
        OWLClass qualified = OWL.Class( NS + "Qualified" );
        OWLClass qualExpert = OWL.Class( NS + "QualifiedAndAIExpert" );
        OWLClass interviewed = OWL.Class(NS + "Interviewed");




        //Create some individual
        OWLNamedIndividual Alice = OWL.Individual(NS + "Alice");

        // Now explain why QualifiedAndAIExpert is a sub class of Qualified
        Set<Set<OWLAxiom>> exp = expGen.getSubClassExplanations( qualExpert, qualified );
        out.println( "Why is " + qualExpert + " subclass of " + qualified + "?" );
        renderer.render( exp );


        Set<OWLAxiom> indiv = expGen.getInstanceExplanation(Alice ,qualified);

        out.println( "Explain the class for  " + Alice );
        renderer.renderSingleExplanation( indiv );


        // Define the DL query: Qualified AND Interviewed
        OWLClassExpression queryExpression = OWL.and(qualified, interviewed);

        // Run the query to get individuals that satisfy "Qualified AND Interviewed"
        Set<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(queryExpression, false).getFlattened();

        // Print out the individuals that satisfy the query
        out.println("Individuals that are both Qualified and Interviewed:");
        for (OWLNamedIndividual individual : individualsNodeSet) {
            out.println("\t" + individual);

            // Generate and print the explanation for the individual being of the class
            Set<OWLAxiom> indivExplanation = expGen.getInstanceExplanation(individual, queryExpression);
            out.println("Explanation for " + individual + "why ALICE is both Qualified and Interviewed:");
            renderer.renderSingleExplanation(indivExplanation);
        }

        renderer.endRendering();
    }

    public static void main(String[] args) throws OWLOntologyCreationException, OWLException,
            IOException {
        InterviewExplanation app = new InterviewExplanation();

        app.run();
    }
}