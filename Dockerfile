FROM tomcat:10.1-jdk17

# 기존 ROOT 앱 제거
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# 빌드된 war를 ROOT로 복사
COPY *.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]