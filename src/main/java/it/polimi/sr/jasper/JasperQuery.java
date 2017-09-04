package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.enumerations.QueryType;
import be.ugent.idlab.rspservice.common.enumerations.RSPComponentStatus;
import be.ugent.idlab.rspservice.common.interfaces.AbstractQueryResultProxy;
import be.ugent.idlab.rspservice.common.interfaces.Query;
import be.ugent.idlab.rspservice.common.interfaces.QueryResultObserverWrapper;
import it.polimi.jasper.engine.query.RSPQuery;
import it.polimi.yasper.core.query.ContinuousQuery;
import it.polimi.yasper.core.utils.QueryConfiguration;

import java.util.*;

/**
 * Created by riccardo on 17/08/2017.
 */
public class JasperQuery implements Query {

    private final QueryType type;
    private List<String> streams;
    private List<String> graphs;
    private QueryConfiguration config;
    private RSPQuery query;
    private String id = null;
    private String body = null;
    private AbstractQueryResultProxy resultProxy = null;
    private HashMap<String, QueryResultObserverWrapper> observers = null;
    private RSPComponentStatus status = null;

    public JasperQuery(
            String queryName, String body, QueryType queryType,
            List<String> streams, List<String> graphs,
            ContinuousQuery continuousQuery,
            QueryConfiguration default_config,
            JasperQueryObserverResultObserver handler) {

        this.config = default_config;
        this.body = body;
        this.streams = streams;
        this.graphs = graphs;
        this.type = queryType;
        this.query = (RSPQuery) continuousQuery;
        this.id = queryName;
        this.status = RSPComponentStatus.RUNNING;
        this.resultProxy = handler;
        this.observers = new HashMap<>();

    }

    @Override
    public boolean addObserver(QueryResultObserverWrapper observer) {
        if (!observers.containsKey(observer.getID())) {
            resultProxy.addObserver(observer.getObserver());
            observers.put(observer.getID(), observer);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeObserver(String observerID) {
        QueryResultObserverWrapper cso = observers.get(observerID);
        resultProxy.deleteObserver(cso.getObserver());
        return observers.remove(observerID) != null;
    }

    @Override
    public Map<String, QueryResultObserverWrapper> getObservers() {
        return observers;
    }

    @Override
    public String getQueryID() {
        return id;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getQueryBody() {
        return body;
    }

    @Override
    public void changeQueryStatus(RSPComponentStatus newStatus) {
        status = newStatus;
    }

    @Override
    public RSPComponentStatus getQueryStatus() {
        return status;
    }

    @Override
    public Collection<String> getStreams() {
        if (streams == null) {
            streams = new ArrayList<>();
            query.getWindows().forEach(window -> streams.add(window.getStreamURI()));
        }
        return streams;
    }

    @Override
    public QueryType getType() {
        return type;
    }

    @Override
    public AbstractQueryResultProxy getResultProxy() {
        return resultProxy;
    }
}
