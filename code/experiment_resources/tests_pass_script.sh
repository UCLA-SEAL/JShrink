#!/bin/bash

build_output=$1

cat ${build_output} | while read entry; do
	project=$(echo ${entry} | cut -d, -f1)
	status=$(echo ${entry} | cut -d, -f2)
	if [[ ${status} == 0 ]]; then
		# 30 minute timeout.
		timeout 1800 mvn -f ${project}/pom.xml test
		test_status=$?
		echo ${project},${test_status} >>tests_output.csv
	fi
done
