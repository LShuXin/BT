FROM centos:centos7.9.2009 AS base
RUN yum -y install apr-devel apr-util-devel net-tools
WORKDIR /teamtalk
EXPOSE 8400

FROM 1569663570/centos792009_develop_server AS builder

FROM base AS runtime
COPY --from=builder /lib64/libstdc++.so.6 /lib64/libstdc++.so.6
COPY --from=builder /develop/im-server-1.0.0/http_msg_server /teamtalk/http_msg_server
COPY --from=builder /develop/im-server-1.0.0/daeml /teamtalk/daeml
COPY ./server/run/monitor.sh /teamtalk/http_msg_server/monitor.sh
COPY ./server/run/restart.sh /teamtalk/restart.sh
COPY ./docker/http_msg_server/main.sh main.sh
RUN chmod +x /teamtalk/main.sh
ENTRYPOINT ["/teamtalk/main.sh"]
CMD ["/bin/bash"]