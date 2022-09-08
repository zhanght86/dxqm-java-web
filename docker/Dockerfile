# syntax=docker/dockerfile:1

FROM java:8
VOLUME /tmp/jnpf-boot
ADD jnpf-admin-3.2.5-RELEASE.jar jnpf-boot.jar
EXPOSE 30000
ENTRYPOINT ["java","-jar", "/jnpf-boot.jar"]
