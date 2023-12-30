FROM lsqtzj/openeuler_nginx AS base
EXPOSE 80
RUN rm -Rf /www/wwwroot/default/*
COPY ./php /www/wwwroot/default
RUN chown -R www:www /www
COPY ./docker/web_server/setupweb.sh /opt/setupweb.sh
RUN chmod +x /opt/setupweb.sh

ENTRYPOINT [ "/opt/setupweb.sh"]
CMD ["/bin/bash"]