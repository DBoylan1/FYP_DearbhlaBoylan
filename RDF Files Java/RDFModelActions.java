package org.csb.dboylan.annotator.service.rdf;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Component
public class RDFModelActions {

    @Value("${org.csb.dboylan.rdf-actions-resource}")
    public String baseUri;

    @Value("${org.csb.dboylan.rdf-actions-prefix:an}")
    public String prefix;

    public OntModel m;
    OntClass actorClass;
    OntClass actionClass;
    OntClass characteristicClass;
    OntClass outcomeClass;
    OntClass actionTargetClass;

    Property nameProperty;
    Property actionNameProperty;
    Property characteristicTypeProperty;
    Property resultProperty;
    Property targetProperty;

    Property performedByProperty;
    Property performedOnProperty;
    Property hasCharacteristic;
    Property producesOutcome;

    public RDFModelActions() {
    }

    @PostConstruct
    public void postConstruct() {
        m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        m.setNsPrefix(prefix, baseUri);
        actorClass = m.createClass(baseUri + "Actor");
        actionClass = m.createClass(baseUri + "Action");
        actionTargetClass = m.createClass(baseUri + "ActionTarget");
        characteristicClass = m.createClass(baseUri + "Characteristic");
        outcomeClass = m.createClass(baseUri + "Outcome");
       // activitiesClass = m.createClass(baseUri + "Activity");

        // Actor properties
        nameProperty = m.createDatatypeProperty(baseUri + "name");
        actorClass.addProperty(nameProperty, "name");

        // Action properties
        actionNameProperty = m.createDatatypeProperty(baseUri + "actionName");
        actionClass.addProperty(actionNameProperty, "actionName");

        // Characteristic properties
        characteristicTypeProperty = m.createDatatypeProperty(baseUri + "characteristicType");
        characteristicClass.addProperty(characteristicTypeProperty, "characteristicType");

        // ActionTarget properties
        targetProperty = m.createDatatypeProperty(baseUri + "target");
        actionTargetClass.addProperty(targetProperty, "target");

        // Outcome properties
        resultProperty = m.createDatatypeProperty(baseUri + "result");
        outcomeClass.addProperty(resultProperty, "result");

        // Statements
        performedByProperty = m.createOntProperty(baseUri + "performedBy");
        Statement performedByStmt = new StatementImpl(actionClass, performedByProperty, actorClass);
        m.add(performedByStmt);

        performedOnProperty = m.createOntProperty(baseUri + "performedOn");
        Statement performedOnStmt = new StatementImpl(actionClass, performedOnProperty, actionTargetClass);
        m.add(performedOnStmt);

        hasCharacteristic = m.createOntProperty(baseUri + "hasCharacteristic");
        Statement hasCharacteristicStmt = new StatementImpl(actionClass, hasCharacteristic, characteristicClass);
        m.add(hasCharacteristicStmt);

        producesOutcome = m.createOntProperty(baseUri + "producesOutcome");
        Statement producesOutcomeStmt = new StatementImpl(actionClass, producesOutcome, outcomeClass);
        m.add(producesOutcomeStmt);

    }

    private void test() {
        m.write(System.out);

        // String personName = "Dean Richards";
        Resource actorResource = m.createResource(baseUri + "commission");
        actorResource.addProperty(nameProperty, "Commission");

        Resource actionResource = m.createResource(baseUri + "cooperation");
        actorResource.addProperty(actionNameProperty, "Co-operation");

        Resource actionTargetResource = m.createOntResource(baseUri + "regulation");
        actionTargetResource.addProperty(targetProperty, "Regulation, Union Law");

        Resource characteristicResource = m.createOntResource(baseUri + "high_risk");
        characteristicResource.addProperty(characteristicTypeProperty, "high risk sectors & uses, consistent");

        actionResource.addProperty(performedByProperty, actorClass);
        actionResource.addProperty(performedOnProperty, actionTargetClass);
        actionResource.addProperty(hasCharacteristic, characteristicResource);
        // No outcome for this one
        //actionResource.addProperty(producesOutcome, )
        System.out.println("\n\n");
        m.write(System.out);
       // String webUrl = "http://wwws.google.com";
        // String webId = Base64.getEncoder().encodeToString(webUrl.getBytes());
        // Resource web = m.createResource(baseUri + webId);
        // web.addProperty(uriProperty, webUrl);

        // String annId = Base64.getEncoder().encodeToString("annotation".getBytes());
        // Resource ann = m.createResource(baseUri + annId);
        // ann.addProperty(textProperty, "text content");
        // ann.addProperty(quoteProperty, "quote content");
        // ann.addProperty(annotatedByProperty, personResource);
        // ann.addProperty(annotatedOnProperty, web);


        // String webUrl2 = "http://www.yahoo.com";
        // String webId2 = Base64.getEncoder().encodeToString(webUrl2.getBytes());
        // Resource web2 = m.createResource(baseUri + webId2);
        // web.addProperty(uriProperty, webUrl2);

        // String ann2Id = Base64.getEncoder().encodeToString("annotation2".getBytes());
        // Resource ann2 = m.createResource(baseUri + ann2Id);
        // ann2.addProperty(textProperty, "text content2");
        // ann2.addProperty(quoteProperty, "quote content2");
        // ann2.addProperty(annotatedByProperty, personResource);
        // ann2.addProperty(annotatedOnProperty, web2);

        // //m.write(System.out);

        // SimpleSelector byPerson = new SimpleSelector(ann, annotatedByProperty, personResource);
        // RDFNode foundAnnotation = byPerson.getSubject();
        // System.out.println("\n\nbyPerson : " + foundAnnotation);
        // Resource lit = foundAnnotation.asResource();
        // System.out.println("Annotation byPerson : " + lit);
        // System.out.println("    Annotation text : " + lit.getProperty(textProperty).getString());


    }

    public static void main(String[] args) {
        RDFModelActions model = new RDFModelActions();
        model.baseUri = "http://data.example.org/activity/";
        model.prefix = "ac";
        model.postConstruct();
        model.test();
    }
}
