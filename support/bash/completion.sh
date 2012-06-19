#!/usr/bin/env bash

# bash completion for Torquebox

__torquebox_generate_completion()
{
  typeset current_word
  current_word="${COMP_WORDS[COMP_CWORD]}"

  COMPREPLY=( $(compgen -W "$1" -- "${current_word}") )
  return 0
}

__torquebox_tasks ()
{
  __torquebox_generate_completion "deploy undeploy run rails archive cli env help list"
}

__torquebox_deploy ()
{
   __torquebox_generate_completion "--context-path= --env= --name="
}

__torquebox_undeploy ()
{
   __torquebox_generate_completion "--name="
}

__torquebox_run ()
{
   __torquebox_generate_completion "--clustered --data-directory= --extra= -e --max-threads= --bind-address= -b --node-name= --port-offset= --jvm-options= -J"
}

__torquebox_archive ()
{
   __torquebox_generate_completion "--deploy"
}

__torquebox_env ()
{
   __torquebox_generate_completion "JBOSS_HOME JRUBY_HOME TORQUEBOX_HOME"
}
__torquebox()
{
  typeset previous_word
  previous_word="${COMP_WORDS[COMP_CWORD-1]}"

  case "${previous_word}" in
    deploy) __torquebox_deploy ;;
  undeploy) __torquebox_undeploy ;;
       run) __torquebox_run ;;
     rails) ;;
   archive) __torquebox_archive ;;
       cli) ;;
       env) __torquebox_env ;;
      list) ;;
         *) __torquebox_tasks ;;
  esac

  return 0
}

complete -o default -F __torquebox torquebox
