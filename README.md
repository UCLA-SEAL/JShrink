# JShrink
JShrink: In-depth Investigation into Debloating Modern Java Applications (FSE 2020)

## Summary 
Modern software is bloated. Demand for new functionality has led developers to include more and more features, many of which become unneeded or unused as software evolves. This phenomenon, known as software bloat, results in software consuming more resources than it otherwise needs to. How to effectively and automatically debloat software is a long-standing problem in software engineering. Various debloating techniques have been proposed since the late 1990s. However, many of these techniques are built upon pure static analysis and have yet to be extended and evaluated in the context of modern Java applications where dynamic language
features are prevalent.

To this end, we develop an end-to-end bytecode debloating framework called JShrink. It augments traditional static reachability analysis with dynamic profiling and type dependency analysis and renovates existing bytecode transformations to account for new language features in modern Java. We highlight several nuanced
technical challenges that must be handled properly and examine behavior preservation of debloated software via regression testing. We find that (1) JShrink is able to debloat our real-world Java benchmark suite by up to 47% (14% on average); (2) accounting for dynamic language features is indeed crucial to ensure behavior preservation—reducing 98% of test failures incurred by a purely static equivalent, Jax, and 84% for ProGuard; and (3) compared with purely dynamic approaches, integrating static analysis with dynamic profiling makes the debloated software more robust to unseen test executions—in 22 out of 26 projects, the debloated software ran successfully under new tests.

## Team 
This project was developed by Professor [Miryung Kim](http://web.cs.ucla.edu/~miryung/)'s Software Engineering and Analysis Laboratory. The main contributors are [Bobby R. Bruce](https://www.bobbybruce.net/) was a postdoc, and [Tianyi Zhang](https://tianyi-zhang.github.io/) was a PhD student at UCLA.

[Bobby Bruce](https://www.bobbybruce.net/): was a postdoc, now a research scientist at UC Davis; 

[Tianyi Zhang](https://https://tianyi-zhang.github.io): was a PhD student at UCLA and now an assistant professor at Purdue; tianyi@purdue.edu

[Jaspreet Arora](https://www.linkedin.com/in/jasarora): was a MS student at UCLA, and now a Software Engineeri at Amazon; 

[Guoqing Harry Xu](http://web.cs.ucla.edu/~harryxu/): Professor at UCLA, harryxu@cs.ucla.edu;

[Miryung Kim](http://web.cs.ucla.edu/~miryung/): Professor at UCLA, miryung@cs.ucla.edu;


Please visit [UCLA Java Bytecode Debloating](https://github.com/jdebloat).

https://github.com/jdebloat/jshrink


## How to cite 
Please refer to our FSE'20 paper, [JShrink: in-depth investigation into debloating modern Java applications](https://dl.acm.org/doi/abs/10.1145/3368089.3409738) for more details. 

### Bibtex  
@inproceedings{10.1145/3368089.3409738,
author = {Bruce, Bobby R. and Zhang, Tianyi and Arora, Jaspreet and Xu, Guoqing Harry and Kim, Miryung},
title = {JShrink: In-Depth Investigation into Debloating Modern Java Applications},
year = {2020},
isbn = {9781450370431},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/3368089.3409738},
doi = {10.1145/3368089.3409738},
booktitle = {Proceedings of the 28th ACM Joint Meeting on European Software Engineering Conference and Symposium on the Foundations of Software Engineering},
pages = {135–146},
numpages = {12},
series = {ESEC/FSE 2020}
}
[DOI Link](https://doi.org/10.1145/3368089.3409738)

## Video
You can watch an FSE'20 presentation video [here](https://www.youtube.com/watch?v=MwIqCkxb6Zs)
