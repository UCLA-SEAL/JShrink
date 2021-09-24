#!/bin/bash

ROOT_DIRECTORY="$(pwd)"

# the list of GitHub repositories built by maven
project_list=$1 #"${ROOT_DIRECTORY}/work_list.dat"

cache_csv="${ROOT_DIRECTORY}/jshrink_caches.csv"

# the destination path to the downloaded projects
dest_dir="${ROOT_DIRECTORY}/sample-projects"

# check whether the dest folder exists first
if [ ! -d "$dest_dir" ]; then
	mkdir $dest_dir
fi

printf "***************Start downloading the given GitHub projects**********************\n"

while read line
do
	#echo "$line"
	if [[ $line != *"_"* ]]; then
		# incorrect repo name
		continue
	fi
	line=`echo $line | awk -F'\r' '{print $1}'`
	username=`echo $line | awk -F'_' '{print $1}'`
	reponame=`echo $line | awk -F'_' '{print $2}'`
	#echo "Name=${username}; Repo=${reponame}"
	project="${username}_${reponame}"

	if [ ! -d "${dest_dir}/${project}" ]; then
		mkdir "${dest_dir}/${project}"
		printf "Beginning to clone $line\n"
		`git clone "https://github.com/${username}/${reponame}.git" "${dest_dir}/${project}" > /dev/null 2>&1` 
		printf "Successfully cloned ${line}!\n\n"
	else
		printf "$line already cloned. Skipp it.\n"
	fi

	#Checkout to a particular date (this keeps experiments constant across time)
	cd ${dest_dir}/${project}
    	current_branch=$(git branch | grep \* | cut -d ' ' -f2)
	git checkout `git rev-list -n 1 --before="2018-10-15 12:00" ${current_branch}` >/dev/null 2>&1
	cd ${ROOT_DIRECTORY}

done < ${project_list}

# If you're receiving an error here, it may be because you cloned this repo
# without the Git LFS (Large File Storage) enabled. To setup Git LFS please
# consult https://github.com/git-lfs/git-lfs/wiki/Installation . Once setup,
# delete this repo and re-pull.
tar xzf "jshrink_caches.tar.gz"
while read line
do
	if [ -d "$(dirname $(dirname $(echo ${line} | cut -d, -f2)))" ]; then
		mkdir -p "$(dirname $(echo ${line} | cut -d, -f2))"
		from=$(echo ${line} | cut -d, -f1)
		to=$(echo ${line} | cut -d, -f2)
		target_dir=$(dirname $(dirname ${to}))
		cp "${from}" "${to}"
	fi
done < ${cache_csv}
rm -rf "jshrink_caches"

# "dieforfree_qart4j" has a unique "pom.xml" file that results in the apache-commons-imaging library being
# fetched whenever maven is run. This caused problems when running experiments. I therefore modify the
# project's "pom.xml" file to use a local version of the apache-commons-imaging library.

if [ -d "sample-projects/dieforfree_qart4j" ]; then
	cp -r qart4j_patch/* sample-projects/dieforfree_qart4j/
fi

# Fix notnoop_java-apns
if [ -d "sample-projects/notnoop_java-apns" ]; then
  cp -r notnoop_java-apns/* sample-projects/notnoop_java-apns/
fi
