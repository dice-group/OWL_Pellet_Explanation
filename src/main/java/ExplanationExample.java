// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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


public class ExplanationExample {

    //private static final String	file	= "file:examples/data/people+pets.owl";

   //private static final String	file ="E:/Workspace_Dice/DataSource/people+pets.owl";
    private static final String	NS	="http://cohse.semanticweb.org/ontologies/people#";


    // Path to the local ontology file
    String localOntologyPath = "E:/Workspace_Dice/DataSource/people+pets.owl"; // Update path if needed



    public void run() throws OWLOntologyCreationException, OWLException, IOException {

        PelletExplanation.setup();
        // The renderer is used to pretty print explanation

        ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();



        // The writer used for the explanation rendered
        PrintWriter out = new PrintWriter( System.out );
        renderer.startRendering( out );


        OWLOntologyManager owlmanager = OWL.manager;
        File file = new File(localOntologyPath);
        OWLOntology ontology1 = owlmanager.loadOntologyFromOntologyDocument(file);

        // Create the reasoner and load the ontology
        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontology1);


        // Create an explanation generator
        PelletExplanation expGen = new PelletExplanation(reasoner);


        // Create some concepts
        OWLClass madCow = OWL.Class( NS + "mad+cow" );
        OWLClass animalLover = OWL.Class( NS + "animal+lover" );
        OWLClass petOwner = OWL.Class( NS + "pet+owner" );
        OWLClass person = OWL.Class(NS+"person");
        OWLClass vehicle = OWL.Class(NS+ "vehicle");
        OWLClass bicycle = OWL.Class(NS+ "bicycle");

        //Create some individual
        OWLNamedIndividual kevin = OWL.Individual(NS + "Kevin");
        OWLNamedIndividual joe = OWL.Individual(NS + "Joe");


        // Explain why mad cow is an unsatisfiable concept
        Set<Set<OWLAxiom>> exp = expGen.getUnsatisfiableExplanations( madCow );
        out.println( "Why is " + madCow + " concept unsatisfiable?" );
        renderer.render( exp );

        // Now explain why animal lover is a sub class of pet owner
        exp = expGen.getSubClassExplanations( animalLover, petOwner );
        out.println( "Why is " + animalLover + " subclass of " + petOwner + "?" );
        renderer.render(exp);

        // Now explain why bicycle is a sub class of vehicle
        exp = expGen.getSubClassExplanations( bicycle, vehicle );
        out.println( "Why is " + vehicle + " subclass of " + bicycle + "?" );
        renderer.render(exp);


        // Now explain the type of Person for Kevin
        Set<OWLAxiom> indiv = expGen.getInstanceExplanation(kevin ,person);
        out.println("Individual: " + kevin);
        out.println("subClass: " + person);
       // indiv = expGen.getInstanceExplanation(kevin ,petOwner);
        out.println( "Explain the individual for  " + kevin );
        renderer.renderSingleExplanation( indiv );

        // Now explain the type of Person for Joe
        Set<Set<OWLAxiom>> indiv1 = expGen.getInstanceExplanations(joe,petOwner,4);
        out.println( "Explain the individual for  " + joe );
        renderer.render(indiv1);

        renderer.endRendering();
    }

    public static void main(String[] args) throws OWLOntologyCreationException, OWLException,
            IOException {
        // Record the start time
        long startTime = System.currentTimeMillis();
        ExplanationExample app = new ExplanationExample();
        app.run();
        // Record the end time
        long endTime = System.currentTimeMillis();

        // Calculate the execution time
        long executionTime = endTime - startTime;

        // Print the execution time
        System.out.println("Execution time: " + executionTime + " milliseconds");

    }
}