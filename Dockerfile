FROM openjdk:8

ARG ENGINE
ENV ENGINE $ENGINE

# Installation dir
ENV RSP_HOME /opt/rspservice

# Install JAR and queries
COPY ./target/*${ENGINE}*jar-with-dependencies.jar ${RSP_HOME}/engine.jar

ADD ./default.properties ${RSP_HOME}/default.properties
ADD ./start_rsp_server.sh ${RSP_HOME}/start_rsp_server.sh

RUN chmod u+x ${RSP_HOME}/start_rsp_server.sh

WORKDIR ${RSP_HOME}

EXPOSE 8182
EXPOSE 9100-9200

#ENTRYPOINT  ["java", "-jar", "engine.jar"]
ENTRYPOINT  ["./start_rsp_server.sh"]
CMD ["./default.properties"]