if [ "$#" -lt 1 ]; then
	echo "Usage: $0 [<backupdir>] <update.zip>"
	exit 1
fi
if [ ! -d "ROOT/assets" ]; then
  echo "WARN: ROOT/assets not found, are you sure to proceed ?"
  read -p "Press <Ctrl-C> to abort, <ENTER> to continue ... "
fi
today=$(date +%Y%m%d)
if [ "$#" -gt 1 ]; then
  if [ -d "$1" ]; then
    zip -ry "$1/${today}.zip" scripts/ ROOT/assets/ ROOT/changeit.txt  ROOT/lib/ ROOT/etc/
  else
    echo "WARN: Backup directory $1 not exists, not backing up."
    read -p "Press <Ctrl-C> to abort, <ENTER> to continue ... "
  fi
  shift
fi
if [ ! -f "$1" ]; then
	echo "Update archive $1 not found, abort."
	exit 3
fi
rm -rf scripts/ ROOT/assets/ ROOT/lib/
unzip "$1"
