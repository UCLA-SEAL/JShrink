--- Setup the VM on AWS ---

To setup the experiment up, please ensure Vagrant and VirtualBox is
installed

For Linux:

"sudo apt install vagrant virtualbox"

To setup the VM, modify the "Vagrantfile", replacing "aws.access_key_id"
"aws.secret_access_key", "aws.keypair_name", and
"override.ssh.private_key_path" to those of your own AWS account. Then,
execute the following commands:

"
vagrant plugin install vagrant-aws
vagrant box add dummy dummy.box
vagrant up --provider=aws
"

Enter the VM using "vagrant ssh"

--- Download the sample projects ---

To download the sample projects (used in the experiments), run:

"./download.sh"

This will download all the projects, from GitHub, stated in
"sample-maven-projects.csv" into a directory called "sample-projects".

--- Run experiments ---

Copy the desired experiment run from "experiment_scripts" to the root
directory, and execute.

Before running, please ensure a copy of
"jshrink-app-1.0-SNAPSHOT-jar-with-dependencies.jar" is within the
current directory. If not, go to "../jshrink", compile
project and copy
"../jshrink/jshrink-app/target/jshrink-app-1.0-SNAPSHOT-jar-with-dependencies.jar"
to the current directory.

The script will run JShrink on all projects stated in the "work_list.dat"
file, and output to "size_data.csv".

Once the experiment script has finished running, please run
"reset_work_list.sh" to clean the projects in "sample-projects" (the
projects must be "cleaned" before any other experiments are run again!).

All the experiments may be run via execution of the "replicate_experiments.sh"
script.
