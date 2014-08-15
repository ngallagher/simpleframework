#!/cygdrive/c/cygwin/bin/env bash
ulimit -S -n 2000 
ant -v run -Dservice=$1 -Dlocation=$2 
 
