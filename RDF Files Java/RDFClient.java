package org.csb.dboylan.annotator.service.rdf;

import org.apache.commons.codec.binary.Base64;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.csb.dboylan.annotator.dal.entity.AnnotationDO;
import org.csb.dboylan.annotator.dal.entity.Permission;
import org.csb.dboylan.annotator.dal.entity.Range;
import org.csb.dboylan.annotator.service.model.AnnotationBO;
import org.csb.dboylan.annotator.service.model.PermissionBO;
import org.csb.dboylan.annotator.service.model.RangeBO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Optional;

@Component
public class RDFClient {
    @Value("${org.csb.dboylan.rdf-destination}")
    private String rdfDestination;

    @Value("${org.csb.dboylan.rdf-annotation-resource}")
    private String rdfAnnotationResource;

    RDFConnectionRemoteBuilder builder;

    private final RDFModel rdfModel;

    public RDFClient(final RDFModel rdfModel) {
        this.rdfModel = rdfModel;
    }

    public Optional<AnnotationDO> findById(final String id) {
        AnnotationDO annotationDO = getAnnotation(rdfModel.baseUri + id);
        return Optional.of(annotationDO);
    }


    public AnnotationBO create(final AnnotationBO annotationBO) {
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            Model model = conn.fetch();
            Resource personResource = getPerson(annotationBO.getUser(), model);
            if (personResource == null) {
                personResource = createPerson(annotationBO.getUser(), model);
            }
            Resource webResource = getWebResource(annotationBO.getUri(), model);
            if (webResource == null) {
                webResource = createWebResource(annotationBO.getUri(), model);
            }
            Resource ann = model.createResource(rdfModel.baseUri + annotationBO.getId());
            ann.addProperty(rdfModel.textProperty, annotationBO.getText());
            ann.addProperty(rdfModel.quoteProperty, annotationBO.getQuote());
            ann.addProperty(rdfModel.consumerProperty, annotationBO.getConsumer());
            ann.addProperty(rdfModel.tags, String.join(",", annotationBO.getTags()));
            if (annotationBO.getPermissions() != null) {
                PermissionBO permissionBO = annotationBO.getPermissions();
                ann.addProperty(rdfModel.permissionAdminProperty, String.join(",", permissionBO.getAdmin()));
                ann.addProperty(rdfModel.permissionDeleteProperty, String.join(",", permissionBO.getDelete()));
                ann.addProperty(rdfModel.permissionReadProperty, String.join(",", permissionBO.getRead()));
                ann.addProperty(rdfModel.permissionUpdateProperty, String.join(",", permissionBO.getUpdate()));
            }
            if (annotationBO.getRanges() != null && !annotationBO.getRanges().isEmpty()) {
                RangeBO range = annotationBO.getRanges().get(0);
                ann.addProperty(rdfModel.rangeEndProperty, range.getEnd());
                ann.addProperty(rdfModel.rangeStartProperty, range.getStart());
                ann.addLiteral(rdfModel.rangeEndOffsetProperty, range.getEndOffset());
                ann.addLiteral(rdfModel.rangeStartOffsetProperty, range.getStartOffset());
            }
            annotationBO.setCreated(OffsetDateTime.now());
            annotationBO.setUpdated(OffsetDateTime.now());

            ann.addProperty(rdfModel.createdProperty, annotationBO.getCreated().format(DateTimeFormatter.ISO_DATE_TIME));
            ann.addProperty(rdfModel.updatedProperty, annotationBO.getUpdated().format(DateTimeFormatter.ISO_DATE_TIME));

            ann.addProperty(rdfModel.annotatedByProperty, personResource);
            ann.addProperty(rdfModel.annotatedOnProperty, webResource);

            conn.put(model);
            return annotationBO;
        }
    }

    private AnnotationDO getAnnotation(final String resourceName) {
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            Model model = conn.fetch();
            Resource resource = model.getResource(resourceName);
            return toAnnotation(resource);
        }
    }

    private Resource getPerson(final String name, final Model model) {
            String personResourceStr = rdfModel.baseUri + Base64.encodeBase64String(name.getBytes());
            Resource personResource = ResourceFactory.createResource(personResourceStr);
            if (model.containsResource(personResource)) {
                return personResource;
            }
        return null;
    }

    private Resource getWebResource(final String url, final Model model) {
            String webId = java.util.Base64.getEncoder().encodeToString(url.getBytes());
            String webResourceStr = rdfModel.baseUri + Base64.encodeBase64String(url.getBytes());
            Resource webResource = ResourceFactory.createResource(webResourceStr);
            if (model.containsResource(webResource)) {
                return webResource;
            }
        return null;
    }

    private Resource createPerson(final String name, final Model m) {
            String personId = Base64.encodeBase64String(name.getBytes());
            Resource personResource = m.createResource(rdfModel.baseUri + personId);
            personResource.addProperty(rdfModel.nameProperty, name);
            return personResource;
    }

    private Resource createWebResource(final String webUrl, final Model m) {
            String webId = Base64.encodeBase64String(webUrl.getBytes());
            Resource webResource = m.createResource(rdfModel.baseUri + webId);
            webResource.addProperty(rdfModel.uriProperty, webUrl);
            //updateRdf(queryString);
            return webResource;
    }

    private void updateRdf(final String queryString) {
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            UpdateRequest request = UpdateFactory.create(queryString);
            UpdateProcessor qe = UpdateExecutionFactory.createRemote(request,
                    rdfDestination + "/update");
            qe.execute();
        } catch (Exception ex) {
            System.out.println("Exception running insert/update for " + queryString);
            ex.printStackTrace();
            throw ex;
        }
    }

    private ResultSet selectRdf(final String queryString) {
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qe = QueryExecutionFactory.sparqlService(
                    rdfDestination + "/sparql", queryString);
            ResultSet results = qe.execSelect();
            ResultSet resultSet = ResultSetFactory.copyResults(results);

            qe.close();
            return resultSet;
        } catch (Exception ex) {
            System.out.println("Exception running select for " + queryString);
            ex.printStackTrace();
            throw ex;
        }
    }

    AnnotationDO toAnnotation(Resource resource) {
        AnnotationDO annotationDO = new AnnotationDO();
        Statement stmt = resource.getProperty(rdfModel.textProperty);
        annotationDO.setText(stmt == null ? null : stmt.getString());

        stmt = resource.getProperty(rdfModel.quoteProperty);
        annotationDO.setQuote(stmt == null ? null : stmt.getString());

        Resource webResource = resource.getPropertyResourceValue(rdfModel.annotatedOnProperty);
        annotationDO.setUri(webResource == null ? null : toUri(webResource));

        Resource personResource = resource.getPropertyResourceValue(rdfModel.annotatedByProperty);
        annotationDO.setUser(personResource == null ? null : toUser(personResource));

        Range range = new Range();
        stmt = resource.getProperty(rdfModel.rangeEndProperty);
        range.setEnd(stmt == null ? null : stmt.getString());
        stmt = resource.getProperty(rdfModel.rangeEndOffsetProperty);
        range.setEndOffset(stmt == null ? null : stmt.getInt());
        stmt = resource.getProperty(rdfModel.rangeStartProperty);
        range.setStart(stmt == null ? null : stmt.getString());
        stmt = resource.getProperty(rdfModel.rangeStartOffsetProperty);
        range.setStartOffset(stmt == null ? null : stmt.getInt());

        annotationDO.setRanges(Arrays.asList(range));

        stmt = resource.getProperty(rdfModel.consumerProperty);
        annotationDO.setConsumer(stmt == null ? null : stmt.getString());

        stmt = resource.getProperty(rdfModel.tags);
        annotationDO.setTags(stmt == null ? null : Arrays.asList(stmt.getString().split("\\s*,\\s*")));

        stmt = resource.getProperty(rdfModel.createdProperty);
        annotationDO.setCreated(stmt == null ? null : convertStringInDateFormat(stmt.getString()));

        stmt = resource.getProperty(rdfModel.updatedProperty);
        annotationDO.setUpdated(stmt == null ? null : convertStringInDateFormat(stmt.getString()));

        Permission permissions = new Permission();
        stmt = resource.getProperty(rdfModel.permissionAdminProperty);
        permissions.setAdmin(stmt == null ? null : Arrays.asList(stmt.getString().split("\\s*,\\s*")));
        stmt = resource.getProperty(rdfModel.permissionDeleteProperty);
        permissions.setDelete(stmt == null ? null : Arrays.asList(stmt.getString().split("\\s*,\\s*")));
        stmt = resource.getProperty(rdfModel.permissionReadProperty);
        permissions.setRead(stmt == null ? null : Arrays.asList(stmt.getString().split("\\s*,\\s*")));
        stmt = resource.getProperty(rdfModel.permissionUpdateProperty);
        permissions.setUpdate(stmt == null ? null : Arrays.asList(stmt.getString().split("\\s*,\\s*")));

        annotationDO.setPermissions(permissions);
        annotationDO.setId(resource.getURI().substring(rdfModel.baseUri.length()));

        return annotationDO;
    }

    String toUri(Resource webResource) {
        return webResource.getProperty(rdfModel.uriProperty).getString();
    }

    String toUser(Resource personResource) {
        return personResource.getProperty(rdfModel.nameProperty).getString();
    }

    @PostConstruct
    public void postConstruct() {
        builder = RDFConnectionFuseki.create()
                .destination(rdfDestination);
    }

    public LocalDateTime convertStringInDateFormat(String date){
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }
}
