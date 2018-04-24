FROM openjdk:latest
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get install -y supervisor
COPY supervisord.conf /etc/supervisor/conf.d/
WORKDIR /opt/docker
ADD opt /opt
RUN ["chown", "-R", "daemon:daemon", "."]
RUN ["chown", "-R", "daemon:daemon", "/var/log/supervisor/"]
ENTRYPOINT ["/usr/bin/supervisord", "-n"]
CMD []
ARG version=local
ENV VERSION $version
ARG commit
LABEL commit="${commit}"