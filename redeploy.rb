#!/usr/bin/env ruby
#`git stash` # sbt still performs some formatting wihtout asking, so simply stash that shit
#`git pull`
project = "fapi"
target_dir = "fapi"
puts `sbt universal:packageBin`
Dir.chdir("target/universal")
puts Dir.pwd
puts `unzip '*.zip'` # unfortunately, we do not get the name fo the resulting folder and cannot redirect into a folder of your choosing (drop the top-level folder)
puts `rm #{project}*.zip` # remove the original zip file to prevent naming conflicts with wildcards. no quotes required
puts `rm -rf #{target_dir}` # the former dir
puts `mv #{project}* #{target_dir}`
puts `pkill -f -9 #{project}` # `pgrep -f article | xargs` works as well , while other variants like `kill -9 ${pgrep -f article}` dont - the substitution is invalid
puts Dir.chdir(target_dir)
puts `nohup bin/#{project} > 9876.log&`
puts `ps ax | grep #{project} | grep -v grep`
