#!/bin/bash
#
#Description: shell script to run lusi
#Redirect stderr and stdout to the same file. Run it like this:  ./lusi.sh PATH &>outputfile.out

# Check command line args
if [ $# -lt 1 ]; then
	printf "\nNo arguments were passed to the script.\n"
	printf "Usage: lusi.sh index_directory\n\n"
	exit 1
elif [ $# -gt 2 ]; then
	printf "\nThe script requires only one argument. More than one arguments were passed.\n"
	printf "Usage: lusi.sh index_directory\n\n"
	exit 1
fi

# Check that is a directory and exists
if [ ! -d $1 ]; then
	printf "\nThe provided argument is not a directory or it does not exist\n\n"
	exit 1
fi

# Check the string passed to ensure it has "/" at the end.
if [[ "$1" == */ ]]; then
	ls -1 $1>.dir_list.out
	for a in `cat .dir_list.out`
	do
		java -jar bin/lusi-1.3-SNAPSHOT.jar $1$a $2
	done
else
	ls -1 $1>.dir_list.out
	for a in `cat .dir_list.out`
	do
		java -jar bin/lusi-1.3-SNAPSHOT.jar $1/$a $2
	done
fi

rm -f .dir_list.out