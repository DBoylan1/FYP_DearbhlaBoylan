package org.csb.dboylan.annotator.service.rdf;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

/*
 * Example of a building a remote connection to a Fuseki server.
 */
public class RDFConnectionExample6 {
    public static void main(String ...args) {
//        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
//                .destination("http://localhost:3030/ds/query");
//
//        Query query = QueryFactory.create("SELECT * WHERE {?x ?r ?y}");
//
//        // In this variation, a connection is built each time.
//        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
//            conn.queryResultSet(query, ResultSetFormatter::out);
//        }
        //Query the collection, dump output
        QueryExecution qe = QueryExecutionFactory.sparqlService(
                "http://localhost:3030/ds/query", "SELECT * WHERE {?x ?r ?y}");
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(System.out, results);
        qe.close();
    }
}