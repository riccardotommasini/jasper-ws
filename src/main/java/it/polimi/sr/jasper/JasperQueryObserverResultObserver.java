package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.configuration.Config;
import be.ugent.idlab.rspservice.common.enumerations.Language;
import be.ugent.idlab.rspservice.common.enumerations.QueryType;
import be.ugent.idlab.rspservice.common.interfaces.AbstractQueryResultProxy;
import it.polimi.jasper.engine.query.response.ConstructResponse;
import it.polimi.jasper.engine.query.response.SelectResponse;
import it.polimi.yasper.core.query.execution.ContinuousQueryExecution;
import it.polimi.yasper.core.query.formatter.QueryResponseFormatter;
import org.apache.jena.query.ResultSetFormatter;

import java.io.ByteArrayOutputStream;
import java.util.Observable;

/**
 * Created by riccardo on 17/08/2017.
 */
public class JasperQueryObserverResultObserver extends AbstractQueryResultProxy {

    private String queryID = null;
    private QueryType queryType = null;
    private ContinuousQueryExecution ceq;

    public JasperQueryObserverResultObserver(String queryID, QueryType queryType, ContinuousQueryExecution ceq) {
        this.queryID = queryID;
        this.queryType = queryType;
        this.ceq = ceq;
        ceq.addObserver(new GenericResponseDispatcher(true));
    }

    @Override
    public String getQueryID() {
        return null;
    }

    @Override
    public QueryType getQueryType() {
        return null;
    }

    private class GenericResponseDispatcher extends QueryResponseFormatter {

        long last_result = -1L;
        boolean distinct;

        public GenericResponseDispatcher(boolean distinct) {
            this.distinct = distinct;
        }


        @Override
        public void update(Observable o, Object arg) {

            Language format = Config.getInstance().getOutputFormat();
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            if (arg instanceof SelectResponse) {
                SelectResponse sr = (SelectResponse) arg;
                if (sr.getCep_timestamp() != last_result && distinct) {
                    last_result = sr.getCep_timestamp();
                    System.out.println("[" + System.currentTimeMillis() + "] Result at [" + last_result + "]");
                    ResultSetFormatter.out(os, sr.getResults());
                }
            } else if (arg instanceof ConstructResponse) {
                ConstructResponse sr = (ConstructResponse) arg;
                if (sr.getCep_timestamp() != last_result && distinct) {
                    sr.getResults().write(os, "JSON-LD");
                    last_result = sr.getCep_timestamp();
                }
            }

            setChanged();
            JasperQueryObserverResultObserver.this.notifyObservers(os.toString());
        }
    }
}
