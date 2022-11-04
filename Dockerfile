FROM adoptopenjdk/openjdk11:alpine-slim
MAINTAINER "Manojv" "manojv@ilimi.in"
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird/learner
#ENV sunbird_learnerstate_actor_host 52.172.24.203
#ENV sunbird_learnerstate_actor_port 8088 
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
COPY ./controller/target/learning-service-1.0-SNAPSHOT-dist.zip /home/sunbird/learner/
COPY trustStoreFile $JAVA_HOME/lib/security
RUN unzip /home/sunbird/learner/learning-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/learner/
WORKDIR /home/sunbird/learner/
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -Djavax.net.ssl.trustStore=/opt/java/openjdk/lib/security/lib/security/trustStoreFile -Djavax.net.ssl.trustStorePassword=changeit -Dplay.server.http.idleTimeout=180s -Dlog4j2.formatMsgNoLookups=true -cp '/home/sunbird/learner/learning-service-1.0-SNAPSHOT/lib/*' play.core.server.ProdServerStart  /home/sunbird/learner/learning-service-1.0-SNAPSHOT

