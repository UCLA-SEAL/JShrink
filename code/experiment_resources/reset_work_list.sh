#!/bin/bash

PWD=`pwd`
work_list=$1
project_dir="${PWD}/sample-projects"

cat ${work_list} | while read project; do
	cd "${project_dir}/${project}"
	mvn clean
	rm -rf libs
done
