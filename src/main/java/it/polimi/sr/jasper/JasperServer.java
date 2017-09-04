package it.polimi.sr.jasper;

import be.ugent.idlab.rspservice.common.RSPServer;
import it.polimi.yasper.core.utils.EngineConfiguration;
import it.polimi.yasper.core.utils.QueryConfiguration;
import lombok.extern.log4j.Log4j;

/**
 * Created by riccardo on 17/08/2017.
 */
@Log4j
public class JasperServer extends RSPServer {

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            QueryConfiguration qc = new QueryConfiguration(args[0]);
            EngineConfiguration ec = new EngineConfiguration(args[0]);
            new JasperServer().start(new JasperEngine(ec, qc), args[0]);
        } else {
            System.out.println("Usage: it.polimi.sr.jasper.JasperServer <configuration file>");
        }
    }
}
