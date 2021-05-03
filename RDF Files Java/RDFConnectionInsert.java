package org.csb.dboylan.annotator.service.rdf;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/*
 * Example of a building a remote connection to a Fuseki server.
 */
public class RDFConnectionInsert {
    public static void main(String ...args) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://localhost:3030/ds");
        // Create person
//        String queryString = "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
//                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//                "prefix an: <http://data.example.org/annotator/>\n" +
//                "\n" +
//                "insert data {\n" +
//                "  an:person21 an:name \"Mary Smyth\" .\n" +
//                "}";

        String queryString = "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "prefix an: <http://data.example.org/annotator/>\n" +
                "\n" +
                "insert data {\n" +
                "  an:annotation11 rdf:type an:Annotation .\n" +
                "  an:annotation11 an:uri \"http://www.google.com/\" .\n" +
                "  an:annotation11 an:text \"Entered annotation on text\" .\n" +
                "  an:annotation11 an:quote \"Entered quote on anno\" .\n" +
                "  an:annotation11 an:annotatedBy <http://data.example.org/annotator/person21> .\n" +
                "}";

        // In this variation, a connection is built each time.
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            UpdateRequest request = UpdateFactory.create(queryString);
            UpdateProcessor qe = UpdateExecutionFactory.createRemote(request,
                    "http://localhost:3030/ds/update");
            qe.execute();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}