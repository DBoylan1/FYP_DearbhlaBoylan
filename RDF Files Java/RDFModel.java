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
public class RDFModel {

    @Value("${org.csb.dboylan.rdf-annotation-resource}")
    public String baseUri;

    @Value("${org.csb.dboylan.rdf-annotation-prefix:an}")
    public String prefix;

    public OntModel m;
    public OntClass personClass;
    public OntClass annotationClass;
    public OntClass webResourceClass;
    public Property nameProperty;
    public Property textProperty;
    public Property quoteProperty;
    public Property rangeStartProperty;
    public Property rangeEndProperty;
    public Property rangeStartOffsetProperty;
    public Property rangeEndOffsetProperty;
    public Property tags;
    public Property uriProperty;
    public Property annotatedByProperty;
    public Property annotatedOnProperty;
    public Property consumerProperty;
    public Property permissionReadProperty;
    public Property permissionAdminProperty;
    public Property permissionUpdateProperty;
    public Property permissionDeleteProperty;
    public Property createdProperty;
    public Property updatedProperty;

    public RDFModel() {
    }

    @PostConstruct
    public void postConstruct() {
        m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        m.setNsPrefix(prefix, baseUri);
        personClass = m.createClass(baseUri + "Person");
        annotationClass = m.createClass(baseUri + "Annotation");
        webResourceClass = m.createClass(baseUri + "WebResource");

        // Person properties
        nameProperty = m.createDatatypeProperty(baseUri + "name");
        personClass.addProperty(nameProperty, "name");

        // Annotator properties
        textProperty = m.createDatatypeProperty(baseUri + "text");
        annotationClass.addProperty(textProperty, "text");

        quoteProperty = m.createDatatypeProperty(baseUri + "quote");
        annotationClass.addProperty(quoteProperty, "quote");

        rangeStartProperty = m.createDatatypeProperty(baseUri + "rangeStart");
        annotationClass.addProperty(rangeStartProperty, "rangeStart");

        rangeEndProperty = m.createDatatypeProperty(baseUri + "rangeEnd");
        annotationClass.addProperty(rangeEndProperty, "rangeEnd");

        rangeStartOffsetProperty = m.createDatatypeProperty(baseUri + "rangeStartOffset");
        annotationClass.addProperty(rangeStartOffsetProperty, "rangeStartOffset");

        rangeEndOffsetProperty = m.createDatatypeProperty(baseUri + "rangeEndOffset");
        annotationClass.addProperty(rangeEndOffsetProperty, "rangeEndOffset");

        consumerProperty = m.createDatatypeProperty(baseUri + "consumer");
        annotationClass.addProperty(consumerProperty, "consumer");

        tags = m.createDatatypeProperty(baseUri + "tags");
        annotationClass.addProperty(tags, "tags");

        permissionAdminProperty = m.createDatatypeProperty(baseUri + "permissionAdmin");
        annotationClass.addProperty(permissionAdminProperty, "permissionAdmin");

        permissionReadProperty = m.createDatatypeProperty(baseUri + "permissionRead");
        annotationClass.addProperty(permissionReadProperty, "permissionRead");

        permissionUpdateProperty = m.createDatatypeProperty(baseUri + "permissionUpdate");
        annotationClass.addProperty(permissionUpdateProperty, "permissionUpdate");

        permissionDeleteProperty = m.createDatatypeProperty(baseUri + "permissionDelete");
        annotationClass.addProperty(permissionDeleteProperty, "permissionDelete");

        createdProperty = m.createDatatypeProperty(baseUri + "created");
        annotationClass.addProperty(createdProperty, "created");

        updatedProperty = m.createDatatypeProperty(baseUri + "updated");
        annotationClass.addProperty(createdProperty, "updated");

        // WebResource properties
        uriProperty = m.createDatatypeProperty(baseUri + "baseUri");
        webResourceClass.addProperty(uriProperty, "baseUri");

        annotatedByProperty = m.createOntProperty(baseUri + "annotatedBy");
        Statement annotatedByStmt = new StatementImpl(annotationClass, annotatedByProperty, personClass);
        m.add(annotatedByStmt);

        annotatedOnProperty = m.createOntProperty(baseUri + "annotatedOn");
        Statement annotatedOnStmt = new StatementImpl(annotationClass, annotatedOnProperty, webResourceClass);
        m.add(annotatedOnStmt);
    }

    private void test() {
        m.write(System.out);

        String personName = "Dean Richards";
        Resource personResource = m.createResource(baseUri + "dean_richards");
        personResource.addProperty(nameProperty, personName);

        String webUrl = "http://www.google.com";
        String webId = Base64.getEncoder().encodeToString(webUrl.getBytes());
        Resource web = m.createResource(baseUri + webId);
        web.addProperty(uriProperty, webUrl);

        String annId = Base64.getEncoder().encodeToString("annotation".getBytes());
        Resource ann = m.createResource(baseUri + annId);
        ann.addProperty(textProperty, "text content");
        ann.addProperty(quoteProperty, "quote content");
        ann.addProperty(annotatedByProperty, personResource);
        ann.addProperty(annotatedOnProperty, web);


        String webUrl2 = "http://www.yahoo.com";
        String webId2 = Base64.getEncoder().encodeToString(webUrl2.getBytes());
        Resource web2 = m.createResource(baseUri + webId2);
        web.addProperty(uriProperty, webUrl2);

        String ann2Id = Base64.getEncoder().encodeToString("annotation2".getBytes());
        Resource ann2 = m.createResource(baseUri + ann2Id);
        ann2.addProperty(textProperty, "text content2");
        ann2.addProperty(quoteProperty, "quote content2");
        ann2.addProperty(annotatedByProperty, personResource);
        ann2.addProperty(annotatedOnProperty, web2);

        //m.write(System.out);

        SimpleSelector byPerson = new SimpleSelector(ann, annotatedByProperty, personResource);
        RDFNode foundAnnotation = byPerson.getSubject();
        System.out.println("\n\nbyPerson : " + foundAnnotation);
        Resource lit = foundAnnotation.asResource();
        System.out.println("Annotation byPerson : " + lit);
        System.out.println("    Annotation text : " + lit.getProperty(textProperty).getString());


    }

    public static void main(String[] args) {
        RDFModel model = new RDFModel();
        model.baseUri = "http://data.example.org/annotator/";
        model.prefix = "an";
        model.postConstruct();
        model.test();
    }
}
