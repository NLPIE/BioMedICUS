FROM ubuntu:20.04

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update

RUN apt-get -qy install git
RUN apt-get -qy install openjdk-11-jre-headless
RUN apt-get -qy install build-essential
RUN apt-get -qy install python3 python3-pip

WORKDIR /home/biomedicus3

RUN pip3 install --upgrade pip
RUN python3 -m pip install --upgrade setuptools

RUN pip3 install biomedicus3
RUN biomedicus download-data --with-stanza

COPY ./python/biomedicus/deployment/biomedicus_deploy_config.yml .
COPY ./python/biomedicus/pipeline/biomedicus_default_pipeline.yml .
COPY ./tools/docker/biomedicus.sh .

RUN chmod +x biomedicus.sh

ENTRYPOINT ./biomedicus.sh
