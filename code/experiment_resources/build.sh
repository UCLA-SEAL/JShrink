#!/bin/bash

#I [Bobby] created this script to generate a CSV of which projects are compilable
#Note: I have to run this script several times as some failing projects make it fail
#E.g., : `./build.sh >build_output.dat; ./build.sh >>build_output.dat; ./build.sh >>build_output.dat`

PWD=`pwd`
# the list of GitHub repositories built by maven
#project_list=${PWD}"/sample-maven-projects.csv"

# the root directory that contains the Java projects downloaded from GitHub
project_dir=${PWD}"/sample-projects"


# check whether the project directory exists first
if [ ! -d "$project_dir" ]; then
	printf "The root directory of GitHub projects does not exist.\n"
	exit 1
fi

ls ${project_dir} | while read line
do
	#echo "$line"
	#if [[ $line != *"/"* ]]; then
	#	# incorrect repo name
	#	continue
	#fi
	#line=`echo $line | awk -F'\r' '{print $1}'`
	#username=`echo $line | awk -F'/' '{print $1}'`
	#reponame=`echo $line | awk -F'/' '{print $2}'`
	#echo "Name=${username}; Repo=${reponame}"
	project=${line}

	if [ -d "${project_dir}/${project}" ]; then
		if [ ! -f "${project_dir}/${project}/onr_build.log" ];then
			#printf "Begin to build $line\n"
			`mvn install -f "${project_dir}/${project}/pom.xml" --quiet --batch-mode -DskipTests=true &> ${project_dir}/${project}/onr_build.log` 
			exit_status=$?
			printf "${project_dir}/${project},${exit_status}\n"
		fi
	else
		printf "The project folder of ${line} does not exits.\n\n"
	fi
done

