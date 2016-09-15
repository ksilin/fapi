# fapi #

Welcome to fapi - a small fake api, crudely simulating a cluster of machines performing generic tasks on data.

## Resources ##

Task - a task is a job description. Currently it only has a name and an id. 

Taskrun - a task that should be run is scheduled first. Only scheduled runs can be run or deleted/aborted.

Load - percentage of CPU and RAM load for a machine plus the number of records stored. Currently generated randomly. 

Machine - not present as an own resource yet, currently a derived resource of Load

## API examples ##

dont forget to add basic auth credentials to each request: 

`http :9123/something/ --auth marco:polo`

All request bodies have to be valid JSON documents.

The API errs on the strict side, i.e. the resources expect a trailing slash. only `.../task/` will return the list of tasks while `.../task` will end up in a 404.

### tasks

all tasks: `http :9123/task/`

one task: `http :9123/task/import_db1`

add task: `echo '{"name":"new_task2","createdAt":1467983405448,"modifiedAt":null,"active":true,"id":null}' | http POST :9123/task/ Accept:'*/*'`

delete task: `http DELETE :9123/task/import_db1`

### running tasks

run/enqueue task: `echo '"import_db1"' | http POST :9123/taskrun/` - provided a task named 'import_db1' does exist

see all task runs: `http :9123/taskrun/`

see one task run: `http :9123/taskrun/123`

see all runs for a task: `http :9123/taskrun/import_db1`

see pending task runs: `http :9123/taskrun/pending`

see finished task runs: `http :9123/taskrun/finished`

see successful task runs: `http :9123/taskrun/successful`

see failed task runs: `http :9123/taskrun/failed`

delete/unqueue a task run: `http DELETE :9123/taskrun/123` - only works for pending runs. once a task run has started, there is no way of removing it

Please note that you should not call any of your tasks 'pending', 'failed', 'finished' or 'successful'. The filtering endpoints have priority and you would not be able to acces the list fo your taskruns.

### machine load

most recent load for all machines: `http :9123/load/`

most recent load for a single machine: `http :9123/load/machine1`

x most recent loads for all machines: `http :9123/load/last/500`  - capped ad 1K per machine

x most recent loads for a single machine: `http :9123/load/last/500`  - capped ad 1K


## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

## TODO ##

* swagger 
* machine resource
* proper session
