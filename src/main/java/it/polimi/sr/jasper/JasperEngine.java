package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.configuration.Config;
import be.ugent.idlab.rspservice.common.enumerations.QueryType;
import be.ugent.idlab.rspservice.common.interfaces.Query;
import be.ugent.idlab.rspservice.common.interfaces.RSPEngine;
import be.ugent.idlab.rspservice.common.interfaces.RuleSet;
import be.ugent.idlab.rspservice.common.interfaces.Stream;
import it.polimi.jasper.engine.JenaRSPQLEngineImpl;
import it.polimi.jasper.engine.query.RSPQuery;
import it.polimi.yasper.core.engine.Entailment;
import it.polimi.yasper.core.enums.Maintenance;
import it.polimi.yasper.core.query.ContinuousQuery;
import it.polimi.yasper.core.query.execution.ContinuousQueryExecution;
import it.polimi.yasper.core.stream.RegisteredStream;
import it.polimi.yasper.core.stream.StreamImpl;
import it.polimi.yasper.core.utils.EngineConfiguration;
import it.polimi.yasper.core.utils.QueryConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.IRIResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by riccardo on 17/08/2017.
 */
public class JasperEngine implements RSPEngine {

    private EngineConfiguration config;
    private JenaRSPQLEngineImpl engine;
    private QueryConfiguration default_config;
    private Map<String, JasperRuleSet> ruleSets;

    public JasperEngine(EngineConfiguration ec, QueryConfiguration default_config) {
        this.default_config = default_config;
        this.config = ec;
        this.ruleSets = new HashMap<>();
    }

    public JasperEngine() {
        try {
            this.default_config = QueryConfiguration.getDefault();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize() throws Exception {
        this.engine = new JenaRSPQLEngineImpl(System.currentTimeMillis(), config);
    }

    @Override
    public Object getRSPEngine() {
        return engine;
    }

    @Override
    public Stream registerStream(String streamName, String uri) {
        String fullName = IRIResolver.resolveFileURL("streams/" + streamName);
        StreamImpl s = new StreamImpl(fullName);
        RegisteredStream regs = (RegisteredStream) engine.register(s);
        JasperRDFStream js = new JasperRDFStream(regs, fullName, streamName, engine);

        if (uri != null && !uri.isEmpty()) {
            extractMetadata(uri, js);
        }

        return js;
    }

    private void extractMetadata(String uri, JasperRDFStream js) {
        Model sGraph = ModelFactory.createDefaultModel().read(uri, "JSON-LD");
        //sGraph.write(System.out);

        QueryExecution qexec = QueryExecutionFactory.create(Config.getInstance().getQuery(), sGraph);

        ResultSet rs = qexec.execSelect();

        String wsUrl = new String();
        String tBoxUrl = new String();
        String aBoxUrl = new String();

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            if (qs.get("wsurl").isLiteral())
                wsUrl = qs.getLiteral("wsurl").getLexicalForm();
            else
                wsUrl = qs.get("wsurl").toString();
            if (qs.contains("tboxurl"))
                if (qs.get("tboxurl").isLiteral())
                    tBoxUrl = qs.getLiteral("tboxurl").getLexicalForm();
                else
                    tBoxUrl = qs.get("tboxurl").toString();
            if (qs.contains("aboxurl"))
                if (qs.get("aboxurl").isLiteral())
                    aBoxUrl = qs.getLiteral("aboxurl").getLexicalForm();
                else
                    aBoxUrl = qs.get("aboxurl").toString();
        }

        js.setTBox(tBoxUrl);
        js.setStaticABox(aBoxUrl);
        js.setSourceURI(wsUrl);
    }

    public Object unregisterStream(String streamName) {
        return null;
    }

    public Object getStream(String streamName) {
        return null;
    }

    @Override
    public Query registerQuery(String queryName, QueryType queryType, String queryBody, List<String> streams, List<String> graphs, String tbox_location, String ruleSet) throws Exception {
        RSPQuery continuousQuery = (RSPQuery) engine.parseQuery(queryBody);
        Model tbox = ModelFactory.createDefaultModel();

        if (!"".equals(tbox_location)) {
            tbox.read(tbox_location);
        }

        Entailment e = !"".equals(ruleSet) ? ruleSets.get(ruleSet).getRules() : null;

        ContinuousQueryExecution cqe = engine.register(continuousQuery, tbox, Maintenance.NAIVE, e, false);

        JasperQueryObserverResultObserver handler = new JasperQueryObserverResultObserver(queryName, queryType, cqe);
        return new JasperQuery(queryName, queryBody, queryType, streams, graphs, continuousQuery, default_config, handler);

    }

    public Query registerQuery(String queryName, QueryType queryType, String queryBody, List<String> streams, List<String> graphs) throws Exception {
        RSPQuery continuousQuery = (RSPQuery) engine.parseQuery(queryBody);
        ContinuousQueryExecution cqe = engine.register(continuousQuery, default_config);

        JasperQueryObserverResultObserver handler = new JasperQueryObserverResultObserver(queryName, queryType, cqe);
        return new JasperQuery(queryName, queryBody, queryType, streams, graphs, continuousQuery, default_config, handler);
    }


    @Override
    public Object unregisterQuery(String queryID) {
        ContinuousQuery continuousQuery = engine.getRegisteredQueries().get(queryID);
        engine.unregister(continuousQuery);
        return null;
    }

    @Override
    public RuleSet registerRuleSet(String s, String s1) {
        Entailment register = engine.register(s, s1);
        JasperRuleSet jasperRuleSet = new JasperRuleSet(s, register);
        ruleSets.put(s, jasperRuleSet);
        return jasperRuleSet;
    }

    @Override
    public Object unregisterRuleSet(String s) {
        engine.unregister(s);
        return null;
    }

    @Override
    public Object getAllQueries() {
        return engine.getRegisteredQueries();
    }

    @Override
    public Object stopQuery(String queryID) {
        return null;
    }

    @Override
    public Object startQuery(String queryID) {
        return null;
    }

    @Override
    public void feedRDFStream(Stream rdfStream, String m) {
        rdfStream.feedRDFStream(m);
    }

    @Override
    public void addStaticKnowledge(String iri, String url, Boolean isDefault, String serialization) {

    }

    @Override
    public void deleteStaticKnowledge(String url) {

    }

    @Override
    public void execUpdateQueryOverStaticKnowledge(String query) {

    }


}