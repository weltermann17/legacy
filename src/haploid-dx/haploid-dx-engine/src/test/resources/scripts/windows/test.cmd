@echo off
rem We must never except parameters, use env-variables instead.
echo Hello!
set > env.txt
dir c:\tmp\worktmp 
dir c:\tmp\worktmp > dir.txt
echo Good bye
exit 0
