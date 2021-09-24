#!/bin/bash

ROOT_DIR="$( cd "$( dirname "$0" )" && pwd )" 

temp_file=$(mktemp /tmp/XXXX)

if [ ! -f "project_metrics.csv" ]; then
	echo "project,app_sloc,test_sloc,compiled_app_size_bytes,compiled_test_size_bytes,dependency_size_compressed_bytes,dependency_size_decompressed_bytes" >project_metrics.csv
fi
ls sample-projects | while read project; do
	
	rm -rf sample-projects/${project}/libs 2>&1 >/dev/null
	mvn -f sample-projects/${project}/pom.xml clean install -Dmaven.repo.local=sample-projects/${project}/libs --quiet --batch-mode -DskipTests=true 2>&1 >/dev/null
	
	#Get the app SLOC
	echo 0 >${temp_file}
	find "sample-projects/${project}" -name "src" | while read src_file; do
		if [ -d "${src_file}/main" ]; then
			i=$(sloccount "${src_file}/main" 2>&1 | awk -F"=" '($1 ~ /Total Physical.*/){print $2}')
			if [[ "${i}" != "" ]]; then
				echo ${i//,/} >>${temp_file}
			fi
		fi
	done
	app_sloc=$(cat ${temp_file} | awk '{total+=$1}END{print total}')

	#Get the test SLOC
	echo 0 >${temp_file}
	find "sample-projects/${project}" -name "src" | while read test_file; do
		if [ -d "${test_file}/test" ]; then
			i=$(sloccount "${test_file}/test" 2>&1 | awk -F"=" '($1 ~ /Total Physical.*/){print $2}')
                	if [[ "${i}" != "" ]]; then
                        	echo ${i//,/} >>${temp_file}
                	fi
		fi
	done
	test_sloc=$(cat ${temp_file} | awk '{total+=$1}END{print total}')

	#Get the size of the compiled app classes
	echo 0 >${temp_file}
	find "sample-projects/${project}" -name "target" | while read classes; do
		if [ -d "${classes}/classes" ];then
			echo $(du -s "${classes}/classes") >>${temp_file}
		fi
	done
	compiled_app_size_bytes=$(cat ${temp_file} | awk '{total+=$1}END{print total}')


	#Get the size of the compiled test classes
	echo 0 >${temp_file}
	find "sample-projects/${project}" -name "target" | while read test_classes; do
		if [ -d "${test_classes}/test-classes" ];then
			echo $(du -s "${test_classes}/test-classes") >>${temp_file}
		fi
	done
	compiled_test_size_bytes=$(cat ${temp_file} | awk '{total+=$1}END{print total}')

	#Get the size of the dependencies in their compressed form
	dependency_size_compressed_bytes=$(du -s "sample-projects/${project}/libs" | awk '{print $1}')

	#Get the size of the dependencies in their decompressed form
	for jar in $(find sample-projects/${project}/libs -name "*.jar")
	do
		dirname=${ROOT_DIR}/$(echo ${jar} | sed 's/\.jar$//')
		mkdir -p "${dirname}"
		unzip "${ROOT_DIR}/${jar}" -d "${dirname}"
		rm -f "${ROOT_DIR}/${jar}" # Uncomment to delete the original zip file
	done
	dependency_size_decompressed_bytes=$(du -s "sample-projects/${project}/libs" | awk '{print $1}')




	echo ${project},${app_sloc},${test_sloc},${compiled_test_size_bytes},${compiled_app_size_bytes},${dependency_size_compressed_bytes},${dependency_size_decompressed_bytes} >>project_metrics.csv
done
