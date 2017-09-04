package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.enumerations.RSPComponentStatus;
import be.ugent.idlab.rspservice.common.interfaces.Stream;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import it.polimi.jasper.engine.JenaRSPQLEngineImpl;
import it.polimi.jasper.engine.stream.GraphStreamItem;
import it.polimi.yasper.core.engine.RSPEngine;
import it.polimi.yasper.core.stream.RegisteredStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by riccardo on 17/08/2017.
 */
public class JasperRDFStream implements Stream {

    private final RSPEngine engine;
    private final RSPComponentStatus status;
    private String id, wsuri, tboxuri, aboxuri, shortName;
    private RegisteredStream stream;
    private Session session;

    public JasperRDFStream(RegisteredStream s, String fullName, String streamName, JenaRSPQLEngineImpl engine) {
        this.stream = s;
        this.id = fullName;
        this.engine = engine;
        this.status = RSPComponentStatus.RUNNING;
        this.shortName = streamName;
    }


    @Override
    public void feedRDFStream(String dataSerialization) {
        try {
            Graph graph = deserializizeAsJsonSerialization(dataSerialization, null);
            engine.process(new GraphStreamItem(System.currentTimeMillis(), graph, id));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonLdError jsonLdError) {
            jsonLdError.printStackTrace();
        }
    }

    @Override
    public String getStreamID() {
        return stream.getInternalName();
    }

    @Override
    public Object getStream() {
        return stream;
    }

    @Override
    public RSPComponentStatus getStatus() {
        return status;
    }

    @Override
    public void setTBox(String graph_uri) {
        this.tboxuri = graph_uri;
    }

    @Override
    public void setStaticABox(String graph_uri) {
        this.aboxuri = graph_uri;
    }

    @Override
    public void setWsSession(Session session) {
        this.session = session;

    }

    @Override
    public String getSourceURI() {
        return wsuri;
    }

    @Override
    public void setSourceURI(String wsUrl) {
        this.wsuri = wsUrl;

    }

    private Graph deserializizeAsJsonSerialization(String asJsonSerialization, JsonLdOptions options) throws IOException, JsonLdError {

        Model model = ModelFactory.createDefaultModel();
        Statement statement = null;
        Object jsonObject = JsonUtils.fromString(asJsonSerialization);
        RDFDataset rd = (RDFDataset) JsonLdProcessor.toRDF(jsonObject);
        Set<String> graphNames = rd.graphNames();
        for (String graphName : graphNames) {
            List<RDFDataset.Quad> l = rd.getQuads(graphName);
            for (RDFDataset.Quad q : l) {
                Property property = ResourceFactory.createProperty(q.getPredicate().getValue());
                Resource subject;
                if (!q.getObject().isBlankNode()) {
                    subject = ResourceFactory.createResource(q.getSubject().getValue());
                } else {
                    subject = new ResourceImpl(new AnonId(q.getSubject().getValue()));
                }

                Resource obj;
                if (!q.getObject().isLiteral()) {
                    if (!q.getObject().isBlankNode()) {
                        statement = ResourceFactory.createStatement(subject, property, ResourceFactory.createResource(q.getObject().getValue()));
                    } else {
                        obj = new ResourceImpl(new AnonId(q.getObject().getValue()));
                        statement = ResourceFactory.createStatement(subject, property, obj);
                    }
                } else {
                    Literal typedLiteral = ResourceFactory.createTypedLiteral(q.getObject().getValue(), NodeFactory.getType(q.getObject().getDatatype()));
                    statement = ResourceFactory.createStatement(subject, property, typedLiteral);
                }

                model.add(statement);
            }

        }
        return model.getGraph();
    }
}
